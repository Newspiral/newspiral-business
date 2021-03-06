package com.jinninghui.newspiral.consensus.impl.hotstuff;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.ConsensusAlgorithmEnum;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.block.BlockHeader;
import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.chain.Peer;
import com.jinninghui.newspiral.common.entity.chain.PeerCert;
import com.jinninghui.newspiral.common.entity.common.logmodule.LogModuleCodes;
import com.jinninghui.newspiral.common.entity.consensus.BlockVoteMsg;
import com.jinninghui.newspiral.common.entity.consensus.ConsensusStageEnum;
import com.jinninghui.newspiral.common.entity.consensus.GenericMsg;
import com.jinninghui.newspiral.common.entity.consensus.GenericQC;
import com.jinninghui.newspiral.common.entity.consensus.HotStuffDataNode;
import com.jinninghui.newspiral.common.entity.consensus.HotStuffMsg;
import com.jinninghui.newspiral.common.entity.consensus.NewViewMsg;
import com.jinninghui.newspiral.common.entity.consensus.View;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContract;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContractDeployToken;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContractInfo;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.common.entity.transaction.ExecutedTransaction;
import com.jinninghui.newspiral.common.entity.transaction.PooledTransaction;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import com.jinninghui.newspiral.common.entity.transaction.TransactionCompile;
import com.jinninghui.newspiral.consensus.hotstuff.DataVerifier;
import com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor.contract.SmartContractCompile;
import com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor.TxExecutorConsensusCache;
import com.jinninghui.newspiral.ledger.mgr.LedgerMgr;
import com.jinninghui.newspiral.ledger.mgr.LocalConfigLedgerMgr;
import com.jinninghui.newspiral.p2p.P2pClient;
import com.jinninghui.newspiral.p2p.hotstuff.HotstuffRPCInterface;
import com.jinninghui.newspiral.security.DataSecurityMgr;
import com.jinninghui.newspiral.security.SecurityService;
import com.jinninghui.newspiral.security.SecurityServiceMgr;
import com.jinninghui.newspiral.transaction.mgr.TransactionMgr;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.jinninghui.newspiral.common.entity.config.ConfigConstant.SHARDING_CONFIG_ISACTIVE;
import static com.jinninghui.newspiral.common.entity.config.ConfigConstant.SHARDING_CONFIG_TYPE;
import static com.jinninghui.newspiral.common.entity.config.ConfigConstant.SHARDING_DB_ACTIVE_NUM;
import static com.jinninghui.newspiral.common.entity.config.ConfigConstant.SHARDING_RANGE;

/**
 * @author lida
 * @date 2019/7/18 18:51
 * NewSpiralHotStuff????????????
 * ?????????????????????????????????
 */
@Slf4j
public class NewSpiralHotStuffProcessor extends TimerTask {
    /**
     * wait()/notify?????????????????????
     */
    private Object lock = new Object();

    @SofaReference
    private HotstuffRPCInterface hotstuffInterface;

    @SofaReference
    private P2pClient p2pClient;

    @SofaReference
    private TransactionMgr tranasctionMgr;

    @SofaReference
    private LedgerMgr ledgerMgr;

    @SofaReference
    private SecurityServiceMgr securityServiceMgr;

    @SofaReference
    private DataSecurityMgr dataSecurityMgr;

    @SofaReference
    private LocalConfigLedgerMgr localConfigLedgerMgr;

    @Autowired
    @Getter
    private TxExecutorConsensusCache txExecutorConsensusCache;

    @Autowired
    private ConsensusMsgProcessorImpl consensusMsgProcessor;

    @Autowired
    private DataVerifier dataVerifier;


    private Map<Long, Map<Integer, Long>> viewTimeMap = new ConcurrentHashMap<>();

    private ExecutorService executorService = Executors.newFixedThreadPool(4);
    @Autowired
    private ViewSyncHelper viewSyncHelper;


    /**
     * ???????????????????????????Channel????????????????????????init???????????????
     */
    ConsensusContext consensusContext;

    /**
     * ????????????????????????????????????????????????
     */
    public volatile boolean flag = true;


    /**
     * ?????????
     *
     * @param channel
     */
    public void init(Channel channel, ConsensusContext consensusContext) {
        if (this.consensusContext != null) {
            return;
        }
        consensusContext.init(channel, getSecurityService(channel), ledgerMgr);
        this.consensusContext = consensusContext;
        this.viewSyncHelper.init(consensusContext);
        this.txExecutorConsensusCache.init(consensusContext);
        tranasctionMgr.processLocalPeerAddToChannel(channel);
        log.info(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION.toString() + ":consensus processor initialization");
    }


    @Override
    public void run() {
        if (flag) {
            try {
                if (consensusContext.getConsensusStageEnum() == ConsensusStageEnum.LEAVE_CHANNEL) {
                    return;
                }
                if (consensusContext.getPeerCount() > 1) {
                    if (!(consensusContext.getConsensusStageEnum() == ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL)) {
                        runOnceAsConsensusPeer();
                    }
                } else if (consensusContext.getPeerCount() == 1) {
                    runOnceAsUniquePeer();
                } else {
                    log.error(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION.toString() + ":no peer in channel " + consensusContext.getChannel().getChannelId());
                }
                // wait 100ms for receiving consensus messages.
                // avoid log too much
                synchronized (lock) {
                    try {
                        lock.wait(100L);
                    } catch (InterruptedException e) {
                        log.debug(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION.toString() + ":InterruptedException:", e);
                    }
                }
            } catch (Exception ex) {//TODO ????????????????????????????????????????????????????????????????????????????????????
                log.error(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION.toString() + ":Exception in consensus processor, System exist. Exception:", ex);
                System.exit(0);
            }
        }
    }


    /**
     * ??????????????????????????????????????????????????????REPLICA_WAIT_BLOCK??????
     */
    void runOnceAsUniquePeer() {
        if (consensusContext.getCurrView().expired()) {
            processViewTimeout();
        } else {
            //????????????????????????????????????
            switch (consensusContext.getConsensusStageEnum()) {
                //???????????????????????????????????????????????????
                case LEADER_WAIT_NEWVIEW:
                    processNewViewMsgIfEnough();
                    break;
                case LEADER_WAIT_TRANS:
                    tryCreateBlockAndBroadcast();
                    break;
                case LEADER_WAIT_BLOCK_VOTE:
                    processBlockVoteIfExist();
                    break;
                case WAIT_SYNC_WITH_CHANNEL:
                    consensusContext.setConsensusStageEnum(ConsensusStageEnum.LEADER_WAIT_NEWVIEW);
                    break;
                case NO_AVALIABLE_DB_ROUTE:
                    processNeedExpandDb();
                    break;
                default://??????????????????????????????
                    log.error(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION.toString() + ":Unexpected consensus stage:" + consensusContext.consensusStageEnum.toString());
            }
        }

    }

    /**
     * ?????????HotStuff?????????
     */
    private void runOnceAsConsensusPeer() {
        /**????????????????????????:
         * ???	???LeaderWaitNewView(n)???LeaderWaitTrans(n)?????????n-f???NewViewMsg??????????????????New-View?????????view?????????GenericQC????????????QC??????????????????GenericMsg(n)?????????
         * ???	???LeaderWaitTrans(n)???LeaderWaitBlockVote???????????????GenericMsg(n)?????????????????????Replica(n)???
         * ???	ReplicaWaitBlock(n+1)???????????????n-f???GenericMsg(n)???Vote?????????????????????Vote????????????GenericQC??????????????????GenericQC????????????Leader(n+1)????????????NewViewMsg(n,n+1)?????????
         * ???	???ReplicaWaitBlock(n)???LeaderWaitNewViewMsg(n+1)????????????Leader(n+1)?????????????????????GenericMsg(n)????????????Vote(n)???
         * ???	???ReplicaWaitBlock(n)???ReplicaWatiBlock(n+1)???????????????Leader(n+1)?????????????????????GenericMsg(n)????????????Vote(n)?????????Leader(n+1)???????????????NewViewMsg(n,n+1)??????
         */
        if (consensusContext.getCurrView().expired()) {
            log.info(ModuleClassification.ConM_NSHP_.toString() + consensusContext.getChannel().getChannelId() +
                    " View time out at view: " + consensusContext.getCurrView().getNo() + ", timeout:" +
                    consensusContext.getCurrentTimeOut() + " ms");
            processViewTimeout();
            //log.info(ModuleClassification.ConM_NSHP_.toString() + " After processViewTimeout, view: " + consensusContext.getCurrView().getNo());
            log.info(ModuleClassification.ConM_NSHP_.toString() + consensusContext.getChannel().getChannelId() +
                    " After processViewTimeout, view: " + consensusContext.getCurrView().getNo() + ", timeout:" +
                    consensusContext.getCurrentTimeOut() + " ms");
        } else {
            //????????????????????????????????????
            switch (consensusContext.getConsensusStageEnum()) {
                case LEADER_WAIT_NEWVIEW:
                    processNewViewMsgIfEnough();
                    break;
                case LEADER_WAIT_TRANS:
                    tryCreateBlockAndBroadcast();
                    break;
                case REPLICA_WAIT_BLOCK:
                    processBlockIfExist();
                    break;
                case LEADER_WAIT_BLOCK_VOTE:
                    processBlockVoteIfExist();
                    break;
                case NO_AVALIABLE_DB_ROUTE:
                    processNeedExpandDb();
                    break;
                default:
                    log.info(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION.toString() + " Peer not in consensus stage," + consensusContext.getConsensusInfo());
            }
        }
    }

    /**
     * ???????????????????????????????????????????????????
     */
    private void onChangeConsensusContext(HotStuffDataNode receiveNode) {

        consensusContext.addNode(receiveNode);
        if (consensusContext.needRollbackWhileSafe(receiveNode)) {
            // ?????????????????????
            consensusContext.rollbackConsensusContext(receiveNode);
        }
    }


