package com.jinninghui.newspiral.gateway;

import com.jinninghui.newspiral.common.entity.block.BlockDetailResp;
import com.jinninghui.newspiral.common.entity.block.BlockResp;
import com.jinninghui.newspiral.common.entity.chain.PeerChannelParams;
import com.jinninghui.newspiral.common.entity.chain.RemovePeerFromChannelRequest;
import com.jinninghui.newspiral.common.entity.common.base.BaseResponse;
import com.jinninghui.newspiral.common.entity.state.StateHistoryResp;
import com.jinninghui.newspiral.gateway.entity.datasource.AddDataSourceConfigReq;
import com.jinninghui.newspiral.common.entity.consensus.ConsensusContextTest;
import com.jinninghui.newspiral.common.entity.consensus.PeerConsensusStateResp;
import com.jinninghui.newspiral.common.entity.record.InterfaceRecord;
import com.jinninghui.newspiral.common.entity.record.InterfaceRecordSummary;
import com.jinninghui.newspiral.common.entity.record.PageInfo;
import com.jinninghui.newspiral.common.entity.state.StateHistoryResp;
import com.jinninghui.newspiral.common.entity.state.WorldStateResp;
import com.jinninghui.newspiral.common.entity.transaction.TransactionResp;
import com.jinninghui.newspiral.gateway.entity.datasource.AddDataSourceConfigReq;
import com.jinninghui.newspiral.gateway.entity.datasource.ModifyDataSourceConfigReq;
import com.jinninghui.newspiral.gateway.entity.datasource.ShardingRuleReq;
import com.jinninghui.newspiral.gateway.vo.req.AddMySelfToChannelReq;
import com.jinninghui.newspiral.gateway.vo.req.BlockHashReq;
import com.jinninghui.newspiral.gateway.vo.req.BlockHeightRegionReq;
import com.jinninghui.newspiral.gateway.vo.req.BlockHeightReq;
import com.jinninghui.newspiral.gateway.vo.req.CertDataReq;
import com.jinninghui.newspiral.gateway.vo.req.ChangeLoggerLevelReq;
import com.jinninghui.newspiral.gateway.vo.req.ChannelInitReq;
import com.jinninghui.newspiral.gateway.vo.req.ClassLimitParamReq;
import com.jinninghui.newspiral.gateway.vo.req.InterfaceRecordReq;
import com.jinninghui.newspiral.gateway.vo.req.IpConstraintReq;
import com.jinninghui.newspiral.gateway.vo.req.LimitParamReq;
import com.jinninghui.newspiral.gateway.vo.req.QueryLatestBlockListReq;
import com.jinninghui.newspiral.gateway.vo.req.QueryTransactionsByBlockHashReq;
import com.jinninghui.newspiral.gateway.vo.req.QueryWorldStateReq;
import com.jinninghui.newspiral.gateway.vo.req.RPCParam;
import com.jinninghui.newspiral.gateway.vo.req.SDKTransactionReq;
import com.jinninghui.newspiral.gateway.vo.req.StateHistoryReq;
import com.jinninghui.newspiral.gateway.vo.req.TransAlreadyConsensusByTimeReq;
import com.jinninghui.newspiral.gateway.vo.req.TransBlockHashReq;
import com.jinninghui.newspiral.gateway.vo.req.TransClientTxIdReq;
import com.jinninghui.newspiral.gateway.vo.req.TransHashReq;
import com.jinninghui.newspiral.gateway.vo.req.TransRegionBlockHashReq;
import com.jinninghui.newspiral.gateway.vo.req.UserCertDataReq;
import com.jinninghui.newspiral.gateway.vo.resp.CreateChannelResp;
import com.jinninghui.newspiral.gateway.vo.resp.PeerStateResp;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @author lida
 * @date 2019/7/15 16:27
 * 需要供SDK，也即是业务程序调用的接口
 */
public interface ServiceForSDK {

    /**
     * 系统Execute智能合约调用，该调用不论是否修改世界状态，均会上链
     *
     * @return 返回交易hash值
     */
    BaseResponse callExecuteSmartContract(SDKTransactionReq sdkTransactionReq);


    /**
     * 系统Query智能合约调用，该调用如果试图修改世界状态，会抛出异常
     *
     * @return
     */
    BaseResponse callQuerySmartContract(SDKTransactionReq sdkTransactionReq);


    /**
     * 业务Execute智能合约调用，该调用不论是否修改世界状态，均会上链
     *
     * @return 返回交易hash值
     */
    BaseResponse callExecuteBusinessSmartContract(SDKTransactionReq sdkTransactionReq);

