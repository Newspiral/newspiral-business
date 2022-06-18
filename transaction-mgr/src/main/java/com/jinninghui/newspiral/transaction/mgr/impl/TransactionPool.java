package com.jinninghui.newspiral.transaction.mgr.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.common.entity.transaction.ExecutedTransaction;
import com.jinninghui.newspiral.common.entity.transaction.PooledTransaction;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import com.jinninghui.newspiral.ledger.mgr.LedgerMgr;
import com.jinninghui.newspiral.member.mgr.MemberMgr;
import com.jinninghui.newspiral.security.SecurityService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lida
 * @date 2019/9/12 11:50
 * 交易池，管理某个通道的所有交易
 * 注意系统交易一定是优先级最高被提取出去的
 * //TODO 追求极限性能，这个类的锁还可以再看看有没有优化的空间
 * //TODO 系统交易与业务交易拆分还没有做
 */
@Slf4j
public class TransactionPool {

    @SofaReference
    private MemberMgr memberMgr;
    @SofaReference
    private LedgerMgr ledgerMgr;

    @Getter
    private String channelId;

    @Setter
    SecurityService securityService;

    /**
     * 交易池中最多的交易数量
     * TODO 两种优化方式，一种是根据内存容量动态自己弄，另一种是通道配置
     */
    @Setter
    long maxTransactionCnt;

    /**
     * key为交易hash的16进制字符串
     */
    private Map<String, PooledTransaction> transacionMap = new ConcurrentHashMap<>(32);

    //private List<String> transHashStrList = new LinkedList<>();
    //private List<String> transClientTxIdStrList = new LinkedList<>();

    /**
     * key为交易的客户端id,value为交易
     */
    @Getter
    private Map<String,PooledTransaction>  clientTxIdTransactionMap = new HashMap<>();


    /*@Getter
    private long totalPredictTransBytes = 0L;*/


    public TransactionPool(String channelId, long maxTransactionCnt) {
        this.channelId = channelId;
        this.maxTransactionCnt = maxTransactionCnt;
    }




    /**
     * 根据客户端执行的交易流水ID判断是否存在
     * @param sdkTransaction
     * @return
     */
    public boolean hasAlreadyAddTranscation(SDKTransaction sdkTransaction) {
        //todo:检查timestamp,如果超时的话直接不接收
        //log.info("hasAlreadyAddTranscation.sdkTransaction all:{}",sdkTransaction);
        //log.info("hasAlreadyAddTranscation.sdkTransaction out:{}",sdkTransaction.getClientTxId());
        if (clientTxIdTransactionMap.containsKey(sdkTransaction.getClientTxId())) {
            //log.info("hasAlreadyAddTranscation.sdkTransaction in:{}",sdkTransaction.getClientTxId());
            return true;
        }
        return false;
    }

    public boolean canAccept() {
        if (transacionMap.size() >= this.maxTransactionCnt) {
            return false;
        }
        return true;
    }


    /**
     * 返回交易Hash的16进制字符串
     *
     * @param sdkTransaction
     * @return
     */
    public String addTransaction(SDKTransaction sdkTransaction) {
        /*if (this.transacionMap.size() + 1 >= this.maxTransactionCnt) {//这里加不加同步锁无所谓
            throw new NewspiralException(NewSpiralErrorEnum.TRANSACTION_POOL_FULL);
        }*/
        //修改成从数据库表channel取进行比较
/*        Channel channel= memberMgr.getChannel(sdkTransaction.getChannelId());
        if (this.transacionMap.size() + 1 >= channel.getMaxTransactionCount()) {
            throw new NewspiralException(NewSpiralErrorEnum.TRANSACTION_POOL_FULL);
        }*/
        PooledTransaction pooledTransaction = this.createPooledTransaction(sdkTransaction);
        this.addPooledTransaction(pooledTransaction);
       // log.info(logPrefix() + "成功添加一条交易" + sdkTransaction.getHashStr() + ",交易池中交易数:" + this.transacionMap.size() + ",总大小:" + this.totalPredictTransBytes + "字节");
        return sdkTransaction.getHash();
    }

    private PooledTransaction createPooledTransaction(SDKTransaction sdkTransaction) {
        PooledTransaction pooledTransaction = new PooledTransaction();
        //TODO 这里应该要有合法性检查
        pooledTransaction.setSdkTransaction(sdkTransaction);
        //sdkTransaction.setAdd2PoolTimestamp(sdkTransaction.getClientTimestamp());
        pooledTransaction.setAdd2PoolTimestamp(System.currentTimeMillis());
        securityService.hash(pooledTransaction);
        return pooledTransaction;
    }

    /**
     * 末尾加入一个交易
     * @param pooledTransaction
     */
    //may be need to sync this function if totalPredictTransBytes need to be accurate.
    private void addPooledTransaction(PooledTransaction pooledTransaction) {
        //TODO 判断一下
        if(!transacionMap.containsKey(pooledTransaction.getSdkTransaction().getHash())) {
            //transHashStrList.add(sdkTransaction.getHashStr());
            clientTxIdTransactionMap.put(pooledTransaction.getSdkTransaction().getClientTxId(),pooledTransaction);
            transacionMap.put(pooledTransaction.getSdkTransaction().getHash(), pooledTransaction);
            //totalTransCount++;
            //totalPredictTransBytes += pooledTransaction.getPredictedBytes();
            //log.info(logPrefix() + "成功添加一条交易" + sdkTransaction.getHashStr() + ",交易池中交易数:" + this.transacionMap.size() + ",总大小:" + this.totalPredictTransBytes + "字节");
        }
    }


