package com.jinninghui.newspiral.ledger.mgr;

import com.jinninghui.newspiral.common.entity.VerifiableData;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.block.BlockHeader;
import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.chain.ChannelSummary;
import com.jinninghui.newspiral.common.entity.chain.Peer;
import com.jinninghui.newspiral.common.entity.chain.PeerCert;
import com.jinninghui.newspiral.common.entity.chain.PeerCertificate;
import com.jinninghui.newspiral.common.entity.chain.PeerCertificateCipher;
import com.jinninghui.newspiral.common.entity.chain.PeerChannelRelation;
import com.jinninghui.newspiral.common.entity.common.base.BizVO;
import com.jinninghui.newspiral.common.entity.consensus.GenericMsg;
import com.jinninghui.newspiral.common.entity.identity.Identity;
import com.jinninghui.newspiral.common.entity.identity.IdentityKey;
import com.jinninghui.newspiral.common.entity.member.Member;
import com.jinninghui.newspiral.common.entity.member.Role;
import com.jinninghui.newspiral.common.entity.record.InterfaceRecord;
import com.jinninghui.newspiral.common.entity.record.InterfaceRecordBO;
import com.jinninghui.newspiral.common.entity.record.InterfaceRecordSummary;
import com.jinninghui.newspiral.common.entity.record.PageInfo;
import com.jinninghui.newspiral.common.entity.smartcontract.QuerySmartContractListReq;
import com.jinninghui.newspiral.common.entity.smartcontract.QuerySmartContractReq;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContract;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContractModelKeyReq;
import com.jinninghui.newspiral.common.entity.state.StateHistoryBO;
import com.jinninghui.newspiral.common.entity.state.StateHistoryModel;
import com.jinninghui.newspiral.common.entity.state.StateHistoryResp;
import com.jinninghui.newspiral.common.entity.state.WorldState;
import com.jinninghui.newspiral.common.entity.state.WorldStateResp;
import com.jinninghui.newspiral.common.entity.task.Task;
import com.jinninghui.newspiral.common.entity.transaction.Transaction;
import com.jinninghui.newspiral.common.entity.transaction.TransactionCompile;
import com.jinninghui.newspiral.common.entity.transaction.TransactionResp;
import com.jinninghui.newspiral.common.entity.version.VersionResp;


import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lida
 * @date 2019/7/5 18:45
 * 所有账本管理模块的需要被其他模块访问的接口均在此接口中定义
 */
public interface LedgerMgr {

    /**
     * 检查是否存在相应交易
     *
     * @param transHash
     * @param channelId
     * @return
     */
    boolean hasTrans(String transHash, String channelId);

    /**
     * 查询某个通道的区块链概要信息，如果没有该链，抛出异常
     * 仅基于本地已同步的数据处理，因此如果一条新建的链在本节点尚未完成数据同步，可能会给出非最新值或直接抛出“不存在该链”的异常
     *
     * @return
     */
    ChannelSummary queryChannelSummary(String channelId);

    /**
     * 读取本节点所有已经加入的通道
     *
     * @return
     */
    List<Channel> readAllChannels();

    /**
     * 通道缓存，如果读不到，则用queryChannel进行查询
     *
     * @param channelId
     * @return
     */
    Channel getChannel(String channelId);

    /**
     * 从数据库读channel，需要节点加入通道
     * 如果根据channelId没有查到，则返回null
     *
     * @param channelId
     * @return
     */
    Channel queryChannel(String channelId);

    /**
     * 插入一个新的通道，如果channelID指定的通道已经存在，抛出异常
     */
    void insertChannel(Channel channel);

    /**
     * 更新某个通道，如果channelId指定的通道不存在，抛出异常
     *
     * @param channel
     */
    void updateChannel(Channel channel);


    /**
     * 获得某个通道的某个配置项，以Stirng型返回
     *
     * @param key
     * @param channelId
     * @return
     */
    String getChannelConfigValue(String key, String channelId);

    /**
     * 获得某个通道的某个配置项，以Long型返回
     */
    Long getChannelConfigLongValue(String key, String channelId);


    /**
     * 插入缓存区块
     *
     * @param channelId
     * @param block
     * @return
     */
    void insertCacheBlock(String channelId, Block block);


    /**
     * 根据区块高度查询某个缓存区块
     *
     * @param channelId
     * @param blockHeight
     * @return
     */
    List<Block> queryCacheBlock(String channelId, Long blockHeight);


    /**
     * 根据区块Hash值查询某个缓存区块
     *
     * @param channelId
     * @param blockHashStr
     * @return
     */
    Block queryCacheBlock(String channelId, String blockHashStr);


