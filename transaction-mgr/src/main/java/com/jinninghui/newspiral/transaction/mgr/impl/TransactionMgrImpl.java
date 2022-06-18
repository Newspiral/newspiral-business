package com.jinninghui.newspiral.transaction.mgr.impl;


import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.chain.Peer;
import com.jinninghui.newspiral.common.entity.chain.PeerCert;
import com.jinninghui.newspiral.common.entity.common.base.BaseTransHashResp;
import com.jinninghui.newspiral.common.entity.common.base.NewspiralStateCodes;
import com.jinninghui.newspiral.common.entity.consensus.ConsensusStageEnum;
import com.jinninghui.newspiral.common.entity.exception.NewSpiralErrorEnum;
import com.jinninghui.newspiral.common.entity.exception.NewspiralException;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.common.entity.transaction.TransactionResp;
import com.jinninghui.newspiral.common.entity.transaction.TxStateEnum;
import com.jinninghui.newspiral.common.entity.transaction.PooledTransaction;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import com.jinninghui.newspiral.consensus.hotstuff.ConsensusMsgProcessor;
import com.jinninghui.newspiral.ledger.mgr.LedgerMgr;
import com.jinninghui.newspiral.ledger.mgr.MemberLedgerMgr;
import com.jinninghui.newspiral.p2p.P2pClient;
import com.jinninghui.newspiral.p2p.hotstuff.HotstuffRPCInterface;
import com.jinninghui.newspiral.security.DataSecurityMgr;
import com.jinninghui.newspiral.security.SecurityServiceMgr;
import com.jinninghui.newspiral.transaction.mgr.TransactionMgr;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lida
 * @date 2019/7/3 17:32
 */
@Slf4j
public class TransactionMgrImpl implements TransactionMgr {

    @SofaReference
    private SecurityServiceMgr securityServiceMgr;
    @SofaReference
    private DataSecurityMgr dataSecurityMgr;

    @SofaReference
    private HotstuffRPCInterface rpcInterface;

    @SofaReference
    private P2pClient p2pClient;

    @SofaReference
    private LedgerMgr ledgerMgr;
    @SofaReference
    private MemberLedgerMgr memberLedgerMgr;
    @SofaReference
    private ConsensusMsgProcessor consensusMsgProcessor;
    @Autowired
    private AsyncTransactionTask asyncTransactionTask;

    static volatile int count = 0;
    /**
     * localPeer
     */
    Peer localPeer;
    /**
     * key为channelId
     */
    ConcurrentHashMap<String, TransactionPool> transactionPoolMap = new ConcurrentHashMap<>();


    /**
     * init
     */
    public void init() {
        localPeer = ledgerMgr.queryLocalPeer();
    }

    /**
     * logPreFix of transaction
     *
     * @param transaction
     * @return
     */
    private String txLogPreFix(SDKTransaction transaction, Enum state) {
        return "TxState" + state + "clientId:" + transaction.getClientTxId() + ",channelId：" + transaction.getChannelId() + ",transHash" + transaction.getHash() + ",";
    }

    /**
     * 支持幂等
     * 本地节点成功加入一个通道后，需调用此方法
     * 这个方法加个synchronized不会影响性能，因为调用不会频繁
     *
     * @param channel
     */
    @Override
    public void processLocalPeerAddToChannel(Channel channel) {
        synchronized (this) {
            if (transactionPoolMap.get(channel.getChannelId()) != null) {
                return;
            } else {
                TransactionPool pool = new TransactionPool(channel.getChannelId(), channel.getMaxTransactionCount());
                pool.setSecurityService(securityServiceMgr.getMatchSecurityService(channel.getSecurityServiceKey()));
                transactionPoolMap.putIfAbsent(channel.getChannelId(), pool);
                log.info(ModuleClassification.TxM_TMI_ + "为通道" + channel.getChannelId() + "创建交易池成功");
            }
        }
    }

