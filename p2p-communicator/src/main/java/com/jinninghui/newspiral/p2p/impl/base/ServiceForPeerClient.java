package com.jinninghui.newspiral.p2p.impl.base;

import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.config.ApplicationConfig;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.chain.ChannelBasicParams;
import com.jinninghui.newspiral.common.entity.chain.ChannelSummary;
import com.jinninghui.newspiral.common.entity.chain.Peer;
import com.jinninghui.newspiral.common.entity.chain.PeerOrganizationParams;
import com.jinninghui.newspiral.common.entity.chain.PeerServiceTypeEnum;
import com.jinninghui.newspiral.common.entity.common.base.BaseTransHashResp;
import com.jinninghui.newspiral.common.entity.common.logmodule.LogModuleCodes;
import com.jinninghui.newspiral.common.entity.consensus.BlockVoteMsg;
import com.jinninghui.newspiral.common.entity.consensus.CurrentConsensusState;
import com.jinninghui.newspiral.common.entity.consensus.GenericMsg;
import com.jinninghui.newspiral.common.entity.consensus.NewViewMsg;
import com.jinninghui.newspiral.common.entity.consensus.QueryViewNoResp;
import com.jinninghui.newspiral.common.entity.exception.NewSpiralErrorEnum;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import com.jinninghui.newspiral.gateway.ServiceForConsensus;
import com.jinninghui.newspiral.gateway.ServiceForPeer;
import com.jinninghui.newspiral.gateway.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * @author lida
 * @date 2019/7/29 17:35
 * 封装SOAFClient，增加一些控制和状态字段
 */
@Configuration
@Slf4j
public class ServiceForPeerClient implements ServiceForPeer, ServiceForConsensus {
    public static final long UNDEFINED_TIME = -1L;
    private ServiceForPeer serviceForPeer = null;

    private ServiceForConsensus serviceForConsensus = null;

    /**
     * targetPeer
     */
    private Peer targetPeer;

    /**
     * 是否已经连接
     */
    private boolean connected = false;
    /**
     * 上次成功发送心跳的时间戳
     */
    private long prevHeartbeatSuccessTime = ServiceForPeerClient.UNDEFINED_TIME;
    /**
     * 上次发送心跳的时间戳
     */
    private long prevHeartbeatTime = ServiceForPeerClient.UNDEFINED_TIME;

    private long prevBusiMsgSendTime = ServiceForPeerClient.UNDEFINED_TIME;

    private long prevBusiMsgSendSuccessTime = ServiceForPeerClient.UNDEFINED_TIME;

    /**
     * 远程调用连续失败次数
     */
    private long continuesFailedCnt = 0L;

    /**
     * 已经发送的消息数，包括成功的和失败的
     */
    private long sentMsgCnt = 0L;

    /**
     * 成功发送的消息数
     */
    private long sentSuccessMsgCnt = 0L;

/*    @SofaReference
    private LedgerMgr ledgerMgr;

    @SofaReference
    private ConsensusMsgProcessor consensusMsgProcessor;*/

    private ApplicationConfig applicationConfig = new ApplicationConfig().setAppName("test-client");
    /**
     * 只能动态创建
     */
    public void init(Peer targetPeer) {
        this.targetPeer = targetPeer;
        String directUrl = "h2://" + targetPeer.getServiceUrls().getUrlByType(PeerServiceTypeEnum.FOR_PEER);

        ConsumerConfig<ServiceForPeer> consumerConfig = new ConsumerConfig<ServiceForPeer>()
                .setApplication(applicationConfig)
                .setInterfaceId(ServiceForPeer.class.getName()) // 指定接口
                .setProtocol("h2")
                .setDirectUrl(directUrl) //
                .setRegister(false)
                .setRepeatedReferLimit(-1)//允许重复指向同一个interfaceID
                .setConnectTimeout(5000)
                .setTimeout(10000);
        // 生成代理类
        //consumerConfig.setTimeout(5000);
        this.serviceForPeer = consumerConfig.refer();

        ConsumerConfig<ServiceForConsensus> consensusConsumerConfig = new ConsumerConfig<ServiceForConsensus>()
                .setApplication(applicationConfig)
                .setInterfaceId(ServiceForConsensus.class.getName()) // 指定接口
                .setProtocol("h2")
                .setDirectUrl(directUrl) //
                .setRegister(false)
                .setRepeatedReferLimit(-1)//允许重复指向同一个interfaceID
                .setConnectTimeout(5000)
                //.setTimeout(10000);
                .setInvokeType(RpcConstants.INVOKER_TYPE_FUTURE);
        this.serviceForConsensus = consensusConsumerConfig.refer();
    }