    /**
     * 删除缓存区块
     *
     * @param channelId
     */
    void deleteCachedBlock(String channelId, Long height);


    void deleteCachedBlockBehind(String channelId, Long height);

    List<WorldState> selectAllState(String channelId);

    /**
     * Delete committed block
     * This only happen when the data of local peer is broken.
     *
     * @param channelId
     * @param height
     */
    void deleteCommittedBlock(String channelId, Long height);


    /**
     * 根据区块高度查询某个区块
     *
     * @param channelId
     * @param blockHeight
     * @return
     */
    Block queryBlock(String channelId, Long blockHeight);

    /**
     * 根据区块Hash查询某个区块
     *
     * @param channelId
     * @param blockHash
     * @return
     */
    Block queryBlock(String channelId, String blockHash);


    /**
     * 根据通道id查询num个最新的区块
     *
     * @param channelId 通道id
     * @param num       查询的数量
     * @return
     */
    List<Block> queryBlock(String channelId, Integer num);


    /**
     * 只查询区块头
     *
     * @param channelId
     * @param blockHash
     * @return
     */
    BlockHeader queryBlockHeader(String channelId, String blockHash);

    /**
     * 根据key查询某个世界状态
     *
     * @param channelId
     * @param key
     * @return
     */
    WorldState queryWorldState(String channelId, String key, boolean flag);

    /**
     * 根据该交易的创建者的身份标识和客户端交易ID查询某个交易
     * 如果该交易尚未成功打包，则返回的ExecutedTransaction中的executeTimestamp等值为null
     *
     * @param clientIdentitykey
     * @param clientTxId
     * @return
     */
    Transaction queryTransaction(String clientIdentitykey, String clientTxId, String channelId);

    /**
     * 根据该交易的哈希值查询某个交易
     * 如果该交易尚未成功打包，则返回的ExecutedTransaction中的executeTimestamp等值为null
     *
     * @param txHash
     * @return
     */
    Transaction queryTransaction(String txHash, String channelId);


    /**
     * 根据该交易的哈希值查询某个交易
     *
     * @param txHash
     * @param channelId
     * @return
     */
    TransactionResp queryTransactionResp(String txHash, String channelId);


    List<Transaction> queryTxHistory(String channelId, String key);

    /**
     * 根据区块Hash和交易在区块中的序号查询交易，txIndex从1开始
     *
     * @param blockHash
     * @param txIndex
     * @return
     */
    Transaction queryTransaction(String blockHash, Integer txIndex, String channelId);

    void cacheTxShort(Block block);

    boolean ifTxExist(String hash);

    /**
     * 获取交易列表
     *
     * @param blockHash
     * @param channelId
     * @return
     */
    List<Transaction> queryTransactionList(String blockHash, String channelId);

    /**
     * 根据区块中的交易索引获取交易列表
     *
     * @param blockHash
     * @param channelId
     * @return
     */
    List<Transaction> queryTransactionByBlockHashAndTxRegion(String blockHash, String channelId, Integer txIndexFrom, Integer txIndexTo);

    /**
     * 根据客户端交易Id来查询交易
     *
     * @param channelId
     * @param clientId
     * @param clientIdentitykey
     * @return
     * @author whj
     */
    Transaction getTransByClientId(String channelId, String clientId, String clientIdentitykey);

    /**
     * 持久化Block
     * witness为证明该Block已经达成共识的证据，对于NewSpiralBFT，就是QC
     * witness放到Block中，但是不参与Hash计算，因为是先生成BLock再得到witness的，放进去会破坏Block的Hash
     *
     * @param block
     */
    void persistBlock(Block block);


    /**
     * 查询所有本地持久化的身份
     *
     * @return
     */
    List<Identity> queryAllIdentities();

    /**
     * 查询所有本地存储的节点数据
     *
     * @return
     */
    List<Peer> queryAllPeers();

    /**
     * 查询在高度为height的区块被commit之后得到的有效节点列表
     *
     * @param channelId
     * @param height
     * @return
     */
    List<Peer> queryPeersOfChannelByHeight(String channelId, Long height);

    /**
     * 查询peer_channel表中最新的一条记录
     * @param peerId
     * @param channelId
     * @return
     */
    PeerChannelRelation queryPeerByPeerAndChannelId(String peerId, String channelId);

    /**
     * 查询所有节点列表
     *
     * @param channelId
     * @return
     */
    List<Peer> queryPeersOfChannel(String channelId);

    /**
     * 根据时间范围提取已经共识的交易列表
     * 时间格式为, eg: System.currentTimeMillis()
     *
     * @param channelID
     * @param startTime
     * @param endTime
     * @return
     */
    List<TransactionResp> queryTransAlreadyConsensusByTimeRegion(String channelID, Long startTime, Long endTime);