    /**
     * 业务Query智能合约调用，该调用如果试图修改世界状态，会抛出异常
     *
     * @return
     */
    BaseResponse callQueryBusinessSmartContract(SDKTransactionReq sdkTransactionReq);

    /**
     * 创建一条新通道，返回通道ID
     *
     * @param channelInitReq
     * @return
     */
    BaseResponse<CreateChannelResp> createChannel(@Valid ChannelInitReq channelInitReq);

    /**
     * 将本节点加入到某个已经存在的通道，调用此接口需要保证节点管理平台已经收集到了足够多该通道内已有成员的approvol
     *
     * @param addMySelfToChannelReq
     */
    BaseResponse addMySelf2Channel(@Valid AddMySelfToChannelReq addMySelfToChannelReq);


    /**
     * remove peer from channel, with approval from enough members in the channel
     */
    BaseResponse removePeerFromChannel(RemovePeerFromChannelRequest removePeerFromChannelRequest);

    /**
     * 测试需要的上下文数据
     *
     * @param channelId
     * @return
     */
    BaseResponse<ConsensusContextTest> queryConsensusContextTest(String channelId);

    /**
     * 根据区块高度查询某个区块
     *
     * @param blockHeightReq
     * @return
     */
    BaseResponse<BlockResp> queryBlockByHeight(@Valid BlockHeightReq blockHeightReq);

    /**
     * 根据区块Hash查询某个区块
     *
     * @param blockHashReq
     * @return
     */
    BaseResponse<BlockResp> queryBlockByHash(@Valid BlockHashReq blockHashReq);

    /**
     * 根据区块高度区间查询某些区块信息
     *
     * @param blockHeightRegionReq
     * @return
     */
    BaseResponse<List<BlockResp>> queryBlockByHeightRegion(@Valid BlockHeightRegionReq blockHeightRegionReq);

    /**
     * 根据交易hash,查询区块信息
     *
     * @param transHashReq
     * @return
     */
    BaseResponse<BlockResp> queryBlockByTransHash(@Valid TransHashReq transHashReq);

    /**
     * 根据客户端交易号以及交易身份查询区块信息
     *
     * @param transClientTxIdReq
     * @return
     */
    BaseResponse<BlockResp> queryBlockByTransClientTxId(@Valid TransClientTxIdReq transClientTxIdReq);

    /**
     * 根据区块Hash查询某个区块完整信息
     *
     * @param blockHashReq
     * @return
     */
    BaseResponse<BlockDetailResp> queryWholeBlockByHash(@Valid BlockHashReq blockHashReq);

    /**
     * 根据区块高度查询某个区块完整信息
     *
     * @param blockHeightReq
     * @return
     */
    BaseResponse<BlockDetailResp> queryWholeBlockByHeight(@Valid BlockHeightReq blockHeightReq);

    /**
     * 根据交易hash,查询区块完整信息
     *
     * @param transHashReq
     * @return
     */
    BaseResponse<BlockDetailResp> queryWholeBlockByTransHash(@Valid TransHashReq transHashReq);

    /**
     * 根据客户端交易号以及交易身份查询区块完整信息
     *
     * @param transClientTxIdReq
     * @return
     */
    BaseResponse<BlockDetailResp> queryWholeBlockByTransClientTxId(@Valid TransClientTxIdReq transClientTxIdReq);

    /**
     * 查询最新的区块信息
     *
     * @param req
     * @return
     */
    BaseResponse<List<BlockResp>> queryLatestBlockList(@Valid QueryLatestBlockListReq req);

    /**
     * 根据key查询某个世界状态
     *
     * @param queryWorldStateReq
     * @return
     */
    BaseResponse<WorldStateResp> queryWorldState(QueryWorldStateReq queryWorldStateReq);


    /**
     * 根据该交易的创建者的身份标识和客户端交易ID查询某个交易
     *
     * @param transClientTxIdReq
     * @return
     */
    BaseResponse<TransactionResp> queryTransactionByTransClientTxId(@Valid TransClientTxIdReq transClientTxIdReq);

    /**
     * 根据该交易的哈希值查询某个交易
     *
     * @param transHashReq
     * @return
     */
    BaseResponse<TransactionResp> queryTransactionByTransHash(@Valid TransHashReq transHashReq);

    /**
     * 根据区块哈希和交易序号查询交易
     */
    BaseResponse<TransactionResp> queryTransByBlockHashAndTransIndex(@Valid TransBlockHashReq transBlockHashReq);

