package com.jinninghui.newspiral.p2p.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.rpc.config.ApplicationConfig;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.chain.ChannelBasicParams;
import com.jinninghui.newspiral.common.entity.chain.Peer;
import com.jinninghui.newspiral.common.entity.chain.PeerOrganizationParams;
import com.jinninghui.newspiral.common.entity.common.base.BaseTransHashResp;
import com.jinninghui.newspiral.common.entity.consensus.CurrentConsensusState;
import com.jinninghui.newspiral.common.entity.consensus.QueryViewNoResp;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import com.jinninghui.newspiral.gateway.ServiceForPeer;
import com.jinninghui.newspiral.gateway.entity.*;
import com.jinninghui.newspiral.p2p.P2pClient;
import com.jinninghui.newspiral.p2p.impl.base.ChannelClientMgr;
import com.jinninghui.newspiral.p2p.impl.base.ServiceForPeerClient;
import com.jinninghui.newspiral.p2p.impl.base.ServiceForPeerClientMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lida
 * @date 2019/7/15 17:49
 */
@Slf4j
public class P2PClientImpl implements P2pClient {

    @Autowired
    private ServiceForPeerClientMgr serviceForPeerClientMgr;

    /**
     * key为节点的服务地址，该容器只保存临时建立的节点连接，如在节点加入通道之前和通道中的节点之间的查询连接。
     */
    private Map<String, ServiceForPeer> easyClientMap = new HashMap<>();


    synchronized ServiceForPeerClient getClient(Peer targetPeer) {
        ChannelClientMgr channelClientMgr = serviceForPeerClientMgr.getChannelClientMgr(targetPeer.getPeerChannelRelation().getChannelId());
        //身份里面赋值一下通道ID
        targetPeer.getPeerId().setChannelId(targetPeer.getPeerChannelRelation().getChannelId());
        ServiceForPeerClient serviceForPeerClient = channelClientMgr.getClient(targetPeer.getPeerId());
        if (null == serviceForPeerClient) {
            serviceForPeerClientMgr.removeChannelClientsMap(targetPeer.getPeerChannelRelation().getChannelId());
            channelClientMgr = serviceForPeerClientMgr.getChannelClientMgr(targetPeer.getPeerChannelRelation().getChannelId());
            return channelClientMgr.getClient(targetPeer.getPeerId());
        }
        return serviceForPeerClient;
    }

    synchronized ServiceForPeer getEasyClient(String url) {
        ServiceForPeer serviceForPeer=easyClientMap.get(url);
        if(serviceForPeer!=null)
        {
            return serviceForPeer;
        }
        ConsumerConfig<ServiceForPeer> consumerConfig = new ConsumerConfig<ServiceForPeer>()
                .setApplication(new ApplicationConfig().setAppName("easyClient"))
                .setInterfaceId(ServiceForPeer.class.getName()) // 指定接口
                .setProtocol("h2")
                .setDirectUrl(url) //
                .setRegister(false)
                .setRepeatedReferLimit(-1)//允许重复指向同一个interfaceID
                .setTimeout(10000);
        // 生成代理类
         serviceForPeer = consumerConfig.refer();
        easyClientMap.put(url, serviceForPeer);
        return serviceForPeer;
    }

    synchronized void removeChannelClientsMap(Peer targetPeer) {
        serviceForPeerClientMgr.removeChannelClientsMap(targetPeer.getPeerChannelRelation().getChannelId());
    }

    @Override
    public BaseTransHashResp addNewPeer2Channel(SDKTransaction newMemberTransaction, String serviceUrlForPeer) {
        ServiceForPeer serviceForPeer = getEasyClient(serviceUrlForPeer);
        return serviceForPeer.addNewPeer2Channel(newMemberTransaction);
    }

    @Override
    public void removePeerFromChannel(SDKTransaction rmPeerTransaction, Peer targetPeer) {
        ServiceForPeerClient client = this.getClient(targetPeer);
        client.removePeerFromChannel(rmPeerTransaction);

    }

    @Override
    public ChannelBasicParams queryChannelBasicParams(QueryChannelReq queryChannelReq, String serviceUrlForPeer) {
        ServiceForPeer serviceForPeer = getEasyClient(serviceUrlForPeer);
        return serviceForPeer.queryChannelBasicParams(queryChannelReq);
    }

    @Override
    public Channel queryChannel(QueryChannelReq queryChannelReq, String serviceUrlForPeer) {
        ServiceForPeer serviceForPeer = getEasyClient(serviceUrlForPeer);
        return serviceForPeer.queryChannel(queryChannelReq);
    }

    @Override
    public QueryChainStateResp queryChainState(QueryChainStateReq queryChainStateReq, Peer targetPeer) {
        ServiceForPeerClient serviceForPeerClient = this.getClient(targetPeer);
        if (serviceForPeerClient == null) {
            log.error("queryChainState.targetPeer={}", JSONObject.toJSON(targetPeer.getPeerId()));
        }
        return serviceForPeerClient.queryChainState(queryChainStateReq);
    }

    @Override
    public QueryHistoryQCResp queryHistoryQC(QueryHistoryQCReq queryHistoryQCReq, Peer targetPeer) {
        return this.getClient(targetPeer).queryHistoryQC(queryHistoryQCReq);
    }

    @Override
    public QueryViewNoResp queryViewNo(QueryViewNoReq queryViewNoReq, Peer targetPeer) {
        return this.getClient(targetPeer).queryViewNo(queryViewNoReq);
    }

    @Override
    public QueryHistoryBlockResp queryHistoryBlock(QueryHistoryBlockReq queryHistoryBlockReq, Peer targetPeer) {
        try {
            return this.getClient(targetPeer).queryHistoryBlock(queryHistoryBlockReq);
        } catch (Exception ex) {
            if (ex.toString().contains("has been closed, remove future when channel inactive")
            ) {
                //移除连接，重新初始化一下
                removeChannelClientsMap(targetPeer);
            }
        }
        return null;
    }

    /************* 节点监控相关接口开始***********************************************/


    @Override
    public CurrentConsensusState queryChainState(QueryChannelReq queryChannelReq, Peer targetPeer) {
        return this.getClient(targetPeer).queryChainStateByChannelId(queryChannelReq);
    }

    @Override
    public Block queryBlock(QueryChainStateReq queryChainStateReq, Peer targetPeer) {
        return this.getClient(targetPeer).queryBlockByHeight(queryChainStateReq);
    }

    @Override
    public Block queryBlock(QueryBlockStateReq queryBlockStateReq, Peer targetPeer) {
        return this.getClient(targetPeer).queryBlockByHash(queryBlockStateReq);
    }

    /************* 节点监控相关接口结束***********************************************/

    @Override
    public PeerOrganizationParams queryPeerOrganizationState(QueryChannelReq queryChannelReq, Peer targetPeer) {
        return this.getClient(targetPeer).queryPeerOrganizationState(queryChannelReq);
        //String serviceUrlForPeer="h2://"+targetPeer.getServiceUrls().getUrlByType(PeerServiceTypeEnum.FOR_PEER);
        //return getEasyClient(serviceUrlForPeer).queryPeerOrganizationState(queryChannelReq);
    }

    @Override
    public void updateChannelClientsMap(Channel channel)
    {
        serviceForPeerClientMgr.updateChannelClientsMap(channel);
    }
}