    synchronized public void processBlockConsensusSuccess(Block consensusedBlock) {
        int successedDelCnt = deleteTransactionOfBlock(consensusedBlock);
        /*log.info(logPrefix() + "接收到了区块共识成功消息，区块中包含" + consensusedBlock.getTransactionList().size() + "个交易,交易池中成功删除了"
                + successedDelCnt + "个交易，删除后池中交易数=" + transacionMap.size() + "，交易总字节数=" + totalPredictTransBytes);*/
    }

    protected int deleteTransactionOfBlock(Block block) {
        int successedDelCnt = 0;
        for (ExecutedTransaction trans : block.getTransactionList()) {
            if(null!=transacionMap.get(trans.getSdkTransaction().getHash())) {
                clientTxIdTransactionMap.remove(trans.getSdkTransaction().getClientTxId());
                PooledTransaction delEle = transacionMap.remove(trans.getSdkTransaction().getHash());
                clientTxIdTransactionMap.remove(trans.getSdkTransaction().getClientTxId());
                //log.info("deleteTransactionOfBlock transHash={}", trans.getSdkTransaction().getHashStr());
                if (null != delEle) {
                    //map里面没有就不用删除  TODO 这里还有优化空间，怎么尽快的删除掉List中的这些记录
                    //transHashStrList.remove(delEle.getHashStr());
                    //totalPredictTransBytes -= delEle.getPredictedBytes();
                    successedDelCnt++;
                    //index--;
                    //log.info("deleteTransactionListByHash:{}",index);
                }
            }

        }
        return successedDelCnt;
    }

    synchronized  public void processBlockCreate(Block block) {
        int successedDelCnt = deleteTransactionOfBlock(block);

        log.info(logPrefix() + "接收到了区块创建成功通知，区块中包含" + block.getTransactionList().size() + "个交易,交易池中成功临时移走了"
                + successedDelCnt + "个交易，处理后池中交易数=" + transacionMap.size());

    }




    private String logPrefix() {
        return "通道" + this.channelId + "的交易池:";
    }

    /**
     * 当前所区的交易map索引，每创建一次递增，删除交易时递减
     */
    public int index = 0;
    synchronized  public List<PooledTransaction> extractTransctionsForCreateBlock(long maxBytes) {
        List<PooledTransaction> transList = new ArrayList<>();
        //int index = 0;
        long extractedBytes = 0L;
        log.info(channelId+",extractTransctionsForCreateBlock-transacionMap:{}",transacionMap);
        //log.info("extractTransctionsForCreateBlock-transHashStrList:{}",transHashStrList);
        for (int i=0;i<transacionMap.size();i++)
        {
            PooledTransaction pooledTransaction=  transacionMap.get(i);
            transList.add(pooledTransaction);
            extractedBytes += JSONObject.toJSONString(pooledTransaction).length();
            if(extractedBytes>=maxBytes)
            {
                break;
            }
        }
        log.info(ModuleClassification.TxM_TP_+","+channelId+",extractTransctionsForCreateBlock-transList:{}",transList);
        log.info(ModuleClassification.TxM_TP_+","+channelId+"交易池当前索引index out while:{}",index);
        return transList;
    }

    synchronized   public long deleteTransactionList(List<PooledTransaction> pooledTransactionList) {
        long delCnt = 0L;

        for (PooledTransaction trans : pooledTransactionList) {
            PooledTransaction delEle = transacionMap.remove(trans.getSdkTransaction().getHash());
            if (null != delEle) {
                //map里面没有就不用删除
                //transHashStrList.remove(delEle.getHashStr());
                //totalPredictTransBytes -= delEle.getPredictedBytes();
                ++delCnt;
                //index--;
                //log.info(ModuleClassification.TxM_TP_+"deleteTransactionList:{}",index);
            }
        }
        return delCnt;
    }


    synchronized  public PooledTransaction getTransactionByHash(String transHash) {
        return transacionMap.get(transHash);
    }

     public long deleteTransactionListByHash(List<String> transHashList) {
        long delCnt = 0L;
        for (String transHashStr : transHashList) {
            PooledTransaction delEle = transacionMap.remove(transHashStr);
            if (null != delEle) {
                //map里面没有就不用删除
                //transHashStrList.remove(delEle.getHashStr());
                //totalPredictTransBytes -= delEle.getPredictedBytes();
                ++delCnt;
                //index--;
                //log.info(ModuleClassification.TxM_TP_+"deleteTransactionListByHash:{}",index);
            }
        }
        return delCnt;
    }

   /* public boolean hasEnoughTransactions(Long blockMaxSize) {
        return totalPredictTransBytes >= blockMaxSize;
    }*/

    /**
     * 是否存在交易
     *
     * @return
     */
    public boolean existTransaction() {
        return this.transacionMap.size() > 0;
    }

    public int prePackTransactionCount(String channelId,LedgerMgr ledgerMgr)
    {
        /*if(totalTransCount>0) {
            return totalTransCount;
        }
        else {
            return  ledgerMgr.getTransactionCount(channelId)+this.transacionMap.size();
        }*/
        return  ledgerMgr.getTransactionCount(channelId)+this.transacionMap.size();
    }

    public void removeTransaction(String hash) {
        transacionMap.remove(hash);
    }

}
