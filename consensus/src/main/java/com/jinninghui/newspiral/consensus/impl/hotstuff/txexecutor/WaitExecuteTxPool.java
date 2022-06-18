package com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor;

import com.alibaba.fastjson.JSON;
import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.common.entity.transaction.ExecutedTransaction;
import com.jinninghui.newspiral.common.entity.transaction.PooledTransaction;
import com.jinninghui.newspiral.ledger.mgr.LedgerMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
@Scope("prototype")
public class WaitExecuteTxPool {
    private Map<String, PooledTransaction> pooledTransactionMap = new ConcurrentHashMap<>();
    private ConcurrentLinkedDeque<String> txHashQueue = new ConcurrentLinkedDeque<>();
    @SofaReference
    private LedgerMgr ledgerMgr;
    private Lock lock = new ReentrantLock();

    //private AtomicLong totalAcceptTxCount = new AtomicLong();
   // private AtomicLong totalConfirmedTxCount = new AtomicLong();
    public void add(PooledTransaction transaction) {
        if (ledgerMgr.ifTxExist(transaction.getSdkTransaction().getHash())) {
            log.info(ModuleClassification.ConM_CC_.toString() + transaction.getSdkTransaction().getChannelId() +
                    " add stale tx " + transaction.getSdkTransaction().getHash());
            return;
        }
        if (null == pooledTransactionMap.put(transaction.getSdkTransaction().getHash(), transaction)) {
            //当且仅当map中不存在该被put的值时，才将hash放进队列中，保证队列中不会有重复的hash，后续的队列清理逻辑是基于队列中不存在重复的hash
            log.info(ModuleClassification.TxM_TP_.toString() + transaction.getSdkTransaction().getChannelId() +
                    " put tx " + transaction.getSdkTransaction().getHash() + " into WaitExecuteTxPool");
            txHashQueue.add(transaction.getSdkTransaction().getHash());
        }
       // totalAcceptTxCount.addAndGet(1L);
    }

    /**
     * 当前所有尚未完成共识的交易数，注意其中有部分可能是已经被预执行完毕了的
     * @return
     */
    public long getTotalTxCount()
    {
        return txHashQueue.size();
    }

/*    public long getTotalAcceptTxCount()
    {
        return totalAcceptTxCount.get();
    }
    public long getTotalConfirmTxCount()
    {
        return totalConfirmedTxCount.get();
    }*/



    public List<PooledTransaction> fetch(Set<String> excludedTxHashSet, long size, String channelId) {
        lock.lock();
        if (pooledTransactionMap.size() == 0) {
            lock.unlock();
            return new ArrayList<>();
        }
        if (pooledTransactionMap.size() > 0) {
            log.info(channelId + ", before fetch, there are {} tx, excludedTxHashSet size {}, going to fetch {} tx", pooledTransactionMap.size(), excludedTxHashSet.size(), size);
        }
        Stack<String> exclude = new Stack<>();
        List<PooledTransaction> pooledTransactionList = new CopyOnWriteArrayList<>();
        Set<String> fetchedTxHashSet = new HashSet<>();
        while (!txHashQueue.isEmpty() && size > 0) {
            String key = txHashQueue.poll();
            if (key == null) {
                break;
            }
            //还没被取过
            if (!fetchedTxHashSet.contains(key)) {
                //压入exclude
                exclude.push(key);
                //不在排除的set里
                if (!excludedTxHashSet.contains(key)) {
                    //加入取过的的set
                    fetchedTxHashSet.add(key);
                    size--;
                }
            } else {
                log.info(ModuleClassification.TxM_TxECC_ + "repeated tx " + key);
            }
        }
        while (!exclude.isEmpty()) {
            //放入队列开头，栈里最底部的交易hash被放入队列的最前面
            txHashQueue.addFirst(exclude.pop());
        }
        if (fetchedTxHashSet.size() > 0) {
            log.info(channelId + ", fetch {} tx", fetchedTxHashSet.size());
        }
        //已经取过的
        for (String hashStr : fetchedTxHashSet) {
            //从map中取出pooledTransaction
            PooledTransaction pooledTransaction = pooledTransactionMap.get(hashStr);
            if (pooledTransaction != null) {
                pooledTransactionList.add(pooledTransaction);
            }
        }
        if (pooledTransactionList.size() > 0) {
            log.info(channelId + ", fetch out {} tx", pooledTransactionList.size());
        }
        lock.unlock();
        return pooledTransactionList;
    }

    public void remove(Block block) {
        Long cur = System.currentTimeMillis();
        log.info(ModuleClassification.TxM_TxECC_.toString()  + block.getBlockHeader().getChannelId()+",before removal at {}, pooltx size {}, block size {}", cur.toString(), pooledTransactionMap.size(), block.getTransactionList().size());
        lock.lock();
        Set<String> txHashSet = new HashSet<>();
        for (ExecutedTransaction tx : block.getTransactionList()) {
            txHashSet.add(tx.getSDKTransactionHash());
            pooledTransactionMap.remove(tx.getSDKTransactionHash());
        }
        //遍历一遍，删除掉txHashQueue中所有已经打包的交易哈希，因为不一定严格按照顺序打包（其他节点构建的区块），所以需要如下处理
        Stack<String> keep = new Stack<>();
        while (!txHashSet.isEmpty() && !txHashQueue.isEmpty()) {
            String key = txHashQueue.poll();
            if (!txHashSet.remove(key)) {
                keep.push(key);
            }
        }
        while (!keep.isEmpty()) {
            txHashQueue.addFirst(keep.pop());
        }
        //totalConfirmedTxCount.addAndGet(block.getTransactionList().size());
        lock.unlock();
    }

    public boolean ifExist(String hashStr) {
        return pooledTransactionMap.containsKey(hashStr);
    }

    public int size() {
        return pooledTransactionMap.size();
    }
    public PooledTransaction getPooledTx(String hashStr) {
        return pooledTransactionMap.get(hashStr);
    }

    public Long getPooledTotalSize(){
        return pooledTransactionMap.values().stream().mapToLong(tx-> JSON.toJSONString(tx).length()).sum();
    }
}