    @Override
    public BaseTransHashResp addNewPeer2Channel(SDKTransaction newMemberTransaction) {
      return   serviceForPeer.addNewPeer2Channel(newMemberTransaction);
    }

    @Override
    public void removePeerFromChannel(SDKTransaction rmPeerTransaction) {
        serviceForPeer.removePeerFromChannel(rmPeerTransaction);
    }

    @Override
    public Channel queryChannel(QueryChannelReq queryChannelReq) {
        return serviceForPeer.queryChannel(queryChannelReq);
    }

    @Override
    public ChannelSummary queryChainSummary(QueryChannelReq queryChannelReq) {
        log.error(NewSpiralErrorEnum.UN_IMPLEMENTED.toString());
        return null;
    }

    @Override
    public ChannelBasicParams queryChannelBasicParams(QueryChannelReq queryChannelReq) {
        return serviceForPeer.queryChannelBasicParams(queryChannelReq);
    }
    @Override
    public Block queryBlockByHeight(QueryChainStateReq queryChainStateReq) {
        return serviceForPeer.queryBlockByHeight(queryChainStateReq);
    }

    @Override
    public Block queryBlockByHash(QueryBlockStateReq queryBlockStateReq) {
        return serviceForPeer.queryBlockByHash(queryBlockStateReq);
    }

    /**
     * 一次成功的RPC调用后更新本地状态
     */
    private void updateLocalStateAfterSuccCall() {
        this.prevBusiMsgSendTime = System.currentTimeMillis();
        this.prevBusiMsgSendSuccessTime = prevBusiMsgSendTime;
        this.sentMsgCnt += 1;
        this.sentSuccessMsgCnt += 1L;
        this.connected = true;
        continuesFailedCnt = 0L;
    }

    /**
     * 一次失败的RPC调用后更新本地状态
     */
    private void updateLocalStateAfterFailCall() {
        this.prevBusiMsgSendTime = System.currentTimeMillis();
        this.sentMsgCnt += 1;
        continuesFailedCnt += 1L;
        if (continuesFailedCnt > 10L) {//一个最简单的连接状态设置策略
            continuesFailedCnt = 0L;
            connected = false;
        }
    }


    /**
     * 新交易加入交易池
     *
     * @param sdkTransaction
     */
    @Override
    public void addSDKTranscation(SDKTransaction sdkTransaction) {
        try {
            //log.info("MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",发送sdkTransaction到" + targetPeer.getPeerId().getValue());
            //TODO 如果交易包含V240.7.3，演示篡改功能 正式时需要注释
/*                if(sdkTransaction.getVersion().equals("V240.7.3"))
                {
                    SDKTransaction newSSDKTransaction=sdkTransaction.clone();
                    Object[] objects=newSSDKTransaction.getSmartContractCallInstnace().getMethodArgs();
                    newSSDKTransaction.getSmartContractCallInstnace().setMethodArgs(new Object[]{objects[0],objects[1],"300"});
                    this.serviceForPeer.addSDKTranscation(newSSDKTransaction);
                    log.info("ServiceForPeerClient.addSDKTranscation,tamper demonstration,transfer 300 accounts");
                }
                else {*/
                    this.serviceForPeer.addSDKTranscation(sdkTransaction);
                //}
            updateLocalStateAfterSuccCall();
        } catch (Exception ex) {
            log.warn("MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",调用sdkTransaction失败,异步系统，此异常并不会抛出，大部分情况下，对系统不会产生严重影响，" +
                    "channelId:" + sdkTransaction.getChannelId() + ",clientTxId:" + sdkTransaction.getClientTxId() + "，异常信息:",ex);
            updateLocalStateAfterFailCall();
        }
    }

