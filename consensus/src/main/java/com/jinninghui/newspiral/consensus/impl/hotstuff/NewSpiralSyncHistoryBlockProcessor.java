package com.jinninghui.newspiral.consensus.impl.hotstuff;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.chain.Peer;
import com.jinninghui.newspiral.common.entity.common.logmodule.LogModuleCodes;
import com.jinninghui.newspiral.common.entity.consensus.ConsensusStageEnum;
import com.jinninghui.newspiral.common.entity.consensus.GenericMsg;
import com.jinninghui.newspiral.common.entity.consensus.GenericQC;
import com.jinninghui.newspiral.common.entity.consensus.HotStuffDataNode;
import com.jinninghui.newspiral.common.entity.consensus.HotStuffMsg;
import com.jinninghui.newspiral.common.entity.consensus.View;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.consensus.hotstuff.DataVerifier;
import com.jinninghui.newspiral.gateway.entity.QueryChainStateReq;
import com.jinninghui.newspiral.gateway.entity.QueryChainStateResp;
import com.jinninghui.newspiral.gateway.entity.QueryHistoryBlockReq;
import com.jinninghui.newspiral.gateway.entity.QueryHistoryBlockResp;
import com.jinninghui.newspiral.gateway.entity.QueryHistoryQCReq;
import com.jinninghui.newspiral.gateway.entity.QueryHistoryQCResp;
import com.jinninghui.newspiral.ledger.mgr.LedgerMgr;
import com.jinninghui.newspiral.p2p.P2pClient;
import com.jinninghui.newspiral.p2p.hotstuff.HotstuffRPCInterface;
import com.jinninghui.newspiral.security.SecurityService;
import com.jinninghui.newspiral.security.SecurityServiceMgr;
import com.jinninghui.newspiral.transaction.mgr.TransactionMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

/**
 * @author ganzirong
 * @date 2019/10/25
 * This is for synchronization with network.
 * Make sure the local state is credible and reasonable before taking part in consensus.
 */
@Slf4j
public class NewSpiralSyncHistoryBlockProcessor extends TimerTask {

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

    @Autowired
    private DataVerifier dataVerifier;

    private NewSpiralHotStuffProcessor newSpiralHotStuffProcessor;
    /**
     * the map of committed block, only used in synchronization.
     * key is the height of committed block,
     * value is the hash string of committed block.
     */
    private Map<Long, String> committedBlockChain;
    /**
     * the max height in map committedBlockChain.
     */
    private Long committedBlockChainHeight = new Long(-1);
    /**
     * cached the committed block requested from other peer.
     */
    private Map<Long, Block> mapCommittedBlock;
    /**
     * cached the preparedQC for blocks;
     */
    private Map<String, GenericQC> mapBlockQC;

    /**
     * map of (block hash, block state)
     * mark the state of block:
     * "prepare": block with only prepareQC
     * "preCommit": block with preCommitQC and prepareQC
     * "commit": block with commitQC, preCommitQC and prepareQC (not used but just for understanding)
     */
    private Map<String, Integer> mapBlockState;

    private Integer prepare = new Integer(1);
    private Integer preCommit = new Integer(2);
    //Integer commit = new Integer(3);
    /**
     * map of (block hash, hash of parent of block)
     * to get the parent, grandparent of one block.
     */
    private Map<String, String> mapBlockParent;

    //TODO:What is the best number of maxQCperRound and maxBlockPerRound
    private Integer maxQCperRound = 1000;

    private Integer maxBlockPerRound = 20;

    //times of synchronization before trying to begin consensus.
    private Integer timesToSync = 3;

    /**
     * initialize in init.
     */
    ConsensusContext consensusContext;
    /**
     * 定义可见变量，用于必要时停止服务
     */
    public volatile boolean flag = true;


