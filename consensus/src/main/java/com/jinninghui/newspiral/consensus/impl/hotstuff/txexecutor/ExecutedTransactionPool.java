package com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor;

import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.common.entity.transaction.ExecutedTransaction;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import com.jinninghui.newspiral.consensus.impl.hotstuff.ConsensusContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
@Slf4j
public class ExecutedTransactionPool {
    @Getter
    private boolean valid;
    private Block blockBase;
    @Getter
    private Set<String> excludedTxHash = new HashSet<>();
    @Getter
    private Map<String, ExecutedTransaction> preExecutedTx = new ConcurrentHashMap<>();
    @Getter
    private Long totalSize = new Long(0);

    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public ExecutedTransactionPool(Block blockBase, ConsensusContext consensusContext) {
        this.blockBase = blockBase;
        calculateExcludedTx(blockBase, consensusContext);
    }
    private void calculateExcludedTx(Block blockBase, ConsensusContext consensusContext) {
        //log.info("calculate block exclude tx of base block {}", blockBase.getHashStr());
        Block block = blockBase;
        Long height = block.getBlockHeader().getHeight();
        //long count = height - consensusContext.getPersistedBlockHeight();
        long count = height - consensusContext.getBlockHeight();
        count = count < 4 ? 4 : count;
        while (height >= 0 && count > 0) {
            //log.info( count + ":exclude tx in block {}, height {}, size is {}", block.getHashStr(), block.getBlockHeader().getHeight(), block.getTransactionMap().size());
            for (ExecutedTransaction tx : block.getTransactionList()) {
                excludedTxHash.add(tx.getSDKTransactionHash());
            }
            height--;
            count--;
            Block prevBlock = consensusContext.getBlock(block.getPrevBlockHash());
            if (prevBlock == null) {
                if (height >= 0) {
                    log.error(ModuleClassification.TxM_TxECC_.toString() +","+consensusContext.getChannel().getChannelId()+","+consensusContext.getChannel().getChannelId()+ " ,can't find block " + block.getPrevBlockHash() +" when generating ExecutedTransactionPool");
                    valid = false;
                }
                break;
            }
            block = prevBlock;
        }
        valid = true;
    }
    public Block getBlockBase() {
        return this.blockBase;
    }

    /**
     * ?????????????????????
     *
     * @param transactions ????????????
     */
    public void addToPool(List<ExecutedTransaction> transactions) {
        readWriteLock.writeLock().lock();
        try {
            for (ExecutedTransaction tx : transactions) {
                //?????????????????????
                if (excludedTxHash.contains(tx.getSDKTransactionHash())) {
                    continue;
                }
                //???????????????????????????
                this.preExecutedTx.put(tx.getSDKTransactionHash(), tx);
                //?????????
                totalSize += tx.getSdkTransaction().getSize();
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @param executedTransactions ???????????????
     * @return ????????????
     */
    public List<SDKTransaction> findFreshPooledTransaction(List<ExecutedTransaction> executedTransactions) {
        List<SDKTransaction> freshTx = new ArrayList<>();
        for (ExecutedTransaction tx : executedTransactions) {
            if (this.excludedTxHash.contains(tx.getSDKTransactionHash()) || this.preExecutedTx.containsKey(tx.getSDKTransactionHash())) {
                //???????????????????????????????????????????????????
                continue;
            }
            freshTx.add(tx.getSdkTransaction());
        }
        return freshTx;
    }

    public boolean needPreExecuteOrNot(String hash) {
        return !excludedTxHash.contains(hash) && !preExecutedTx.containsKey(hash);
    }

    public ExecutedTransaction getTx(String hashStr) {
        return preExecutedTx.get(hashStr);
    }
}
