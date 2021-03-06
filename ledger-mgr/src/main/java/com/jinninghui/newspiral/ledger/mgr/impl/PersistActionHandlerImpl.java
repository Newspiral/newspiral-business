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
    //?????????????????????
    private ThreadLocal<List<ExecutedTransaction>> transactionListContainer;
    //?????????passed??????????????????
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


    //?????????
    //????????????????????????????????????????????????????????????finish?????????
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


    //???????????????
    public PersistActionHandlerImpl persistCommitedBlock() {
        BlockModel blockModel = null;
        String channelId = channelIdContainer.get();
        try {
            blockModel = BlockModel.createInstance(blockContainer.get());
            BlockModel form = blockModelMapper.selectByHash(channelId, blockHashContainer.get());
            if (null != form) {
                log.warn(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",persistBlock-Block?????????????????????????????????????????????");
            }
            blockModel.setPersistenceTimeStamp(new Date());
            //????????????
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


    //???????????????
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
                        //??????????????????????????????????????????????????????
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


    //?????????????????????
    public PersistActionHandlerImpl persistWorldState() {
        try {
            for (ExecutedTransaction tran : passedTransContainer.get()) {
                for (WorldStateModifyRecord sRecord : tran.getModifiedWorldStateList()) {
                    PersistConstant.PersistTarget key;
                    WorldState worldState;
                    if (sRecord.getOldState() == null && sRecord.getNewState() != null) {//??????
                        key = stateAdd;
                        worldState = sRecord.getNewState();
                    } else if (sRecord.getOldState() != null && sRecord.getNewState() == null) {//??????
                        key = stateRemove;
                        worldState = sRecord.getOldState();
                    } else {//??????
                        key = stateModify;
                        worldState = sRecord.getNewState();
                    }
                    StateModel stateModel = StateModel.createInstance(worldState, channelIdContainer.get(), blockContainer.get(), tran.getSdkTransaction().getHash());
                    //?????????????????????
                    doPersist(key, stateModel);
                }
            }
            //?????????????????????????????????obj???????????????
            doPersist(stateRest, null);
        } catch (Exception e) {
            ex = e;
        }
        return this;
    }


    //???????????????
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
                    //????????????hash
                    String tranHash = trans.getSDKTransactionHash();
                    //???????????????????????????id
                    Object actionData = change.getActionData();
                    if (actionData instanceof Role) {
                        //??????newChannel??????role???roleid???????????????roleid????????????
                        List<Role> roles = newChannel.getRoles();
                        if (StringUtils.isEmpty(((Role) actionData).getRoleId())) {
                            ((Role) actionData).setRoleId(tranHash + roles.size());
                            change.setActionData(actionData);
                        }
                    }
                    PersistConstant.PersistTarget key = change.getActionTag();
                    doPersist(key, newChannel);
                    log.info(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "????????????:" + key + ",???????????????Channel:channelId=" + newChannel.getChannelId());
                    //todo:????????????????????????????????????????????????????????????
                    Channel persistChannel = ledgerMgr.queryChannel(newChannel.getChannelId());
                    consensusMsgProcessor.processChannelUpdate(persistChannel);
                    log.info(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",?????????Block?????????hash" + trans.getSdkTransaction().getHash() + ",?????????Channel:" + persistChannel.getChannelId());
                }
            }
        } catch (Exception e) {
            ex = e;
        }
        return this;
    }


    //???????????????------???????????????????????????????????????????????????????????????????????????
    public void finish() {
        String channelId = channelIdContainer.get();
        String blockHash = blockHashContainer.get();
        Block block = blockContainer.get();
        try {
            //??????????????????????????????????????????????????????cache????????????
            int num = 0;
            for (Future<Integer> future : futureContainer.get()) {
                num += future.get();
            }
            log.debug("{} num of trans put in db succeed! Go to delete cached block and trans", num);
            //??????blockcache???
            executorServiceForDeleteCache.execute(() -> {
                //?????????????????????????????????????????????????????????????????????????????????
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
            //???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            if (ex != null) {
                ConsensusStageEnum stageEnum = consensusMsgProcessor.queryConsensusStage(channelId);
                if (!stageEnum.equals(ConsensusStageEnum.NO_AVALIABLE_DB_ROUTE)){
                    log.error("persist block occured error:", ex);
                    consensusMsgProcessor.changeConsensusStage(channelId, block.getBlockHeader().getHeight(), ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL);
                }
            }
            //??????ThreadLocal
            blockContainer.remove();
            channelIdContainer.remove();
            blockHashContainer.remove();
            transactionListContainer.remove();
            passedTransContainer.remove();
            futureContainer.remove();
            //????????????
            LedgerThreadLocalContext.blockChangesMap.removeSnap(blockHash);
            LedgerThreadLocalContext.blockChangesSnapshots.remove();
            ex = null;
        }
    }

    /*
     *
     * ??????actionData???????????????jsonobject???????????????????????????????????????actionData?????????jsonObject??????????????????????????????
     * ???????????????????????? ??????????????? ???????????????
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