    /**
     * ????????????????????????????????????????????????Block?????????????????????????????????????????????Vote??????????????????Leader?????????????????????NewView(n,n+1)????????????Leader(n+1)???
     * ???????????????????????????view???n+1
     */
    private void processBlockIfExist() {
        if (consensusContext.getCachedGenericMsgs().containsKey(consensusContext.getCurrView().getNo())) {
            //for debug
            Map<Integer, Long> map = viewTimeMap.get(consensusContext.getCurrView().getNo());
            if (null != map) {
                map.put(6, System.currentTimeMillis());
            }

            GenericMsg currGenericMsg = consensusContext.getCachedGenericMsgs().get(consensusContext.getCurrView().getNo());
            log.info(ModuleClassification.ConM_NSHP_.toString() + " process GenericMsg: " + currGenericMsg.toString());
            HotStuffDataNode hotStuffDataNode = currGenericMsg.getHotStuffDataNode();//?????????Node
            Long curr = System.currentTimeMillis();
            /*if (isConsecutive3NullBlock(hotStuffDataNode.getBlock())) {
                //if this block the third null block in a row, only process QC in this genericMsg.
                updateGenericQCIfNeed(currGenericMsg.getHotStuffDataNode().getJustify());
                updateHighestQCIfNeeded(currGenericMsg.getHotStuffDataNode().getJustify());
                enterNextView(false);
                log.info("3 null block in a row, only process GenericQC if needed");
                return;
            }*/
            if (processHotStuffDataNode(hotStuffDataNode)) {
                //todo:accept and update the QC in this hotStuffDataNode if it is valid, no matter whether block is safe.
                if (checkIfConsecutive3BlankBlock(hotStuffDataNode.getBlock())) {
                    //????????????????????????????????????????????????QC????????????????????????????????????
                    updateHighestQCIfNeeded(hotStuffDataNode.getJustify());
                    updateGenericQCIfNeed(hotStuffDataNode.getJustify());
                    //?????????QC????????????
                    consensusContext.addGenericQC(hotStuffDataNode.getJustify());
                    consensusContext.adjustBackViewTimeOut();
                    enterNextView();
                    log.info(ModuleClassification.ConM_NSHP_.toString() + consensusContext.getChannel().getChannelId() +
                            " receive consecutive 3 blank block, ignore it and enter next view");
                    return;
                }
                //????????????????????????????????????????????????????????????????????????
                updateContextAfterProcessDataNode(hotStuffDataNode);
                if (validatePeer(consensusContext.myself)) {
                    BlockVoteMsg blockVoteMsg = createBlockVoteMsg(currGenericMsg);
                    hotstuffInterface.sendBlockVoteMsg(blockVoteMsg, consensusContext.getCurrLeader(), consensusContext.getChannel().getChannelId());
                    log.info(ModuleClassification.ConM_NSHP_.toString() + " Send BlockVoteMsg to peer " +
                            consensusContext.getCurrLeader().getPeerId().toString() + " BlockVoteMsg:" + blockVoteMsg.toString());
                    /*if (!isConsecutive3NullBlock(hotStuffDataNode.getBlock())) {
                        hotstuffInterface.sendBlockVoteMsg(createBlockVoteMsg(currGenericMsg), consensusContext.getCurrLeader(), consensusContext.getChannel().getChannelId());
                        log.info("Send vote msg in view {} at {}, using time {}", consensusContext.getCurrView().getNo(), System.currentTimeMillis(), System.currentTimeMillis()-kkk);
                    } else {
                        log.info("3 consecutive null block," + consensusContext.toSimpleStr());
                    }*/
                    //asynConsensusTask.sendBlockVoteMsg(createBlockVoteMsg(currGenericMsg), consensusContext.getCurrLeader(), consensusContext.getChannel().getChannelId());
                }
                //todo:accept and update the QC in this hotStuffDataNode if it is valid, no matter whether block is safe.
                //updateContextAfterProcessDataNode(hotStuffDataNode);
                enterNextView();
            } else {
                log.info(ModuleClassification.ConM_NSHP_.toString() + " Invalid GenericMsg: " + currGenericMsg.toString());
            }
        }
    }


    private boolean checkIfConsecutive3BlankBlock(Block block) {
        if (block.getTransactionList().size() > 0) {
            return false;
        }
        Block prev1 = consensusContext.getBlock(block.getPrevBlockHash());
        if (null == prev1) {
            return false;
        }
        if (prev1.getTransactionList().size() > 0) {
            return false;
        }
        Block prev2 = consensusContext.getBlock(prev1.getPrevBlockHash());
        if (null == prev2){
            return false;
        }
        if (prev2.getTransactionList().size() > 0) {
            return false;
        }
        return true;
    }


    /**
     * ??????????????????
     *
     * @param peer
     * @return
     */
    private boolean validatePeer(Peer peer) {
        boolean isPeerWhiteList = peer.isState();
/*        for (PeerCert peerCert : peer.getPeerCert()) {
            if (peerCert.getFlag().equals("0")) {
                isPeerCertificateWhiteList = true;
                break;
            }
        }*/
        return isPeerWhiteList;
    }

    private boolean verifyBlockSignAndVoteMsg(HotStuffDataNode hotStuffDataNode) {
        // TODO:??????????????????????????????QC??????
        return true;
    }

    /**
     * ????????????????????????????????????
     *
     * @param blockHash
     * @return
     */
    private boolean findIfBlockHasReceived(String blockHash) {
        // ????????????????????????????????????
        Block prevBlock = consensusContext.getBlock(blockHash);
        if (prevBlock == null) {
            return false;
        }
        return true;
    }

    public boolean processHotStuffDataNode(HotStuffDataNode hotStuffDataNode) {

        // ??????????????????????????????????????????????????????
        if (!findIfBlockHasReceived(hotStuffDataNode.getJustify().getBlockHash())) {
            consensusContext.setConsensusStageEnum(ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL);
            log.info(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION.toString() + " Peer misses block " +
                    hotStuffDataNode.getJustify().getBlockHash() + ", set local stage to " + ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL.toString());
            return false;
        }
        Future<Boolean> isSafeNode = checkIfSafeNode(hotStuffDataNode);
        Future<Boolean> isVoteMsgValid = checkVoteMsg(hotStuffDataNode);
        Future<Boolean> isTxValid = checkTransactionOfBlock(hotStuffDataNode);
        while (!isSafeNode.isDone() && !isSafeNode.isCancelled()
                && !isVoteMsgValid.isDone() && !isVoteMsgValid.isCancelled()
                && !isTxValid.isDone() && !isTxValid.isCancelled()) ;
        try {
            if (false == isSafeNode.get()) {
                log.info(ModuleClassification.ConM_NSHP_.toString() + " Invalid date node:" + hotStuffDataNode.toString());
                return false;
            }
            if (false == isVoteMsgValid.get()) {
                log.info(ModuleClassification.ConM_NSHP_.toString() + " Invalid voteMsg in data node:" + hotStuffDataNode.toString());
                return false;
            }
            if (false == isTxValid.get()) {
                log.info(ModuleClassification.ConM_NSHP_.toString() + " Invalid tx in data node:" + hotStuffDataNode.toString());
                return false;
            }
            return true;
        } catch (Exception ex) {
            log.info(ModuleClassification.ConM_NSHP_.toString() + " Exception in processHotStuffDataNode:" + hotStuffDataNode.toString(), ex);
            consensusContext.setGenericMsgOfCurrView(null);
            return false;
        }
    }

    public void updateContextAfterProcessDataNode(HotStuffDataNode hotStuffDataNode) {
        updateHighestQCIfNeeded(hotStuffDataNode.getJustify());
        updateGenericQCIfNeed(hotStuffDataNode.getJustify());
        consensusContext.tryUpdateConsensusBasis(hotStuffDataNode.getBlock().getBlockHeader().getHeight());
        updateHashPrePrepareBlock(hotStuffDataNode);
        consensusContext.adjustBackViewTimeOut();
        txExecutorConsensusCache.updateWorldStateSnapShootList(hotStuffDataNode.getBlock());
        // ???????????????????????????????????????
        onChangeConsensusContext(hotStuffDataNode);
    }

    private Future<Boolean> checkIfSafeNode(HotStuffDataNode hotStuffDataNode) {
        return executorService.submit(() -> consensusContext.isSafeNode(hotStuffDataNode));
    }

    private Future<Boolean> checkVoteMsg(HotStuffDataNode hotStuffDataNode) {
        return executorService.submit(() -> verifyBlockSignAndVoteMsg(hotStuffDataNode));
    }

    private Future<Boolean> checkTransactionOfBlock(HotStuffDataNode hotStuffDataNode) {
        return executorService.submit(() -> verifyTransactionOfBlock(hotStuffDataNode));
    }

    /**
     * ??????????????????????????????hash
     *
     * @param hotStuffDataNode
     */
    private void updateHashPrePrepareBlock(HotStuffDataNode hotStuffDataNode) {
        consensusContext.setHashPrePrepareBlock(hotStuffDataNode.getBlock().getHash());
    }


    private boolean checkIfDoubleSpendAttach(HotStuffDataNode hotStuffDataNode) {

        if (checkIfReplayAttach(hotStuffDataNode.getBlock())) {
            return true;
        }
        return false;
    }

    private boolean checkIfReplayAttach(Block block) {
        Long height = block.getBlockHeader().getHeight();
        Long localHeight = consensusContext.getBlockHeight();
        Block prevBlock = null;
        List<Block> blocks = new ArrayList<>();
        Set<String> txHashSet = block.getTxHashSet();
        while (height - 1 > localHeight) {
            String hashOfPrevBlock = block.getBlockHeader().getPrevBlockHash();
            prevBlock = consensusContext.getBlock(hashOfPrevBlock);
            if (prevBlock == null) {
                log.info(ModuleClassification.ConM_NSHP_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",cannot find prev block of receiving block");
                return true;
            }
            blocks.add(prevBlock);
            height--;
        }
        boolean flag = blocks.parallelStream().anyMatch(
                block1 -> ifBlockHasSameTx(txHashSet, block1)
        );
        if (flag) {
            return true;
        }
        if (ifBlockHasCommittedTx(block)) {
            log.info(ModuleClassification.ConM_NSHP_.toString() + " Block {} has transaction already committed", block.getHash());
            return true;
        }
        return false;
    }

    private boolean ifBlockHasSameTx(Set<String> txHashSet, Block block) {
        return block.getTransactionList().parallelStream().anyMatch(
                tx -> txHashSet.contains(tx.getSDKTransactionHash())
        );
    }