    /**
     * Only invoke once, before run()
     *
     * @param consensusContext
     */
    public void init(ConsensusContext consensusContext, NewSpiralHotStuffProcessor newSpiralHotStuffProcessor) {
        if (this.consensusContext != null) {
            return;
        }
        this.consensusContext = consensusContext;
        this.newSpiralHotStuffProcessor = newSpiralHotStuffProcessor;
        this.mapCommittedBlock = new HashMap<>();
        this.mapBlockQC = new HashMap<>();
        this.mapBlockParent = new HashMap<>();
        this.mapBlockState = new HashMap<>();
        this.committedBlockChain = new HashMap<>();
        log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",初始化数据同步线程成功");
    }

    /**
     * 获得本节点的匹配的安全服务实例，要求consensusContext已经初始化完毕
     *
     * @return
     */
    private SecurityService getLocalSecurityService() {
        return securityServiceMgr.getMatchSecurityService(consensusContext.getChannel().getSecurityServiceKey());
    }


    @Override
    public void run() {
        if (flag) {
            try {
                if (consensusContext.getConsensusStageEnum().equals(ConsensusStageEnum.LEAVE_CHANNEL)) {
                    return;
                }
                // TODO:use notification mode here may be better.
                if (ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL.equals(consensusContext.consensusStageEnum)) {
                    log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",Peer is left behind, try to sync with channel");
                    //get chain state
                    QueryChainStateResp bestChainState = queryBestChainStateFromNetwork();
                    if (bestChainState == null) {
                        log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",No available peer");
                        tryUpdateConsensusStage();
                        return;
                    }
                    log.info(ModuleClassification.ConM_Sync_.toString() + consensusContext.getChannel().getChannelId() +
                            " receive best chain state: height: " + bestChainState.getHeight() + " genericQC:" +
                            bestChainState.getPrePrepare().getJustify().toString() + ", locedQC:" +
                            bestChainState.getPrepare().getJustify().toString() + ", committedQC:" +
                            bestChainState.getPreCommit().getJustify().toString());
                    if (updateConsensusContextFromNetwork(bestChainState)) {
                        log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",Find higher block data, update local state successfully");
                        if (requestHistoryBlock(bestChainState)) {
                            if (processCachedGenericMsg(bestChainState)) {
                                log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",Succeed to sync with channel, try to update consensus stage");
                                tryUpdateConsensusStage();
                                //newSpiralHotStuffProcessor.trySyncHighAndViewNo();
                            }
                            deleteStalePersistedCachedGenericMsg();
                        } else {
                            log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",Failed to get history blocks, wait 2 seconds and try again");
                        }
                    } else {
                        log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",Peer may be already updated, wait and try again");
                        if (this.timesToSync-- > 0) {
                            return;
                        }
                        this.timesToSync = 3;
                        //tryUpdateConsensusStage();
                    }
                }
            } catch (Exception ex) {//Exception here is severe, so just inform the monitor.
                log.error(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",The synchronization is abnormal, this peer can be in byzantine-fault", ex);
                // TODO:上报业务监控
            }
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

    private QueryChainStateResp queryBestChainStateFromNetwork() {
        QueryChainStateReq queryChainStateReq = createQueryChainStateReq();
        QueryChainStateResp queryChainStateResp = null;
        QueryChainStateResp bestChainStateResp = null;
        for (Peer peer :
                consensusContext.getOrderedPeerList()) {
            if (peer.getIsLocalPeer()) {
                continue;
            }
            //log.info(ModuleClassification.ConM_Sync_+"Get chain state from peer {}", peer.getPeerChannelRelation().getExtendedData());
            queryChainStateResp = this.p2pClient.queryChainState(queryChainStateReq, peer);
            if (queryChainStateResp == null) {
                log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",get chain state null from peer " + peer.getPeerId().toString());
                continue;
            }
            if (!checkChainStateFromNetwork(queryChainStateResp)) {
                log.warn(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",check chain state failed,queryChainStateResp={}",JSONObject.toJSON(queryChainStateResp));
                continue;
            }
            if (bestChainStateResp == null) {
                log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",no available best chain state");
                bestChainStateResp = queryChainStateResp;
            } else if (queryChainStateResp != null) {
                if (queryChainStateResp.betterThan(bestChainStateResp)) {
                    bestChainStateResp = queryChainStateResp;
                    break;
                }
            }
        }
        //TODO:应该将bestchainstate的节点的身份也返回。
        return bestChainStateResp;
    }

    private QueryChainStateReq createQueryChainStateReq() {
        QueryChainStateReq queryChainStateReq = new QueryChainStateReq();
        String channelId = consensusContext.getChannel().getChannelId();
        //attention: the calleridentity in queryChannleReq should be set
        // in the implementation of ServiceForPeer.
        queryChainStateReq.setChannelId(channelId);
        queryChainStateReq.setBlockHeight(consensusContext.getBlockHeight());
        SecurityService securityService = getLocalSecurityService();
        queryChainStateReq.setSignerIdentityKey(null);
        securityService.hash(queryChainStateReq);
        securityService.signByGMCertificate(queryChainStateReq, consensusContext.getChannel().getChannelId());
        return queryChainStateReq;
    }

    /**
     * the chain state from other peer may not be correct, but make sure the data
     * is useful.
     *
     * @param queryChainStateResp
     * @return
     */
    private boolean checkChainStateFromNetwork(QueryChainStateResp queryChainStateResp) {
        try {
            if (queryChainStateResp.getHeight() < consensusContext.getBlockHeight()) {
                log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",check failed 111");
                return false;
            }
            if (!queryChainStateResp.getChannelId().equals(consensusContext.getChannel().getChannelId())) {
                log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",check failed 222");
                return false;
            }
            if (!queryChainStateResp.getPrePrepare().getJustify().getHeight().equals(queryChainStateResp.getHeight() + 2)) {
                //when this happen, the height in this peer may not be true.
                //just reject it to simplify the process of synchronization.
                // special case when height = 0
                if (queryChainStateResp.getHeight() != 0 &&
                        !queryChainStateResp.getPrePrepare().getBlock().getHash().equals(queryChainStateResp.getPrepare().getBlock().getHash())) {
                    log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",check failed 333");
                    return false;
                }
            }
            if (!checkHotStuffDataNode(queryChainStateResp.getPrePrepare()) ||
                    !checkHotStuffDataNode(queryChainStateResp.getPrepare()) ||
                    !checkHotStuffDataNode(queryChainStateResp.getPreCommit())) {
                log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",check failed 444");
                return false;
            }
            if (queryChainStateResp.getPrePrepare().getBlock().getHash().equals(queryChainStateResp.getPrepare().getBlock().getHash()) == false) {
                /*if (!queryChainStateResp.getPrePrepare().getParentNodeHashStr().equals(Block.bytesToHexString(queryChainStateResp.getPrePrepare().getBlock().getBlockHeader().getPrevBlockHash()))) {
                    log.debug("the prev block hash of prePrepare block is not equal with prepared block");
                    log.info("MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",check failed 555");
                    return false;
                }*/
                if (!queryChainStateResp.getPrePrepare().getJustify().getBlockHash().equals(queryChainStateResp.getPrepare().getBlock().getHash())) {
                    if (queryChainStateResp.getHeight() != 0 &&
                            !queryChainStateResp.getPrePrepare().getBlock().getHash().equals(queryChainStateResp.getPrepare().getBlock().getHash())) {
                        log.debug("prepareQC is conflict with the prepared block");
                        log.info(consensusContext.getChannel().getChannelId()+",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",check failed 666");
                        return false;
                    }
                }
            }
            if (!queryChainStateResp.getPrepare().getJustify().getBlockHash().equals(queryChainStateResp.getPreCommit().getBlock().getHash())) {
                log.debug(ModuleClassification.ConM_Sync_ + "lockedQC is conflict with the locked block");
                log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",check failed 777");
                //for debug
                log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",preprepare = {}", JSON.toJSONString(queryChainStateResp.getPrePrepare()));
                log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",prepareQC = {}", JSON.toJSONString(queryChainStateResp.getPrepare()));
                log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",precommit= {}", JSON.toJSONString(queryChainStateResp.getPreCommit()));
                return false;
            }
            if (!queryChainStateResp.getPreCommit().getJustify().getHeight().equals(queryChainStateResp.getHeight())) {
                log.debug(ModuleClassification.ConM_Sync_ + "QC is not successive");
                log.warn(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",check failed 888,queryChainStateResp={}", JSONObject.toJSON(queryChainStateResp));
                return false;
            }
        } catch (Exception ex) {
            log.error(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",check failed, exception:" + ex.toString(), ex);
            return false;
        }
        return true;
    }

    /**
     * this function only check hotstuffDataNode without context.
     *
     * @param hotStuffDataNode
     * @return
     */
    private boolean checkHotStuffDataNode(HotStuffDataNode hotStuffDataNode) {
        try {
            if (!verifyQC(hotStuffDataNode.getJustify())) {
                log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "invalid QC in datanode" + hotStuffDataNode.toString());
                return false;
            }
            if (!hotStuffDataNode.getJustify().getBlockHash().equals(hotStuffDataNode.getBlock().getPrevBlockHash())) {
                log.debug(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",The block:{} is conflict with QC:{}", hotStuffDataNode.getBlock().getHash(), hotStuffDataNode.getJustify().getBlockHash());
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    private boolean updateConsensusContextFromNetwork(QueryChainStateResp queryChainStateResp) {
        //for debug
        Long ttt = System.currentTimeMillis();
        consensusContext.writeLock(ttt);
        if (queryChainStateResp.getHeight() < consensusContext.getBlockHeight()) {
            consensusContext.writeUnlock(ttt);
            return false;
        }
        try {
            long currentViewNo = queryChainStateResp.getPrePrepare().getJustify().getBlockViewNo() + 1;
            View currentView = View.createView(currentViewNo, Long.parseLong(consensusContext.getChannel().getExtendParam(ConsensusExtendedParamEnum.VIEW_TIMEOUT_MS.getCode())));
            consensusContext.setCurrView(currentView);
            consensusContext.setGenericQC(queryChainStateResp.getPrePrepare().getJustify());
            consensusContext.setLockedQC(queryChainStateResp.getPrepare().getJustify());
            consensusContext.setHashPrePrepareBlock(queryChainStateResp.getPrePrepare().getBlock().getHash());
            consensusContext.setHighestQC(queryChainStateResp.getHighestQC());
            consensusContext.addNode(queryChainStateResp.getPrePrepare());
            consensusContext.addNode(queryChainStateResp.getPrepare());
            consensusContext.addNode(queryChainStateResp.getPreCommit());
        } catch (Exception ex) {
            log.error(ModuleClassification.ConM_NSHP_.toString() +consensusContext.getChannel().getChannelId()+ " updateConsensusContextFromNetwork exception:", ex);
        } finally {
            consensusContext.writeUnlock(ttt);
        }

        return true;
    }


    private boolean requestHistoryBlock(QueryChainStateResp bestChainState) {
        Long currentNetworkHeight = findNetworkHeight(bestChainState);
        long currentLocalHeight = consensusContext.getBlockHeight();
        log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",Try to request QC of blocks from height {} to {}", currentLocalHeight, currentNetworkHeight);
        int cnt = 0;
        while (consensusContext.getBlockHeight() < currentNetworkHeight) {
            /*if (detectIfStalling(lastRoundLocalHeight, currentLocalHeight)) {
                stallingTimes++;
                if (stallingTimes > 20) {
                    return false;
                }
            }*/

            QueryHistoryQCResp queryHistoryQCResp = requestHistoryQC(currentNetworkHeight);
            if (queryHistoryQCResp == null) {
                return false;
            }

            if (!processHistoryQC(queryHistoryQCResp.getListQC())) {
                log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",No available QC in this round of requesting");
                continue;
            }
            processQCinChainStateIfNeeded(bestChainState);
            while (consensusContext.getBlockHeight() < committedBlockChainHeight) {
                //log.info(ModuleClassification.ConM_Sync_+"开始请求已经确认commit的区块");
                requestCommittedBlock();
                if (processCommittedBlock() == false) {
                    break;
                }
            }

            //Update the current height before asking for more data from other peer.
            if (currentLocalHeight == consensusContext.getBlockHeight()) {
                cnt++;
            } else {
                currentLocalHeight = consensusContext.getBlockHeight();
            }
            if (cnt >= 10) {
                log.info(ModuleClassification.ConM_Sync_.toString() +consensusContext.getChannel().getChannelId()+ " Synchronization is stalling, jump out and try again");
                return false;
            }
            log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",本请求历史区块数据循环结束后，本地高度为{}", currentLocalHeight);
        }
        return true;
    }

    private Long findNetworkHeight(QueryChainStateResp resp) {
        return resp.getHeight();
    }

    private boolean detectIfStalling(Long lastRoundLocalHeight, Long currentLocalHeight) {
        if (lastRoundLocalHeight.equals(currentLocalHeight)) {
            return true;
        } else {
            return false;
        }
    }

    private Peer choosePeerForBlock() {
        Peer peer = null;
        while (peer == null || peer.getIsLocalPeer()) {
            int rand = (int) (Math.random() * 100);
            peer = this.consensusContext.getChannel().getMemberPeerList().get(rand % consensusContext.getPeerCount());
            //make sure the chosen peer never left the channel.
            if (peer.getPeerChannelRelation().getOutBlockHeight() != 0) {
                peer = null;
                continue;
            }
        }
        return peer;
    }

    private QueryHistoryQCResp requestHistoryQC(Long currentNetworkHeight) {
        QueryHistoryQCReq queryHistoryQCReq = createQueryHistoryQCReq(currentNetworkHeight);
        Peer targetPeer = choosePeerForBlock();
        log.info(ModuleClassification.ConM_Sync_ + consensusContext.getChannel().getChannelId()+",Try to request QC from peer {}, from {} to {}", targetPeer.getPeerChannelRelation().getExtendedData(), queryHistoryQCReq.getFromHeight(), queryHistoryQCReq.getToHeight());
        QueryHistoryQCResp queryHistoryQCResp;

        queryHistoryQCResp = this.p2pClient.queryHistoryQC(queryHistoryQCReq, targetPeer);
        Integer trial = 2 * consensusContext.getPeerCount();
        while (queryHistoryQCResp == null || queryHistoryQCResp.getListQC().isEmpty()) {
            targetPeer = choosePeerForBlock();
            log.info(ModuleClassification.ConM_Sync_ + consensusContext.getChannel().getChannelId()+",request QC from peer {}", targetPeer.getPeerChannelRelation().getExtendedData());
            queryHistoryQCResp = this.p2pClient.queryHistoryQC(queryHistoryQCReq, targetPeer);
            trial--;
            if (trial < 0) {
                log.debug(ModuleClassification.ConM_Sync_ + consensusContext.getChannel().getChannelId()+",Failed to get history QC after {} times", 2 * consensusContext.getPeerCount());
                return null;
            }
        }
        return queryHistoryQCResp;
    }

    private QueryHistoryQCReq createQueryHistoryQCReq(Long currentNetworkHeight) {
        QueryHistoryQCReq queryHistoryQCReq = new QueryHistoryQCReq();
        queryHistoryQCReq.setChannelId(consensusContext.getChannel().getChannelId());
        Long currentLocalHeight = consensusContext.getBlockHeight();
        Long targetHeight = consensusContext.getBlockHeight() + maxQCperRound;
        targetHeight = (targetHeight < currentNetworkHeight) ? targetHeight : currentNetworkHeight;
        queryHistoryQCReq.setFromHeight(currentLocalHeight + 1);
        queryHistoryQCReq.setToHeight(targetHeight);
        getLocalSecurityService().hash(queryHistoryQCReq);
        getLocalSecurityService().signByGMCertificate(queryHistoryQCReq, consensusContext.getChannel().getChannelId());
        return queryHistoryQCReq;
    }

    private boolean processHistoryQC(List<GenericQC> qcList) {
        log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",The size of QC list is {}", qcList.size());
        if (qcList.isEmpty()) {
            return false;
        }
        //Cached genericQC for later synchronization.
        for (GenericQC qc :
                qcList) {
            //for debug
            //log.info(ModuleClassification.ConM_Sync_+"store qc of height {}, block {}", qc.getHeight(), qc.getBlockHash());
            this.mapBlockQC.put(qc.getBlockHash(), qc);
        }
        Long QCheight = qcList.get(0).getHeight();
        //the first QC should be of the next block of current local highest block
        if (!QCheight.equals(consensusContext.getBlockHeight() + 1)) {
            log.info(ModuleClassification.ConM_Sync_ + consensusContext.getChannel().getChannelId()+",The height {} of first QC is not equal to the local height", consensusContext.getBlockHeight());
            return false;
        }
        Iterator<GenericQC> iterator = qcList.iterator();
        while (iterator.hasNext()) {
            GenericQC genericQC = iterator.next();
            if (genericQC.getHeight() > QCheight) {
                log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",discontinuous QC, the missing one is of {}", QCheight.toString());
                break;
            }
            if (dataVerifier.verifyGenericQC(genericQC, consensusContext.getChannel()) == false) {
                log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",invalid history QC of height {}, block {}", genericQC.getHeight(), genericQC.getBlockHash());
                break;
            }
            processGenericQCforCommittedBlock(genericQC);
            //the height of block in QC should be consecutive.
            QCheight = genericQC.getHeight() + 1;
        }
        return true;
    }



    /**
     * 历史同步时某个消息是本通道的成员发送的并且签名正确
     *
     * @param msg
     * @return
     */
    private boolean isChannelMemberMsgWithCorrectSignWhenSyncHistoryBlock(HotStuffMsg msg) {
        //发送者是否是Channel的成员
        Peer sendPeer = new Peer();
        //List<Peer> memberList = consensusContext.getOrderedPeerList();
        List<Peer> memberList = consensusContext.getChannel().getMemberPeerList();
        boolean isChannelMember = false;
        for (Peer member : memberList) {
            if (msg.getSignerIdentityKey().equalsWithIdentityKey(member.getPeerId())) {
                sendPeer = member;
                isChannelMember = true;
            }
        }
        if (!isChannelMember) {
            log.warn(ModuleClassification.ConM_Sync_ + consensusContext.getChannel().getChannelId()+",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + ",发送者并不是本Channel的成员，忽略该消息");
            return false;
        }
        //TODO sender exist white peerCertificateList verySign true
        if (false == this.getLocalSecurityService().syncHistoryBlockVerifySignatureByGMCertificate(msg, sendPeer.getPeerCert())) {
            log.warn(ModuleClassification.ConM_Sync_ + consensusContext.getChannel().getChannelId()+",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + "," + msg.getClass().getCanonicalName() + "消息验签失败，忽略该消息");
            return false;
        }
        return true;
    }

    private void processGenericQCforCommittedBlock(GenericQC genericQC) {
        log.info(ModuleClassification.ConM_Sync_ + "," + consensusContext.getChannel().getChannelId() + ",process genericQC " + genericQC.toString());
        if (this.mapBlockState.containsKey(genericQC.getBlockHash())) {
            return;
        }
        this.mapBlockParent.put(genericQC.getBlockHash(), genericQC.getPrevBlockHash());
        log.info(ModuleClassification.ConM_Sync_ + "," + consensusContext.getChannel().getChannelId() + ",mark block height {}, hash {} as prepare", genericQC.getHeight(), genericQC.getBlockHash());
        this.mapBlockState.put(genericQC.getBlockHash(), this.prepare);
        Integer prevBlockState = mapBlockState.get(genericQC.getPrevBlockHash());
        if (prevBlockState != null) {
            if (prevBlockState.equals(this.prepare)) {
                log.info(ModuleClassification.ConM_Sync_ + "," + consensusContext.getChannel().getChannelId() + ",mark block height {}, hash {} as precommit", genericQC.getHeight() - 1, genericQC.getPrevBlockHash());
                mapBlockState.put(genericQC.getPrevBlockHash(), this.preCommit);
            }
            String grandParentBlockHash = this.mapBlockParent.get(genericQC.getPrevBlockHash());
            Integer grandParentState = this.mapBlockState.get(grandParentBlockHash);
            if (grandParentState != null) {
                if (grandParentState.equals(this.preCommit)) {
                    Long higherCommittedBlock = genericQC.getHeight() - 2;
                    log.info(ModuleClassification.ConM_Sync_ + "," + consensusContext.getChannel().getChannelId() + ",put commit block height {}, hash {}", higherCommittedBlock, grandParentBlockHash);
                    this.committedBlockChain.put(higherCommittedBlock, grandParentBlockHash);
                    this.mapBlockState.remove(grandParentBlockHash);
                    this.mapBlockParent.remove(grandParentBlockHash);
                    if (higherCommittedBlock > committedBlockChainHeight) {
                        committedBlockChainHeight = higherCommittedBlock;
                    }
                }
            }
        }
    }

    private void processQCinChainStateIfNeeded(QueryChainStateResp bestChainState) {
        if (mapBlockState.containsKey(bestChainState.getPreCommit().getJustify().getPrevBlockHash()) ||
            mapBlockState.containsKey(bestChainState.getPreCommit().getJustify().getBlockHash())) {
            log.info(ModuleClassification.ConM_Sync_.toString() + consensusContext.getChannel().getChannelId() + "processQCinChainStateIfNeeded, height " +
                    bestChainState.getHeight() + " genericQC:" +
                    bestChainState.getPrePrepare().getJustify().toString() + " lockedQC:" +
                    bestChainState.getPrepare().getJustify().toString() + " committedQC:" +
                    bestChainState.getPreCommit().getJustify().toString());
            GenericQC lockedQC = bestChainState.getPrepare().getJustify();
            GenericQC genericQC = bestChainState.getPrePrepare().getJustify();
            GenericQC highestQC = bestChainState.getHighestQC();
            GenericQC committedQC = bestChainState.getPreCommit().getJustify();
            this.mapBlockQC.put(highestQC.getBlockHash(), highestQC);
            this.mapBlockQC.put(genericQC.getBlockHash(), genericQC);
            this.mapBlockQC.put(lockedQC.getBlockHash(), lockedQC);
            this.mapBlockQC.put(committedQC.getBlockHash(), committedQC);
            processGenericQCforCommittedBlock(committedQC);
            processGenericQCforCommittedBlock(lockedQC);
            processGenericQCforCommittedBlock(genericQC);
            processGenericQCforCommittedBlock(highestQC);

            Block lockedBlock = bestChainState.getPreCommit().getBlock();
            lockedBlock.getBlockHeader().setWitness(JSON.toJSONString(lockedQC));
            ledgerMgr.insertCacheBlock(this.consensusContext.getChannel().getChannelId(), lockedBlock);

            Block preparedBlock = bestChainState.getPrepare().getBlock();
            preparedBlock.getBlockHeader().setWitness(JSON.toJSONString(genericQC));
            ledgerMgr.insertCacheBlock(this.consensusContext.getChannel().getChannelId(), preparedBlock);
        }
    }

    private boolean requestCommittedBlock() {
        //TODO: this function can be multi-thread.
        //      all the block should be put into this.mapBlock.
        Long fromHeight = consensusContext.getBlockHeight() + 1;
        QueryHistoryBlockReq queryHistoryBlockReq = createQueryHistoryBlockReq(fromHeight);
        if (null == queryHistoryBlockReq) {
            return true;
        }
        Peer targetPeer = choosePeerForBlock();
        QueryHistoryBlockResp queryHistoryBlockResp = null;
        while (queryHistoryBlockResp == null) {
            //TODO: if stuck here, what should do?
            targetPeer = choosePeerForBlock();
            //for debug
            log.info(ModuleClassification.ConM_Sync_ + "," + consensusContext.getChannel().getChannelId() + ",query history block, from hash {}, toHash {}", queryHistoryBlockReq.getFromHash(), queryHistoryBlockReq.getToHash());
            queryHistoryBlockResp = this.p2pClient.queryHistoryBlock(queryHistoryBlockReq, targetPeer);
        }
        List<Block> blockList = queryHistoryBlockResp.getListBlock();
        Long heightInOrder = fromHeight;
        Iterator<Block> iterator = blockList.iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();
            if (!block.getBlockHeader().getHeight().equals(heightInOrder) || !block.getHash().equals(committedBlockChain.get(heightInOrder))) {
                log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + ",Discontinuous block, the missing block is of height {}", heightInOrder);
                return false;
            }
            this.mapCommittedBlock.put(block.getBlockHeader().getHeight(), block);
            heightInOrder++;
        }
        return true;
    }

    private QueryHistoryBlockReq createQueryHistoryBlockReq(Long fromHeight) {
        QueryHistoryBlockReq queryHistoryBlockReq = new QueryHistoryBlockReq();
        queryHistoryBlockReq.setChannelId(this.consensusContext.getChannel().getChannelId());
        Long toHeight = (fromHeight + maxBlockPerRound - 1) > committedBlockChainHeight ? committedBlockChainHeight : (fromHeight + maxBlockPerRound - 1);
        if (toHeight < fromHeight) {
            return null;
        }
        String fromHash = committedBlockChain.get(fromHeight);
        String toHash = committedBlockChain.get(toHeight);
        Long count = toHeight - fromHeight + 1;
        queryHistoryBlockReq.setCount(count);
        queryHistoryBlockReq.setFromHash(fromHash);
        queryHistoryBlockReq.setToHash(toHash);
        queryHistoryBlockReq.setFromHeight(fromHeight);
        queryHistoryBlockReq.setToHeight(toHeight);
        getLocalSecurityService().hash(queryHistoryBlockReq);
        getLocalSecurityService().signByGMCertificate(queryHistoryBlockReq, consensusContext.getChannel().getChannelId());
        return queryHistoryBlockReq;
    }

    private boolean processCommittedBlock() {
        long height = consensusContext.getBlockHeight() + 1;
        while (mapCommittedBlock.containsKey(height)) {
            //TODO:process block
            Block block = mapCommittedBlock.get(height);
            if (dataVerifier.verifyBlock(block, consensusContext.getChannel().getSecurityServiceKey()) == false) {
                log.error(ModuleClassification.ConM_Sync_.toString() +consensusContext.getChannel().getChannelId()+","+ ModuleClassification.ConM_Verify_ +
                        " invalid history block:blockHash={},block={}" , block.getHash(),JSONObject.toJSON(block));
                return false;
            }
            if (executeBlock(block)) {
                GenericQC genericQC = this.mapBlockQC.get(block.getHash());
                if (genericQC != null && persistBlock(block, genericQC)) {
                    if (consensusContext.getBlockHeight() != height) {
                        log.error(ModuleClassification.ConM_Sync_.toString() + consensusContext.getChannel().getChannelId() +
                                " discontinuous committed block:" + block.getBlockHeader().getHash() + " height:" + block.getBlockHeader().getHeight());
                        return false;
                    }
                    height += 1;
                    this.mapBlockQC.remove(block.getHash());
                    this.mapCommittedBlock.remove(block.getBlockHeader().getHeight());
                } else {
                    log.error(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+","+ "TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + ",Succeed to execute the block but failed to persist it");
                    return false;
                }
            } else {
                log.warn(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + ",Failed to execute block");
                break;
            }

        }
        return true;
    }

    private void requestCachedBlock(QueryChainStateResp bestChainState) {
        if (bestChainState.getPrePrepare().getJustify().getBlockHash().equals(bestChainState.getPreCommit().getBlock().getHash())) {

        }
    }

    /**
     * Process all the cached genericMsg during synchronization.
     * This function is used after geting
     *
     * @param queryChainStateResp
     * @return
     */
    private boolean processCachedGenericMsg(QueryChainStateResp queryChainStateResp) {
        if (!processConsensusCachedBlock(queryChainStateResp)) {
            log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + ",Failed to process ConsensusCachedBlock");
            return false;
        }
        // log.info(ModuleClassification.ConM_Sync_+"MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",begin to process persisted cached genericMsgs");
        processPersistedCachedGenericMsg();
        Map<Long, GenericMsg> genericMsgLeftMap = consensusContext.getPersistedCachedGenericMsg(consensusContext.getCurrView().getNo());
        //log.info(ModuleClassification.ConM_Sync_+"MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",pass to process persisted cached genericMsgs, there are " + genericMsgLeftMap.size() + " unprocessed left in database");
        deleteStalePersistedCachedGenericMsg();
        Long needProcessViewNo = consensusContext.getCurrView().getNo();
        while (needProcessViewNo < consensusContext.getViewNoSync()) {
            //This condition can not assure that the peer catch up with the network.
            //The cached genericMsgs may be persisted in this moment, but the probability is much little,
            //So, just rerun this cycle instead of keeping state of whether local peer already finished
            //processing history blocks.
            Map<Long, GenericMsg> genericMsgMap = consensusContext.getCachedGenericMsgs();
            if (genericMsgMap.containsKey(needProcessViewNo)) {
                GenericMsg genericMsg = genericMsgMap.get(needProcessViewNo);
                if (genericMsg == null) {// the returned cachedGenericMsgs may be changed by other thread.
                    continue;
                }
                HotStuffDataNode hotStuffDataNode = genericMsg.getHotStuffDataNode();
                try {
                    newSpiralHotStuffProcessor.processHotStuffDataNode(hotStuffDataNode);
                    newSpiralHotStuffProcessor.updateContextAfterProcessDataNode(hotStuffDataNode);
                } catch (Exception ex) {
                    log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + ",Failed to process dataNode, view=｛｝, height=｛｝", genericMsg.getViewNo(), hotStuffDataNode.getBlock().getBlockHeader().getHeight(), ex);
                }
                genericMsgMap.remove(needProcessViewNo);
                enterNextView();
            }
            needProcessViewNo++;
        }
        /*if (consensusContext.getCurrView().getNo() < consensusContext.getViewNoSync()) {
            log.info(ModuleClassification.ConM_Sync_+"MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",Still left behind after synchronization the current viewNo is {}, the ViewNo of network is {}", consensusContext.getCurrView().getNo(), consensusContext.getViewNoSync());
            //When this happen, it means the peer haven't catch up with the network.Just redo synchronization again.
            //If the network condition is poor, peer may keep synchronization all the time.
            return false;
        }*/
        return true;
    }

    private void processPersistedCachedGenericMsg() {
        Long currentViewNo = consensusContext.getCurrView().getNo();
        Long currentViewSync = consensusContext.getViewNoSync();
        Map<Long, GenericMsg> genericMsgs = consensusContext.getPersistedCachedGenericMsg(currentViewNo);
        if (genericMsgs.isEmpty()) {
            return;
        }
        for (; currentViewNo <= currentViewSync; currentViewNo++) {
            if (genericMsgs.containsKey(currentViewNo)) {
                GenericMsg genericMsg = genericMsgs.get(currentViewNo);
                HotStuffDataNode hotStuffDataNode = genericMsg.getHotStuffDataNode();
                try {
                    newSpiralHotStuffProcessor.processHotStuffDataNode(hotStuffDataNode);
                    newSpiralHotStuffProcessor.updateContextAfterProcessDataNode(hotStuffDataNode);
                } catch (Exception ex) {
                    log.error(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + ",process node failed in processing of persisted cached genericMsg", ex);
                    log.error(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + ",Exception:" + ex.toString(), ex);
                }
                genericMsgs.remove(currentViewNo);
            }
            enterNextView();
        }
    }

    private void deleteStalePersistedCachedGenericMsg() {
        log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + ",delte stale persisted cached genericMsg after processing cached genericMsgs, view <= " + (consensusContext.getCurrView().getNo() - 1));
        ledgerMgr.deleteGenericMsg(consensusContext.getChannel().getChannelId(), consensusContext.getCurrView().getNo() - 1);
    }

    private void enterNextView() {
        //consensusContext.writeLock();
        consensusContext.getCurrView().enterNewView(Long.parseLong(consensusContext.getChannel().getExtendParam(ConsensusExtendedParamEnum.VIEW_TIMEOUT_MS.getCode())));
        consensusContext.setCurrLeader(consensusContext.calcLeader(consensusContext.getCurrView().getNo()));
        //consensusContext.writeUnlock();
    }

    /**
     * before process cached genericMsg, process the locked, prepared, pre-prepared Block
     * in chain state.
     *
     * @param queryChainStateResp
     */
    private boolean processConsensusCachedBlock(QueryChainStateResp queryChainStateResp) {
        Block lockedBlock = queryChainStateResp.getPreCommit().getBlock();
        Block preparedBlock = queryChainStateResp.getPrepare().getBlock();
        Block prePreparedBlock = queryChainStateResp.getPrePrepare().getBlock();
        try {
            tranasctionMgr.processBlockConsensusSuccess(lockedBlock);
            tranasctionMgr.processBlockConsensusSuccess(preparedBlock);
            tranasctionMgr.processBlockConsensusSuccess(prePreparedBlock);
        } catch (Exception ex) {
            log.error(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + ",Exception thrown in the processing of consensusCached block", ex);
            return false;
        }
        return true;
    }

    private boolean executeBlock(Block block) {
        try {
            tranasctionMgr.processBlockConsensusSuccess(block);
        } catch (Exception ex) {
            log.error(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + ",Failed to execute block", ex);
            return false;
        }
        return true;
    }

    private boolean persistBlock(Block block, GenericQC genericQC) {
        try {
            block.getBlockHeader().setWitness(JSON.toJSONString(genericQC));
            consensusContext.putCommittedBlock(block);
            newSpiralHotStuffProcessor.getTxExecutorConsensusCache().removeTxFromTransactionPool(block);
            //ledgerMgr.persistBlock(block);
            //newSpiralHotStuffProcessor.cleanPooledTransactionPool(block);
            return true;
        } catch (Exception ex) {
            log.error(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + ",Failed to persist committed block, the system may be in poor condition.", ex);
            return false;
        }
    }

    private boolean verifyQC(GenericQC genericQC) {
        //TODO:the verification of genericQC in history is different with that in normal case,
        // for some peers or organizations may be removed away from the channel and cause the
        // very genericQC to invalid.
        return true;
    }

    private void tryUpdateConsensusStage() {
        //consensusContext.writeLock();
        if (ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL.equals(consensusContext.consensusStageEnum)) {
            consensusContext.tryUpdateConsensusBasis(consensusContext.getChannel().getLatestChannelChangeHeight() + 3);
            if (consensusContext.calcLeader(consensusContext.getCurrView().getNo()).equals(consensusContext.myself)) {
                consensusContext.setConsensusStageEnum(ConsensusStageEnum.LEADER_WAIT_NEWVIEW);
                log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + ",Set consensus stage to LEADER_WAIT_NEWVIEW");
            } else {
                consensusContext.setConsensusStageEnum(ConsensusStageEnum.REPLICA_WAIT_BLOCK);
                log.info(ModuleClassification.ConM_Sync_ +consensusContext.getChannel().getChannelId()+ ",MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + consensusContext.getChannel().getChannelId() + ",Set consensus staget to REPLICA_WAIT_BLOCK");
            }
        }
        //consensusContext.writeUnlock();
    }
}
