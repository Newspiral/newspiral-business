package com.jinninghui.newspiral.ledger.mgr.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.chain.ChannelChange;
import com.jinninghui.newspiral.common.entity.chain.ChannelModifyRecord;
import com.jinninghui.newspiral.common.entity.chain.Peer;
import com.jinninghui.newspiral.common.entity.common.logmodule.LogModuleCodes;
import com.jinninghui.newspiral.common.entity.common.persist.PersistConstant;
import com.jinninghui.newspiral.common.entity.consensus.ConsensusStageEnum;
import com.jinninghui.newspiral.common.entity.member.Role;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.common.entity.state.WorldState;
import com.jinninghui.newspiral.common.entity.state.WorldStateModifyRecord;
import com.jinninghui.newspiral.common.entity.transaction.ExecutedTransaction;
import com.jinninghui.newspiral.consensus.hotstuff.ConsensusMsgProcessor;
import com.jinninghui.newspiral.ledger.mgr.LedgerThreadLocalContext;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.BlockModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.StateModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.TransactionModelWithBLOBs;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.BlockModelMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.TransactionModelMapper;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.jinninghui.newspiral.common.entity.common.persist.PersistConstant.PersistTarget.*;


@Slf4j
@Component
public class PersistActionHandlerImpl extends PersistActionHandler<PersistConstant.PersistTarget> {

    private ThreadLocal<Block> blockContainer;

    private ThreadLocal<String> channelIdContainer;

    private ThreadLocal<String> blockHashContainer;
    //全部的交易列表
    private ThreadLocal<List<ExecutedTransaction>> transactionListContainer;
    //状态为passed了的交易列表
    private ThreadLocal<List<ExecutedTransaction>> passedTransContainer = ThreadLocal.withInitial(ArrayList::new);

    private ThreadLocal<List<Future<Integer>>> futureContainer = ThreadLocal.withInitial(ArrayList::new);


    @SofaReference
    private ConsensusMsgProcessor consensusMsgProcessor;
    @Autowired
    private TransactionModelMapper transactionModelMapper;
    @Autowired
    private BlockModelMapper blockModelMapper;
    @Autowired
    private LedgerMgrImpl ledgerMgr;

    private ExecutorService executorService = Executors.newFixedThreadPool(32);

    private ExecutorService executorServiceForDeleteCache = Executors.newSingleThreadExecutor();

    private static final String PASSED = "1";

    private Exception ex;


    //初始化
    //不随父事务回滚而回滚，这些赋值的数据用于finish的操作
    public PersistActionHandlerImpl init(Block block) {
        try {
            this.blockContainer = ThreadLocal.withInitial(() -> block);
            this.channelIdContainer = ThreadLocal.withInitial(() -> block.getBlockHeader().getChannelId());
            this.blockHashContainer = ThreadLocal.withInitial(block::getHash);
            this.transactionListContainer = ThreadLocal.withInitial(block::getTransactionList);
            this.passedTransContainer = ThreadLocal.withInitial(ArrayList::new);
        } catch (Exception e) {
            ex = e;
        }
        return this;
    }


    //持久化区块
    public PersistActionHandlerImpl persistCommitedBlock() {
        BlockModel blockModel = null;
        String channelId = channelIdContainer.get();
        try {
            blockModel = BlockModel.createInstance(blockContainer.get());
            BlockModel form = blockModelMapper.selectByHash(channelId, blockHashContainer.get());
            if (null != form) {
                log.warn(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",persistBlock-Block数据已经持久化，请不要重复提交");
            }
            blockModel.setPersistenceTimeStamp(new Date());
            //持久化块
            blockModelMapper.insertCommittedBlock(blockModel);
        } catch (MyBatisSystemException e){
            if (null == blockModel) {
                ex = e;
            } else {
                log.error("persist block occured error:", e);
                //blockModelMapper.deleteCachedBlockBehind(channelId, blockModel.getBlockId());
                consensusMsgProcessor.changeConsensusToNoAvailable(channelId, blockModel.getPrevBlockHeight());
            }
        }catch (Exception e) {
            ex = e;
        }
        return this;
    }