    /**
     * 尝试添加交易，功能与addTransaction一直，不同点在于如果存在也返回正常的交易hash
     *
     * @param transaction
     * @return
     */
    public String tryAddTransaction(SDKTransaction transaction) {
        if (!isNormalPeer(transaction)) {
            log.info("非正常节点，不允许尝试添加交易");
            return null;
        }

        TransactionPool transactionPool = getMatchPool(transaction.getChannelId());
        if (transactionPool != null && false == transactionPool.canAccept()) {
            log.info("交易所在通道不存在或者不允许添加交易，尝试添加交易失败");
            return null;
        }

        String transacitonHash = null;
        if (!transactionPool.hasAlreadyAddTranscation(transaction)) {
            broadcastTranscation(transaction, false);
            transacitonHash = transactionPool.addTransaction(transaction);
        } else {
            transacitonHash = transaction.getHash();
        }
        return transacitonHash;
    }

    @Override
    public BaseTransHashResp addTransaction(SDKTransaction transaction, boolean fromSDK) {

        if (!isNormalPeer(transaction)) {
            return new BaseTransHashResp(null,NewspiralStateCodes.SYSTEM_PEER_NOTNORMAL);
        }
        // 如果交易池中无此交易，则执行
        TransactionPool transactionPool = getMatchPool(transaction.getChannelId());
        if (transactionPool != null && false == transactionPool.canAccept()) {
            log.info(ModuleClassification.TxM_TP_.toString() + " up to maximum of transaction pool");
            return new BaseTransHashResp(null,NewspiralStateCodes.SYSTEM_TRANSACTION_POOL_MAXIMUM);
        }


        if (!transactionPool.hasAlreadyAddTranscation(transaction)) {
            //TODO 测评防篡改hash比对工作
            if (!fromSDK) {
                String bytesHash = transaction.getHash();
                dataSecurityMgr.hash(transaction);
                if (!transaction.getHash().equals(bytesHash)) {
                    log.error("TransactionMgrImpl.addTransaction,forward transaction data may be modified");
                    return new BaseTransHashResp(null,NewspiralStateCodes.SYSTEM_TRANSACTION_DATA_MODIFIED);
                }
            }

            broadcastTranscation(transaction, fromSDK);
            /*count++;
            if (count > 50000) {
                log.info("Repeated tx");
            }*/
            String trasHash = transactionPool.addTransaction(transaction);
            if(!StringUtils.isEmpty(trasHash)) {
                return new BaseTransHashResp(trasHash, NewspiralStateCodes.SUCCESS);
            }else
            {
                return new BaseTransHashResp(null,NewspiralStateCodes.SYSTEM_TRANSACTION_DATA_REPEAT);
            }
        }
        log.warn(txLogPreFix(transaction, TxStateEnum.TX_POOLED) + ModuleClassification.TxM_TMI_ + "TransactionMgrImpl.addTransaction,same transaction data transaction={}", JSONObject.toJSON(transaction));
        return new BaseTransHashResp(null,NewspiralStateCodes.SYSTEM_TRANSACTION_DATA_REPEAT);
    }

//    /**
//     * 把预编译功能放到最外层的这里,这里做预编译有可能会导致部份逻辑没有最终执行预编译
//     *
//     * @param transaction
//     * @param fromSDK     是否从SDK添加交易
//     * @return
//     */
//    @Override
//    public String addTransaction(SDKTransaction transaction, boolean fromSDK) {
//
//        if (!isNormalPeer(transaction)) {
//            return null;
//        }
//        // 如果交易池中无此交易，则执行
//        TransactionPool transactionPool = getMatchPool(transaction.getChannelId());
//        if (transactionPool != null && false == transactionPool.canAccept()) {
//            log.info(ModuleClassification.TxM_TP_.toString() + " up to maximum of transaction pool");
//            return null;
//        }
//
//        String trasHash = null;
//        if (!transactionPool.hasAlreadyAddTranscation(transaction)) {
//            broadcastTranscation(transaction, fromSDK);
//            trasHash = transactionPool.addTransaction(transaction);
//        }
//
//        // 执行预编译，只针对部署智能合约类型(methodArgs为 ArrayListl类型)，同时调用的是deploySmartContract，其他执行智能合约函数的交易类型可以直接略过
//        try {
//            long startTime = System.currentTimeMillis();
//            if ((transaction.getSmartContractCallInstnace().getMethodArgs()[0] instanceof ArrayList)
//                    && (transaction.getSmartContractCallInstnace().getMethodName().equals("deploySmartContract"))) {
//                SmartContractInfo smartContractInfo = ((List<SmartContractDeployToken>) (transaction.getSmartContractCallInstnace().getMethodArgs()[0])).get(0).getSmartContractInfo();
//                if (this.ledgerMgr.getTransactionCompile(smartContractInfo.toString()) == null) {
//                    SmartContract smartContractCopy = SmartContract.createInstance(smartContractInfo);
//                    new SmartContractCompile().compileSmartContract(smartContractCopy);
//                    smartContractCopy.setClassFileHash(this.dataSecurityMgr.getHash(Base64.getEncoder().encodeToString(smartContractCopy.getClassFileBytes())));
//                    this.ledgerMgr.setTransactionCompile(smartContractInfo.toString(), new TransactionCompile(transaction, smartContractCopy));
//                    log.info("预编译智能合约耗时：{}", System.currentTimeMillis() - startTime);
//                }
//            }
//        } catch (Exception ex) {
//            log.error("执行预编译失败，交易仍会被正常收录，将在交易执行时重新进行编译；有可能交易不是部署智能合约类型，如果是部署智能合约类型，则有可能会因编译耗时导致交易超时而执行失败：{}", ex.toString());
//        }
//
//        if (trasHash == null) {
//            log.warn(txLogPreFix(transaction, TxStateEnum.TX_POOLED) + ModuleClassification.TxM_TMI_ + "TransactionMgrImpl.addTransaction,same transaction data transaction={}", JSONObject.toJSON(transaction));
//        }
//        return trasHash;
//    }


