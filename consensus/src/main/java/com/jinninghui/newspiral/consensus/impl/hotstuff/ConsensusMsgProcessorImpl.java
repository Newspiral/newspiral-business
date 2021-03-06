package com.jinninghui.newspiral.consensus.impl.hotstuff;

import com.alibaba.fastjson.JSON;
import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.Enum.ActionTypeEnum;
import com.jinninghui.newspiral.common.entity.Enum.PeerActionTypeEnum;
import com.jinninghui.newspiral.common.entity.QueryPeerParam;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.block.BlockResp;
import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.chain.ChannelBasicParams;
import com.jinninghui.newspiral.common.entity.chain.ChannelDynamicParams;
import com.jinninghui.newspiral.common.entity.chain.ChannelShort;
import com.jinninghui.newspiral.common.entity.chain.Peer;
import com.jinninghui.newspiral.common.entity.chain.PeerBasicParams;
import com.jinninghui.newspiral.common.entity.chain.PeerCert;
import com.jinninghui.newspiral.common.entity.chain.PeerChannelOperationRecord;
import com.jinninghui.newspiral.common.entity.chain.PeerChannelParams;
import com.jinninghui.newspiral.common.entity.chain.PeerChannelRelation;
import com.jinninghui.newspiral.common.entity.chain.PeerOrganization;
import com.jinninghui.newspiral.common.entity.chain.PeerOrganizationParams;
import com.jinninghui.newspiral.common.entity.common.base.BaseResponse;
import com.jinninghui.newspiral.common.entity.common.base.NewspiralStateCodes;
import com.jinninghui.newspiral.common.entity.common.base.ResponseUtil;
import com.jinninghui.newspiral.common.entity.common.logmodule.LogModuleCodes;
import com.jinninghui.newspiral.common.entity.consensus.BlockVoteMsg;
import com.jinninghui.newspiral.common.entity.consensus.ConsensusContextTest;
import com.jinninghui.newspiral.common.entity.consensus.ConsensusStageEnum;
import com.jinninghui.newspiral.common.entity.consensus.CurrentConsensusState;
import com.jinninghui.newspiral.common.entity.consensus.GenericMsg;
import com.jinninghui.newspiral.common.entity.consensus.GenericQC;
import com.jinninghui.newspiral.common.entity.consensus.HotStuffDataNode;
import com.jinninghui.newspiral.common.entity.consensus.HotStuffDataNodeResp;
import com.jinninghui.newspiral.common.entity.consensus.NewViewMsg;
import com.jinninghui.newspiral.common.entity.consensus.PeerConsensusStateResp;
import com.jinninghui.newspiral.common.entity.consensus.PerformanceContext;
import com.jinninghui.newspiral.common.entity.consensus.QueryViewNoResp;
import com.jinninghui.newspiral.common.entity.exception.NewSpiralErrorEnum;
import com.jinninghui.newspiral.common.entity.exception.NewspiralException;
import com.jinninghui.newspiral.common.entity.member.Member;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.common.entity.transaction.ExecutedTransaction;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import com.jinninghui.newspiral.common.entity.transaction.TransactionResp;
import com.jinninghui.newspiral.common.entity.transaction.TxStateEnum;
import com.jinninghui.newspiral.consensus.hotstuff.ConsensusMsgProcessor;
import com.jinninghui.newspiral.consensus.hotstuff.DataVerifier;
import com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor.WaitExecuteTxPool;
import com.jinninghui.newspiral.gateway.entity.QueryChannelReq;
import com.jinninghui.newspiral.ledger.mgr.LedgerMgr;
import com.jinninghui.newspiral.ledger.mgr.MemberLedgerMgr;
import com.jinninghui.newspiral.p2p.P2pClient;
import com.jinninghui.newspiral.security.SecurityService;
import com.jinninghui.newspiral.security.SecurityServiceMgr;
import com.jinninghui.newspiral.security.StartSecurityMgr;
import com.jinninghui.newspiral.transaction.mgr.TransactionMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author lida
 * @date 2019/7/23 19:45
 * ??????????????????Channel???HotStuff??????
 */