    private boolean ifBlockHasCommittedTx(Block block) {
        //????????????????????????????????????
        List<SDKTransaction> sdkTransactions = txExecutorConsensusCache.outOfTransactionPool(block);
        for (SDKTransaction tx : sdkTransactions) {
            if (ledgerMgr.queryTransaction(tx.getHash(), consensusContext.getChannel().getChannelId()) != null) {
                log.info(ModuleClassification.ConM_NSHP_.toString() + " Block {} has transaction already committed,transactionHash={}",
                        block.getHash(), tx.getHash());
                return true;
            }
        }
        return false;
    }

    /**
     * ??????????????????????????????
     *
     * @param hotStuffDataNode ????????????
     */
    private boolean verifyTransactionOfBlock(HotStuffDataNode hotStuffDataNode) {
        if (checkIfDoubleSpendAttach(hotStuffDataNode)) {
            log.info(ModuleClassification.ConM_NSHP_.toString() + " Replay attach in data node:" + hotStuffDataNode.toString() + consensusContext.getConsensusInfo());
            return false;
        }
        boolean flag = txExecutorConsensusCache.verifyBlockForExecution(hotStuffDataNode.getBlock());
        if (!flag) {
            log.info(ModuleClassification.ConM_NSHP_.toString() + " VerifyBlockForExecution failed in date node:" + hotStuffDataNode.toString() + consensusContext.getConsensusInfo());
        }
        return flag;
    }


    /**
     * ????????????????????????????????????Block????????????????????????????????????GenericMsg???????????????????????????processBlockIfExist?????????
     * ????????????????????????????????????????????????????????????????????????????????????BlockVote??????
     */
    private boolean processLocalNewBlockCreate(GenericMsg genericMsg) {
        log.info(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION.toString() +
                " processLocalNewBlockCreate, genericMsg:" + genericMsg.toString() + consensusContext.getConsensusInfo());
        HotStuffDataNode hotStuffDataNode = genericMsg.getHotStuffDataNode();
        updateGenericQCIfNeed(hotStuffDataNode.getJustify());
        updateHighestQCIfNeeded(hotStuffDataNode.getJustify());
        if (false == checkIfConsecutive3BlankBlock(hotStuffDataNode.getBlock())) {
            BlockVoteMsg voteMsg = createBlockVoteMsg(genericMsg);
            Map<String, BlockVoteMsg> blockVoteMsgMap = consensusContext.blockVoteMap.get(hotStuffDataNode.getBlock().getHash());
            if (null == blockVoteMsgMap) {
                blockVoteMsgMap = createBlockVoteMsgMap(hotStuffDataNode.getBlock().getHash());
            }
            blockVoteMsgMap.put(voteMsg.getBussinessKey(), voteMsg);
            updateHashPrePrepareBlock(genericMsg.getHotStuffDataNode());
            return true;
        } else {
            return false;
        }
    }

    private synchronized Map<String, BlockVoteMsg> createBlockVoteMsgMap(String hash) {
        Map<String, BlockVoteMsg> blockVoteMsgMap = consensusContext.blockVoteMap.get(hash);
        if (null == blockVoteMsgMap) {
            blockVoteMsgMap = new ConcurrentHashMap<>();
            consensusContext.blockVoteMap.put(hash, blockVoteMsgMap);
        }
        return blockVoteMsgMap;
    }


    /**
     * ???????????????????????????
     *
     * @param currGenericMsg
     * @return
     */
    private BlockVoteMsg createBlockVoteMsg(GenericMsg currGenericMsg) {
        BlockVoteMsg voteMsg = new BlockVoteMsg();
        voteMsg.setBlockHash(currGenericMsg.getHotStuffDataNode().getBlock().getHash());
        voteMsg.setViewNo(consensusContext.getCurrView().getNo());
        voteMsg.setChannelId(this.consensusContext.channel.getChannelId());
        getLocalSecurityService().hash(voteMsg);
        //voteMsg??????????????????????????????????????????channelId??????????????????????????????????????????????????????PRCparm?????????????????????
        getLocalSecurityService().signByGMCertificate(voteMsg, this.consensusContext.channel.getChannelId());
        return voteMsg;
    }

    /**
     * ??????GenriceQC??????LockQC???????????????Block
     *
     * @param genericQC
     */
    private void updateGenericQCIfNeed(GenericQC genericQC) {
        /*if (genericQC.getBlockHash().equals(consensusContext.getGenericQC().getBlockHash()) ||
            genericQC.getBlockHash().equals(consensusContext.getLockedQC().getBlockHash())) {
            return;
        }*/

        HotStuffDataNode prev1HotStuffDataNodeByJustify = consensusContext.findNodeByBlockHash(genericQC.getBlockHash());
        if (prev1HotStuffDataNodeByJustify != null) {
            prev1HotStuffDataNodeByJustify.getBlock().getBlockHeader().setWitness(JSON.toJSONString(genericQC));
            //ledgerMgr.insertCacheBlock(consensusContext.getChannel().getChannelId(), prev1HotStuffDataNodeByJustify.getBlock());
            Block block = prev1HotStuffDataNodeByJustify.getBlock();
            block.getBlockHeader().setConsensusTimestamp(new Date().getTime());
            consensusContext.putCachedBlock(block);
            //????????????genericQC???lockedQC
            //for test
            Long ttt = System.currentTimeMillis();
            consensusContext.writeLock(ttt);
            try {
                log.info(ModuleClassification.ConM_NSHP_.toString() + consensusContext.getChannel().getChannelId() +
                        " update genericQC as:" + genericQC.toString());
                consensusContext.setGenericQC(genericQC);
                updateLockQCIfNeed(prev1HotStuffDataNodeByJustify);
            } catch (Exception ex) {
                log.error(ModuleClassification.ConM_NSHP_.toString() +consensusContext.getChannel().getChannelId()+ " updateGenericQCIfNeed exception:", ex);
            } finally {
                consensusContext.writeUnlock(ttt);
            }
            HotStuffDataNode prev2HotStuffDataNodeByJustify = consensusContext.findNodeByBlockHash(prev1HotStuffDataNodeByJustify.getParentNodeHashStr());
            if (prev2HotStuffDataNodeByJustify != null) {
                persistPrevBlockIfNeed(prev2HotStuffDataNodeByJustify);
                //TODO:????????????????????????
            } else {
                log.info(ModuleClassification.ConM_NSHP_.toString() + consensusContext.getChannel().getChannelId() +
                        " can't find prev block of genericQC");
            }

        } else {
            log.info(ModuleClassification.ConM_NSHP_.toString() + consensusContext.getChannel().getChannelId()+" can't find dataNode of given geneircQC " + genericQC.toString() + consensusContext.getConsensusInfo());
        }
    }


    /**
     * ??????LockQC???????????????Block
     *
     * @param prev1HotStuffDataNodeByJustify
     */
    private void updateLockQCIfNeed(HotStuffDataNode prev1HotStuffDataNodeByJustify) {
        HotStuffDataNode prev2HotStuffDataNodeByJustify = consensusContext.findNodeByBlockHash(prev1HotStuffDataNodeByJustify.getParentNodeHashStr());
        if (prev2HotStuffDataNodeByJustify != null) {
            if (prev1HotStuffDataNodeByJustify.getParentNodeHashStr().equals(prev2HotStuffDataNodeByJustify.getBlock().getHash())) {
                log.info(ModuleClassification.ConM_NSHP_.toString() + consensusContext.getChannel().getChannelId()+" update LockedQC as " + prev1HotStuffDataNodeByJustify.getJustify().toString());
                consensusContext.setLockedQC(prev1HotStuffDataNodeByJustify.getJustify());
            }
        }
    }

    private void updateHighestQCIfNeeded(GenericQC genericQC) {
        if (genericQC.newerThan(consensusContext.getHighestQC()) && genericQC.getHeight() >= consensusContext.getHighestQC().getHeight() &&
                consensusContext.getBlock(genericQC.getBlockHash()) != null) {
            log.info(ModuleClassification.ConM_NSHP_.toString() +consensusContext.getChannel().getChannelId()+ " update highestQC as " + genericQC.toString());
            consensusContext.setHighestQC(genericQC);
        }
    }

    /**
     * ?????????Block
     *
     * @param prev2HotStuffDataNodeByJustify
     */
    private void persistPrevBlockIfNeed(HotStuffDataNode prev2HotStuffDataNodeByJustify) {
        HotStuffDataNode prev3HotStuffDataNodeByJustify = consensusContext.findNodeByBlockHash(prev2HotStuffDataNodeByJustify.getParentNodeHashStr());
        if (prev3HotStuffDataNodeByJustify != null) {
            if (prev2HotStuffDataNodeByJustify.getParentNodeHashStr().equals(prev3HotStuffDataNodeByJustify.getBlock().getHash())) {
                log.info(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + " persist Block 1 " + prev3HotStuffDataNodeByJustify.getBlock().getHash());
                Block block = prev3HotStuffDataNodeByJustify.getBlock();
                persistBlockAction(block, prev2HotStuffDataNodeByJustify.getJustify());
            }
        } else {
            //??????????????????????????????????????????????????????????????????????????????????????????????????????datanode
            //todo????????????????????????commit??????????????????????????????????????????????????????
            String hash = prev2HotStuffDataNodeByJustify.getParentNodeHashStr();
            Block block = consensusContext.getBlock(hash);
            if (block == null) {
                log.info(ModuleClassification.ConM_NSHP_.toString() +consensusContext.getChannel().getChannelId()+ " miss block " + hash + " in persistPrevBlockIfNeed");
            }
            GenericQC genericQC = consensusContext.genericQCMap.get(hash);
            if (null != block && null != genericQC) {
                log.info(ModuleClassification.ConM_NSHP_.toString() +consensusContext.getChannel().getChannelId()+ " persist Block 2 " + block.getHash());
                persistBlockAction(block, genericQC);
            }
        }
    }