    /**
     * 查询本地节点
     *
     * @return
     */
    Peer queryLocalPeer();

    /**
     * 插入一个新的Task对象
     *
     * @param persistTask
     */
    void insertTask(Task persistTask);

    /**
     * 使用入参更新对应的Task记录，返回更新的记录数
     *
     * @param task
     */
    int updateTask(Task task);

    /**
     * 查询未持久化任务记录
     *
     * @return
     */
    List<Task> getTaskListByStatus();

    /**
     * 获得本地Peer的身份
     *
     * @return
     */
    Identity queryLocalIdentity();

    /**
     * 查询本通道内的所有世界状态
     */
    List<WorldState> queryAllWorldState(String channelId, Long from, Long to);

    /**
     * 根据通道Id和高度范围查询区块列表
     */
    List<Block> queryBlockByChannelIdAndHeightRegion(String channelId, Long from, Long to);


    /******************** StateStorage 的接口开始**************************/
    byte[] getState(String key);

    void insertState(String key, byte[] value);

    void updateState(String key, byte[] newValue);

    /**
     * 设置key指定的状态的value为输入的value，如果不存在则新增，如果存在则更新
     *
     * @param key
     * @param value
     * @param value
     */
    void putState(String key, byte[] value);

    boolean existKey(String key);

    /**
     * 删除成功，返回true，否则返回false
     *
     * @param key
     * @return
     */
    boolean deleteKey(String key);

    byte[] queryState(String key);
/******************** StateStorage 的接口结束**************************/

/******************** 密钥 的接口开始**************************/
    /**
     * 节点对应渠道的私钥集
     * key1为peerValue，key2为channelId
     *
     * @return
     */
    Map<String, Map<String, String>> queryPeerPrivateKeyMap();


/******************** 密钥 的接口结束**************************/


    /**
     * 更新节点私钥,channelId、peerId、userPrivateKey必须要有
     *
     * @param peer
     */
    void updateChannelPeerPrivateKey(Channel channel, List<Peer> peer);

    /*********************genericMsg cache interfaces begins**********************/

    void insertGenericMsg(String channelId, GenericMsg genericMsg);

    void deleteGenericMsg(String channelId, Long viewNo);

    Map<Long, GenericMsg> selectGenericMsg(String channelId, Long viewNo);

    /*********************genericMsg cache interfaces ends**********************/
    /**
     * select peer identity by primarykey
     *
     * @param identityKey
     * @return
     */
    Identity getIdentityByPrimaryKey(IdentityKey identityKey);

    /**
     * select peer by primarykey
     *
     * @param peerIdValue
     * @return
     */
    Peer getPeerByPrimaryKey(String peerIdValue, String channleId);

    /**
     * 查询该交易的
     *
     * @param channelId
     * @param transHash
     * @param clientTxId
     * @return
     */
    Transaction getTransBlockHash(String channelId, String transHash, String peerId, String clientTxId);

    /**
     * query peer Certificate
     *
     * @return
     */
    Map<String, PeerCertificateCipher> queryPeerCertificateMap();

    /**
     * @param peerId
     * @return
     */
    PeerCertificateCipher queryPeerCertificateByPeerId(String peerId);

    /**
     * query peer Certificate
     *
     * @return
     */
    Map<String, Map<String, PeerCertificateCipher>> querypeerChannelCertificateCipherMap();

    /**
     * @param peer
     * @return
     */
    void updatePeerCertificateByPrimaryKey(Peer peer, String channelId);


    /**
     * 根据memberId来获取该成员的角色
     *
     * @param memberId
     * @return
     */
    List<Role> getRolesOfMember(Long memberId);

    /**
     * @param channelId 根绝通道Id和角色名称获取所有成员
     * @param roleName
     * @return
     */
    List<Member> getMembersOfRole(String channelId, String roleName);

    /**
     * 查询智能合约记录 数据库
     *
     * @return
     */
    SmartContract getSmartContractByKey(SmartContractModelKeyReq smartContractModelKey);


    /**
     * 根据节点身份获取证书
     *
     * @param identityKey
     * @return
     */
    PeerCertificate getPeerCertificate(IdentityKey identityKey, String channelId);

    /**
     * 根据peerId 和 channelId获取查询是否存在有效的节点证书
     * @param peerId
     * @param channelId
     * @return
     */
    PeerCert getPeerCertificate(String peerId,String channelId);

    /**
     * 修改节点证书状态
     *
     * @param peerCertificate
     * @param flag
     * @param channelId
     */
    void updatePeerCertificateState(PeerCertificate peerCertificate, String flag, String channelId);