    //持久化交易
    public PersistActionHandlerImpl persistTrans() {
        List<ExecutedTransaction> transactionList = transactionListContainer.get();
        List<ExecutedTransaction> passedTrans = passedTransContainer.get();
        String blockhash = blockHashContainer.get();
        String channelId = channelIdContainer.get();
        try {
            int transSize = transactionList.size();
            log.debug(ModuleClassification.LedM_LMI_.toString() + channelId + " persist block " + blockhash + " with " + transSize + " tx");
            List<TransactionModelWithBLOBs> transList = new ArrayList<>();
            for (int i = 0; i < transSize; i++) {
                ExecutedTransaction tran = transactionList.get(i);
                log.debug(ModuleClassification.LedM_LMI_.toString() + channelId + tran.toString());
                TransactionModelWithBLOBs transModelWithBLOBs = TransactionModelWithBLOBs.createInstance(tran, blockContainer.get(), i + 1);
                transList.add(transModelWithBLOBs);
                if (i > 0 && (i % 200 == 0)) {
                    try {
                        //异步的，下面清除可能会影响这边入库。
                        List<TransactionModelWithBLOBs> persistList = new ArrayList<>();
                        persistList.addAll(transList);
                        Future<Integer> transFuture = executorService.submit(() -> transactionModelMapper.batchInsert(persistList));
                        futureContainer.get().add(transFuture);
                    } catch (Exception ex) {
                        log.error("exception in tx insertion", ex);
                        throw ex;
                    }
                    transList.clear();
                }
                if (tran.getPass().equals(PASSED)) {
                    log.debug("add passed trans:{}", tran);
                    passedTrans.add(tran);
                }
            }
            passedTransContainer.set(passedTrans);
            if (!CollectionUtils.isEmpty(transList)) {
                Future<Integer> transFuture = executorService.submit(() -> transactionModelMapper.batchInsert(transList));
                futureContainer.get().add(transFuture);
            }
        } catch (Exception e) {
            ex = e;
        }
        return this;
    }


    //持久化世界状态
    public PersistActionHandlerImpl persistWorldState() {
        try {
            for (ExecutedTransaction tran : passedTransContainer.get()) {
                for (WorldStateModifyRecord sRecord : tran.getModifiedWorldStateList()) {
                    PersistConstant.PersistTarget key;
                    WorldState worldState;
                    if (sRecord.getOldState() == null && sRecord.getNewState() != null) {//插入
                        key = stateAdd;
                        worldState = sRecord.getNewState();
                    } else if (sRecord.getOldState() != null && sRecord.getNewState() == null) {//删除
                        key = stateRemove;
                        worldState = sRecord.getOldState();
                    } else {//更新
                        key = stateModify;
                        worldState = sRecord.getNewState();
                    }
                    StateModel stateModel = StateModel.createInstance(worldState, channelIdContainer.get(), blockContainer.get(), tran.getSdkTransaction().getHash());
                    //持久化世界状态
                    doPersist(key, stateModel);
                }
            }
            //做剩余的动作，这个方法obj不需要传参
            doPersist(stateRest, null);
        } catch (Exception e) {
            ex = e;
        }
        return this;
    }