    private void persistBlockAction(Block block, GenericQC genericQC) {
        if (genericQC.getBlockHash().equals(block.getHash()) == false) {
            log.error(ModuleClassification.ConM_NSHP_.toString() + consensusContext.getChannel().getChannelId()+" Invalid persist block action");
            return;
        }
        block.getBlockHeader().setWitness(JSON.toJSONString(genericQC));
        txExecutorConsensusCache.removeStalePool(block);
        //remove tx from transaction pool before put the committed block in map
        txExecutorConsensusCache.removeTxFromTransactionPool(block);
        //todo: put removing tx to persisting block thread.
        //log.info("remove tx from transaciton pool time is {}", System.currentTimeMillis() - curr);
        consensusContext.putCommittedBlock(block);
        ledgerMgr.cacheTxShort(block);
        if (block.getBlockHeader().getHeight().equals(consensusContext.getChannel().getLatestChannelChangeHeight()) &&
            block.getBlockHeader().getHeight().equals(consensusContext.getLatestChannelUpdateHeight()) == false) {
            hotstuffInterface.updateChannelPeerClient(consensusContext.getChannel(), 3l);
            consensusContext.setLatestChannelUpdateHeight(block.getBlockHeader().getHeight());
        }
        if (block.getBlockHeader().getHeight().equals(consensusContext.getChannel().getLatestChannelChangeHeight() + 4)) {
            //todo:?????????????????????????????????????????????????????????????????????
            hotstuffInterface.updateChannelPeerClient(consensusContext.getChannel(), 4l);
        }
    }


    /**
     * ???????????????Channel???????????????????????????
     *
     * @return
     */
    SecurityService getSecurityService(Channel channel) {
        return securityServiceMgr.getMatchSecurityService(channel.getSecurityServiceKey());
    }

    /**
     * ??????????????????????????????????????????????????????consensusContext?????????????????????
     *
     * @return
     */
    SecurityService getLocalSecurityService() {
        return securityServiceMgr.getMatchSecurityService(consensusContext.getChannel().getSecurityServiceKey());
    }


    /**
     * ????????????n-f???Vote??????????????????GenericQC??????????????????GenricQC?????????????????????NewView(n,n+1)????????????Leader(n+1)
     * ???????????????????????????view???n+1
     */
    private void processBlockVoteIfExist() {
        //TODO:???????????????????????????????????????????????????
        if (consensusContext.collectVoteTime < 0) {
            consensusContext.collectVoteTime = System.currentTimeMillis();
        }
        GenericMsg genericMsg = consensusContext.getGenericMsgOfCurrView();
        Map<String, BlockVoteMsg> map = consensusContext.getBlockVoteMap().get(genericMsg.getHotStuffDataNode().getBlock().getHash());
        if (null == map) {
            return;
        }
        if (map.size() >= consensusContext.getQcMinNodeCnt()) {
            log.info(ModuleClassification.ConM_NSHP_.toString() +consensusContext.getChannel().getChannelId()+  " collect enough BlockVote." + consensusContext.getConsensusInfo());
            GenericQC genericQC = createGenericQC();
            updateHighestQCIfNeeded(genericQC);
            updateGenericQCIfNeed(consensusContext.getGenericMsgOfCurrView().getHotStuffDataNode().getJustify());
            consensusContext.adjustBackViewTimeOut();
            enterNextView();
        }

    }


    /**
     * ??????????????????????????????????????????
     */
    private boolean processNeedExpandDb() {
        Long ttt = System.currentTimeMillis();
        Map<String, String> map = localConfigLedgerMgr.queryByType(SHARDING_CONFIG_TYPE);
        BigInteger blockheight = new BigInteger(String.valueOf(consensusContext.blockHeight + 1));
        BigInteger range = new BigInteger(map.get(SHARDING_RANGE));
        BigInteger dbNum = new BigInteger(map.get(SHARDING_DB_ACTIVE_NUM));
        BigInteger total = range.multiply(dbNum);
        if (blockheight.compareTo(total) >= 0) {
            //???????????????????????????
            ledgerMgr.deleteCachedBlockBehind(consensusContext.getChannel().getChannelId(), consensusContext.blockHeight);
            log.error(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + " no available datasource to route,current blockheight: " + blockheight + ",current db total: " + total);
            return false;
        } else {
            if ("true".equals(map.get(SHARDING_CONFIG_ISACTIVE))) {
                //???????????????????????????????????????
                consensusMsgProcessor.changeNoAvailableConsensus(consensusContext.getChannel().getChannelId(), consensusContext.blockHeight - 1, ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL);
                return true;
            }
        }
        return false;
    }


    /*
     * ??????Vote????????????GenericQC????????????????????????GenericQC
     */
    private GenericQC createGenericQC() {
        GenericQC genericQC = new GenericQC();
        Block block = consensusContext.getGenericMsgOfCurrView().getHotStuffDataNode().getBlock();
        genericQC.setBlockHash(block.getHash());
        genericQC.setPrevBlockHash(block.getBlockHeader().getPrevBlockHash());
        genericQC.setHeight(block.getBlockHeader().getHeight());
        //??????genriceQC???????????????view???BLock???QC
        genericQC.setBlockViewNo(consensusContext.getCurrView().getNo());
        //????????????????????????clear,??????new?????????
        Map<String, BlockVoteMsg> map = consensusContext.getBlockVoteMap().get(block.getHash());
        genericQC.setVoteMap(new LinkedHashMap<>(map));
        genericQC.setBlockCreateTimestamp(consensusContext.getGenericMsgOfCurrView().getHotStuffDataNode().getBlock().getBlockHeader().getTimestamp());
        //????????????Qc???????????????????????????????????????
        List<Peer> peerList = consensusContext.getOrderedPeerList().parallelStream().filter(peer -> peer.isState()).collect(Collectors.toList());
        genericQC.setPeerCnt(peerList.size());
        //??????genericQc???hash???
        SecurityService securityService = getLocalSecurityService();
        genericQC.setHash(SignForConsensus.hashGenericQc(genericQC, securityService));
        return genericQC;
    }

    /**
     * ???????????????VIew???????????????Leader???????????????Replica???????????????????????????????????????????????????
     * ????????????????????????New-View????????????????????????View???Leader??????????????????????????????????????????View
     * ???????????????GenericQC??????????????????
     */
    private void enterNextView() {
        NewViewMsg newViewMsg = createNewViewMsg();
        sendNewViewMsg(newViewMsg);
        consensusContext.cleanCachedGenericMsgs(consensusContext.getCurrView().getNo());
        consensusContext.localEnterNewView();
        consensusContext.collectVoteTime = -1;
        consensusContext.collectNewViewTime = -1;
        consensusContext.canCreateTime = -1;
        consensusContext.totalViewTime = System.currentTimeMillis();
        log.info(ModuleClassification.ConM_NSHP_.toString() +consensusContext.getChannel().getChannelId()+ " succeed to enter next view. " + consensusContext.getConsensusInfo());
    }

    private NewViewMsg createNewViewMsg() {
        NewViewMsg newViewMsg = new NewViewMsg();
        newViewMsg.setJustify(GenericQC.clone(consensusContext.getHighestQC()));
        newViewMsg.setViewNo(new Long(consensusContext.getCurrView().getNo()));
        newViewMsg.setChannelId(consensusContext.getChannel().getChannelId());
        getLocalSecurityService().hash(newViewMsg);
        getLocalSecurityService().signByGMCertificate(newViewMsg, consensusContext.channel.getChannelId());
        return newViewMsg;
    }

    private void sendNewViewMsg(NewViewMsg newViewMsg) {
        Peer nextLeader = consensusContext.calcLeader(consensusContext.getCurrView().getNo() + 1);
        if (!validatePeer(nextLeader)) {
            //TODO ???????????????????????????????????????????????????????????????????????????????????????????????????
            log.info(ModuleClassification.ConM_NSHP_ + " nextLeader cert is invalid,{}", nextLeader.getPeerId().toString());
        }
        if (false == nextLeader.equals(consensusContext.myself)) {
            hotstuffInterface.sendNewView(newViewMsg, nextLeader, consensusContext.getChannel().getChannelId());
            log.info(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + " send newViewMsg " + newViewMsg.toString());
        } else {//????????????
            log.info(ModuleClassification.ConM_NSHP_.toString() +consensusContext.getChannel().getChannelId()+ " accept own newViewMsg:" + newViewMsg.toString());
            this.acceptNewViewMsg(newViewMsg);
        }

    }


