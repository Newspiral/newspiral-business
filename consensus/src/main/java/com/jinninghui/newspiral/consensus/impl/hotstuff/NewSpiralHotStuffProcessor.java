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
 * NewSpiralHotStuff的主线程
 * 负责处理一个通道的共识
 */
@Slf4j
public class NewSpiralHotStuffProcessor extends TimerTask {
    /**
     * wait()/notify机制所使用的锁
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
     * 此属性需依赖读取的Channel信息动态设置，在init方法中设置
     */
    ConsensusContext consensusContext;

    /**
     * 定义可见变量，用于必要时停止服务
     */
    public volatile boolean flag = true;


    /**
     * 初始化
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
            } catch (Exception ex) {//TODO 这里需要做一个异常日志抑制机制，这里的异常是非预期的异常
                log.error(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION.toString() + ":Exception in consensus processor, System exist. Exception:", ex);
                System.exit(0);
            }
        }
    }


    /**
     * 通道中只有一个节点的运行逻辑，不会有REPLICA_WAIT_BLOCK状态
     */
    void runOnceAsUniquePeer() {
        if (consensusContext.getCurrView().expired()) {
            processViewTimeout();
        } else {
            //没有超时，则进行状态处理
            switch (consensusContext.getConsensusStageEnum()) {
                //这里的枚举是按照逻辑发生顺序排序的
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
                default://其他阶段什么都不用干
                    log.error(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION.toString() + ":Unexpected consensus stage:" + consensusContext.consensusStageEnum.toString());
            }
        }

    }

