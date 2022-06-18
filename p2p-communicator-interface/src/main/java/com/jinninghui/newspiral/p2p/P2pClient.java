package com.jinninghui.newspiral.p2p;

import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.chain.ChannelBasicParams;
import com.jinninghui.newspiral.common.entity.chain.Peer;
import com.jinninghui.newspiral.common.entity.chain.PeerOrganizationParams;
import com.jinninghui.newspiral.common.entity.common.base.BaseTransHashResp;
import com.jinninghui.newspiral.common.entity.consensus.CurrentConsensusState;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import com.jinninghui.newspiral.gateway.entity.*;
import com.jinninghui.newspiral.common.entity.consensus.QueryViewNoResp;

/**
 * @author lida
 * @date 2019/7/15 11:53
 * 临时的一次性调用使用此对象
 * 其他非一次性调用，一般均是强场景相关的，使用对应的Interface，强场景相关的Interface底层实现会利用连接复用等机制提高通信效率
 */
public interface P2pClient  {

    /**
     * 新组织加入通道，不抛异常表示对端节点已经接受该请求，是否加入成功需要使用queryChannel接口查询
     * @param newMemberTransaction
     */
    BaseTransHashResp addNewPeer2Channel(SDKTransaction newMemberTransaction, String serviceUrlForPeer);

    /**
     * remove the given peer declared in the sdkTransaction.
     * @param rmPeerTransaction
     * @param targetPeer
     */
    void removePeerFromChannel(SDKTransaction rmPeerTransaction, Peer targetPeer);

    /**
     * 查询某个通道的基本配置信息
     * @param queryChannelReq
     * @return
     */
    ChannelBasicParams queryChannelBasicParams(QueryChannelReq queryChannelReq, String serviceUrlForPeer);

    /**
     * 查询某个通道的全量详细信息
     * @param queryChannelReq
     * @param serviceUrlForPeer
     * @return
     */
    Channel queryChannel(QueryChannelReq queryChannelReq, String serviceUrlForPeer);

    /**
     * 查询某个通道当前的链共识状态数据
     * @param queryChainStateReq
     * @param peer
     * @return
     */
    QueryChainStateResp queryChainState(QueryChainStateReq queryChainStateReq, Peer peer);

    /**
     * 查询节点的view值
     * @param queryViewNoReq
     * @param peer
     * @return
     */
    QueryViewNoResp queryViewNo(QueryViewNoReq queryViewNoReq, Peer peer);

    /**
     * query the History QC of channel
     * @param queryHistoryQCReq
     * @param peer
     * @return
     */
    QueryHistoryQCResp queryHistoryQC(QueryHistoryQCReq queryHistoryQCReq, Peer peer);

    /**
     * query history block of channel
     * @param queryHistoryBlockReq
     * @return
     */
    QueryHistoryBlockResp queryHistoryBlock(QueryHistoryBlockReq queryHistoryBlockReq, Peer peer);

    /************* 节点监控相关接口开始***********************************************/

    /**
     * select current consensus state include (block view peerlist)
     * @param queryChannelReq
     * @return
     */
    CurrentConsensusState queryChainState(QueryChannelReq queryChannelReq, Peer peer);

    /**
     * 节点组织根证书以及节点状态
     * @param queryChannelReq
     * @param targetPeer
     * @return
     */
    PeerOrganizationParams queryPeerOrganizationState(QueryChannelReq queryChannelReq, Peer targetPeer);

    /**
     * 根据区块高度查询某个区块
     * @param queryChainStateReq
     * @param peer peer
     * @return
     */
    Block queryBlock(QueryChainStateReq queryChainStateReq,Peer peer);

    /**
     * 根据区块Hash查询某个区块
     * @param queryBlockStateReq
     * @param peer
     * @return
     */
    Block queryBlock(QueryBlockStateReq queryBlockStateReq, Peer peer);

    /************* 节点监控相关接口结束***********************************************/

    /**
     * 更新通道客户端
     * @param channel
     */
    void updateChannelClientsMap(Channel channel);
}