    @Override
    public void removeTransaction(String hash, String channelId) {
        if (transactionPoolMap.get(channelId) != null) {
            transactionPoolMap.get(channelId).removeTransaction(hash);
        }
    }

    public void broadcastTranscation(SDKTransaction sdkTransaction, boolean flag) {
        if (!flag) return;
        HashSet<Peer> peers = new HashSet<>();
        peers.add(localPeer);
        rpcInterface.broadcastTranscation(sdkTransaction, sdkTransaction.getChannelId(), peers, judgeBroadcasetScopeByFromSDK(flag));
    }

    /**
     * peer cert valid
     *
     * @param peerCerts
     * @return
     */
    private boolean locationPeerCertValid(List<PeerCert> peerCerts) {
        for (PeerCert peerCert : peerCerts) {
            if ("0".equals(peerCert.getFlag())) {
                return true;
            }
        }
        return false;
    }

    private HotstuffRPCInterface.BroadcastScope judgeBroadcasetScopeByFromSDK(boolean fromSDK) {
        if (fromSDK) {
            return HotstuffRPCInterface.BroadcastScope.Channel;
        } else {
            return HotstuffRPCInterface.BroadcastScope.TwentyFivePercentage;
        }
    }


    /**
     * 如果找不到，会抛出异常
     *
     * @param channelId
     * @return
     */
    private TransactionPool getMatchPool(String channelId) {
        TransactionPool matchPool = transactionPoolMap.get(channelId);
        if (matchPool == null) {
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "没有输入交易对应的通道的交易池,channelID:" + channelId);
        }
        return matchPool;
    }


    /**
     * @param transaction
     * @return
     */
    private boolean isNormalPeer(SDKTransaction transaction) {
        Channel channel = ledgerMgr.getChannel(transaction.getChannelId());
        boolean isPeerState = false;
        for (Peer peer : channel.getMemberPeerList()) {
            if (peer.equals(localPeer)) {
                isPeerState = peer.isState();
                break;
            }
        }
        //同步状态过滤交易、节点冻结状态过滤交易
        ConsensusStageEnum consensusStageEnum = consensusMsgProcessor.queryConsensusStage(transaction.getChannelId());
        if (null == consensusStageEnum) {
            log.info("null");
        } else if (consensusStageEnum.equals(ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL)) {
            log.info("wait sync");
        }
        if (isPeerState == false) {
            log.info("state false");
        }
        if (null == consensusStageEnum ||
                consensusStageEnum.equals(ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL) == true ||
                isPeerState == false) {
            //判断是否是证书操作(包括节点状态修改)，如果不是则返回
            if (!transaction.getSmartContractCallInstnace().getMethodName().contains("updatePeerCertificate")) {
                log.warn(ModuleClassification.TxM_TMI_ + "TransactionMgrImpl.isValdatePeer,ConsensusStage is WAIT_SYNC_WITH_CHANNEL or peer is invalid");
                return false;
            }
        }
        return true;

    }

    @Override
    public void processBlockConsensusSuccess(Block consensusedBlock) {
        getMatchPool(consensusedBlock.getBlockHeader().getChannelId())
                .processBlockConsensusSuccess(consensusedBlock);
    }


    @Override
    public PooledTransaction extractTransactionsByHash(String transHash, String channelId) {
        return getMatchPool(channelId).getTransactionByHash(transHash);
    }


    /**
     * 根据通道id和clientTxId查询交易池中是否存在交易
     */
    public TransactionResp getTxInTxPoolByClientId(String callerChannelId, String clienTxId) {
        TransactionResp transactionResp = null;
        try {
            transactionResp = new TransactionResp();
            PooledTransaction pooledTransaction = null;
            //根据交易拿到交易池
            TransactionPool transactionPool = transactionPoolMap.get(callerChannelId);
            if (transactionPool.getClientTxIdTransactionMap().keySet() != null) {
                for (String cTxId : transactionPool.getClientTxIdTransactionMap().keySet()) {
                    if (cTxId.equals(clienTxId)) {
                        pooledTransaction = transactionPool.getClientTxIdTransactionMap().get(clienTxId);
                        break;
                    }
                }
            }
            transactionResp.setPooledTransaction(pooledTransaction.getSdkTransaction());
            transactionResp.setTransactionState(TxStateEnum.TX_WAIT_FOR_PACKAGE);
        } catch (Exception e) {
            return null;
        }
        return transactionResp;
    }

    /**
     * 根据通道id和交易hash查询交易池中是否存在交易
     */
    public TransactionResp getTxInTxPoolByTransHash(String callerChannelId, String transHash) {
        TransactionResp transactionResp = null;
        try {
            transactionResp = new TransactionResp();
            //根据交易拿到交易池
            TransactionPool transactionPool = transactionPoolMap.get(callerChannelId);
            PooledTransaction pooledTransaction = transactionPool.getTransactionByHash(transHash);
            transactionResp.setPooledTransaction(pooledTransaction.getSdkTransaction());
            transactionResp.setTransactionState(TxStateEnum.TX_WAIT_FOR_PACKAGE);
            Channel channel = ledgerMgr.getChannel(callerChannelId);
            transactionResp.setChannelId(channel.getChannelId());
            transactionResp.setChannelName(channel.getName());
        } catch (Exception e) {
            return null;
        }
        return transactionResp;
    }

    @Override
    public void reloadLocalPeer() {
        log.info("reloadLocalPeer");
        localPeer = ledgerMgr.queryLocalPeer();
    }
}