    /**
     * 根区块hash和交易index范围查询某个区块中的交易列表
     */
    BaseResponse<List<TransactionResp>> queryTransOfBlockByBlockHash(@Valid TransRegionBlockHashReq transRegionBlockHashReq);


    /**
     * 查看系统版本号
     *
     * @return 版本号
     */
    BaseResponse version(RPCParam RPCParam);

    /**
     * 停止节点
     */
    BaseResponse stopPeer(RPCParam RPCParam);

    /**
     * 获取节点状态
     *
     * @return
     */
    BaseResponse<PeerStateResp> getPeerCurrentState(RPCParam RPCParam);


    /**
     * 获取节点在某个通道的共识状态
     *
     * @return
     */
    BaseResponse<PeerConsensusStateResp> queryConsensesStateOfPeer(RPCParam RPCParam);


    /**
     * 查询某节点加入的通道列表
     *
     * @return
     */
    BaseResponse<List<PeerChannelParams>> queryAllChannelOfPeer(RPCParam RPCParam);

    /**
     * 查询某个通道信息
     *
     * @return
     */
    BaseResponse<PeerChannelParams> queryOneChannelOfPeer(RPCParam RPCParam);


    /**
     * 查询某个区块的所有交易
     *
     * @return
     */
    BaseResponse<List<TransactionResp>> getTransactionsByHash(QueryTransactionsByBlockHashReq queryTransactionsByBlockHashReq);

    /**
     * 删除某个通道
     *
     * @param RPCParam
     * @return
     */
    BaseResponse deleteChannel(RPCParam RPCParam);


    BaseResponse queryAuthList(RPCParam rpcParam);

    /**
     * 动态修改日志权限
     *
     * @param changeLoggerLevelReq
     * @return
     */
    BaseResponse changeLoggerLevel(ChangeLoggerLevelReq changeLoggerLevelReq);

    /**
     * 动态查询日志权限
     *
     * @return
     */
    BaseResponse<List<Map<String, String>>> queryLoggerLevel(RPCParam param);

    /**
     * 区块数据合法性检查
     */
    public BaseResponse<String> blockLegalCheck(BlockHeightReq blockHeightReq);

    /**
     * 区块数据合法性检查结果查询
     */
    public BaseResponse<String> queryBlockLegalCheckResult(BlockHeightReq blockHeightReq);

    /**
     * 查询接口调用详细记录
     *
     * @return
     */
    public BaseResponse<PageInfo<InterfaceRecord>> queryInterfaceRecord(InterfaceRecordReq interfaceRecordReq);

    /**
     * 查询接口调用总体情况
     *
     * @return
     */
    public BaseResponse<List<InterfaceRecordSummary>> queryInterfaceRecordSummary(RPCParam param);


    public BaseResponse refreshShardingConfig(RPCParam rpcParam);


    public BaseResponse addShardingDataSourceConfig(AddDataSourceConfigReq config);


    public BaseResponse modifySharidngRuleConfig(ShardingRuleReq req);


    public BaseResponse modifyShardingDataSourceConfig(ModifyDataSourceConfigReq req);


    public BaseResponse queryShardingConfig(RPCParam rpcParam);

    public BaseResponse<PageInfo<StateHistoryResp>> queryWorldStateHistory(StateHistoryReq stateHistoryReq);

    public BaseResponse queryChannelMaxBlockId(RPCParam rpcParam);

    /**
     * 按照方法名，批量修改限流参数
     *
     * @param limitParamReq
     * @return
     */
    public BaseResponse modifyLimitRequestParam(LimitParamReq limitParamReq);

    public BaseResponse modifyClassLimitRequestParam(ClassLimitParamReq classLimitParamReq);

    public BaseResponse<String> addIpIntoIpConstraintList(IpConstraintReq ipConstraintReq);

    public BaseResponse<String> modifyIpConstraintList(IpConstraintReq ipConstraintReq);

    public BaseResponse createRootCert(CertDataReq certDataReq);

    public BaseResponse createUserCert(UserCertDataReq certDataReq);

    /**
     * 根据时间范围提取已经共识的交易列表
     * 时间格式，eg: System.currentTimeMillis()
     *
     * @param transAlreadyConsensusByTimeReq
     * @return
     */
    public BaseResponse<List<TransactionResp>> queryTransAlreadyConsensusByTimeRegion(TransAlreadyConsensusByTimeReq transAlreadyConsensusByTimeReq);

}