    /**
     * ????????????Channel???????????????????????????????????????Channel???????????????????????????
     */
    private void tryCreateBlockAndBroadcast() {
        if (consensusContext.canCreateTime < 0) {
            consensusContext.canCreateTime = System.currentTimeMillis();
        }
        Channel channel = consensusContext.getChannel();
        if (canCreateBlock()) {
            //for debug
            List<List<ExecutedTransaction>> outputExecutedTransactions = txExecutorConsensusCache.packageExecutedTx4NewBlock();
            Block newBlock = createBlock(outputExecutedTransactions);
            log.info(ModuleClassification.ConM_NSHP_.toString() +consensusContext.getChannel().getChannelId()+ " create block " + newBlock.getHash() + " consensus state: " + consensusContext.getConsensusInfo());
            GenericMsg genericMsg = createGenericMsg(newBlock);
            consensusContext.setGenericMsgOfCurrView(genericMsg);
            //consensusContext.addNode(genericMsg.getHotStuffDataNode());
            //???????????????????????????
            Set<Peer> excluedPeers = new HashSet<>();
            excluedPeers.add(consensusContext.getMyself());
            hotstuffInterface.broadcastGenericMsg(genericMsg, channel.getChannelId(), excluedPeers);
            log.info(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION.toString() + " broad cast GenericMsg: " + genericMsg.toString());
            /*if (isConsecutive3NullBlock(genericMsg.getHotStuffDataNode().getBlock())) {
                log.info("3 consecutive null block, only broadcast genericMsg");
                enterNextView(true);
                return;
            }*/
            if (false == processLocalNewBlockCreate(genericMsg)) {
                //????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                log.info(ModuleClassification.ConM_NSHP_.toString() + consensusContext.getChannel().getChannelId() +
                        " create consecutive 3 blank block, ignore it and step into next view");
                consensusContext.adjustBackViewTimeOut();
                enterNextView();
                return;
            }
            //???????????????????????????????????????????????????
            consensusContext.addNode(genericMsg.getHotStuffDataNode());
            consensusContext.tryUpdateConsensusBasis(newBlock.getBlockHeader().getHeight());
            txExecutorConsensusCache.updateWorldStateSnapShootList(newBlock);
            consensusContext.setConsensusStageEnum(ConsensusStageEnum.LEADER_WAIT_BLOCK_VOTE);
        }
    }

    /**
     * create GenericMsg, the param block must be hashing first.
     *
     * @param block
     * @return
     */
    private GenericMsg createGenericMsg(Block block) {
        GenericMsg msg = new GenericMsg(block, consensusContext.getHighestQC(), consensusContext.getCurrView().getNo(), consensusContext.getChannel().getChannelId());
        msg.setHash(SignForConsensus.hashDataNodeDirectly(msg.getHotStuffDataNode(), getLocalSecurityService()));
        //getLocalSecurityService().sm9Sign(msg,this.consensusContext.channel.getChannelId());
        getLocalSecurityService().signByGMCertificate(msg, this.consensusContext.channel.getChannelId());
        return msg;
    }

    /**
     * ????????????????????????
     * ????????????????????????????????????????????????:(1)?????????????????????????????????????????????????????????????????????????????????blockMaxInterval?????????????????????????????????
     * (2)????????????????????????????????????Channel???????????????blockMaxSize
     *
     * @return
     */
    private boolean canCreateBlock() {
        Channel channel = consensusContext.getChannel();
        if (txExecutorConsensusCache.hasEnoughTransactions(channel.getBlockMaxSize())) {
            return true;
        }
        //TODO:Check if the block verified by highestQC is available, if not, request it.
        if (consensusContext.findNodeByBlockHash(consensusContext.getHighestQC().getBlockHash()) == null) {
            log.error(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + ",Can't find the HotStuffDataNode concerning with highestQC:" + consensusContext.getHighestQC().toString() + consensusContext.getConsensusInfo());
            return false;
        }
        long msFromLastBlock = System.currentTimeMillis() - consensusContext.getCurrView().getStartTimestamp();
        if (msFromLastBlock >= channel.getBlockMaxInterval()) {
            if (txExecutorConsensusCache.getWaitPackageTxPoolMap().get(consensusContext.getHighestQC().getBlockHash()) != null) {
                return true;
            } /*else if (msFromLastBlock >= consensusContext.getCurrentTimeOut()/2) {
                log.info("longer than half of time out, base block {}, view {}, height{}", consensusContext.getHighestQC().getBlockHashStr(), consensusContext.getCurrView().getNo(), consensusContext.getBlockHeight());
                return true;
            }*/ else {
                return true;
            }
        }
        return false;
    }

    /**
     * create block with transList, where tx must be hashing first.
     *
     * @param transList
     * @return
     */
    private Block createBlock(List<List<ExecutedTransaction>> transList) {
        Block block = new Block();
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setChannelId(consensusContext.getChannel().getChannelId());
        blockHeader.setConsensusAlgorithm(ConsensusAlgorithmEnum.NEWSPIRAL_HOT_STUFF);

        //???????????????
        blockHeader.setHeight(consensusContext.getHighestQC().getHeight() + 1);
        blockHeader.setPrevBlockHash(consensusContext.getHighestQC().getBlockHash());
        blockHeader.setTimestamp(System.currentTimeMillis());
        blockHeader.setVersion(BlockHeader.VERSION_1_0);
        block.setBlockHeader(blockHeader);
        int size = 0;
        List<ExecutedTransaction> txList = transList.get(0);
        size += txList.size();
        size += transList.get(1).size();
        txList.addAll(transList.get(1));
        block.addTransactionList(txList);

        //?????????Hash?????????

        SecurityService securityService = getLocalSecurityService();
        String merkleRoot = SignForConsensus.merkleBlock(block);
        blockHeader.setMerkleRoot(merkleRoot);
        //??????block???hash
        block.setHash(SignForConsensus.hashBlockHeader(blockHeader, securityService));

        //securityService.sm9Sign(block,this.consensusContext.channel.getChannelId());
        securityService.signByGMCertificate(block, this.consensusContext.channel.getChannelId());
        log.info(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + ",create block " + block.getHash() + " where tx size " + size);
        return block;
    }

    private boolean isConsecutive3NullBlock(Block block) {
        Block iterBlock = block;
        int i = 3;
        while (i > 0 && iterBlock != null) {
            if (iterBlock.getTransactionList().size() != 0) {
                return false;
            }
            iterBlock = consensusContext.getBlock(iterBlock.getPrevBlockHash());
            i--;
        }
        return true;
    }

    /**
     * ??????????????????n-f???New-view??????????????????LEADER_WAIT_TRANS
     */
    private void processNewViewMsgIfEnough() {

/*        //TODO ????????????????????????????????????????????????????????????view
        if(consensusContext.getLatestTimeOutViewNo()==consensusContext.currView.getNo()-1)
        {
            this.enterNextView();
        }*/
        if (consensusContext.collectNewViewTime < 0) {
            consensusContext.collectNewViewTime = System.currentTimeMillis();
        }
        if (existEnoughNewView()) {
            log.info(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + ",collect enough newViewMsg. " + consensusContext.getConsensusInfo());
            //log.info("collect newViewMsg time {}, view {}", System.currentTimeMillis() - consensusContext.collectNewViewTime, consensusContext.getCurrView().getNo());
            updateHighestQCFromNewViewMsg();
            updateGenericQCIfNeed(consensusContext.getHighestQC());
            //TODO:Check if the block verified by the highestQC exist,
            // if not, two available solution:
            // 1. There must be at least n-f peers that have the very block,
            // so just request it and process it before create new block.
            // 2. Set the state of peer to WAIT_SYNC_WITH_CHANNEL, and sync with other peers.
            consensusContext.setConsensusStageEnum(ConsensusStageEnum.LEADER_WAIT_TRANS);
            log.info(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + ",after processNewViewMsgIfEnough " + consensusContext.getConsensusInfo());
        }
    }

    /**
     * ??????n-f???NewView??????????????????????????????view???Leader?????????NewView??????
     *
     * @return
     */
    private boolean existEnoughNewView() {
        Map<String, NewViewMsg> newViewMsgMapOfCurrView = consensusContext.getNewViewMsgMap().get(consensusContext.getCurrView().getNo() - 1);
        if (newViewMsgMapOfCurrView == null) {
            return false;
        }
        if (newViewMsgMapOfCurrView.size() < consensusContext.getQcMinNodeCnt()) {
            //log.info(ModuleClassification.ConM_NSHP_+"MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",less than {} newView Messages", consensusContext.getQcMinNodeCnt());
            return false;
        }
        Peer prevViewLeader = consensusContext.calcLeader(consensusContext.getCurrView().getNo() - 1);
/*
        //??????????????????????????????View???Leader?????????NewView?????????
        return newViewMsgMapOfCurrView.values().parallelStream().anyMatch(
                Msg->{
                   return Msg.getCallerIdentityKey().equalsWithIdentify(prevViewLeader.getPeerId());
                }
        );
*/
        //???????????????????????????????????????????????????????????????????????????????????????newViewMsg?????????????????????????????????????????????????????????leader???????????????????????????????????????
        //todo:??????????????????????????????????????????????????????????????????leader???newViewMsg???????????????????????????newViewMsg????????????????????????????????????
        for (NewViewMsg newViewMsg : newViewMsgMapOfCurrView.values()) {
            try {
                if (newViewMsg.getSignerIdentityKey().getIdentityKey().equals(prevViewLeader.getPeerId())) {//?????????View???Leader?????????NewView??????
                    return true;
                }
            } catch (Exception ex) {
                log.info(ModuleClassification.ConM_NSHP_ + "," + consensusContext.getChannel().getChannelId() + ",exception in processNewViewMsg:" + newViewMsg.toString(), ex);
            }
        }
        log.info(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + ",not enough newView messages");
        return false;

    }

    /**
     * ??????NewView??????????????????????????????genericQC??????????????????genericeQC???????????????NewView????????????????????????Quarum???????????????
     * ????????????????????????Leader?????????Generic?????????GenericQC
     */
    private void updateHighestQCFromNewViewMsg() {
        Map<String, NewViewMsg> viewMsgMap = consensusContext.getNewViewMsgMap().get(consensusContext.getCurrView().getNo() - 1);
        GenericQC highestQC = consensusContext.getHighestQC();//?????????????????????highestQC
        for (NewViewMsg newViewMsg : viewMsgMap.values()) {
            if (newViewMsg.getJustify().newerThan(highestQC)) {
                highestQC = newViewMsg.getJustify();
            }
        }
        //??????QC????????????
        if (highestQC.getHeight() > consensusContext.getBlockHeight() + 3) {
            log.info(ModuleClassification.ConM_NSHP_.toString() + consensusContext.getChannel().getChannelId()
                    + " find better genericQC, set local state to WAIT_SYNC_WITH_CHANNEL");
            consensusContext.setConsensusStageEnum(ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL);
        }
        if (consensusContext.getBlock(highestQC.getBlockHash()) == null) {
            log.info(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + ",received a genericQC where corresponding block cannot be found, genericQC:" + highestQC.toString());
            consensusContext.setConsensusStageEnum(ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL);
        } else {
            log.info(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + ",set highestQC as " + highestQC.toString());
            consensusContext.setHighestQC(highestQC);
        }
    }

    /**
     * ?????????View??????
     * ??????????????????????????????????????????view??????
     */
    private void processViewTimeout() {
        if (consensusContext.consensusStageEnum.equals(ConsensusStageEnum.NO_AVALIABLE_DB_ROUTE)) {
            boolean expandDb = processNeedExpandDb();
            if (!expandDb) {
                return;
            }
        }
        Long maxTimeOut = new Long(16000);
        if (this.consensusContext.getCurrentTimeOut() >= maxTimeOut) {
            if (viewSyncHelper.trySyncHighAndViewNo() == false) {
                log.info(ModuleClassification.ConM_NSHP_.toString() + consensusContext.getChannel().getChannelId()
                        + " find out higher block in synchronizing view, set local to " + ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL);
                return;
            }
        } else {
            if (this.consensusContext.getLatestTimeOutViewNo().equals(this.consensusContext.getCurrView().getNo() - 1)) {
                log.info(ModuleClassification.ConM_NSHP_ + "," + consensusContext.getChannel().getChannelId() + " Consecutive view timeout, double time out value");
                this.consensusContext.setCurrentTimeOut(this.consensusContext.getCurrentTimeOut() * 2);
            }
        }
        this.consensusContext.setLatestTimeOutViewNo(this.consensusContext.getCurrView().getNo());
        this.enterNextView();
    }

    private String logPrefix() {
        return "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + ",Stage:" + consensusContext.getConsensusStageEnum().code + "(view=" + this.consensusContext.getCurrView().getNo() + ","
                + "channelId=" + consensusContext.getChannel().getChannelId() + "):";
    }


    /**
     * ???????????????GenericMsg????????????????????????Replica??????????????????????????????????????????
     * ????????????????????????????????????????????????????????????Node?????????????????????OK
     * ??????????????????????????????????????????genericMsg??????QC?????????????????????????????????????????????????????????view???
     * ?????????????????????????????????????????????????????????????????????
     * <p>
     * This function also rehashes sdkTransactions.
     *
     * @param genericMsg
     */
    public void acceptGenericMsg(GenericMsg genericMsg) {
        //TODO:?????????genericMsg????????????????????????????????? ?????????????????????
/*        if (false == isValidQCAndCorrectSign(genericMsg.getHotStuffDataNode().getJustify())) {
            log.info(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + " ,Invalid QC in genericMsg:" + genericMsg.toString());
            return;
        }*/
        if (dataVerifier.verifyGenericQC(genericMsg.getHotStuffDataNode().getJustify(), consensusContext.getChannel()) == false) {
            log.error(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + " ,Invalid QC in genericMsg:" + genericMsg.toString());
            return;
        }
        /*Block block = genericMsg.getHotStuffDataNode().getBlock();
        //calcHashOfTx(genericMsg.getHotStuffDataNode().getBlock());
        if (ifBlockModified(block)) {
            log.info(ModuleClassification.ConM_NSHP_.toString() +","+consensusContext.getChannel().getChannelId()+ ",Transaction in GenericMsg has been modified:" + genericMsg.toString());
            return;
        }
        reHashGenericMsg(genericMsg);*/
        if (dataVerifier.verifyBlock(genericMsg.getHotStuffDataNode().getBlock(), consensusContext.getChannel().getSecurityServiceKey()) == false) {
            log.error(ModuleClassification.ConM_NSHP_.toString() + ModuleClassification.ConM_Verify_ + consensusContext.getChannel().getChannelId() +
                    " block modified in genericMsg:" + genericMsg.toString());
            return;
        }
        if (passCommonHotstuffMsgCheck(genericMsg, genericMsg.getHotStuffDataNode().getJustify().getHeight() + 1)) {
            /*consensusContext.writeLock();*/
            consensusContext.acceptGenericMsg(genericMsg);
            //consensusContext.getGenericMsgMap().put(genericMsg.getViewNo(), genericMsg);
            //we should only believe the viewNo in QC
            Long maxViewNo = genericMsg.getHotStuffDataNode().getJustify().getBlockViewNo() + 1;
            if (maxViewNo > consensusContext.getViewNoSync()) {
                consensusContext.setViewNoSync(maxViewNo);
            }
            if (consensusContext.getCurrView().getNo() < consensusContext.getViewNoSync() - 1) {
                log.info(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + ",acceptGenericMsg with reasonable bigger view, set WAIT_SYNC_WITH_CHANNEL:" + genericMsg.toString());
                consensusContext.setConsensusStageEnum(ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL);
            }
            if (!ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL.equals(consensusContext.consensusStageEnum)) {
                synchronized (lock) {
                    //?????????????????????
                    if (genericMsg.getViewNo().equals(consensusContext.getCurrView().getNo())) {
                        lock.notifyAll();
                    }

                }
            }
        } else {
            log.info(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + ",accept invalid genericMsg:" + genericMsg.toString() + " Consensus state:" + consensusContext.getConsensusInfo());
        }
    }

    private boolean ifBlockModified(Block block) {
/*        boolean flag = block.getTransactionMap().parallelStream().anyMatch(
                tx -> {
                    if (txExecutorConsensusCache.getWaitExecuteTxPool().ifExist(tx.getSDKTransactionHash())) {
                        return false;
                    } else {
                        if (tranasctionMgr.addTransaction(tx.getSdkTransaction(), false) != null) {
                            return false;
                        } else if (tranasctionMgr.verifySDKTransaction(tx.getSdkTransaction())) {
                            return false;
                        }
                        return true;
                    }
                }
        );
        if (flag) {
            return true;
        }*/
        SecurityService securityService = getLocalSecurityService();
        String merkleRoot = SignForConsensus.merkleBlock(block);
        if (!merkleRoot.equals(block.getBlockHeader().getMerkleRoot())) {
            log.info(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + ",the transaction list order may be modified. block {}", block.getHash());
            return true;
        }
        return false;
    }

    private void reHashGenericMsg(GenericMsg genericMsg) {
        Block block = genericMsg.getHotStuffDataNode().getBlock();
        SecurityService securityService = getLocalSecurityService();
        block.setHash(SignForConsensus.hashBlockHeader(block.getBlockHeader(), securityService));
        genericMsg.setHash(SignForConsensus.hashDataNodeDirectly(genericMsg.getHotStuffDataNode(), securityService));
    }

    private void calcHashOfTx(Block block) {
        /*block.getTransactionMap().parallelStream().forEach(
                tx -> tx.getSdkTransaction().setHash(dataSecurityMgr.calcHashBytes(tx.getSdkTransaction()))
        );*/
        for (SDKTransaction tx : block.getSDKTransactions()) {
            log.info(consensusContext.getChannel().getChannelId() + ",tx hash before recalc {}", tx.getHash());
            tx.setHash(dataSecurityMgr.calcHash(tx));
            log.info(consensusContext.getChannel().getChannelId() + ",tx hash after recalc {}", tx.getHash());
        }
    }

    private void updateViewByGenericMsg(GenericMsg genericMsg) {
        if (consensusContext.getConsensusStageEnum().equals(ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL)) {
            if (genericMsg.getViewNo() == consensusContext.getCurrView().getNo() + 1) {
                Long expire = Long.parseLong(consensusContext.channel.getExtendParam(ConsensusExtendedParamEnum.VIEW_TIMEOUT_MS.getCode()));
                View newView = View.createView(genericMsg.getViewNo(), expire);
                consensusContext.setCurrView(newView);
            }
            if (genericMsg.getViewNo() > consensusContext.getCurrView().getNo() + 1) {
                if (genericMsg.getViewNo() == consensusContext.getViewNoSync() + 1) {
                    consensusContext.setViewNoSync(consensusContext.getViewNoSync() + 1);
                    consensusContext.setViewNoSyncConfirmTimes(consensusContext.getViewNoSyncConfirmTimes() + 1);
                    if (consensusContext.getViewNoSyncConfirmTimes() == 4) {
                        //if this bigger view number accept by three successive leader, trust it.
                        Long expire = Long.parseLong(consensusContext.channel.getExtendParam(ConsensusExtendedParamEnum.VIEW_TIMEOUT_MS.getCode()));
                        View newView = View.createView(genericMsg.getViewNo(), expire);
                        consensusContext.setCurrView(newView);
                    }
                } else {
                    consensusContext.setViewNoSync(genericMsg.getViewNo());
                    consensusContext.setViewNoSyncConfirmTimes(0);
                }
            }
        }
    }

    /**
     * ??????GenericQC????????????
     * ???????????????????????????BlockVoteMsg????????????????????????????????????????????????????????????Block
     *
     * @param justify
     * @return
     */
    private boolean isValidQCAndCorrectSign(GenericQC justify) {
        if (!isValidQC(justify)) {
            return false;
        }

        for (BlockVoteMsg msg : justify.getVoteMap().values()) {
            //TODO xxm ????????????????????????
            if (!isChannelMemberMsgWithCorrectSign(msg, justify.getHeight())) {
                log.info(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + ",invalid voteMsg in QC:" + justify.toString());
                return false;
            }
        }
        return true;
    }

    /**
     * ??????GenericQC????????????
     * ???????????????????????????BlockVoteMsg????????????????????????????????????????????????????????????Block
     *
     * @param justify
     * @return
     */
    private boolean isValidQC(GenericQC justify) {
        if (this.checkIfQChasEnoughVotes(justify) == false) {
            log.info(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + ",checkIfQChasEnoughVotes failed,QC:" + justify.toString());
            return false;
        }
        //??????????????????BlockVoteMsg
        if (justify.basicCheck() == false) {
            return false;
        }

        return true;
    }

    private boolean checkIfQChasEnoughVotes(GenericQC genericQC) {
        if (genericQC.getHeight() <= 2) {
            return true;
        } else {
            int qcMin = consensusContext.getQCMinCntOfHeight(genericQC.getHeight());
            if (genericQC.getVoteMap().size() < qcMin) {
                return false;
            }
        }
        return true;
    }


    /**
     * ?????????????????????????????????????????????
     */
    public void acceptNewTransaction(String transHash) {

        Channel channel = consensusContext.getChannel();
        //TODO:???????????????????????????????????????
        PooledTransaction trans = tranasctionMgr.extractTransactionsByHash(
                transHash, channel.getChannelId());
        //List<PooledTransaction> pooledTransactionsList = new LinkedList<PooledTransaction>();
        if (null == trans) {
            //Duplicate transaction
            return;
        }
        //???????????????????????????
        if (dataVerifier.verifyTransaction(trans.getSdkTransaction(), consensusContext.getChannel().getSecurityServiceKey()) == false) {
            log.debug(ModuleClassification.ConM_Verify_.toString() + consensusContext.getChannel().getChannelId() +
                    " accept tx modified.tx:transHash={},tx={}", trans.getSdkTransaction().getHash(), JSONObject.toJSON(trans.getSdkTransaction()));
            tranasctionMgr.removeTransaction(transHash, consensusContext.getChannel().getChannelId());
            return;
        }

        // ???????????????????????????????????????????????????(methodArgs??? ArrayListl??????)??????????????????????????????????????????????????????????????????
        try {
            long startTime = System.currentTimeMillis();
            if ((trans.getSdkTransaction().getSmartContractCallInstnace().getMethodArgs()[0] instanceof ArrayList)
                    && (trans.getSdkTransaction().getSmartContractCallInstnace().getMethodName().equals("deploySmartContract"))) {
                SmartContractInfo smartContractInfo = ((List<SmartContractDeployToken>) (trans.getSdkTransaction().getSmartContractCallInstnace().getMethodArgs()[0])).get(0).getSmartContractInfo();
                if (this.ledgerMgr.getTransactionCompile(smartContractInfo.toString()) == null) {
                    SmartContract smartContractCopy = SmartContract.createInstance(smartContractInfo);
                    new SmartContractCompile().compileSmartContract(smartContractCopy);
                    HashMap<String, byte[]> map = smartContractCopy.getInnerClassFileList();
                    byte[] byteCode = smartContractCopy.getClassFileBytes();
                    if (null != map) {
                        Collection<byte[]> values = map.values();
                        for (int i = 0; i < values.size(); i++) {
                            byteCode = ArrayUtils.addAll(byteCode, map.get(i));
                        }
                    }
                    smartContractCopy.setClassFileHash(this.dataSecurityMgr.getHash(Base64.getEncoder().encodeToString(byteCode)));
                    this.ledgerMgr.setTransactionCompile(smartContractInfo.toString(), new TransactionCompile(trans.getSdkTransaction(), smartContractCopy));
                    log.info(consensusContext.getChannel().getChannelId()+",??????????????????????????????{}", System.currentTimeMillis() - startTime);
                }
            }
        } catch (Exception ex) {
            log.error(consensusContext.getChannel().getChannelId()+",??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????\n{}", ex.toString());
        }

        txExecutorConsensusCache.addPooledTxToPool(trans);
        /*List<PooledTransaction> pooledTransactionsList = new ArrayList<>();
        pooledTransactionsList.add(trans);
        txExecutorConsensusCache.preExecutePooledTransaction(pooledTransactionsList, consensusContext);*/
    }


    public void acceptNewViewMsg(NewViewMsg newViewMsg) {
        //????????????????????????????????????????????????????????????????????????????????????????????????????????????
        if (consensusContext.getPeerCount() > 1 && consensusContext.getConsensusStageEnum().equals(ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL)) {
            log.info(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + ",can't accept newViewMsg at "
                    + consensusContext.consensusStageEnum + " msg:" + newViewMsg.toString());
            return;
        }

        if (!isValidQC(newViewMsg.getJustify())) {
            log.info(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + ",invalid QC in newViewMsg:" + newViewMsg.toString());
            return;
        } else if (consensusContext.getBlock(newViewMsg.getJustify().getBlockHash()) != null) {
            updateHighestQCIfNeeded(newViewMsg.getJustify());
            updateGenericQCIfNeed(newViewMsg.getJustify());
        }

        if (false == isChannelMemberMsgWithCorrectSign(newViewMsg, newViewMsg.getJustify().getHeight() + 1)) {
        //if (false == isChannelMemberMsgWithCorrectSign(newViewMsg, newViewMsg.getJustify().getHeight())) {
            log.warn(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + ",not signed by channel member,newViewMsg:" + newViewMsg.toString());
            return;
        }

        /**
         * NewViewMsg????????????ViewNo??????????????????????????????Leader(n+1)???????????????????????????viewNo???
         * ??????NewViewMsg???viewNo??????????????????????????????viewNo-1?????????????????????????????????
         */
        if (newViewMsg.getViewNo() < consensusContext.getCurrView().getNo() - 1) {
            log.info(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + " ,view too small in newViewMsg" + newViewMsg.toString() + consensusContext.getConsensusInfo());
            return;
        }

        Map<String, NewViewMsg> newViewMsgMap = consensusContext.getNewViewMsgMap().get(newViewMsg.getViewNo());
        if (newViewMsgMap != null) {
            if (newViewMsgMap.get(newViewMsg.getBussinessKey()) != null) {
                //??????????????????????????????
                log.warn(ModuleClassification.ConM_NSHP_ +consensusContext.getChannel().getChannelId()+"," +logPrefix() + "??????????????????NewViewMsg????????????:" + newViewMsg.toString());
                return;
            } else {
                newViewMsgMap.put(newViewMsg.getBussinessKey(), newViewMsg);
            }
        } else {
            synchronized (this) {
                if (consensusContext.getNewViewMsgMap().get(newViewMsg.getViewNo()) == null) {
                    newViewMsgMap = new ConcurrentHashMap<>();
                    consensusContext.getNewViewMsgMap().put(newViewMsg.getViewNo(), newViewMsgMap);
                } else {
                    newViewMsgMap = consensusContext.getNewViewMsgMap().get(newViewMsg.getViewNo());
                }
            }
            try {
                newViewMsgMap.put(newViewMsg.getBussinessKey(), newViewMsg);
            } catch (Exception ex) {
                log.info(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + " accept genericMsg failed, just ignore it. NewViewMsg:" +
                        newViewMsg.toString() + ",consensus info:" + consensusContext.getConsensusInfo(), ex);
                return;
            }
        }

        synchronized (lock) {
            //?????????????????????
            lock.notifyAll();
        }
        log.info(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + " accept valid newViewMsg:" + newViewMsg.toString());

    }

    /**
     * @param voteMsg     ????????????
     * @param blockHeight ???????????????????????????
     */
    public void acceptBlockVoteMsg(BlockVoteMsg voteMsg, Long blockHeight) {
        //????????????????????????????????????????????????????????????????????????????????????????????????????????????
        if (consensusContext.getConsensusStageEnum().equals(ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL)) {
            log.info(ModuleClassification.ConM_NSHP_ + "," + consensusContext.getChannel().getChannelId() + ",can't accept blockVoteMsg at " + ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL +
                    " voteMsg:" + voteMsg.toString() + consensusContext.getConsensusInfo());
            return;
        }
        if (!voteMsg.getViewNo().equals(consensusContext.getCurrView().getNo())) {
            log.info(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + ",Block vote msg with wrong viewNo, omit it " + voteMsg.toString() + consensusContext.getConsensusInfo());
            return;
        }

        GenericMsg genericMsg = consensusContext.getGenericMsgOfCurrView();
        if (genericMsg == null) {
            log.info(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + ",Stale block vote Msg" + voteMsg.toString() + consensusContext.getConsensusInfo());
            return;
        }

        Map<String, BlockVoteMsg> blockVoteMapOfCurrView = consensusContext.getBlockVoteMap().get(genericMsg.getHotStuffDataNode().getBlock().getHash());
        if (blockVoteMapOfCurrView == null) {
            blockVoteMapOfCurrView = createBlockVoteMsgMap(genericMsg.getHotStuffDataNode().getBlock().getHash());
        }
        if (blockVoteMapOfCurrView.get(voteMsg.getBussinessKey()) != null) {
            //??????????????????????????????
            log.warn(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + " ,repeated BlockVoteMsg " + voteMsg.toString() + consensusContext.getConsensusInfo());
            return;
        }
        Block currentBlock = consensusContext.getGenericMsgOfCurrView().getHotStuffDataNode().getBlock();
        String localBlockHashStr = currentBlock.getHash();
        if (false == localBlockHashStr.equals(voteMsg.getBlockHash())) {
            log.warn(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + " ,conflict voteMsg " + voteMsg.toString() + consensusContext.getConsensusInfo());
            return;
        }
        if (isFromValidPeer(voteMsg) == false) {
            log.warn(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + ",Accept invalid block vote msg from invalid peer. " + voteMsg.toString());
            return;
        }

        if (passCommonHotstuffMsgCheck(voteMsg, currentBlock.getBlockHeader().getHeight())) {
            blockVoteMapOfCurrView.put(voteMsg.getBussinessKey(), voteMsg);
            synchronized (lock) {
                //?????????????????????
                lock.notifyAll();
            }
            log.info(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + " ,accept valid BlockVoteMsg:" + voteMsg.toString() + consensusContext.getConsensusInfo());
        }
    }

    private boolean isFromValidPeer(BlockVoteMsg msg) {
        Block votedBlock = consensusContext.getBlock(msg.getBlockHash());
        if (null == votedBlock) {
            log.info(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + ",Cannot find the block of the BlockVoteMsg " + msg.toString());
            return false;
        }
        List<Peer> validPeerList = consensusContext.getChannel().getValidMemberPeerList(votedBlock.getBlockHeader().getHeight());
        for (Peer peer : validPeerList) {
            if (peer.getPeerId().equals(msg.getSignerIdentityKey().getIdentityKey())) {
                return true;
            }
        }
        return false;
    }


    /**
     * ???????????????height????????????commit?????????????????????????????????????????????????????????
     * ????????????????????????
     * 1???viewNo???????????????viewNo
     * 2??????????????????Channel?????????
     * 3????????????????????????
     * ??????????????????true???????????????false
     *
     * @return
     */
    public boolean passCommonHotstuffMsgCheck(HotStuffMsg msg, Long height) {
        if (msg.getViewNo() < consensusContext.getCurrView().getNo()) {
            log.warn(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + " ,message with too small view" + consensusContext.getConsensusInfo());
            return false;
        }
        if (!isChannelMemberMsgWithCorrectSign(msg, height)) {
            log.warn(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + " ,message not from channel member");
            return false;
        }
        return true;
    }

    /**
     * ???????????????blockHeight????????????commit??????????????????????????????????????????
     * ??????????????????????????????????????????????????????????????????
     *
     * @param msg
     * @return
     */
    private boolean isChannelMemberMsgWithCorrectSign(HotStuffMsg msg, Long blockHeight) {
        //??????????????????Channel?????????
        List<Peer> memberList = consensusContext.getChannel().getValidMemberPeerList(blockHeight);
        //for debug
/*        for (Peer peer : memberList) {
            log.info("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",valid peer {}", peer.getPeerId().getValue());
        }*/
        if (isMsgSignedByChannelMember(msg, memberList, blockHeight) == false) {
            return false;
        }
        return true;
    }

    public boolean isMsgSignedByChannelMember(HotStuffMsg msg, List<Peer> peerList, Long blockHeight) {
        boolean isChannelMember = false;
        Peer sendPeer = null;
        for (Peer member : peerList) {
            if (msg == null) {
                log.info(consensusContext.getChannel().getChannelId()+",msg is null");
            }
            if (msg.getSignerIdentityKey() == null) {
                log.info(consensusContext.getChannel().getChannelId()+",signerIdentityKey is null");
            }
            if (member == null) {
                log.info(consensusContext.getChannel().getChannelId()+",member is null");
            }
            if (member.getPeerId() == null) {
                log.info(consensusContext.getChannel().getChannelId()+",peerid is null");
            }
            if (msg.getSignerIdentityKey().equalsWithIdentityKey(member.getPeerId())) {
                sendPeer = member;
                isChannelMember = true;
            }
        }
        if (!isChannelMember) {
            log.warn(ModuleClassification.ConM_NSHP_ + "," + consensusContext.getChannel().getChannelId() + ",Not sign by valid peer member");
            for (Peer member : peerList) {
                log.info(consensusContext.getChannel().getChannelId()+",isMsgSignedByChannelMember peer {}", member.getPeerId().getValue());
            }
            return false;
        }
        //TODO ???????????????????????????????????????
        boolean isPeerCertificateWhiteList = false;
        //for debug
        //log.info("the number of certificate of peer is {}", sendPeer.getPeerCert().size());
        long maxBlockHeight = 0;
        for (PeerCert peerCert : sendPeer.getPeerCert()) {
            if (peerCert.getFlag().equals("0")) {
                isPeerCertificateWhiteList = true;
            }
            //?????????????????????
            if (peerCert.getBlockHeight() > maxBlockHeight) {
                maxBlockHeight = peerCert.getBlockHeight();
            }
        }
        if (blockHeight <= maxBlockHeight + 3) {
            //??????????????????????????????????????????????????????????????????
            return true;
        }
        if (!isPeerCertificateWhiteList) {
            log.warn(ModuleClassification.ConM_NSHP_ + "," + consensusContext.getChannel().getChannelId() + " ,invalid peer certifcate");
            if (null != sendPeer) {
                log.info(ModuleClassification.ConM_NSHP_ + "," + consensusContext.getChannel().getChannelId() + " ,certblacklist sender={}", sendPeer.getPeerId().getValue());
            }
            return false;
        }
        //if (false == this.getLocalSecurityService().sm9VerifySignature(msg,this.consensusContext.channel.getChannelId()))
        if (msg instanceof GenericMsg) {
            if (!SignForConsensus.verifySignOfGenericMsg((GenericMsg) msg, getLocalSecurityService())) {
                log.info(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + ",Invalid signature of genericMsg:" + msg.toString());
                return false;
            }
        } else if (false == this.getLocalSecurityService().verifySignatureByGMCertificate(msg, consensusContext.getChannel().getChannelId())) {
            log.info(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + " ,Invalid signature");
            return false;
        }
        return true;
    }


    public void processChannelUpdate(Channel newChannel) {
        log.info(consensusContext.getChannel().getChannelId()+",processChannelUpdate.start");
        /*Channel oldChannel = this.consensusContext.getChannel();
        List<SmartContract> addedScList = new ArrayList<>();
        List<Peer> addedPeerList = new ArrayList<>();
        List<Peer> deledPeerList = new ArrayList<>();
        List<Peer> updatePeerList = new ArrayList<>();

        Channel.compare(newChannel, oldChannel, addedScList, addedPeerList, deledPeerList,updatePeerList);
        log.info("Channel??????:?????????????????????=" + addedScList + ",?????????Peer=" + addedPeerList + "????????????Peer=" + deledPeerList);
        this.consensusContext.writeLock();
        if (addedPeerList.size() > 0) {
            //TODO
            this.consensusContext.getOrderedPeerList().addAll(addedPeerList);
            oldChannel.getMemberPeerList().addAll(addedPeerList);
        }
        if (deledPeerList.size() > 0) {
            //TODO
            Peer peer = this.consensusContext.myself;
            for (int i = 0; i < deledPeerList.size(); i++) {
                Peer delPeer = deledPeerList.get(i);
                this.consensusContext.getOrderedPeerList().remove(delPeer);
                oldChannel.getMemberPeerList().remove(delPeer);
                if (peer.equals(delPeer)) {
                    //????????????
                    this.consensusContext.setConsensusStageEnum(ConsensusStageEnum.LEAVE_CHANNEL);

                    consensusMsgProcessor.newSpiralSyncHistoryBlockHashMap.get(oldChannel.getChannelId()).flag = false;
                    consensusMsgProcessor.newSpiralHotStuffHashMap.get(oldChannel.getChannelId()).flag = false;
                    consensusMsgProcessor.newSpiralSyncHistoryBlockHashMap.remove(oldChannel.getChannelId());
                    consensusMsgProcessor.newSpiralHotStuffHashMap.remove(oldChannel.getChannelId());
                }
            }
        }
        if (addedScList.size() > 0) {
            this.txExecutorConsensusCache.loadSmartContractList(addedScList);
            List<SmartContract> smartContractList = new LinkedList<>();
            smartContractList.addAll(addedScList);
            if (!CollectionUtils.isEmpty(oldChannel.getSmartContractList())) {
                smartContractList.addAll(oldChannel.getSmartContractList());
            }
            oldChannel.setSmartContractList(smartContractList);
        }
        oldChannel.setLatestChannelChangeHeight(newChannel.getLatestChannelChangeHeight());
        this.consensusContext.channel = oldChannel;*/
        //this.consensusContext.writeLock();
        //Channel newChannel=this.ledgerMgr.queryChannel(this.consensusContext.getChannel().getChannelId())
        //??????????????????????????????????????????
//        List<SmartContract> smartContracts=Channel.compareSmartContractList(this.consensusContext.getChannel().getSmartContractList(),newChannel.getSmartContractList());
//        if(!CollectionUtils.isEmpty(smartContracts)) {
//            this.txExecutorConsensusCache.updateSmartContract(smartContracts);
//        }
        this.consensusContext.channel = newChannel;
        this.consensusContext.orderedPeerList = this.consensusContext.calcOrderPeerList(this.consensusContext.channel.getMemberPeerList());
        //List<Peer> list=this.consensusContext.orderedPeerList.parallelStream().filter(peer -> peer.isState()).collect(Collectors.toList());
        log.info(consensusContext.getChannel().getChannelId()+",the orderedPeerList  size {}", this.consensusContext.orderedPeerList.size());
        log.info(consensusContext.getChannel().getChannelId()+",the memberPeerList list size {}", this.consensusContext.getChannel().getMemberPeerList().size());
        //for debug
        /*log.info(ModuleClassification.ConM_NSHP_+"the peer list of newChannel is as follow");
        for (Peer peer : this.consensusContext.channel.getMemberPeerList()) {
            log.info(ModuleClassification.ConM_NSHP_+"peer {}, inheight {}, outheight{}", peer.getPeerId().getValue(), peer.getPeerChannelRelation().getInBlockHeight(),
                    peer.getPeerChannelRelation().getOutBlockHeight());
        }*/
        ledgerMgr.updateChannelCache(newChannel);
        if (this.consensusContext.consensusStageEnum.equals(ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL) == false) {
            //for debug
            //log.info(ModuleClassification.ConM_NSHP_+"latestChannelChangeHeight {}", newChannel.getLatestChannelChangeHeight());
            //hotstuffInterface.updateChannelPeerClient(newChannel);
            boolean isLeaveChannel = newChannel.getAvailable().intValue() == 0 ? true : false;
            List<Peer> peerList = this.consensusContext.channel.getValidMemberPeerList(newChannel.getLatestChannelChangeHeight() + 4);
            if (peerList.contains(this.consensusContext.myself) == false && isLeaveChannel) {
                log.info(ModuleClassification.ConM_NSHP_ + "," + consensusContext.getChannel().getChannelId() + " ,Local peer is going to leave channel " + consensusContext.getConsensusInfo());
                this.consensusContext.setConsensusStageEnum(ConsensusStageEnum.LEAVE_CHANNEL);
                //this.consensusContext.writeUnlock();
                //todo: ????????????????????????????????????????????????????????????????????????????????????????????????
                consensusMsgProcessor.newSpiralSyncHistoryBlockHashMap.get(newChannel.getChannelId()).flag = false;
                consensusMsgProcessor.newSpiralHotStuffHashMap.get(newChannel.getChannelId()).flag = false;
                consensusMsgProcessor.transactionExecutionProcessorHashMap.get(newChannel.getChannelId()).flag = false;
                consensusMsgProcessor.newSpiralSyncHistoryBlockHashMap.remove(newChannel.getChannelId());
                consensusMsgProcessor.newSpiralHotStuffHashMap.remove(newChannel.getChannelId());
                consensusMsgProcessor.transactionExecutionProcessorHashMap.remove(newChannel.getChannelId());
                return;
            }
        }
        //this.consensusContext.writeUnlock();
    }


    /**
     * copy ??????
     *
     * @param newChannel
     * @param oldChannel
     */
    private void setChannel(Channel newChannel, Channel oldChannel) {
        oldChannel.setChannelId(newChannel.getChannelId());
        oldChannel.setName(newChannel.getName());
        oldChannel.setBlockMaxSize(newChannel.getBlockMaxSize());
        oldChannel.setBlockMaxInterval(newChannel.getBlockMaxInterval());
        oldChannel.setAllowTimeErrorSeconds(newChannel.getAllowTimeErrorSeconds());
        oldChannel.setMaxPeerCount(newChannel.getMaxPeerCount());
        oldChannel.setTxPoolSize(newChannel.getTxPoolSize());
        oldChannel.setSecurityServiceKey(newChannel.getSecurityServiceKey());
        oldChannel.setAllowTimeErrorSeconds(newChannel.getAllowTimeErrorSeconds());
        oldChannel.setModifyStrategy(newChannel.getModifyStrategy());
        oldChannel.setRoles(newChannel.getRoles());
        oldChannel.setSmartContractDeplyStrategy(newChannel.getSmartContractDeplyStrategy());
        oldChannel.setExtendsParams(newChannel.getExtendsParams());
        oldChannel.setPeerAddStrategyEnum(newChannel.getPeerAddStrategyEnum());
        oldChannel.setConsensusAlgorithm(newChannel.getConsensusAlgorithm());
    }

    public Object processQueryTransaction(SDKTransaction sdkTransaction) {
        return txExecutorConsensusCache.executeQueryTransaction(sdkTransaction);
    }

    public void cleanPooledTransactionPool(Block block) {
        txExecutorConsensusCache.removeTxFromTransactionPool(block);
    }

    public void processPeerCertificateByPeerId(List<PeerCert> peerCerts) {
        //this.consensusContext.writeLock();
        String peerIdValue = peerCerts.get(0).getPeerId();
        for (Peer peer : this.consensusContext.getOrderedPeerList()) {
            if (peer.getPeerId().getValue().equals(peerIdValue)) {
                peer.setPeerCert(peerCerts);
                break;
            }
        }
        //this.consensusContext.writeUnlock();
    }
}