    /**
     * 校验节点证书是否重复
     *
     * @param certificateCerFile
     * @return
     */
    boolean verifyPeerCertificateRepeat(byte[] certificateCerFile, String channelId);

    /**
     * 根据节点Id获取证书
     *
     * @param peerId
     * @return
     */
    List<PeerCert> getPeerCertList(String peerId, String channelId);

    /*
    缓存数据部分接口
     */
    void updateChannelCache(Channel channel);

    /**
     * @param o
     * @return
     */
    String getHash(Object o);

    /**
     * 删除整个通道信息
     *
     * @param channelId
     */
    void deleteChannelData(String channelId);

    /**
     * 删除整个通道的信息，但是不删除该通道的交易和区块信息
     */
    void deleteChannelBasicData(String channelId);

    /**
     * 删除区块、交易、世界状态等业务数据
     */
    void deleteBusinessDataByChannelId(String channelId);

    /**
     * 根据区块高度删除区块、交易、世界状态等业务数据
     *
     * @param height
     */
    void deleteBusinessDataByBlockHeight(String channelId, Long height);

    /**
     * 处理无效证书
     *
     * @param peers
     * @param members
     */
    void processInvalidCertificates(List<Peer> peers, List<Member> members);

    /**
     * 透传验签逻辑
     *
     * @param verifiableData
     * @param publicKeyStr
     * @return
     */
    boolean verifySign(VerifiableData verifiableData, String publicKeyStr);


    /**
     * 通过证书解析成员
     *
     * @param member
     * @return
     */
    boolean processMemberCertificate(Member member);

    /**
     * @param vo
     * @return
     */
    BizVO getSmartContractAllList(QuerySmartContractListReq vo);

    /**
     * @param approval
     * @return
     */
    SmartContract getSmartContractInfo(QuerySmartContractReq approval);


    /**
     * @param approval
     * @return
     */
    SmartContract getActiveSmartContract(QuerySmartContractReq approval);

    /**
     * 根据时间区间 查询账本列表
     *
     * @param channelId
     * @param startTime
     * @param endTime
     * @return
     */
    List<WorldStateResp> queryStatesByTimeRegion(String channelId, Long startTime, Long endTime);


    List<TransactionResp> queryStatesHistory(String channelId, Long startTime, Long endTime, String key);

    /**
     * 查询本地节点信息
     */
    int getTransactionCount(String channelId);

    int getTransactionCountByHeight(String channelId, Long height);

    void persistInterfaceRecord(List<InterfaceRecord> interfaceRecords, List<InterfaceRecordSummary> interfaceRecordSummaryList);

    void persistInterfaceRecordSummary(List<InterfaceRecordSummary> interfaceRecordSummaryList);

    PageInfo<InterfaceRecord> queryInterfaceRecord(InterfaceRecordBO interfaceRecordBO);

    List<InterfaceRecordSummary> queryInterfaceRecordSummary();

    void cleanUpRecord(String beforeDayTime);

    void insertSmartContract(SmartContract sc);

    Long queryMaxBlockHightByChannelId(String channelId);

    void insertLatestBlockIntoCachedBlock(String channelId, Integer count);

    Transaction selectByClientTransId(String channelId, String clientTransId);

    /**
     * 添加新的预编译智能合约
     *
     * @param txCompileID
     * @param txCompile
     */
    void setTransactionCompile(String txCompileID, TransactionCompile txCompile);

    /**
     * 获取SmartContractInfo.toString对应的预交易信息
     *
     * @param txCompileID
     * @return
     */
    TransactionCompile getTransactionCompile(String txCompileID);

    Set<String> getServiceUrlList();

    /**
     * 将transaction转化为transactionResp
     *
     * @param transaction
     * @return
     */
    TransactionResp transferTransactionResp(Transaction transaction);

    /**
     * 将transactionlist转化为transactionRespList
     *
     * @param transactionList
     * @return
     */
    List<TransactionResp> transferTransactionRespList(List<Transaction> transactionList);

    Map<String, Long> queryChannelMaxBlockId();

    PageInfo<StateHistoryResp> queryStateHistory(StateHistoryBO stateHistoryBO);

    void deleteLatestBlockData(String channelId);

    Map<Long, List<StateHistoryModel>> queryStateHistoryList(String channelId, Long fromBlockId, Long toBlockId);

    Long queryLatestBlockId(String channelId);

    void persisitStateHistory(List<StateHistoryModel> batchInsertList);

    Long queryIfExistBlocksAfterFromBlock(String channelId, Long fromBlockId);

    Long queryBlockTableLatestBlockId(String channelId);

    VersionResp queryLatestSystemVersion();

    PeerChannelRelation selectLatestRecordByPeerIdAndChanneId(String value, String channelId,String actionType);
}
