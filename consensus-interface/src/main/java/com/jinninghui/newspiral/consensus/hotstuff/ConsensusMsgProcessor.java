package com.jinninghui.newspiral.consensus.hotstuff;

import com.jinninghui.newspiral.common.entity.QueryPeerParam;
import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.chain.ChannelShort;
import com.jinninghui.newspiral.common.entity.chain.PeerCert;
import com.jinninghui.newspiral.common.entity.chain.PeerChannelParams;
import com.jinninghui.newspiral.common.entity.chain.PeerOrganizationParams;
import com.jinninghui.newspiral.common.entity.common.base.BaseResponse;
import com.jinninghui.newspiral.common.entity.consensus.BlockVoteMsg;
import com.jinninghui.newspiral.common.entity.consensus.ConsensusContextTest;
import com.jinninghui.newspiral.common.entity.consensus.ConsensusStageEnum;
import com.jinninghui.newspiral.common.entity.consensus.CurrentConsensusState;
import com.jinninghui.newspiral.common.entity.consensus.GenericMsg;
import com.jinninghui.newspiral.common.entity.consensus.NewViewMsg;
import com.jinninghui.newspiral.common.entity.consensus.PeerConsensusStateResp;
import com.jinninghui.newspiral.common.entity.consensus.PerformanceContext;
import com.jinninghui.newspiral.common.entity.consensus.QueryViewNoResp;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import com.jinninghui.newspiral.common.entity.transaction.TransactionResp;

import java.util.List;
import java.util.Set;

/**
 * @author lida
 * @date 2019/7/24 16:29
 * 不同的共识算法会有不同的消息，这个其实蛮恶心
 * 这里定义这个接口，其主要作用在于被consensus模块和gateway模块共同依赖而已
 */
public interface ConsensusMsgProcessor {

     void addGenericMsg(GenericMsg genericMsg);

    /**
     * 处理某个通道的NewView消息
     * @param newViewMsg
     * @param channelId
     */
     void addNewViewMsg(NewViewMsg newViewMsg, String channelId);


    /**
     * 处理新交易
     *
     * @param channelId
     */
    void addNewTransaction(String channelId, String transHash);

    /**
     * 处理某个通道的BlockVote消息
     * @param voteMsg
     */
     void addBlockVoteMsg(BlockVoteMsg voteMsg);

    /**
     * 处理本节点新加入一个通道
     * @param channel
     */
    void processLocalPeerAdd2Channel(Channel channel);

    /**
     * 处理本节点已在的某个通道发生变更，这个变更是已经达成共识的，例如新部署了智能合约，新增了节点
     */
    void processChannelUpdate(Channel channel);


    /**
     * 处理查询类交易
     * @param sdkTransaction
     * @return
     */
    Object processQueryTransaction(SDKTransaction sdkTransaction);

    /**
     * 查询某个通道当前的链共识状态数据
     * @param channelId
     * @return
     */
    CurrentConsensusState queryChainState(String channelId, Long blockHeightOfRequester);


    /**
     * 查询视图
     * @param channelId
     * @param blockHeightOfRequester
     * @return
     */
    QueryViewNoResp queryViewNo(String channelId, Long blockHeightOfRequester);

    /**
     * select current consensus state include (block view peerlist)
     * @param queryPeerReq
     * @return
     */
    CurrentConsensusState queryChainState(QueryPeerParam queryPeerReq);

    /**
     * 测试需要的上下文数据
     * @param channelId
     * @return
     */
    ConsensusContextTest queryConsensusContextTest(String channelId);

    /**
     *
     * @param peerCerts
     */
    void processPeerCertificateByPeerId(List<PeerCert> peerCerts, String channelId);

    /**
     * query Consensus Stage
     * @param channelId
     * @return
     */
    ConsensusStageEnum queryConsensusStage(String channelId);

    /**
     *
     */
    void removeAllChannel();

    List <ChannelShort> getChannelShortList();

    /**
     *
     * @param channelId
     * @return
     */
    PeerChannelParams  getPeerAddChannel(String channelId);

    /**
     *
     * @return
     */
    List<PeerChannelParams>  getPeerAddChannelList();

    PeerOrganizationParams queryPeerOrganizationState(String channelId);

    /**
     * 删除通道
     * @param channelId
     * @return
     */
    BaseResponse deleteChannel(String channelId);

    /**
     * 得到节点的共识状态
     * @param callerChannelId
     * @return
     */
    PeerConsensusStateResp getPeerConsensusState(String callerChannelId);

    /**
     * 得到在共识区块但还未共识成功的交易
     * @param callerChannelId
     * @param clienTxId
     * @return
     */
    public TransactionResp getTxInConsensusBlockByClientId(String callerChannelId, String clienTxId);

    /**
     * 根据channeld和交易hash得到共识缓存中的交易
     * @param callerChannelId
     * @param transHash
     * @return
     */
    public TransactionResp getTxInConsensusBlockByTransHash(String callerChannelId, String transHash);



    PerformanceContext getContextParameter(String channelId);


    void processPersistCacheTransHashs(Set<String> transHashs);


    void changeConsensusStage(String channelId, Long height, ConsensusStageEnum consensusStageEnum);

    void changeConsensusToNoAvailable(String channelId, Long height);

    void changeNoAvailableConsensus(String channelId, Long height, ConsensusStageEnum consensusStageEnum);

    void reloadLocalPeer();


}
