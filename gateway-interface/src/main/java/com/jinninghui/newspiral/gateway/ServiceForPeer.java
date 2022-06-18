package com.jinninghui.newspiral.gateway;

import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.chain.ChannelBasicParams;
import com.jinninghui.newspiral.common.entity.chain.ChannelSummary;
import com.jinninghui.newspiral.common.entity.chain.PeerOrganizationParams;
import com.jinninghui.newspiral.common.entity.common.base.BaseTransHashResp;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import com.jinninghui.newspiral.common.entity.consensus.CurrentConsensusState;
import com.jinninghui.newspiral.common.entity.consensus.QueryViewNoResp;
import com.jinninghui.newspiral.gateway.entity.*;

/**
 * @author lida
 * @date 2019/7/15 16:42
 * 其他节点所需要使用的接口
 * 这个定义技术上说，是给SOFA Client使用的
 * 需要由P2P模块封装得到更符合业务场景的接口给到其他模块例如共识模块使用
 */
public interface ServiceForPeer {


    /**
     * 新交易加入交易池
     * @param sdkTransaction
     */
    void addSDKTranscation(SDKTransaction sdkTransaction);


    /**
     * 新组织加入通道，供其他节点调用
     * @param newMemberTransaction
     */
    BaseTransHashResp addNewPeer2Channel(SDKTransaction newMemberTransaction);

    /**
     * remove peer from channel.
     * @param rmPeerTransaction
     */
    void removePeerFromChannel(SDKTransaction rmPeerTransaction);

    /**
     * 查询某个节点保存的某个通道基础参数
     * 返回简单的可以向通道外公布的通道信息，典型场景：新节点加入Channel时查询一些信息
     * 不需要签名认证，也即输入参数中的签名可为空
     * @param queryChannelReq
     * @return
     */
    ChannelBasicParams queryChannelBasicParams(QueryChannelReq queryChannelReq);


    /**
     * 查询某个通道的详细信息，要求调用方已经在该通道中
     * @param queryChannelReq
     * @return
     */
    Channel queryChannel(QueryChannelReq queryChannelReq);

    /**
     * 查询某个通道的区块链概要信息，如果没有该链，抛出异常
     * 仅基于本地已同步的数据处理，因此如果一条新建的链在本节点尚未完成数据同步，可能会给出非最新值或直接抛出“不存在该链”的异常
     * @return
     */
    ChannelSummary queryChainSummary(QueryChannelReq queryChannelReq);


    /**
     * 根据区块高度查询某个区块
     * @param queryChainStateReq
     * @return
     */
    Block queryBlockByHeight(QueryChainStateReq queryChainStateReq);

    /**
     * 根据区块Hash查询某个区块
     * @param queryBlockStateReq
     * @return
     */
    Block queryBlockByHash(QueryBlockStateReq queryBlockStateReq);


    /************ 节点同步数据相关接口***********************/
    /**
     * get chain state before taking part in consensus.
     * @param queryChainStateReq
     * @return
     */
    QueryChainStateResp queryChainState(QueryChainStateReq queryChainStateReq);

    /**
     * get current view number of peer.
     * @param queryViewNoReq
     * @return
     */
    QueryViewNoResp queryViewNo(QueryViewNoReq queryViewNoReq);

    /**
     * query the QC concerned with committed block
     * @param queryHistoryQCReq
     * @return
     */
    QueryHistoryQCResp queryHistoryQC(QueryHistoryQCReq queryHistoryQCReq);

    /**
     * query the committed block
     * @param queryHistoryBlockReq
     * @return
     */
    QueryHistoryBlockResp queryHistoryBlock (QueryHistoryBlockReq queryHistoryBlockReq);


    /************* 节点监控相关接口开始***********************************************/

    /**
     * select current consensus state include (block view peerlist)
     * @param queryChannelReq
     * @return
     */
    CurrentConsensusState queryChainStateByChannelId(QueryChannelReq queryChannelReq);



    /************* 节点监控相关接口结束***********************************************/


    /**
     *
     * @param queryChannelReq
     * @return
     */
    PeerOrganizationParams queryPeerOrganizationState(QueryChannelReq queryChannelReq);


}
