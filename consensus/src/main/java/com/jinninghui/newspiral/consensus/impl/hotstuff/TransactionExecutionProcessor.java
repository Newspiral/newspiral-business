package com.jinninghui.newspiral.consensus.impl.hotstuff;

import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.consensus.ConsensusStageEnum;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.common.entity.transaction.PooledTransaction;
import com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor.ExecutedTransactionPool;
import com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor.TxExecutorConsensusCache;
import com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor.WaitPackageTxPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class TransactionExecutionProcessor extends TimerTask {

    private TxExecutorConsensusCache executorConsensusCache;

    private ConsensusContext consensusContext;

    private List<PooledTransaction> fetchedTx = new ArrayList<>();

    private Set<String> alreadyFetch = new HashSet<>();

    private String blockBase = null;

    Long lastFetchTime = 0L;


    /**
     * 定义可见变量，用于必要时停止服务
     */
    public volatile boolean flag = true;

    public void init(TxExecutorConsensusCache txExecutorConsensusCache, ConsensusContext consensusContext) {
        this.executorConsensusCache = txExecutorConsensusCache;
        this.consensusContext = consensusContext;
        blockBase = consensusContext.getHighestQC().getBlockHash();
    }

    @Override
    public void run() {
        if (flag) {
            try {
                if (consensusContext.getConsensusStageEnum().equals(ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL) == false) {
                    if (consensusContext.getHashPrePrepareBlock().equals(blockBase) == false) {
                        //log.info("preExecute on block {}, {} tx", blockBase, alreadyFetch.size());
                        //log.info("update blockbase {}", consensusContext.getHashPrePrepareBlock());
                        alreadyFetch.clear();
                        if (consensusContext.getBlockHeight() == 0) {
                            //节点第一个无数据启动时，hashPrePrepareBlock的值为一个虚构的值，故取最高的QC对应的区块hash
                            blockBase = consensusContext.getHighestQC().getBlockHash();
                        } else {
                            blockBase = consensusContext.getHashPrePrepareBlock();
                        }
                    }
                    WaitPackageTxPool waitPackageTxPool = executorConsensusCache.getWaitPackageTxPoolMap().get(blockBase);
                    if (waitPackageTxPool != null && waitPackageTxPool.getSize() > consensusContext.getChannel().getBlockMaxSize() / 500) {
                        log.debug(ModuleClassification.TxM_TxECC_.toString() + consensusContext.getChannel().getChannelId() +
                                " more than needed executedTransaction");
                        return;
                    }
                    fetchPooledTx();
                    if (fetchedTx.size() > 0) {
                        log.info(ModuleClassification.TxM_TxECC_.toString() + consensusContext.getChannel().getChannelId() +
                                " fetch " + fetchedTx.size() + " tx, base on block " + blockBase);
                        try {
                            executorConsensusCache.preExecutePooledTransaction(fetchedTx, blockBase);
                        } catch (Exception e) {
                            log.error(consensusContext.getChannel().getChannelId() + ",Exception in preExecution", e);
                        }
                        //log.info("preExecution time=== is {} on block {}, {} tx, on this block total pre executed {} transactions", System.currentTimeMillis() - curr, blockBase, fetchedTx.size(),
                        //       executorConsensusCache.getExecutedTransactionPoolMap().get(blockBase).getPreExecutedTx().size());
                        if (waitPackageTxPool == null) {
                            log.debug(consensusContext.getChannel().getChannelId() + ",error in preExecution");
                        } else {
                            log.info(consensusContext.getChannel().getChannelId() + ",wait package pool of block {}, size {}", blockBase, waitPackageTxPool.getSize() / 1000);
                        }
                        fetchedTx.clear();
                        //executorConsensusCache.putTxToPackagePool(consensusContext.getHighestQC().getBlockHashStr());
                    }
                }
            } catch (Exception ex) {
                log.error(consensusContext.getChannel().getChannelId() + ",Exception in preExecution thread", ex);
            }
        }
    }

    private void fetchPooledTx() {
        Block block = consensusContext.getBlock(blockBase);
        if (block == null) {
            log.info(consensusContext.getChannel().getChannelId()+",block null {}", blockBase);
            return;
        }
        ExecutedTransactionPool pool = executorConsensusCache.getExecutedTransactionPoolMap().get(block.getHash());
        if (pool == null) {
            pool = executorConsensusCache.insertExecutionPool(block, consensusContext);
            if (!pool.isValid()) {
                log.info(ModuleClassification.TxM_TxECC_.toString() +","+consensusContext.getChannel().getChannelId()+ " ,invalid ExecutedTransactionPool of block" + blockBase + ", remove it");
                executorConsensusCache.getExecutedTransactionPoolMap().remove(blockBase);
                return;
            }
        }
        long cnt = consensusContext.getChannel().getBlockMaxSize() / 1000;
        if (alreadyFetch.size() >= cnt) {
            cnt = cnt / 4;
        }
        Set<String> exclude = pool.getExcludedTxHash();
        //log.info("to fetch tx base on block {}, alreadyFetch size {}", blockBase, alreadyFetch.size());
        alreadyFetch.addAll(exclude);
        //
        List<PooledTransaction> list = executorConsensusCache.getWaitExecuteTxPool().fetch(alreadyFetch, Math.max(1,cnt), consensusContext.getChannel().getChannelId());
        for (PooledTransaction tx : list) {
            if (exclude.contains(tx.getSdkTransaction().getHash())) {
                log.info(consensusContext.getChannel().getChannelId()+",wrong fetch tx {}", tx.getSdkTransaction().getHash());
            }
        }
        fetchedTx.addAll(list);
        /*if (fetchedTx.size() < 500) {
            //已经抓到的交易的数量少于500且与上次抓取的时间间隔在5s内，清空
            if (System.currentTimeMillis() - lastFetchTime < 500L) {
                fetchedTx.clear();
                return;
            }
        }*/
        Iterator<PooledTransaction> iterator = fetchedTx.iterator();
        while (iterator.hasNext()) {
            PooledTransaction pooledTransaction = iterator.next();
            if (alreadyFetch.contains(pooledTransaction.getSdkTransaction().getHash())) {
                //log.info("fetch repeated tx");
                iterator.remove();
            } else {
                alreadyFetch.add(pooledTransaction.getSdkTransaction().getHash());
            }
        }
        lastFetchTime = System.currentTimeMillis();
        if(fetchedTx.size()>0) {
            long totalTxInPool = executorConsensusCache.getWaitExecuteTxPool().getTotalTxCount();
            log.info("fetch transaction for preExecute, fetchTx {}, totalSize {}, alreadyFetch Size{},left size {}", fetchedTx.size(), totalTxInPool,
                    alreadyFetch.size(), totalTxInPool - alreadyFetch.size());
        }
    }

   /* private boolean checkIfPrev3BlockCommitted(Block block) {
        int i = 2;
        Block committedBlock = block;
        while (i-- > 0 && committedBlock.getBlockHeader().getHeight() > 0) {
            committedBlock = consensusContext.getBlock(committedBlock.getPrevBlockHashStr());
            if (null == committedBlock) {
                log.warn("Cannot find prev block of highestQC");
                return false;
            }
        }
        if (null == consensusContext.getCommittedBlock(committedBlock.getHashStr())) {
            return false;
        }
        return true;
    }*/
}