    /**
     * 参与到HotStuff共识中
     */
    private void runOnceAsConsensusPeer() {
        /**正常状态转换说明:
         * 	从LeaderWaitNewView(n)到LeaderWaitTrans(n)：收到n-f个NewViewMsg消息；并选取New-View消息中view最大的GenericQC，该最高QC将被用来构建GenericMsg(n)消息。
         * 	从LeaderWaitTrans(n)到LeaderWaitBlockVote：构建成功GenericMsg(n)并广播给所有的Replica(n)。
         * 	ReplicaWaitBlock(n+1)：收集到了n-f个GenericMsg(n)的Vote消息，使用这些Vote构造一个GenericQC，更新本地的GenericQC，然后给Leader(n+1)发送一个NewViewMsg(n,n+1)消息。
         * 	从ReplicaWaitBlock(n)到LeaderWaitNewViewMsg(n+1)：自身是Leader(n+1)且收到了合法的GenericMsg(n)，回复了Vote(n)；
         * 	从ReplicaWaitBlock(n)到ReplicaWatiBlock(n+1)：自身不是Leader(n+1)且收到了合法的GenericMsg(n)，回复了Vote(n)，并给Leader(n+1)发送了一个NewViewMsg(n,n+1)消息
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
            //没有超时，则进行状态处理
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
     * 执行校验成功后，共识状态同步修改。
     */
    private void onChangeConsensusContext(HotStuffDataNode receiveNode) {

        consensusContext.addNode(receiveNode);
        if (consensusContext.needRollbackWhileSafe(receiveNode)) {
            // 回退共识上下文
            consensusContext.rollbackConsensusContext(receiveNode);
        }
    }


    /**
     * 适用于副本节点，如果收到了合法的Block，更新本地的数据，然后发送一个Vote消息给本轮的Leader，然后发送一个NewView(n,n+1)消息给到Leader(n+1)，
     * 发送之后更新本地的view为n+1
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
            HotStuffDataNode hotStuffDataNode = currGenericMsg.getHotStuffDataNode();//最新的Node
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
                    //连续第三个空块时，只处理消息中的QC并调整超时时间后直接返回
                    updateHighestQCIfNeeded(hotStuffDataNode.getJustify());
                    updateGenericQCIfNeed(hotStuffDataNode.getJustify());
                    //需要将QC缓存起来
                    consensusContext.addGenericQC(hotStuffDataNode.getJustify());
                    consensusContext.adjustBackViewTimeOut();
                    enterNextView();
                    log.info(ModuleClassification.ConM_NSHP_.toString() + consensusContext.getChannel().getChannelId() +
                            " receive consecutive 3 blank block, ignore it and enter next view");
                    return;
                }
                //非连续第三个空块时，正常更新所有本地共识状态数据
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
     * 证书是否有效
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
        // TODO:这里需要签名校验以及QC校验
        return true;
    }

    /**
     * 检查是否收到对应的父区块
     *
     * @param blockHash
     * @return
     */
    private boolean findIfBlockHasReceived(String blockHash) {
        // 检查本地缓存是否收到区块
        Block prevBlock = consensusContext.getBlock(blockHash);
        if (prevBlock == null) {
            return false;
        }
        return true;
    }

    public boolean processHotStuffDataNode(HotStuffDataNode hotStuffDataNode) {

        // 如果本地未收到相应区块，设置区块同步
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
        // 验证通过后共识状态同步修改
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
     * 更新最新投票的节点的hash
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
        //查找出不在交易池里的交易
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
     * 执行一遍区块进行验证
     *
     * @param hotStuffDataNode 收到区块
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
     * 处理主节点本地生成了新的Block，处理方法副本节点接收到GenericMsg十分类似（即上面的processBlockIfExist方法）
     * 不同之处在于不用更改本地的处理阶段，不用真的通过网络发送BlockVote消息
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
     * 副本节创建投票信息
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
        //voteMsg中增加了通道信息，但是这里的channelId仍然需要保留，原因是签名方法的参数是PRCparm接口形式调用的
        getLocalSecurityService().signByGMCertificate(voteMsg, this.consensusContext.channel.getChannelId());
        return voteMsg;
    }

    /**
     * 更新GenriceQC以及LockQC以及持久化Block
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
            //同步更新genericQC与lockedQC
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
                //TODO:执行系统智能合约
            } else {
                log.info(ModuleClassification.ConM_NSHP_.toString() + consensusContext.getChannel().getChannelId() +
                        " can't find prev block of genericQC");
            }

        } else {
            log.info(ModuleClassification.ConM_NSHP_.toString() + consensusContext.getChannel().getChannelId()+" can't find dataNode of given geneircQC " + genericQC.toString() + consensusContext.getConsensusInfo());
        }
    }


    /**
     * 更新LockQC以及持久化Block
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
     * 持久化Block
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
            //此种情况是考虑到在节点启动的时候，只会从其他节点处同步某个高度以后的datanode
            //todo：所有区块的共识commit逻辑应该一直，即使用同一个数据结构。
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
            //todo:当节点无交易不出空块时，这部分逻辑可能会一直跑
            hotstuffInterface.updateChannelPeerClient(consensusContext.getChannel(), 4l);
        }
    }


    /**
     * 获得与入参Channel匹配的安全服务实例
     *
     * @return
     */
    SecurityService getSecurityService(Channel channel) {
        return securityServiceMgr.getMatchSecurityService(channel.getSecurityServiceKey());
    }

    /**
     * 获得本节点的匹配的安全服务实例，要求consensusContext已经初始化完毕
     *
     * @return
     */
    SecurityService getLocalSecurityService() {
        return securityServiceMgr.getMatchSecurityService(consensusContext.getChannel().getSecurityServiceKey());
    }


    /**
     * 如果存在n-f个Vote，就构造一个GenericQC并更新本地的GenricQC，然后发送一个NewView(n,n+1)消息给到Leader(n+1)
     * 发送之后更新本地的view为n+1
     */
    private void processBlockVoteIfExist() {
        //TODO:将是否有足够的投票封装成单独的函数
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
     * 如果需要扩库了，那么停止共识
     */
    private boolean processNeedExpandDb() {
        Long ttt = System.currentTimeMillis();
        Map<String, String> map = localConfigLedgerMgr.queryByType(SHARDING_CONFIG_TYPE);
        BigInteger blockheight = new BigInteger(String.valueOf(consensusContext.blockHeight + 1));
        BigInteger range = new BigInteger(map.get(SHARDING_RANGE));
        BigInteger dbNum = new BigInteger(map.get(SHARDING_DB_ACTIVE_NUM));
        BigInteger total = range.multiply(dbNum);
        if (blockheight.compareTo(total) >= 0) {
            //没有足够的容量存放
            ledgerMgr.deleteCachedBlockBehind(consensusContext.getChannel().getChannelId(), consensusContext.blockHeight);
            log.error(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + " no available datasource to route,current blockheight: " + blockheight + ",current db total: " + total);
            return false;
        } else {
            if ("true".equals(map.get(SHARDING_CONFIG_ISACTIVE))) {
                //有足够的容量，切回共识状态
                consensusMsgProcessor.changeNoAvailableConsensus(consensusContext.getChannel().getChannelId(), consensusContext.blockHeight - 1, ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL);
                return true;
            }
        }
        return false;
    }


    /*
     * 基于Vote消息构建GenericQC并更新本地的最新GenericQC
     */
    private GenericQC createGenericQC() {
        GenericQC genericQC = new GenericQC();
        Block block = consensusContext.getGenericMsgOfCurrView().getHotStuffDataNode().getBlock();
        genericQC.setBlockHash(block.getHash());
        genericQC.setPrevBlockHash(block.getBlockHeader().getPrevBlockHash());
        genericQC.setHeight(block.getBlockHeader().getHeight());
        //这个genriceQC必然是当前view的BLock的QC
        genericQC.setBlockViewNo(consensusContext.getCurrView().getNo());
        //引用类型，后面会clear,所以new一个哦
        Map<String, BlockVoteMsg> map = consensusContext.getBlockVoteMap().get(block.getHash());
        genericQC.setVoteMap(new LinkedHashMap<>(map));
        genericQC.setBlockCreateTimestamp(consensusContext.getGenericMsgOfCurrView().getHotStuffDataNode().getBlock().getBlockHeader().getTimestamp());
        //设置当前Qc所对应的通道正常节点的总数
        List<Peer> peerList = consensusContext.getOrderedPeerList().parallelStream().filter(peer -> peer.isState()).collect(Collectors.toList());
        genericQC.setPeerCnt(peerList.size());
        //设置genericQc的hash值
        SecurityService securityService = getLocalSecurityService();
        genericQC.setHash(SignForConsensus.hashGenericQc(genericQC, securityService));
        return genericQC;
    }

    /**
     * 进入下一个VIew，既适用于Leader，也适用于Replica，也适用于通道内只有一个节点的情况
     * 内部逻辑为：构造New-View消息发送给下一个View的Leader，更新本地的数据以适配下一个View
     * 要求本地的GenericQC已经正确设置
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
            //TODO 节点冻结之后，这里应该不能继续发送消息了，该节点成为一个拜占庭节点
            log.info(ModuleClassification.ConM_NSHP_ + " nextLeader cert is invalid,{}", nextLeader.getPeerId().toString());
        }
        if (false == nextLeader.equals(consensusContext.myself)) {
            hotstuffInterface.sendNewView(newViewMsg, nextLeader, consensusContext.getChannel().getChannelId());
            log.info(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + " send newViewMsg " + newViewMsg.toString());
        } else {//发给自己
            log.info(ModuleClassification.ConM_NSHP_.toString() +consensusContext.getChannel().getChannelId()+ " accept own newViewMsg:" + newViewMsg.toString());
            this.acceptNewViewMsg(newViewMsg);
        }

    }


    /**
     * 既适用于Channel有多个节点的情况，也适用于Channel只有一个节点的情况
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
            //广播不用发送给自己
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
                //当出现连续第三个空块时，走这里的逻辑，此时只需要更新超时时间并进入下一个视图即可
                log.info(ModuleClassification.ConM_NSHP_.toString() + consensusContext.getChannel().getChannelId() +
                        " create consecutive 3 blank block, ignore it and step into next view");
                consensusContext.adjustBackViewTimeOut();
                enterNextView();
                return;
            }
            //当且仅当不是被忽略的空块才缓存起来
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
     * 是否可以创建区块
     * 满足以下任意一个条件即可创建区块:(1)距离上一个区块的生成时间已经超过配置的最大区块生成间隔blockMaxInterval而且存在待处理的交易；
     * (2)已有的交易字节数大于等于Channel的配置参数blockMaxSize
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

        //当前构建的
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

        //先计算Hash后签名

        SecurityService securityService = getLocalSecurityService();
        String merkleRoot = SignForConsensus.merkleBlock(block);
        blockHeader.setMerkleRoot(merkleRoot);
        //计算block的hash
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
     * 如果收集到了n-f个New-view消息，就进入LEADER_WAIT_TRANS
     */
    private void processNewViewMsgIfEnough() {

/*        //TODO 需要确认，上一轮超时，这里直接进入下一个view
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
     * 存在n-f个NewView消息并且其中包含上一view的Leader发送的NewView消息
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
        //并行检查一下（上一个View的Leader发送了NewView消息）
        return newViewMsgMapOfCurrView.values().parallelStream().anyMatch(
                Msg->{
                   return Msg.getCallerIdentityKey().equalsWithIdentify(prevViewLeader.getPeerId());
                }
        );
*/
        //此处为了尽可能保证共识往前走，需要等待上一个视图中的节点的newViewMsg，但是严格上，这可能会导致因为上一轮的leader拜占庭而影响本视图的共识；
        //todo:寻找一个合理的平衡机制，在“等待上一个视图的leader的newViewMsg”与“收集到足够的newViewMsg之后直接往下走”之间平衡
        for (NewViewMsg newViewMsg : newViewMsgMapOfCurrView.values()) {
            try {
                if (newViewMsg.getSignerIdentityKey().getIdentityKey().equals(prevViewLeader.getPeerId())) {//上一个View的Leader发送了NewView消息
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
     * 使用NewView消息列表来更新本地的genericQC，使得本地的genericeQC是所有发送NewView消息节点所构成的Quarum中的最新值
     * 此最新值将作为本Leader发送的Generic消息的GenericQC
     */
    private void updateHighestQCFromNewViewMsg() {
        Map<String, NewViewMsg> viewMsgMap = consensusContext.getNewViewMsgMap().get(consensusContext.getCurrView().getNo() - 1);
        GenericQC highestQC = consensusContext.getHighestQC();//初始化为本地的highestQC
        for (NewViewMsg newViewMsg : viewMsgMap.values()) {
            if (newViewMsg.getJustify().newerThan(highestQC)) {
                highestQC = newViewMsg.getJustify();
            }
        }
        //检查QC的合法性
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
     * 处理本View超时
     * 超时的处理十分简单，进下一个view即可
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
     * 接收到一条GenericMsg的处理，既适用于Replica，也适用于只有一个节点的情况
     * 此处执行基本检查，但不检查该消息与本地的Node集合的关系是否OK
     * 如果节点处于同步状态，则如果genericMsg中的QC合法，则将该消息缓存起来，并更新本地的view值
     * 如此，可以避免节点重新向其他节点请求该区块数据
     * <p>
     * This function also rehashes sdkTransactions.
     *
     * @param genericMsg
     */
    public void acceptGenericMsg(GenericMsg genericMsg) {
        //TODO:检查该genericMsg是否应该由该发送者发送 已经用下面这个
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
                    //通知主线程运行
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
     * 某个GenericQC是否合法
     * 合法的条件是：每个BlockVoteMsg都是合法的，不重复且数量足够，指向相同的Block
     *
     * @param justify
     * @return
     */
    private boolean isValidQCAndCorrectSign(GenericQC justify) {
        if (!isValidQC(justify)) {
            return false;
        }

        for (BlockVoteMsg msg : justify.getVoteMap().values()) {
            //TODO xxm 演示暂时关闭注释
            if (!isChannelMemberMsgWithCorrectSign(msg, justify.getHeight())) {
                log.info(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + ",invalid voteMsg in QC:" + justify.toString());
                return false;
            }
        }
        return true;
    }

    /**
     * 某个GenericQC是否合法
     * 合法的条件是：每个BlockVoteMsg都是合法的，不重复且数量足够，指向相同的Block
     *
     * @param justify
     * @return
     */
    private boolean isValidQC(GenericQC justify) {
        if (this.checkIfQChasEnoughVotes(justify) == false) {
            log.info(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + ",checkIfQChasEnoughVotes failed,QC:" + justify.toString());
            return false;
        }
        //不能有重复的BlockVoteMsg
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
     * 有新交易入池时处理交易预执行。
     */
    public void acceptNewTransaction(String transHash) {

        Channel channel = consensusContext.getChannel();
        //TODO:根据交易哈希抽取入池的交易
        PooledTransaction trans = tranasctionMgr.extractTransactionsByHash(
                transHash, channel.getChannelId());
        //List<PooledTransaction> pooledTransactionsList = new LinkedList<PooledTransaction>();
        if (null == trans) {
            //Duplicate transaction
            return;
        }
        //校验交易是否被篡改
        if (dataVerifier.verifyTransaction(trans.getSdkTransaction(), consensusContext.getChannel().getSecurityServiceKey()) == false) {
            log.debug(ModuleClassification.ConM_Verify_.toString() + consensusContext.getChannel().getChannelId() +
                    " accept tx modified.tx:transHash={},tx={}", trans.getSdkTransaction().getHash(), JSONObject.toJSON(trans.getSdkTransaction()));
            tranasctionMgr.removeTransaction(transHash, consensusContext.getChannel().getChannelId());
            return;
        }

        // 执行预编译，只针对部署智能合约类型(methodArgs为 ArrayListl类型)，其他执行智能合约函数的交易类型可以直接略过
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
                    log.info(consensusContext.getChannel().getChannelId()+",预编译智能合约耗时：{}", System.currentTimeMillis() - startTime);
                }
            }
        } catch (Exception ex) {
            log.error(consensusContext.getChannel().getChannelId()+",执行预编译失败，交易仍会被正常收录，将在交易执行时重新进行编译；有可能交易不是部署智能合约类型，如果是部署智能合约类型，则有可能会因编译耗时导致交易超时而执行失败：\n{}", ex.toString());
        }

        txExecutorConsensusCache.addPooledTxToPool(trans);
        /*List<PooledTransaction> pooledTransactionsList = new ArrayList<>();
        pooledTransactionsList.add(trans);
        txExecutorConsensusCache.preExecutePooledTransaction(pooledTransactionsList, consensusContext);*/
    }


    public void acceptNewViewMsg(NewViewMsg newViewMsg) {
        //这么一个个检查，是为了便于记录精确的错误日志，降低根据日志分析问题的难度
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
         * NewViewMsg不能要求ViewNo是否与本地相等，因为Leader(n+1)不一定是第一个切换viewNo的
         * 但是NewViewMsg的viewNo一定要大于等于本地的viewNo-1，否则接收了也没有意义
         */
        if (newViewMsg.getViewNo() < consensusContext.getCurrView().getNo() - 1) {
            log.info(ModuleClassification.ConM_NSHP_.toString() + "," + consensusContext.getChannel().getChannelId() + " ,view too small in newViewMsg" + newViewMsg.toString() + consensusContext.getConsensusInfo());
            return;
        }

        Map<String, NewViewMsg> newViewMsgMap = consensusContext.getNewViewMsgMap().get(newViewMsg.getViewNo());
        if (newViewMsgMap != null) {
            if (newViewMsgMap.get(newViewMsg.getBussinessKey()) != null) {
                //这个检查更快，放前面
                log.warn(ModuleClassification.ConM_NSHP_ +consensusContext.getChannel().getChannelId()+"," +logPrefix() + "接收到重复的NewViewMsg，忽略它:" + newViewMsg.toString());
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
            //通知主线程运行
            lock.notifyAll();
        }
        log.info(ModuleClassification.ConM_NSHP_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + " accept valid newViewMsg:" + newViewMsg.toString());

    }

    /**
     * @param voteMsg     投票列表
     * @param blockHeight 被投票的区块的高度
     */
    public void acceptBlockVoteMsg(BlockVoteMsg voteMsg, Long blockHeight) {
        //这么一个个检查，是为了便于记录精确的错误日志，降低根据日志分析问题的难度
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
            //这个检查更快，放前面
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
                //通知主线程运行
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
     * 基于高度为height的区块被commit完成时，所得到的通道的节点列表进行检查
     * 通用检查，包括：
     * 1、viewNo大于本地的viewNo
     * 2、发送者是本Channel的成员
     * 3、发送者签名合法
     * 检查通过返回true，否则返回false
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
     * 基于高度为blockHeight的区块被commit之后得到的的节点列表进行检查
     * 检查某个消息是本通道的成员发送的并且签名正确
     *
     * @param msg
     * @return
     */
    private boolean isChannelMemberMsgWithCorrectSign(HotStuffMsg msg, Long blockHeight) {
        //发送者是否是Channel的成员
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
        //TODO 发送者证书是否在百名单里面
        boolean isPeerCertificateWhiteList = false;
        //for debug
        //log.info("the number of certificate of peer is {}", sendPeer.getPeerCert().size());
        long maxBlockHeight = 0;
        for (PeerCert peerCert : sendPeer.getPeerCert()) {
            if (peerCert.getFlag().equals("0")) {
                isPeerCertificateWhiteList = true;
            }
            //证书变化的高度
            if (peerCert.getBlockHeight() > maxBlockHeight) {
                maxBlockHeight = peerCert.getBlockHeight();
            }
        }
        if (blockHeight <= maxBlockHeight + 3) {
            //过滤掉当前证书变动生效之后的几个高度时的验证
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
        log.info("Channel变更:新增的智能合约=" + addedScList + ",新增的Peer=" + addedPeerList + "，删除的Peer=" + deledPeerList);
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
                    //移除进程
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
        //移除重新加载业务智能合约方法
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
                //todo: 如果通道不可用，则关闭该通道的同步模块，共识模块以及交易处理模块
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
     * copy 一下
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