@Slf4j
@Component
public class ConsensusMsgProcessorImpl implements ConsensusMsgProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;
    /**
     * key???channelId???value?????????????????????????????????
     */
    public Map<String, NewSpiralHotStuffProcessor> newSpiralHotStuffHashMap = new ConcurrentHashMap<>();


    /**
     * key???channelId???value?????????????????????????????????
     */
    public HashMap<String, NewSpiralSyncHistoryBlockProcessor> newSpiralSyncHistoryBlockHashMap = new HashMap<>();

    public HashMap<String, TransactionExecutionProcessor> transactionExecutionProcessorHashMap = new HashMap<>();


    public Set<String> oldTransHashs = new HashSet<>();

    public Set<String> newTransHashs = new HashSet<>();

    public final static Integer cacheTransHashSize = 10000;

    @SofaReference
    private LedgerMgr ledgerMgr;
    @SofaReference
    private StartSecurityMgr startSecurityMgr;
    @SofaReference
    private P2pClient p2pClient;
    @SofaReference
    private MemberLedgerMgr memberLedgerMgr;
    @SofaReference
    private SecurityServiceMgr securityServiceMgr;
    @SofaReference
    private TransactionMgr transactionMgr;

    @Autowired
    private DataVerifier dataVerifier;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * ?????????????????????????????????????????????Spring??????????????????
     */
    void init() {
        log.info(ModuleClassification.ConM_CMPI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",???????????????");
        try {
            List<Channel> channelList = this.ledgerMgr.readAllChannels();
            log.info(ModuleClassification.ConM_CMPI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",???????????????" + channelList.size() + "???Channel");
            if (channelList != null && channelList.size() > 100) {
                log.error(ModuleClassification.ConM_CMPI_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",??????????????????" + channelList.size() + "???Channel????????????????????????");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "??????????????????" + channelList.size() + "???Channel????????????????????????");
            } else {
                for (Channel channel : channelList) {
                    if (channel.getAvailable().equals(0)) {
                        log.info(ModuleClassification.ConM_CMPI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",The channel {} is not available, ignore it", channel.getChannelId());
                        continue;
                    }
                    startConsensusForChannel(channel);
                }
                log.info(ModuleClassification.ConM_CMPI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",??????????????????????????????" + newSpiralHotStuffHashMap.size() + "???NewSpiralHotStuffProcessor??????");
            }
            //start persist thread.
            PersistBlockProcessor persistBlockProcessor = (PersistBlockProcessor) applicationContext.getBean("persistBlockProcessor");
            persistBlockProcessor.init(newSpiralHotStuffHashMap);
            Timer persistTimer = new Timer();
            persistTimer.scheduleAtFixedRate(persistBlockProcessor, 0, 100);
            //new Thread(persistBlockProcessor).start();

        } catch (Exception ex) {
            log.error(ModuleClassification.ConM_CMPI_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",?????????????????????,????????????", ex);
            System.exit(1);
        }

    }

    /**
     * ????????????????????????????????????
     *
     * @param channel
     */
    private void startConsensusForChannel(Channel channel) {
        //??????????????????????????????
        if (!startSecurityMgr.initVerifyGMCertificateValidity(channel.getChannelId())) {
            log.error(ModuleClassification.ConM_CMPI_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channel.getChannelId() + ",ConsensusMsgProcessorImpl.startConsensusForChannel,no authority do action!");
            return;
        }
        // TODO: ?????????????????????????????????????????????????????????????????????????????????
        //      *   ????????????????????????????????????????????????????????????????????????????????????????????????????????????notify??????

        // ???????????????????????????????????????????????????????????????????????????
        ConsensusContext consensusContext = new ConsensusContext();
        //newSpiralHotStuffProcessor?????????scope=prototype???Bean?????????????????????Spring???????????????
        NewSpiralHotStuffProcessor hotStuffProcessor = (NewSpiralHotStuffProcessor)
                applicationContext.getBean("newSpiralHotStuffProcessor");
        hotStuffProcessor.init(channel, consensusContext);
        this.newSpiralHotStuffHashMap.put(channel.getChannelId(), hotStuffProcessor);
        log.info(ModuleClassification.ConM_CMPI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",???Channel:" + channel.getChannelId() + "???????????????NewSpiralHotStuffProcessor??????:" + hotStuffProcessor);

        NewSpiralSyncHistoryBlockProcessor syncHistoryBlockProcessor = (NewSpiralSyncHistoryBlockProcessor)
                applicationContext.getBean("newSpiralSyncHistoryBlockProcessor");
        syncHistoryBlockProcessor.init(consensusContext, hotStuffProcessor);
        this.newSpiralSyncHistoryBlockHashMap.put(channel.getChannelId(), syncHistoryBlockProcessor);
        log.info(ModuleClassification.ConM_CMPI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",???Channel:" + channel.getChannelId() + "???????????????NewSpiralSyncHistoryBlockProcessor??????:" + syncHistoryBlockProcessor);

        TransactionExecutionProcessor transactionExecutionProcessor = (TransactionExecutionProcessor)
                applicationContext.getBean("transactionExecutionProcessor");
        transactionExecutionProcessor.init(hotStuffProcessor.getTxExecutorConsensusCache(), consensusContext);
        this.transactionExecutionProcessorHashMap.put(channel.getChannelId(), transactionExecutionProcessor);
        //????????????????????????????????????????????????
        //?????????leader????????????????????? ???????????????
        Timer syncTimer = new Timer();
        syncTimer.scheduleAtFixedRate(syncHistoryBlockProcessor, 0, 1000);
        Timer hotStuffTimer = new Timer();
        hotStuffTimer.scheduleAtFixedRate(hotStuffProcessor, 0, 200);
        Timer transExecTimer = new Timer();
        transExecTimer.scheduleAtFixedRate(transactionExecutionProcessor, 0, 1000);
        //new Thread(syncHistoryBlockProcessor).start();
        //???????????????????????????????????????
        //hotStuffProcessor.consensusContext.setConsensusStageEnum(ConsensusStageEnum.LEADER_WAIT_NEWVIEW);
        /*new Thread(hotStuffProcessor).start();
        Thread threadTransaction = new Thread(transactionExecutionProcessor);
        threadTransaction.start();*/

    }


    @Override
    synchronized public void processLocalPeerAdd2Channel(Channel channel) {
        //?????????????????????????????????????????????????????????
        if (newSpiralHotStuffHashMap.get(channel.getChannelId()) != null) {
            log.warn(ModuleClassification.ConM_CMPI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channel.getChannelId() + ",???????????????????????????????????????????????????????????????????????????processLocalPeerAdd2Channel??????");
        } else {
            this.startConsensusForChannel(channel);
        }
    }

    @Override
    public void processChannelUpdate(Channel channel) {
        try {
            NewSpiralHotStuffProcessor processor = newSpiralHotStuffHashMap.get(channel.getChannelId());
            if (processor == null) {
                if (channel.getAvailable() == 1) {
                    log.warn(ModuleClassification.ConM_CMPI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channel.getChannelId() + ",????????????????????????????????????????????????????????????????????????Channel????????????");
                    this.startConsensusForChannel(channel);
                }
            } else {
                processor.processChannelUpdate(channel);
            }
        } catch (Exception ex) {
            log.error(ModuleClassification.ConM_CMPI_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + ",NewSpiralHotStuffProcessor??????Channel?????????????????????:", ex);
            throw ex;
        }
    }

    @Override
    public Object processQueryTransaction(SDKTransaction sdkTransaction) {
        NewSpiralHotStuffProcessor processor = newSpiralHotStuffHashMap.get(sdkTransaction.getChannelId());
        if (processor == null) {
            log.warn(ModuleClassification.ConM_CMPI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + sdkTransaction.getChannelId() + ",????????????????????????????????????????????????????????????????????????Channel????????????");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "???????????????????????????????????????");
        } else {
            return processor.processQueryTransaction(sdkTransaction);
        }
    }


    @Override
    public void addGenericMsg(GenericMsg genericMsg) {
        try {
            String channelId = genericMsg.getChannelId();
            //for debug
            /*for (ExecutedTransaction tx : genericMsg.getHotStuffDataNode().getBlock().getTransactionMap()) {
                log.info("receiving genericMsg, tx sucess {}", tx.getPass());
            }*/
            NewSpiralHotStuffProcessor matchNewSpiralHostStuff = newSpiralHotStuffHashMap.get(channelId);
            if (matchNewSpiralHostStuff != null) {
                //TODO:??????????????????????????????
                if (matchNewSpiralHostStuff.consensusContext.getConsensusStageEnum().equals(ConsensusStageEnum.LEAVE_CHANNEL)) {
                    newSpiralHotStuffHashMap.remove(channelId);
                    return;
                }
                log.info(ModuleClassification.ConM_CMPI_.toString() + "," + channelId + ",accept genericMsg:" + genericMsg.toString());
                matchNewSpiralHostStuff.acceptGenericMsg(genericMsg);
                //for debug
                /*for (ExecutedTransaction tx : genericMsg.getHotStuffDataNode().getBlock().getTransactionMap()) {
                    log.info("after acceptGenericMsg, tx sucess {}", tx.getPass());
                }*/
            } else {
                log.warn(ModuleClassification.ConM_CMPI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",?????????HotStuff?????????GenericMsg???????????????channelId??????????????????NewSpiralHotStuffProcessor?????????????????????????????????????????????" +
                        "?????????????????????channel????????????channelId:" + channelId);
            }
        } catch (Exception ex) {//??????????????????????????????????????????????????????????????????????????????????????????????????????
            log.error(ModuleClassification.ConM_CMPI_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + genericMsg.getChannelId() + ",NewSpiralHotStuffProcessor??????GenericMsg??????????????????????????????????????????????????????:", ex);
        }
    }

    @Override
    public void addNewViewMsg(NewViewMsg newViewMsg, String channelId) {
        try {
            NewSpiralHotStuffProcessor matchNewSpiralHostStuff = newSpiralHotStuffHashMap.get(channelId);
            if (matchNewSpiralHostStuff != null) {
                //TODO:??????????????????????????????
                if (matchNewSpiralHostStuff.consensusContext.getConsensusStageEnum().equals(ConsensusStageEnum.LEAVE_CHANNEL)) {
                    newSpiralHotStuffHashMap.remove(channelId);
                    return;
                }
                matchNewSpiralHostStuff.acceptNewViewMsg(newViewMsg);
            } else {
                log.warn(ModuleClassification.ConM_CMPI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",?????????HotStuff?????????NewViewMsg???????????????channelId??????????????????NewSpiralHotStuffProcessor???????????????????????????" +
                        "?????????????????????channel????????????channelId:" + channelId);
            }
        } catch (Exception ex) {//??????????????????????????????????????????????????????????????????????????????????????????????????????
            log.error(ModuleClassification.ConM_CMPI_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channelId + ",NewSpiralHotStuffProcessor??????NewViewMsg??????????????????????????????????????????????????????:", ex);
        }

    }


    @Override
    public void addNewTransaction(String channelId, String transHash) {
        try {
            NewSpiralHotStuffProcessor matchNewSpiralHostStuff = newSpiralHotStuffHashMap.get(channelId);
            if (matchNewSpiralHostStuff != null) {
                //TODO:??????????????????????????????
                if (matchNewSpiralHostStuff.consensusContext.getConsensusStageEnum().equals(ConsensusStageEnum.LEAVE_CHANNEL)) {
                    newSpiralHotStuffHashMap.remove(channelId);
                    return;
                }
                //for debug
                log.debug(ModuleClassification.ConM_CMPI_ + "add transaciton {} into channel {}", transHash, channelId);
                //TODO:????????????????????????????????????????????????
                matchNewSpiralHostStuff.acceptNewTransaction(transHash);
            } else {
                log.warn(ModuleClassification.ConM_CMPI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",?????????HotStuff?????????NewTransaction???????????????channelId??????????????????NewSpiralHotStuffProcessor???????????????????????????" +
                        "?????????????????????channel????????????channelId:" + channelId);
            }
        } catch (Exception ex) {//??????????????????????????????????????????????????????????????????????????????????????????????????????
            log.error(ModuleClassification.ConM_CMPI_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channelId + ",NewSpiralHotStuffProcessor??????NewTransaction??????????????????????????????????????????????????????:", ex);
        }

    }

    @Override
    public void addBlockVoteMsg(BlockVoteMsg voteMsg) {
        try {
            String channelId = voteMsg.getChannelId();
            NewSpiralHotStuffProcessor matchNewSpiralHostStuff = newSpiralHotStuffHashMap.get(channelId);
            if (matchNewSpiralHostStuff != null) {
                //TODO:??????????????????????????????
                if (matchNewSpiralHostStuff.consensusContext.getConsensusStageEnum().equals(ConsensusStageEnum.LEAVE_CHANNEL)) {
                    newSpiralHotStuffHashMap.remove(channelId);
                    return;
                }
                matchNewSpiralHostStuff.acceptBlockVoteMsg(voteMsg, matchNewSpiralHostStuff.consensusContext.getGenericQC().getHeight() - 2);
            } else {
                log.warn(ModuleClassification.ConM_CMPI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",?????????HotStuff?????????BlockVoteMsg???????????????channelId??????????????????NewSpiralHotStuffProcessor???????????????????????????" +
                        "?????????????????????channel????????????channelId:" + channelId);
            }
        } catch (Exception ex) {//??????????????????????????????????????????????????????????????????????????????????????????????????????
            log.error(ModuleClassification.ConM_CMPI_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + voteMsg.getChannelId() + ",NewSpiralHotStuffProcessor??????BlockVoteMsg??????????????????????????????????????????????????????:", ex);
        }

    }


    @Override
    public CurrentConsensusState queryChainState(String channelId, Long blockHeightOfRequester) {
        CurrentConsensusState currentConsensusState = new CurrentConsensusState();
        currentConsensusState.setChannelId(channelId);
        NewSpiralHotStuffProcessor processor = newSpiralHotStuffHashMap.get(channelId);
        if (processor == null) {
            log.warn(ModuleClassification.ConM_CMPI_ + "," + channelId + ",????????????????????????????????????????????????????????????????????????Channel????????????");
            return null;
        } else {
            ConsensusContext consensusContext = processor.consensusContext;
            //for debug
            Long ttt = System.currentTimeMillis();
            consensusContext.readLock(ttt);
            try {
                if (consensusContext.getConsensusStageEnum().equals(ConsensusStageEnum.LEAVE_CHANNEL)) {
                    newSpiralHotStuffHashMap.remove(channelId);
                    log.info(ModuleClassification.ConM_CMPI_ + "peer is going to leave the channel {}", channelId);
                    consensusContext.readUnlock(ttt);
                    return null;
                }
                //log.info("set current view to {}", consensusContext.getCurrView().getNo());
                currentConsensusState.setCurrentView(consensusContext.getCurrView().getNo());
                //currentConsensusState.setHeight(consensusContext.getBlockHeight());
            /*if (consensusContext.getBlockHeight() < blockHeightOfRequester) {
                //if the blockHeight of requester is higher than local peer, only
                // return viewNo and blockHeight.
                consensusContext.readUnlock();
                return currentConsensusState;
            }*/
                HotStuffDataNode hotStuffDataNode;
                hotStuffDataNode = consensusContext.findNodeByBlockHash(consensusContext.getHashPrePrepareBlock());
                if (hotStuffDataNode != null) {
                    currentConsensusState.setPrePrepare(hotStuffDataNode);
                }
                hotStuffDataNode = consensusContext.findNodeByBlockHash(consensusContext.getGenericQC().getBlockHash());
                if (hotStuffDataNode != null) {
                    currentConsensusState.setPrepare(hotStuffDataNode);
                }
                GenericQC lockedQC = consensusContext.getLockedQC();
                currentConsensusState.setHeight(lockedQC.getHeight() - 1);
                hotStuffDataNode = consensusContext.findNodeByBlockHash(consensusContext.getLockedQC().getBlockHash());
                if (hotStuffDataNode != null) {
                    currentConsensusState.setPreCommit(hotStuffDataNode);
                }
                currentConsensusState.setHighestQC(consensusContext.getHighestQC());
                currentConsensusState.setOrderedPeerList(consensusContext.getOrderedPeerList());
                currentConsensusState.setConsensusStageEnum(consensusContext.getConsensusStageEnum().getCode());
            } catch (Exception ex) {
                log.error(ModuleClassification.ConM_CMPI_.toString() + "," + channelId + " queryChainState exception:", ex);
            } finally {
                consensusContext.readUnlock(ttt);
            }
        }
        return currentConsensusState;
    }

    @Override
    public QueryViewNoResp queryViewNo(String channelId, Long blockHeightOfRequester) {
        QueryViewNoResp queryViewNoResp = new QueryViewNoResp();
        NewSpiralHotStuffProcessor processor = newSpiralHotStuffHashMap.get(channelId);
        if (processor == null) {
            log.warn(ModuleClassification.ConM_CMPI_ + "," + channelId + ",????????????????????????????????????????????????????????????????????????Channel????????????");
            return null;
        } else {
            ConsensusContext consensusContext = processor.consensusContext;
            //for debug
            Long ttt = System.currentTimeMillis();
            //consensusContext.readLock(ttt);
            try {
                if (consensusContext.getConsensusStageEnum().equals(ConsensusStageEnum.LEAVE_CHANNEL)) {
                    newSpiralHotStuffHashMap.remove(channelId);
                    log.info(ModuleClassification.ConM_CMPI_ + "peer is going to leave the channel {}", channelId);
                    //consensusContext.readUnlock(ttt);
                    return null;
                }
                queryViewNoResp.setViewNo(consensusContext.getCurrView().getNo());
                queryViewNoResp.setHeight(consensusContext.getBlockHeight());
            } catch (Exception ex) {
                log.error(ModuleClassification.ConM_CMPI_.toString() + "," + channelId + " queryViewNo exception:", ex);
            } finally {
                //consensusContext.readUnlock(ttt);
            }
        }
        return queryViewNoResp;
    }

    @Override
    public CurrentConsensusState queryChainState(QueryPeerParam queryPeerReq) {
        NewSpiralHotStuffProcessor processor = newSpiralHotStuffHashMap.get(queryPeerReq.getChannelId());
        CurrentConsensusState currentConsensusState = new CurrentConsensusState();
        if (processor == null) {
            return null;
        }
        ConsensusContext consensusContext = processor.consensusContext;
        Long ttt = System.currentTimeMillis();
        consensusContext.readLock(ttt);
        try {
            QueryChannelReq channelReq = createQueryChannelReqEntity(consensusContext);
            for (Peer peer : queryPeerReq.getPeerList()) {
                currentConsensusState = p2pClient.queryChainState(channelReq, peer);
            }
        } catch (Exception ex) {
            log.error(ModuleClassification.ConM_CMPI_.toString() + "," + " queryChainState exception:", ex);
        } finally {
            consensusContext.readUnlock(ttt);
        }
        return currentConsensusState;
    }

    private QueryChannelReq createQueryChannelReqEntity(ConsensusContext consensusContext) {
        QueryChannelReq channelReq = new QueryChannelReq();
        channelReq.setChannelId(consensusContext.getChannel().getChannelId());
        SecurityService securityService = securityServiceMgr.getMatchSecurityService(consensusContext.getChannel().getSecurityServiceKey());
        securityService.hash(channelReq);
        securityService.signByGMCertificate(channelReq, consensusContext.getChannel().getChannelId());
        return channelReq;
    }

    /**
     * ????????????
     *
     * @param channelId
     * @return
     */
    @Override
    public ConsensusContextTest queryConsensusContextTest(String channelId) {
        ConsensusContextTest consensusContextTest = new ConsensusContextTest();
        try {
            NewSpiralHotStuffProcessor processor = newSpiralHotStuffHashMap.get(channelId);
            if (processor == null) {
                //TODO:??????????????????????????????
                if (processor.consensusContext.getConsensusStageEnum().equals(ConsensusStageEnum.LEAVE_CHANNEL)) {
                    newSpiralHotStuffHashMap.remove(channelId);
                    return null;
                }
                log.warn(ModuleClassification.ConM_CMPI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channelId + ",NewSpiralHotStuffProcessor??????queryConsensusContextTest????????????????????????????????????????????????????????????????????????Channel????????????");
                return null;
            } else {
                ConsensusContext consensusContext = processor.consensusContext;
                Long ttt = System.currentTimeMillis();
                consensusContext.readLock(ttt);
                try {
                    //BeanUtils.copyProperties(consensusContext,consensusContextTest);
                    consensusContextTest.setBlockHeight(consensusContext.getBlockHeight());
                    consensusContextTest.setBlockVoteMap(consensusContext.getBlockVoteMap());
                    consensusContextTest.setQcMinNodeCnt(consensusContext.getQcMinNodeCnt());
                    consensusContextTest.setGenericMsgMap(consensusContext.getGenericMsgMap());
                    consensusContextTest.setGenericMsgMapBackup(consensusContext.getGenericMsgMapBackup());
                    consensusContextTest.setNewViewMsgMap(consensusContext.getNewViewMsgMap());
                    consensusContextTest.setCurrView(consensusContext.getCurrView());
                    consensusContextTest.setConsensusStageEnum(consensusContext.getConsensusStageEnum());
                    consensusContextTest.setGenericMsgOfCurrView(consensusContext.getGenericMsgOfCurrView());
                    consensusContextTest.setOrderedPeerList(consensusContext.getOrderedPeerList());
                    consensusContextTest.setGenericQC(consensusContext.getGenericQC());
                    consensusContextTest.setLockedQC(consensusContext.getLockedQC());
                    consensusContextTest.setHashPrePrepareBlock(consensusContext.getHashPrePrepareBlock());
                    consensusContextTest.setWaitExecutedPoolSize(processor.getTxExecutorConsensusCache().getWaitExecuteTxPool().size());
                } catch (Exception ex) {
                    log.error(ModuleClassification.ConM_CMPI_.toString() + "," + channelId + ",queryConsensusContextTest exception:", ex);
                } finally {
                    consensusContext.readUnlock(ttt);
                }
                return consensusContextTest;
            }
        } catch (Exception e) {
            log.error(ModuleClassification.ConM_CMPI_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channelId + ",NewSpiralHotStuffProcessor??????queryConsensusContextTest?????????ConsensusContextTest:" + consensusContextTest, e);
            log.error(ModuleClassification.ConM_CMPI_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channelId + ",NewSpiralHotStuffProcessor??????queryConsensusContextTest?????????????????????:", e);
            return null;
        }
    }

    @Override
    public void processPeerCertificateByPeerId(List<PeerCert> peerCerts, String channelId) {
        if (CollectionUtils.isEmpty(peerCerts)) {
            return;
        }
        NewSpiralHotStuffProcessor processor = newSpiralHotStuffHashMap.get(channelId);
        if (processor == null) {
            log.warn(ModuleClassification.ConM_CMPI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channelId + ",????????????????????????????????????????????????");
        } else {
            processor.processPeerCertificateByPeerId(peerCerts);
        }
    }

    @Override
    public ConsensusStageEnum queryConsensusStage(String channelId) {
        try {
            NewSpiralHotStuffProcessor processor = newSpiralHotStuffHashMap.get(channelId);
            if (processor == null) {
                log.warn(ModuleClassification.ConM_CMPI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channelId + ",ConsensusMsgProcessorImpl.queryConsensusStage,NewSpiralHotStuffProcessor??????queryConsensusStage????????????????????????????????????????????????????????????????????????Channel????????????");
                return null;
            } else {
                return processor.consensusContext.getConsensusStageEnum();
            }
        } catch (Exception e) {
            log.warn(ModuleClassification.ConM_CMPI_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channelId + ",ConsensusMsgProcessorImpl.queryConsensusStage,NewSpiralHotStuffProcessor??????queryConsensusStage????????????????????????????????????????????????????????????????????????Channel????????????,e={}", e.getMessage(), e);
            return null;
        }

    }

    @Override
    public void removeAllChannel() {
        for (String channelId : newSpiralHotStuffHashMap.keySet()) {
            newSpiralSyncHistoryBlockHashMap.get(channelId).flag = false;
            newSpiralHotStuffHashMap.get(channelId).flag = false;
            newSpiralSyncHistoryBlockHashMap.remove(channelId);
            newSpiralHotStuffHashMap.remove(channelId);
        }
    }

    /**
     * ??????channeld??????????????????id??????????????????????????????
     *
     * @param callerChannelId
     * @param clienTxId
     * @return
     */
    public TransactionResp getTxInConsensusBlockByClientId(String callerChannelId, String clienTxId) {
        //????????????Id?????????????????????
        NewSpiralHotStuffProcessor processor = newSpiralHotStuffHashMap.get(callerChannelId);
        if (null == processor) {
            return null;
        }

        TransactionResp transactionResp = new TransactionResp();
        ConsensusContext consensusContext = processor.consensusContext;
        //for debug
        Long ttt = System.currentTimeMillis();
        consensusContext.readLock(ttt);
        try {
            //???????????????????????????????????????????????????
            HotStuffDataNode prePrapareNode = consensusContext.localDataNodeMap.get(consensusContext.getGenericQC().getBlockHash());
            HotStuffDataNode prapareNode = consensusContext.localDataNodeMap.get(consensusContext.getLockedQC().getBlockHash());
            HotStuffDataNode preCommitNode = consensusContext.localDataNodeMap.get(consensusContext.getHighestQC());
            //???????????????????????????null????????????????????????block????????????????????????????????????list???
            List<ExecutedTransaction> list = new ArrayList<>();
            if (prePrapareNode != null) {
                list.addAll(prePrapareNode.getBlock().getTransactionList());
            }
            if (prapareNode != null) {
                list.addAll(prapareNode.getBlock().getTransactionList());
            }
            if (preCommitNode != null) {
                list.addAll(preCommitNode.getBlock().getTransactionList());
            }
            //??????????????????????????????
            list = list.stream().filter(Objects::nonNull).filter(ExecutedTransaction -> ExecutedTransaction.getSdkTransaction().getClientTxId().equals(clienTxId)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(list)) {
                transactionResp = null;
            } else {
                //??????????????????????????????
                ExecutedTransaction collectTx = list.get(0);
                //???????????????????????????????????????????????????
                //????????????????????????????????????transaction??????
                transactionResp.setPooledTransaction(collectTx.getSdkTransaction());
                transactionResp.setTransactionState(TxStateEnum.TX_HAVING_CONSENSUS);
                Channel channel = ledgerMgr.getChannel(callerChannelId);
                transactionResp.setChannelId(channel.getChannelId());
                transactionResp.setChannelName(channel.getName());
            }
        } catch (Exception e) {
            log.info(ModuleClassification.ConM_CMPI_.toString() + "," + callerChannelId + " getTxInConsensusBlockByClientId exception:", e);
            transactionResp = null;
        } finally {
            consensusContext.readUnlock(ttt);
            return transactionResp;
        }
    }

    /**
     * ??????channeld?????????hash??????????????????????????????
     *
     * @param callerChannelId
     * @param transHash
     * @return
     */
    public TransactionResp getTxInConsensusBlockByTransHash(String callerChannelId, String transHash) {
        //????????????Id?????????????????????
        NewSpiralHotStuffProcessor processor = newSpiralHotStuffHashMap.get(callerChannelId);
        if (null == processor) {
            return null;
        }
        TransactionResp transactionResp = new TransactionResp();
        ConsensusContext consensusContext = processor.consensusContext;
        //for debug
        Long ttt = System.currentTimeMillis();
        consensusContext.readLock(ttt);
        try {
            //???????????????????????????????????????????????????
            HotStuffDataNode prePrapareNode = consensusContext.localDataNodeMap.get(consensusContext.getGenericQC().getBlockHash());
            HotStuffDataNode prapareNode = consensusContext.localDataNodeMap.get(consensusContext.getLockedQC().getBlockHash());
            HotStuffDataNode preCommitNode = consensusContext.localDataNodeMap.get(consensusContext.getHighestQC());
            //???????????????????????????null????????????????????????block????????????????????????????????????list???
            List<ExecutedTransaction> list = new ArrayList<>();
            if (prePrapareNode != null) {
                list.addAll(prePrapareNode.getBlock().getTransactionList());
            }
            if (prapareNode != null) {
                list.addAll(prapareNode.getBlock().getTransactionList());
            }
            if (preCommitNode != null) {
                list.addAll(preCommitNode.getBlock().getTransactionList());
            }
            list = list.stream().filter(Objects::nonNull).filter(ExecutedTransaction -> ExecutedTransaction.getSDKTransactionHash().equals(transHash)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(list)) {
                transactionResp = null;
            } else {
                //??????????????????????????????
                ExecutedTransaction collectTx = list.get(0);
                //???????????????????????????????????????????????????
                //????????????????????????????????????transaction??????
                transactionResp.setPooledTransaction(collectTx.getSdkTransaction());
                transactionResp.setTransactionState(TxStateEnum.TX_HAVING_CONSENSUS);
                Channel channel = ledgerMgr.getChannel(callerChannelId);
                transactionResp.setChannelId(channel.getChannelId());
                transactionResp.setChannelName(channel.getName());
            }
        } catch (Exception e) {
            log.error(ModuleClassification.ConM_CMPI_.toString() + "," + callerChannelId + " getTxInConsensusBlockByTransHash exception:", e);
            transactionResp = null;
        } finally {
            consensusContext.readUnlock(ttt);
            return transactionResp;
        }
    }


    /**
     * ?????????????????????????????????????????????
     *
     * @param callerChannelId
     * @return
     * @data 2020/10/26
     * @author whj
     */
    public PeerConsensusStateResp getPeerConsensusState(String callerChannelId) {
        PeerConsensusStateResp peerConsensusStateResp = new PeerConsensusStateResp();
        boolean flag = false;
        for (String channelId : newSpiralHotStuffHashMap.keySet()) {
            if (channelId.equals(callerChannelId)) {
                flag = true;
                break;
            }
        }
        if (!flag) {
            log.warn(ModuleClassification.ConM_CMPI_ + "," + callerChannelId + "TError");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM);
        }
        NewSpiralHotStuffProcessor processor = newSpiralHotStuffHashMap.get(callerChannelId);
        ConsensusContext consensusContext = processor.consensusContext;
        //for debug
        Long ttt = System.currentTimeMillis();
        consensusContext.readLock(ttt);
        try {
            peerConsensusStateResp.setChannelId(callerChannelId);
            peerConsensusStateResp.setBlockHeight(consensusContext.blockHeight);
            peerConsensusStateResp.setConsensusStage(consensusContext.getConsensusStageEnum());
            peerConsensusStateResp.setPeerId(ledgerMgr.queryLocalIdentity().getKey().getValue());
            //?????????????????????
            PeerChannelParams peerChannelParams = new PeerChannelParams();
            getChannelDynamicParamsAndPeerBasicParams(consensusContext, peerChannelParams);
            peerConsensusStateResp.setPeerBasicParams(peerChannelParams.getPeerBasicParams());
            peerConsensusStateResp.setViewNo(consensusContext.getCurrView().getNo());
            //prePrepareNode
            HotStuffDataNode prePrepareNode = consensusContext.localDataNodeMap.get(consensusContext.getGenericQC().getBlockHash());
            peerConsensusStateResp.setPrePrepareNode(hotstuffNodeTransfer(prePrepareNode));
            //prePareNode
            HotStuffDataNode prePareNode = consensusContext.localDataNodeMap.get(consensusContext.getLockedQC().getBlockHash());
            peerConsensusStateResp.setPrePareNode(hotstuffNodeTransfer(prePareNode));
            //preCommitNode
            HotStuffDataNode preCommitNode = consensusContext.localDataNodeMap.get(consensusContext.getHighestQC().getBlockHash());
            peerConsensusStateResp.setPreCommitNode(hotstuffNodeTransfer(preCommitNode));
        } catch (Exception ex) {
            log.error(ModuleClassification.ConM_CMPI_.toString() + "," + callerChannelId + " getPeerConsensusState exception:", ex);
        } finally {
            consensusContext.readUnlock(ttt);
        }
        return peerConsensusStateResp;
    }

    /**
     * hotstuffNode????????????
     */
    private HotStuffDataNodeResp hotstuffNodeTransfer(HotStuffDataNode hotStuffDataNode) {
        HotStuffDataNodeResp hotStuffDataNodeResp = new HotStuffDataNodeResp();
        hotStuffDataNodeResp.setJustify(hotStuffDataNode.getJustify());
        hotStuffDataNodeResp.setParentNodeHashStr(hotStuffDataNode.getParentNodeHashStr());
        hotStuffDataNodeResp.setBlockResp(blockTransfer(hotStuffDataNode.getBlock()));
        return hotStuffDataNodeResp;
    }

    /**
     * ??????????????????
     *
     * @param block
     * @return
     */
    private BlockResp blockTransfer(Block block) {
        BlockResp blockResp = new BlockResp();
        blockResp.setVersion(block.getBlockHeader().getVersion());
        blockResp.setHeight(block.getBlockHeader().getHeight());
        blockResp.setPrevBlockHash(block.getPrevBlockHash());
        blockResp.setHash(block.getHash());
        blockResp.setChannelId(block.getBlockHeader().getChannelId());
        blockResp.setBlockSize(JSON.toJSONString(block).length());
        blockResp.setTransactionNum(block.getTransactionList().size());
        blockResp.setBuilderPeerId(block.getBlockHeader().getPackagerAndSign().getIdentityKey().getValue());
        //??????????????????????????????hash??????
        blockResp.setTransactionHashList(block.getSDKTransactions().stream().map(tran -> tran.getHash()).collect(Collectors.toList()));
        blockResp.setTimestamp(block.getBlockHeader().getTimestamp());
        blockResp.setConsensusTime(null);
        blockResp.setPersistenceTime(null);
        return blockResp;
    }

    @Override
    public List<ChannelShort> getChannelShortList() {
        List<ChannelShort> channelShorts = new ArrayList<>();
        for (String channelId : newSpiralHotStuffHashMap.keySet()) {
            ChannelShort channelShort = new ChannelShort();
            channelShort.setChannelId(channelId);
            NewSpiralHotStuffProcessor processor = newSpiralHotStuffHashMap.get(channelId);
            ConsensusContext consensusContext = processor.consensusContext;
            channelShort.setHeight(consensusContext.blockHeight);
            Long transactionSize = consensusContext.channel.getBlockMaxSize();
            channelShort.setTransactionSize(transactionSize);
            channelShorts.add(channelShort);
        }
        return channelShorts;
    }

    @Override
    public PeerChannelParams getPeerAddChannel(String channelId) {
        PeerChannelParams peerChannelParams = new PeerChannelParams();
        peerChannelParams.setChannelId(channelId);
        try {
            NewSpiralHotStuffProcessor processor = newSpiralHotStuffHashMap.get(channelId);
            if (processor == null) {
                return peerChannelParams;
            } else {
                Long ttt = System.currentTimeMillis();
                ConsensusContext consensusContext = processor.consensusContext;
                try {
                    consensusContext.readLock(ttt);
                    ChannelBasicParams channelBasicParams = new ChannelBasicParams();
                    //consensusContext = consensusContext.clone();
                    BeanUtils.copyProperties(consensusContext.getChannel(), channelBasicParams);
                    peerChannelParams.setChannelBasicParams(channelBasicParams);
                    //TODO ??????????????????
                    Member member = memberLedgerMgr.queryOrganziationMember(channelId, consensusContext.getChannel().getOrganizationId());
                    if (null != member) {
                        peerChannelParams.setPublicCertFile(member.getCertificateCerFile());
                    }
                    getChannelDynamicParamsAndPeerBasicParams(consensusContext, peerChannelParams);
                }
                catch (Exception e)
                {
                    return peerChannelParams;
                }
                finally {
                    consensusContext.readUnlock(ttt);
                    return peerChannelParams;
                }
            }
        } catch (Exception e) {
            return peerChannelParams;
        }
    }

    @Override
    public List<PeerChannelParams> getPeerAddChannelList() {
        List<PeerChannelParams> peerChannelParams = new ArrayList<>();
        for (String channelId : newSpiralHotStuffHashMap.keySet()) {
            PeerChannelParams params = getPeerAddChannel(channelId);
            peerChannelParams.add(params);
        }
        return peerChannelParams;
    }

    private void getChannelDynamicParamsAndPeerBasicParams(ConsensusContext consensusContext, PeerChannelParams peerChannelParams) {
        ChannelDynamicParams channelDynamicParams = new ChannelDynamicParams();
        //channelDynamicParams.setHeight(consensusContext.getBlockHeight());
        String prevBlockHash = consensusContext.getLockedQC().getPrevBlockHash();
        channelDynamicParams.setBlockHash(prevBlockHash);
        Block block = consensusContext.getBlock(prevBlockHash);
        channelDynamicParams.setHeight(block.getBlockHeader().getHeight());
        channelDynamicParams.setLatestTime(block.getBlockHeader().getTimestamp());
        long normalPeerCount = consensusContext.getOrderedPeerList().parallelStream().filter(Peer::isState).count();
        channelDynamicParams.setNormalPeerCount((int) (normalPeerCount));
        channelDynamicParams.setTransanctionCount(block.getTransactionList().size());

        //todo ??????????????????
        String[] cachedBlockHash = new String[]{consensusContext.getLockedQC().getBlockHash()
                , consensusContext.getGenericQC().getBlockHash(), consensusContext.getHashPrePrepareBlock()};
        Map<String, Block> cachedBlockMap = consensusContext.getCachedBlockMap();
        ArrayList<ExecutedTransaction> toConsensusList = new ArrayList<>();
        for (String hash : cachedBlockHash) {
            Block cachedBlock = cachedBlockMap.get(hash);
            if (cachedBlock != null) {
                toConsensusList.addAll(cachedBlock.getTransactionList());
            }
        }
        //??????????????????
        long consensusTxNum = toConsensusList.size();
        long consensusSize = toConsensusList.stream().mapToLong(tx -> JSON.toJSONString(tx).length()).sum();
        channelDynamicParams.setToConsensusTransNum(consensusTxNum);
        channelDynamicParams.setToConsensusTransSize(consensusSize);

        //?????????
        NewSpiralHotStuffProcessor processor = newSpiralHotStuffHashMap.get(consensusContext.getChannel().getChannelId());
        WaitExecuteTxPool txPool = processor.getTxExecutorConsensusCache().getWaitExecuteTxPool();
        long totalTxCount = txPool.getTotalTxCount();
        Long totalSize = txPool.getPooledTotalSize();
        //?????????-???????????????=???????????????
        channelDynamicParams.setToPackageTransSize(totalSize - consensusSize);
        channelDynamicParams.setToPackageTransNum(totalTxCount - consensusTxNum);
        channelDynamicParams.setViewNum(consensusContext.currView.getNo());
/*        if (System.currentTimeMillis() - block.getBlockHeader().getTimestamp() > 3 * consensusContext.getChannel().getBlockMaxInterval()) {
            channelDynamicParams.setConsensusFlag(false);
        }*/
        channelDynamicParams.setConsensusFlag(getConsensusFlag(consensusContext));
        List<PeerBasicParams> peerBasicParams = new ArrayList<>();
        //???????????????3
        List<Peer> validPeer = consensusContext.getChannel().getValidMemberPeerList(block.getBlockHeader().getHeight()+3L);
        for (Peer peer : validPeer) {
            PeerBasicParams params = new PeerBasicParams();
            params.setPeerId(peer.getPeerId().getValue());
            params.setServiceUrls(peer.getServiceUrls());
            try {
                params.setPublicCertFile((new String(peer.getCertificateCerFile(), "UTF-8")));
            } catch (Exception e) {
                log.warn(ModuleClassification.ConM_CMPI_ + "," + consensusContext.getChannel().getChannelId() + "TError", e);
            }
            setLatestJoinTime(params,peer);
            params.setCurrentState(peer.isState() ? "1" : "3");
            params.setOrganizationPublicCertFile(peer.getPeerOrganization().getCertificateCerFile());
            params.setOrganizationID(peer.getPeerOrganization().getOrganizationId());
            QueryChannelReq channelReq = createQueryChannelReq(consensusContext);
            //
            try {
                Peer locationPeer = ledgerMgr.queryLocalPeer();
                if (!peer.equals(locationPeer)) {
                    PeerOrganizationParams peerOrganizationParams = p2pClient.queryPeerOrganizationState(channelReq, peer);
                    params.setOrganizationPublicCertFile(peerOrganizationParams.getOrganizationPublicCertFile());
                } else {
                    params.setOrganizationPublicCertFile(locationPeer.getPeerOrganization().getCertificateCerFile());
                }
            } catch (Exception e) {
                params.setCurrentState("2");
                log.warn(ModuleClassification.ConM_CMPI_ + "," + consensusContext.getChannel().getChannelId() + "TError" + "peer not accessible,peerUrl={}", peer.getServiceUrls(), e);
            }
            peerBasicParams.add(params);
            //???????????????????????????????????????
            setHistoryActionList(params,peer);
        }
        peerChannelParams.setChannelDynamicParams(channelDynamicParams);
        peerChannelParams.setPeerBasicParams(peerBasicParams);
    }

    /**
     * ????????????????????????????????????
     * @param params
     * @param peer
     */
    private void setLatestJoinTime(PeerBasicParams params, Peer peer) {
        for (PeerChannelRelation peerChannelRelation:peer.getPeerChannelRelationList()) {
            String actionType = peerChannelRelation.getActionType();
            if (PeerActionTypeEnum.IN_OUT.getCode().equals(actionType)) {
                params.setJoinTime(peerChannelRelation.getJoinTimeStamp().getTime());
                return;
            }
        }
    }

    /**
     * ?????????????????????????????????
     */
    private void setHistoryActionList(PeerBasicParams params, Peer peer) {
        List<PeerChannelOperationRecord> historyActionList = new ArrayList<>();
        for (PeerChannelRelation peerChannelRelation:peer.getPeerChannelRelationList()) {
            String actionType = peerChannelRelation.getActionType();
            if (PeerActionTypeEnum.IN_OUT.getCode().equals(actionType)) {
                //????????????????????????
                //IN??????
                PeerChannelOperationRecord peerChannelInRecord = new PeerChannelOperationRecord();
                peerChannelInRecord.setActionTypeEnum(ActionTypeEnum.IN);
                peerChannelInRecord.setBlockHeight(peerChannelRelation.getInBlockHeight()+3L);
                peerChannelInRecord.setActionTime(peerChannelRelation.getJoinTimeStamp().getTime());
                historyActionList.add(peerChannelInRecord);
                if (peerChannelRelation.getOutBlockHeight() != 0) {
                    //OUT??????
                    PeerChannelOperationRecord peerChannelOutRecord = new PeerChannelOperationRecord();
                    peerChannelOutRecord.setActionTypeEnum(ActionTypeEnum.OUT);
                    peerChannelOutRecord.setBlockHeight(peerChannelRelation.getOutBlockHeight()+3L);
                    peerChannelOutRecord.setActionTime(peerChannelRelation.getUpdateTimeStamp().getTime());
                    historyActionList.add(peerChannelOutRecord);
                }
            } else {
                //????????????????????????
                //Frozen??????
                PeerChannelOperationRecord peerChannelFrozenRecord = new PeerChannelOperationRecord();
                peerChannelFrozenRecord.setActionTypeEnum(ActionTypeEnum.FROZEN);
                peerChannelFrozenRecord.setBlockHeight(peerChannelRelation.getInBlockHeight()+3L);
                peerChannelFrozenRecord.setActionTime(peerChannelRelation.getJoinTimeStamp().getTime());
                historyActionList.add(peerChannelFrozenRecord);
                if (peerChannelRelation.getOutBlockHeight() != 0) {
                    //UnFrozen??????
                    PeerChannelOperationRecord peerChannelUnFrozenRecord = new PeerChannelOperationRecord();
                    peerChannelUnFrozenRecord.setActionTypeEnum(ActionTypeEnum.UNFROZEN);
                    peerChannelUnFrozenRecord.setBlockHeight(peerChannelRelation.getOutBlockHeight()+3L);
                    peerChannelUnFrozenRecord.setActionTime(peerChannelRelation.getUpdateTimeStamp().getTime());
                    historyActionList.add(peerChannelUnFrozenRecord);
                }
            }

        }
        //??????
        Collections.sort(historyActionList);
        params.setHistoryActionList(historyActionList);
    }

    private QueryChannelReq createQueryChannelReq(ConsensusContext consensusContext) {
        QueryChannelReq channelReq = new QueryChannelReq();
        channelReq.setChannelId(consensusContext.getChannel().getChannelId());
        SecurityService securityService = securityServiceMgr.getMatchSecurityService(consensusContext.getChannel().getSecurityServiceKey());
        securityService.hash(channelReq);
        securityService.signByGMCertificate(channelReq, consensusContext.getChannel().getChannelId());
        return channelReq;

    }

    private boolean getConsensusFlag(ConsensusContext consensusContext) {
        //???????????????????????????
        //????????????
        if (consensusContext.consensusStageEnum.equals(ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL) ||
                consensusContext.consensusStageEnum.equals(ConsensusStageEnum.LEAVE_CHANNEL)
        ) {
            return false;
        }
        //?????????????????????,????????????leader ???leader????????????leader????????????????????????
        Peer locationPeer = ledgerMgr.queryLocalPeer();
        QueryChannelReq channelReq = createQueryChannelReq(consensusContext);
        if (locationPeer.equals(consensusContext.currLeader)) {
            for (Peer peer : consensusContext.getOrderedPeerList()) {
                if (!peer.equals(locationPeer) && peer.isState()) {
                    try {
                        p2pClient.queryPeerOrganizationState(channelReq, consensusContext.currLeader);
                        break;
                    } catch (Exception e) {
                        return false;
                    }
                }
            }
        } else {
            try {
                p2pClient.queryPeerOrganizationState(channelReq, consensusContext.currLeader);
            } catch (Exception e) {
                return false;
            }
        }
        //TODO ??????????????????

        return true;
    }

    @Override
    public PeerOrganizationParams queryPeerOrganizationState(String channelId) {
        PeerOrganizationParams peerOrganizationParams = new PeerOrganizationParams();
        try {
            NewSpiralHotStuffProcessor processor = newSpiralHotStuffHashMap.get(channelId);
            if (processor == null) {
                return peerOrganizationParams;
            } else {
                PeerOrganization peerOrganization = ledgerMgr.queryLocalPeer().getPeerOrganization();
                peerOrganizationParams.setOrganizationPublicCertFile(peerOrganization.getCertificateCerFile());
                return peerOrganizationParams;
            }
        } catch (Exception e) {
            return peerOrganizationParams;
        }
    }

    @Override
    public BaseResponse deleteChannel(String channelId) {
        NewSpiralHotStuffProcessor processor = newSpiralHotStuffHashMap.get(channelId);
        if (processor == null) {
            return ResponseUtil.error(NewspiralStateCodes.SYSTEM_ERROR);
        } else {
            ConsensusContext consensusContext = processor.consensusContext;
            //???????????????????????????????????????
            Block block = consensusContext.getBlock(consensusContext.getLockedQC().getPrevBlockHash());
            int peerCount = consensusContext.getChannel().getCurrentValidMemberPeerListByHeight(block.getBlockHeader().getHeight()).size();
            if (peerCount > 1) {
                return ResponseUtil.error(NewspiralStateCodes.SYSTEM_DELELTCHANNELPEER_NOT_ONLY_ONE);
            }
        }
        newSpiralSyncHistoryBlockHashMap.get(channelId).flag = false;
        newSpiralHotStuffHashMap.get(channelId).flag = false;
        newSpiralHotStuffHashMap.remove(channelId);
        newSpiralSyncHistoryBlockHashMap.remove(channelId);
        //LedgerThreadLocalContext.wholeTransModifiedChannel.set(null);
        //??????????????????
        //ledgerMgr.deleteChannelData(channelId);
        return ResponseUtil.success();
    }

    @Override
    public PerformanceContext getContextParameter(String channelId) {
        PerformanceContext performanceContext = new PerformanceContext();

        try {
            NewSpiralHotStuffProcessor processor = newSpiralHotStuffHashMap.get(channelId);
            if (processor == null) {
                log.warn("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channelId + ",NewSpiralHotStuffProcessor??????getContextParameter????????????????????????????????????????????????????????????????????????Channel????????????");
                return null;
            } else {
                ConsensusContext consensusContext = processor.consensusContext;
                performanceContext.setBlockHeight(consensusContext.getBlockHeight());
                performanceContext.setNodeNumber(consensusContext.orderedPeerList.size());
                return performanceContext;
            }
        } catch (Exception e) {
            log.error("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channelId + ",NewSpiralHotStuffProcessor??????getContextParameter?????????????????????:", e);
            return null;
        }
    }

    @Override
    public void processPersistCacheTransHashs(Set<String> transHashs) {
        /*oldTransHashs.addAll(transHashs);
        newTransHashs.addAll(transHashs);
        if(oldTransHashs.size()>cacheTransHashSize)
        {
            newTransHashs.clear();
            for(String str:oldTransHashs)
            {
                newTransHashs.add(str);
            }
            oldTransHashs.clear();
        }*/
        newTransHashs.addAll(transHashs);
        if (newTransHashs.size() > cacheTransHashSize) {
            oldTransHashs.clear();
            oldTransHashs = newTransHashs;
            newTransHashs = new HashSet<>();
        }

    }

    @Override
    public void changeConsensusStage(String channelId, Long height, ConsensusStageEnum consensusStageEnum) {
        NewSpiralHotStuffProcessor processor = newSpiralHotStuffHashMap.get(channelId);
        if (processor == null) {
            return;
        } else {
            ConsensusContext consensusContext = processor.consensusContext;
            Long ttt = System.currentTimeMillis();
            try {
                consensusContext.readLock(ttt);
                consensusContext.setConsensusStageEnum(consensusStageEnum);
                consensusContext.setBlockHeight(height);
            } catch (Exception ex) {
                log.error("change consensusStage occured error:", ex);
            } finally {
                consensusContext.readUnlock(ttt);
            }
        }
    }


    @Override
    public void changeConsensusToNoAvailable(String channelId, Long height) {
        NewSpiralHotStuffProcessor processor = newSpiralHotStuffHashMap.get(channelId);
        if (processor == null) {
            return;
        } else {
            ConsensusContext consensusContext = processor.consensusContext;
            Long ttt = System.currentTimeMillis();
            try {
                consensusContext.readLock(ttt);
                consensusContext.setConsensusStageEnum(ConsensusStageEnum.NO_AVALIABLE_DB_ROUTE);
                consensusContext.setBlockHeight(Math.min(height, consensusContext.blockHeight));
                consensusContext.setPersistedBlockHeight(Math.min(height, consensusContext.blockHeight));
            } catch (Exception ex) {
                log.error("change consensusStage occured error:", ex);
            } finally {
                consensusContext.readUnlock(ttt);
            }
        }
    }


    @Override
    public void changeNoAvailableConsensus(String channelId, Long height, ConsensusStageEnum consensusStageEnum) {
        NewSpiralHotStuffProcessor processor = newSpiralHotStuffHashMap.get(channelId);
        if (processor == null) {
            return;
        } else {
            ConsensusContext consensusContext = processor.consensusContext;
            if (!consensusContext.getConsensusStageEnum().equals(ConsensusStageEnum.NO_AVALIABLE_DB_ROUTE)) {
                return;
            }
            Long ttt = System.currentTimeMillis();
            try {
                consensusContext.readLock(ttt);
                consensusContext.setNoAvailableConsensusStageToAnOther(consensusStageEnum);
                consensusContext.setPersistedBlockHeight(Math.min(height, consensusContext.blockHeight));
                consensusContext.setBlockHeight(Math.min(height, consensusContext.blockHeight));
            } catch (Exception ex) {
                log.error("change consensusStage occured error:", ex);
            } finally {
                consensusContext.readUnlock(ttt);
            }
        }
    }

    @Override
    public void reloadLocalPeer() {
        transactionMgr.reloadLocalPeer();
    }

}