    //持久化通道
    public PersistActionHandlerImpl persistChannel() {
        try {
            log.debug("persist Channel begin");
            for (ExecutedTransaction trans : passedTransContainer.get()) {
                log.debug("passed trans:{}",trans);
                for (ChannelModifyRecord modifyRecord : trans.getModifiedChannelRecordList()) {
                    ChannelChange change = modifyRecord.getNewChannel().getChannelChange();
                    log.debug("persist channel changes:{}", change);
                    Channel newChannel = modifyRecord.getNewChannel();
                    newChannel.setLatestChannelChangeHeight(blockContainer.get().getBlockHeader().getHeight());
                    //获取交易hash
                    String tranHash = trans.getSDKTransactionHash();
                    //为新通道的角色赋值id
                    Object actionData = change.getActionData();
                    if (actionData instanceof Role) {
                        //如果newChannel中的role的roleid为空，则对roleid进行赋值
                        List<Role> roles = newChannel.getRoles();
                        if (StringUtils.isEmpty(((Role) actionData).getRoleId())) {
                            ((Role) actionData).setRoleId(tranHash + roles.size());
                            change.setActionData(actionData);
                        }
                    }
                    PersistConstant.PersistTarget key = change.getActionTag();
                    doPersist(key, newChannel);
                    log.info(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "标签动作:" + key + ",开始持久化Channel:channelId=" + newChannel.getChannelId());
                    //todo:这里会不会因为查询不到最新的数据库数据？
                    Channel persistChannel = ledgerMgr.queryChannel(newChannel.getChannelId());
                    consensusMsgProcessor.processChannelUpdate(persistChannel);
                    log.info(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",持久化Block，交易hash" + trans.getSdkTransaction().getHash() + ",更新了Channel:" + persistChannel.getChannelId());
                }
            }
        } catch (Exception e) {
            ex = e;
        }
        return this;
    }


    //结束持久化------清除一些附属数据，并有些特殊情况进行处理，补偿机制
    public void finish() {
        String channelId = channelIdContainer.get();
        String blockHash = blockHashContainer.get();
        Block block = blockContainer.get();
        try {
            //因为交易是异步，当交易入库后，再移除cache表数据。
            int num = 0;
            for (Future<Integer> future : futureContainer.get()) {
                num += future.get();
            }
            log.debug("{} num of trans put in db succeed! Go to delete cached block and trans", num);
            //删除blockcache表
            executorServiceForDeleteCache.execute(() -> {
                //删除缓存表中的数据，高度小于等于当前持久化中的块的高度
                long heightToDel = block.getBlockHeader().getHeight() - 2;
                List<BlockModel> delList = blockModelMapper.selectBlockCacheListByHeight(channelId, heightToDel);
                for (BlockModel delModel : delList) {
                    transactionModelMapper.deleteCacheTransactionByBlockHash(channelId, delModel.getHash());
                }
                blockModelMapper.deleteBlockCacheListByHeight(channelId, heightToDel);
            });
        } catch (Exception e) {
            ex = e;
        } finally {
            //如果执行过程中发生异常，将最新的区块高度设置为当前区块的上一个区块高度，等待其他节点同步正确的区块
            if (ex != null) {
                ConsensusStageEnum stageEnum = consensusMsgProcessor.queryConsensusStage(channelId);
                if (!stageEnum.equals(ConsensusStageEnum.NO_AVALIABLE_DB_ROUTE)){
                    log.error("persist block occured error:", ex);
                    consensusMsgProcessor.changeConsensusStage(channelId, block.getBlockHeader().getHeight(), ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL);
                }
            }
            //移除ThreadLocal
            blockContainer.remove();
            channelIdContainer.remove();
            blockHashContainer.remove();
            transactionListContainer.remove();
            passedTransContainer.remove();
            futureContainer.remove();
            //移除快照
            LedgerThreadLocalContext.blockChangesMap.removeSnap(blockHash);
            LedgerThreadLocalContext.blockChangesSnapshots.remove();
            ex = null;
        }
    }

    /*
     *
     * 由于actionData为泛型的，jsonobject在转换是无法判断目标类型，actionData仍会为jsonObject，这边需要手动转一下
     * 转换的问题只会在 同步的交易 场景下出现
     * */
    public ChannelChange transferActionData(ChannelChange channelChange, Class<?> type) {
        Object actionData = channelChange.getActionData();
        if (actionData instanceof JSONObject) {
            actionData = JSONObject.parseObject(((JSONObject) actionData).toJSONString(), type);
            channelChange.setActionData(actionData);
        }
        return channelChange;
    }
}
