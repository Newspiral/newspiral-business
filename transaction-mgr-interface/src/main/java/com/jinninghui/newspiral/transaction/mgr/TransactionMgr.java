package com.jinninghui.newspiral.transaction.mgr;

import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.common.base.BaseTransHashResp;
import com.jinninghui.newspiral.common.entity.transaction.PooledTransaction;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import com.jinninghui.newspiral.common.entity.transaction.TransactionResp;

/**
 * @author lida
 * @date 2019/7/3 14:20
 */
public interface TransactionMgr {

    void processLocalPeerAddToChannel(Channel channel);

    /**
     * 添加Execute交易
     * transaction分为系统交易（即是系统自带的智能合约的调用）和业务交易
     * 系统交易会被更早的执行，即系统交易具有更高的执行优先级
     * @param transaction
     * @param fromSDK 是否从SDK添加交易
     * @return
     */
    BaseTransHashResp addTransaction(SDKTransaction transaction, boolean fromSDK);

    /**
     * 尝试添加交易，功能与addTransaction一直，不同点在于如果存在也返回正常的交易hash
     * @param transaction
     * @return
     */
    String tryAddTransaction(SDKTransaction transaction);
    /**
     * 删除交易内存池中的交易
     * @param hash
     */
    void removeTransaction(String hash, String channelId);


    /**
     * 处理区块共识成功，核心逻辑为从本地的交易池中删除所有该区块已经包含的交易
     * 共识成功是指成功持久化到数据库
     * @param consensusedBlock
     */
    void processBlockConsensusSuccess(Block consensusedBlock);

    /**
     * 交易池中取得特定交易
     * @param transHash
     * @param channelId
     * @return
     */
    PooledTransaction extractTransactionsByHash(String transHash, String channelId);

    /**
     * 根据通道id和clientTxId查询交易池中是否存在交易
     */
    public TransactionResp getTxInTxPoolByClientId(String callerChannelId, String clienTxId);

    /**
     * 根据通道id和交易hagetTxInTxPoolByTransHashsh查询交易池中是否存在交易
     */
    public TransactionResp getTxInTxPoolByTransHash(String callerChannelId,String transHash);

    void reloadLocalPeer();


}
