package com.jinninghui.newspiral.ledger.mgr.impl.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.TransactionModelExpandStateHistory;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.TransactionModelWithBLOBs;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 不需要的各种Delete/update接口都删除
 */
public interface TransactionModelMapper {

    @DS("sharding")
    int insert(@Param("record") TransactionModelWithBLOBs record);

    int insertCache(@Param("record") TransactionModelWithBLOBs record);

    @DS("sharding")
    int batchInsert(@Param("list") List<TransactionModelWithBLOBs> list);

    int batchInsertCache(@Param("list") List<TransactionModelWithBLOBs> list);

    @DS("sharding")
    List<TransactionModelWithBLOBs> selectAll(@Param("channelId") String channelId);

    @DS("sharding")
    TransactionModelWithBLOBs selectByPrimaryKey(@Param("channelId") String channelId, @Param("roleId") Long id);

    /**
     * 输入16进制字符串标识的区块Hash
     * @param blockHash
     * @return
     */
    @DS("sharding")
    List<TransactionModelWithBLOBs> selectByBlockHash(@Param("channelId") String channelId,@Param("blockHash") String blockHash);

    List<TransactionModelWithBLOBs> selectCacheByBlockHash(@Param("channelId") String channelId,@Param("blockHash") String blockHash);
    /**
     * 输入客户指定的TxID
     * @param clientIdentityKey
     * @param clientTxId
     * @return
     */
    @DS("sharding")
    TransactionModelWithBLOBs selectByClientTxId(@Param("clientIdentityKey") String clientIdentityKey,@Param("clientTxId") String clientTxId,@Param("channelId") String channelId);

    /**
     *
     * @param clientTransId
     * @param channelId
     * @return
     */
    @DS("sharding")
    TransactionModelWithBLOBs selectByClientTransId(@Param("clientTransId") String clientTransId,@Param("channelId") String channelId);

    @DS("sharding")
    TransactionModelWithBLOBs selectByTxHash(@Param("channelId") String channelId,@Param("txHashStr") String txHashStr);

    /**
     * 查询区块BlockHash指定的区块中序号为txIndex的交易，txIndex从1开始
     * @param blockHash
     * @param txIndex
     * @return
     */
    @DS("sharding")
    TransactionModelWithBLOBs selectByBlockHashAndIndex(@Param("channelId") String channelId,@Param("blockHash")String blockHash,@Param("txIndex") Integer txIndex);

    void deleteCacheTransactionByBlockHash(@Param("channelId") String channelId, @Param("blockHash") String blockHash);

    void deleteCacheTransactionByChannelId(@Param("channelId") String channelId);
    @DS("sharding")
    void deleteTransactionByChannelId(@Param("channelId") String channelId);
    @DS("sharding")
    List<TransactionModelWithBLOBs> selectAllByKey(@Param("channelId") String channelId,@Param("key") String key);
    @DS("sharding")
    List<TransactionModelWithBLOBs> selectByBlockHashAndIndexRegion(@Param("channelId") String channelId,@Param("blockHash") String blockHash, @Param("txIndexFrom") Integer txIndexFrom,@Param("txIndexTo") Integer txIndexTo);
    @DS("sharding")
    int countTxByHash(@Param("channelId") String channelId, @Param("blockHash") String blockHash);
    @DS("sharding")
    int countTx(@Param("channelId") String channelId);

    /**
     * 根据客户端交易Id查询交易
     * @return
     * @param channelId
     * @param clientId
     * @param clientIdentitykey
     */
    @DS("sharding")
    TransactionModelWithBLOBs selectTransByClientId(@Param("channelId") String channelId,@Param("clientId") String clientId,@Param("clientIdentitykey") String clientIdentitykey);

    @DS("sharding")
    List<TransactionModelWithBLOBs> selectByCreateTimestamp(@Param("channelId") String channelId, @Param("startTime") String startTime, @Param("endTime") String endTime);

    void deleteCacheTransactionByBlockHeight(@Param("channelId") String channelId, @Param("blockHash") String blockHash);

    @DS("sharding")
    void deleteTransactionByBlockHeight(@Param("channelId") String channelId, @Param("blockHash") String height);

    @DS("sharding")
    List<TransactionModelExpandStateHistory> selectTransListByBlockIdRegion(@Param("channelId")String channelId, @Param("blockId") Long blockId);
}
