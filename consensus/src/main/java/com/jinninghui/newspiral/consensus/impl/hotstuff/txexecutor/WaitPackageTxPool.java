package com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor;

import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.transaction.ExecutedTransaction;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class WaitPackageTxPool {
    @Getter
    private Block blockBase;
    private ConcurrentLinkedDeque<String> txHashQueue = new ConcurrentLinkedDeque<>();
    private ConcurrentHashMap<String, ExecutedTransaction> transactionMap = new ConcurrentHashMap<>();
    @Getter
    private Long size = new Long(0);
    private Lock lock = new ReentrantLock();

    public WaitPackageTxPool(Block block) {
        this.blockBase = block;
    }
    public List<ExecutedTransaction> getTransactionMap(Long size) {
        lock.lock();
        //log.info("before package, txHashQueue size {} wait package pool size {} on block {}", txHashQueue.size(), transactionMap.size(), blockHash);
        List<ExecutedTransaction> result = new ArrayList<>();
        Set<String> hashSet = new HashSet<>();
        long total = 0;
        Stack<String> keep = new Stack<>();
        while (total < size) {
            String key = txHashQueue.poll();
            if (key == null) {
                break;
            }
            if (hashSet.contains(key)) {
                continue;
            }
            ExecutedTransaction transaction = transactionMap.get(key);
            result.add(transaction);
            hashSet.add(key);
            keep.push(key);
            total += transaction.getSdkTransaction().getSize();
        }
        while (!keep.isEmpty()) {
            txHashQueue.addFirst(keep.pop());
        }
        lock.unlock();
        //log.info("after package, txHashQueue size {} wait package pool size {} on block {}", txHashQueue.size(), transactionMap.size(), blockHash);
        return result;
    }

    public void addToPool(List<ExecutedTransaction> transactions) {
        lock.lock();
        for (ExecutedTransaction tx : transactions) {
            if (transactionMap.get(tx.getSDKTransactionHash()) == null) {
                transactionMap.put(tx.getSDKTransactionHash(), tx);
                txHashQueue.add(tx.getSDKTransactionHash());
                size += tx.getSdkTransaction().getSize();
            }
        }
        lock.unlock();
    }

    public ExecutedTransaction getExecutedTransaction(String hash) {
        ExecutedTransaction tx = transactionMap.get(hash);
        return tx;
    }
}
