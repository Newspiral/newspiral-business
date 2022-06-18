package com.jinninghui.newspiral.ledger.mgr.impl.persist;

import com.jinninghui.newspiral.common.entity.common.persist.PersistActionInterface;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.StateModel;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.StateModelMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Component
public class WorldStateAction implements PersistActionInterface<StateModel> {

    @Autowired
    private StateModelMapper stateModelMapper;

    private ExecutorService executorService = Executors.newFixedThreadPool(32);


    List<Future<Integer>> insertTasks = new ArrayList<>();
    List<Future<Integer>> deleteTasks = new ArrayList<>();
    List<Future<Integer>> updateTasks = new ArrayList<>();


    List<StateModel> insertStateModel = new ArrayList<>();
    Set<String> insertStateKeySet = new HashSet<>();
    //状态修改列表
    List<StateModel> updateStateModel = new ArrayList<>();
    //缓存状态更新
    Set<String> updateStateKeySet = new HashSet<>();
    //状态删除列表
    List<StateModel> deleteStateModel = new ArrayList<>();
    Set<String> deleteStateKeySet = new HashSet<>();





    @Override
    public void doAdd(StateModel stateModel) {
        insertStateModel.add(stateModel);
        //insertWorldState.add(stateModel.toWorldState());
        insertStateKeySet.add(stateModel.getKey());
        //if this key is deleted in before tx, carry on deletion.
        if (deleteStateKeySet.contains(stateModel.getKey())) {
            deleteTasks.add(executorService.submit(() ->
                    stateModelMapper.batchDeleteByPrimaryKey(deleteStateModel)));
            try {
                for (Future<Integer> task : deleteTasks) {
                    task.get();
                }
            } catch (Exception ex) {
                log.error("worldstate persist action , do add occured error" + ex.getMessage());
            }
            deleteTasks.clear();
            deleteStateModel.clear();
            deleteStateKeySet.clear();
        }
        if (insertStateModel.size() > 0 && insertStateModel.size() % 200 == 0) {
            List<StateModel> stateModels = new ArrayList<>();
            stateModels.addAll(insertStateModel);
            insertTasks.add(executorService.submit(() -> stateModelMapper.batchInsert(stateModels)));
            insertStateModel.clear();
            insertStateKeySet.clear();
        }
    }

    @Override
    public void doRemove(StateModel stateModel) {
        deleteStateModel.add(stateModel);
        deleteStateKeySet.add(stateModel.getKey());
        if (insertStateKeySet.contains(stateModel.getKey())) {
            insertTasks.add(executorService.submit(() -> stateModelMapper.batchInsert(insertStateModel)));
            try {
                for (Future<Integer> task : insertTasks) {
                    task.get();
                }
            } catch (Exception ex) {
                log.error("worldstate persist action , do remove occured error" + ex.getMessage());
            }
            insertTasks.clear();
            insertStateModel.clear();
            insertStateKeySet.clear();
        }
        if (updateStateKeySet.contains(stateModel.getKey())) {
            updateTasks.add(executorService.submit(() -> stateModelMapper.batchUpdateByPrimaryKey(updateStateModel)));
            try {
                for (Future<Integer> task : updateTasks) {
                    task.get();
                }
            } catch (Exception ex) {
                log.error("worldstate persist action , do remove occured error" + ex.getMessage());
            }
            updateTasks.clear();
            updateStateModel.clear();
            updateStateKeySet.clear();
        }
        if (deleteStateModel.size() > 0 && deleteStateModel.size() % 200 == 0) {
            List<StateModel> stateModels = new ArrayList<>();
            stateModels.addAll(deleteStateModel);
            deleteTasks.add(executorService.submit(() -> stateModelMapper.batchDeleteByPrimaryKey(stateModels)));
            deleteStateModel.clear();
            deleteStateKeySet.clear();
        }
    }

    @Override
    public void doModify(StateModel stateModel) {
        updateStateModel.add(stateModel);
        //updateWorldState.add(stateModel.toWorldState());
        updateStateKeySet.add(stateModel.getKey());
        //if this key is inserted in before tx, carry on insertion.
        if (insertStateKeySet.contains(stateModel.getKey())) {
            insertTasks.add(executorService.submit(() -> stateModelMapper.batchInsert(insertStateModel)));
            try {
                for (Future<Integer> task : insertTasks) {
                    task.get();
                }
            } catch (Exception ex) {
                log.error("worldstate persist action , do modify occured error" + ex.getMessage());
            }
            insertTasks.clear();
            insertStateModel.clear();
            insertStateKeySet.clear();
        }
        if (updateStateModel.size() > 0 && (updateStateModel.size() % 200 == 0)) {
            List<StateModel> stateModels = new ArrayList<>();
            stateModels.addAll(updateStateModel);
            updateTasks.add(executorService.submit(() -> stateModelMapper.batchUpdateByPrimaryKey(stateModels)));
            updateStateModel.clear();
            updateStateKeySet.clear();
        }
    }

    @Override
    public void doRest(StateModel stateModel) {
        List<Future> restTask = new ArrayList<>();
        if (!CollectionUtils.isEmpty(insertStateModel)) {
            restTask.add(executorService.submit(() -> {
                stateModelMapper.batchInsert(insertStateModel);
            }));
        }
        if (!CollectionUtils.isEmpty(deleteStateModel)) {
            restTask.add(executorService.submit(() -> {
                stateModelMapper.batchDeleteByPrimaryKey(deleteStateModel);
            }));
        }
        if (!CollectionUtils.isEmpty(updateStateModel)) {
            /*for (StateModel model:updateStateModel){
                restTask.add(executorService.submit(() -> {
                    stateModelMapper.updateByPrimaryKey(model);
                }));
            }*/
            restTask.add(executorService.submit(() -> {
                stateModelMapper.batchUpdateByPrimaryKey(updateStateModel);
            }));
        }
        try {
            for (Future task : restTask) {
                task.get();
            }
        } catch (Exception ex) {
            log.error("worldstate persist action , do rest occured error:" + ex.getMessage());
        }finally {
            insertStateModel.clear();
            insertStateKeySet.clear();
            deleteStateModel.clear();
            deleteStateKeySet.clear();
            updateStateKeySet.clear();
            updateStateModel.clear();
        }
    }

}