    /**
     * 共识相关的接口
     */
    public void addGenericMsg(GenericMsg genericMsg) {
        try {
            serviceForConsensus.addGenericMsg(genericMsg);
        } catch (Exception ex) {
            log.warn("MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",addGenericMsg failed," +
                    "channelId:" + genericMsg.getChannelId() + ",genericMsg:" + genericMsg.createSimpleDespStr() + ", exception:" , ex);
        }

    }

    public void addNewViewMsg(NewViewMsg newViewMsg) {

        try {
            serviceForConsensus.addNewViewMsg(newViewMsg);
        } catch (Exception ex) {
            log.warn("MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",addNewViewMsg failed," +
                    "channelId:" + newViewMsg.getChannelId() + ",newViewMsg:" + newViewMsg.toString() + ", exception:" , ex);
        }
    }

    public void addBlockVoteMsg(BlockVoteMsg voteMsg) {

        try {
            serviceForConsensus.addBlockVoteMsg(voteMsg);
        } catch (Exception ex) {
            log.warn("MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",addBlockVoteMsg failed," +
                    "channelId:" + voteMsg.getChannelId() + ",voteMsg:" + voteMsg.toString() + ", exception:", ex);
        }
    }

    public Peer getTargetPeer() {
        return targetPeer;
    }

    @Override
    public QueryChainStateResp queryChainState(QueryChainStateReq queryChainStateReq) {
        try{
            QueryChainStateResp queryChainStateResp = this.serviceForPeer.queryChainState(queryChainStateReq);
            return queryChainStateResp;
        } catch (Exception ex) {
            log.info("MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",Failed to query chain state from peer, exception:", ex);
            return null;
        }

    }

    @Override
    public QueryViewNoResp queryViewNo(QueryViewNoReq queryViewNoReq) {
        try {
            QueryViewNoResp queryViewNoResp = this.serviceForPeer.queryViewNo(queryViewNoReq);
            //SofaResponseFuture.getResponse(1000, true);
            return queryViewNoResp;
        } catch (Exception ex) {
            log.info("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",Failed to query view number from peer, exception:", ex);
            return null;
        }
    }

    @Override
    public QueryHistoryQCResp queryHistoryQC(QueryHistoryQCReq queryHistoryQCReq) {
        try {
            QueryHistoryQCResp queryHistoryQCResp = this.serviceForPeer.queryHistoryQC(queryHistoryQCReq);
            return queryHistoryQCResp;
        } catch (Exception ex) {
            log.info("MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",Failed to query the history QCs from peer, exception:", ex);
            return null;
        }
    }

    @Override
    public QueryHistoryBlockResp queryHistoryBlock(QueryHistoryBlockReq queryHistoryBlockReq) {
        try {
            QueryHistoryBlockResp queryHistoryBlockResp = this.serviceForPeer.queryHistoryBlock(queryHistoryBlockReq);
            return queryHistoryBlockResp;
        } catch (Exception ex) {
            log.info("MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",Failed to query history block from peer, exception:", ex);
           throw ex;
        }
    }

    /************* 节点监控相关接口开始***********************************************/
    @Override
    public CurrentConsensusState queryChainStateByChannelId(QueryChannelReq queryChannelReq) {
        return this.serviceForPeer.queryChainStateByChannelId(queryChannelReq);
    }

    public PeerOrganizationParams queryPeerOrganizationState(QueryChannelReq queryChannelReq) {
        return serviceForPeer.queryPeerOrganizationState(queryChannelReq);
    }

    /************* 节点监控相关接口结束***********************************************/

}
