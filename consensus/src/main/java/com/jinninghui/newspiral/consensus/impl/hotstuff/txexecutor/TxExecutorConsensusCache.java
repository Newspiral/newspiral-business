package com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor;

import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.chain.ChannelChange;
import com.jinninghui.newspiral.common.entity.chain.ChannelModifyRecord;
import com.jinninghui.newspiral.common.entity.common.logmodule.LogModuleCodes;
import com.jinninghui.newspiral.common.entity.member.Member;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.common.entity.state.WorldStateModifyRecord;
import com.jinninghui.newspiral.common.entity.transaction.ExecutedTransaction;
import com.jinninghui.newspiral.common.entity.transaction.PooledTransaction;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import com.jinninghui.newspiral.common.entity.transaction.TxStateEnum;
import com.jinninghui.newspiral.consensus.hotstuff.ConsensusMsgProcessor;
import com.jinninghui.newspiral.consensus.impl.hotstuff.ConsensusContext;
import com.jinninghui.newspiral.ledger.mgr.LedgerMgr;
import com.jinninghui.newspiral.ledger.mgr.LedgerThreadLocalContext;
import com.jinninghui.newspiral.ledger.mgr.StateAccessModeEnum;
import com.jinninghui.newspiral.ledger.mgr.BlockChangesSnapshots;
import com.jinninghui.newspiral.security.contract.SandboxException;
import com.jinninghui.newspiral.transaction.mgr.TransactionMgr;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 交易池的预执行结果的共识缓存，根据共识缓存和共识世界状态缓存，收到区块时候进行执行结果分析。
 * 新增区块或者获取区块时候如果交易已经预执行过，则不需要执行交易，直接取执行结果即可
 */
@Slf4j
@Component
@Scope("prototype")
public class TxExecutorConsensusCache extends TxExecutor {
    /**
     * key: hash of block
     * value: executedTransactionPool based on one block.
     * this map only for preExecute transaction
     */
    @Getter
    private Map<String, ExecutedTransactionPool> executedTransactionPoolMap = new ConcurrentHashMap<>();
    /**
     * this map only for packaging
     */
    @Getter
    private Map<String, WaitPackageTxPool> waitPackageTxPoolMap = new ConcurrentHashMap<>();

    /**
     * this pool is used for packaging block only, meanwhile the transactionpool in transactionMgr is for receiving.
     */
    @Getter
    //private ConcurrentLinkedHashMap<String, PooledTransaction> transactionPool = new ConcurrentLinkedHashMap.Builder<String, PooledTransaction>().maximumWeightedCapacity(500000).build();
    @Autowired
    private WaitExecuteTxPool waitExecuteTxPool;
    @SofaReference
    private LedgerMgr ledgerMgr;
    /**
     * 接收到的区块，内部交易也加入等待内存池，再走执行内存池
     */
    @SofaReference
    private TransactionMgr tranasctionMgr;
    /**
     * 通道交易接收处理，激活区块内的交易预编译(如果还没编译)
     */
    @SofaReference
    private ConsensusMsgProcessor consensusMsgProcessor;
    private ConsensusContext consensusContext;

    //private ForkJoinPool forkJoinPool = new ForkJoinPool(8);
    /**
     * 并发线程流
     */
    private ExecutorService executorTX = Executors.newFixedThreadPool(10);

    /**
     * 设定信号量
     */
    private final Semaphore finalSemaphore = new Semaphore(500);

    private static int reExecuteTimeLimit = 2000;

    private static int reExecuteTxLimit = 100;

    /**
     * logPreFix of transaction
     *
     * @param transaction
     * @return
     */
    private String txLogPreFix(SDKTransaction transaction, TxStateEnum state) {
        return "TxState" + state.getMessage() + "clientId:" + transaction.getClientTxId() + ",channelId：" + transaction.getChannelId() + ",transHash" + transaction.getHash() + ",";

    }

    public void init(ConsensusContext consensusContext) {
        this.consensusContext = consensusContext;
    }


    /**
     * 检查是否有足够的交易，考虑到都是内存操作，而且是轮询执行，就不做容量计算临时计算了，
     * 实地测试如果造成性能问题，再优化一下。
     */
    public boolean hasEnoughTransactions(long maxBlockSize) {
        Block block = consensusContext.getBlock(consensusContext.getHighestQC().getBlockHash());
        WaitPackageTxPool pool = waitPackageTxPoolMap.get(block.getHash());
        if (pool == null) {
            return false;
        }
        if (maxBlockSize <= pool.getSize()) {
            return true;
        }
        return false;
    }

    /**
     * 预执行步进交易，所以虽然是多线程处理但是要设置成同步
     *
     * @param flowTransList
     */
    private List<ExecutedTransaction> preExecuteFlowPooledTransaction(List<SDKTransaction> flowTransList, List<BlockChangesSnapshots> blockChangesSnapShots,
                                                                      StateAccessModeEnum stateAccessModeEnum) {
        // TODO：可以额外验证Flow是否正确
        return execute(flowTransList, stateAccessModeEnum, blockChangesSnapShots);
    }


    /**
     * @param pooledTransactionList
     */
    public void preExecutePooledTransaction(List<PooledTransaction> pooledTransactionList, String blockHash) {
        List<SDKTransaction> sdkTransactions = new ArrayList<>();
        for (PooledTransaction pooledTransaction : pooledTransactionList) {
            sdkTransactions.add(pooledTransaction.getSdkTransaction());
        }
        try {
            executePooledTransactionBasedOnBlock(sdkTransactions, blockHash);
        } catch (Exception e) {
            log.error(ModuleClassification.TxM_TxECC_.toString() + " exception in preExecutePooledTransaction:", e);
        }
    }

    /**
     * @param transactionList 并行预执行交易
     * @param blockHash
     * @return
     */
    public List<ExecutedTransaction> executePooledTransactionBasedOnBlock(List<SDKTransaction> transactionList, String blockHash) {
        //区块
        Block block = consensusContext.getBlock(blockHash);
        if (block == null) {
            log.info(ModuleClassification.TxM_TxECC_ + " can't execute tx on block {} because can't find it in local", blockHash);
            return new ArrayList<>();
        }
        List<BlockChangesSnapshots> snapshoots = new ArrayList<>();
        findWorldStateSnapShootForExecution(block, snapshoots);
        //for debug
        //log.info("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",preExecute transaction based on block {}, has following snapshoot", block.getHashStr());
        //TODO:snapshoots may be null, when the executed block is based on committed block.
        // So pay attention to preExecuteFlowPooledTransaction with parameter forExecutionSnapShoots of size zero.
        // for debug
        /*for (PooledTransaction tx : transactionList) {
            log.info(ModuleClassification.TxM_TxECC+"preExecute tx {}, sdkversion {}, base on block {} of height {}", tx.getHash(),
                    tx.getSdkTransaction().getVersion(), block.getHash(), block.getBlockHeader().getHeight());
        }*/
        List<ExecutedTransaction> executedTransactions = preExecuteFlowPooledTransaction(transactionList, snapshoots,
                StateAccessModeEnum.TRANS_CREATE_FOR_FIXQC_VERIFY);
        putTxToExecutedPool(executedTransactions, block);
        return executedTransactions;
    }


    /**
     * @param transactions 交易
     * @param blockBase    交易hash
     */
    private void putTxToExecutedPool(List<ExecutedTransaction> transactions, Block blockBase) {
        ExecutedTransactionPool pool = executedTransactionPoolMap.get(blockBase);
        if (pool == null) {
            pool = insertExecutionPool(blockBase, consensusContext);
        }
        //log.info("put Tx to ExecutedPool on block {}", blockBase);
        pool.addToPool(transactions);
        putTxToPackagePool(blockBase, transactions);

    }
/*    public void putTxToPackagePool(String hash) {
        ExecutedTransactionPool pool = executedTransactionPoolMap.get(hash);
        if (pool == null) {
            log.info("ExcutedTransaction Pool is null");
            return;
        }
        putTxToPackagePool(pool);
    }*/


    /**
     * 将交易放入待打包的池子
     *
     * @param blockBase       区块
     * @param transactionList 交易列表
     */
    private void putTxToPackagePool(Block blockBase, List<ExecutedTransaction> transactionList) {
        WaitPackageTxPool packageTxPool = waitPackageTxPoolMap.get(blockBase.getHash());
        if (packageTxPool == null) {
            packageTxPool = insertWaitPackagePool(blockBase);
        }
        packageTxPool.addToPool(transactionList);
    }

    /**
     * 生成世界状态快照
     *
     * @param block 区块
     * @return 世界状态快照
     */
    private BlockChangesSnapshots generateWorldStateSnapShootOfBlock(Block block) {
        BlockChangesSnapshots snapshoot = new BlockChangesSnapshots();
        snapshoot.setBlockHashStr(block.getHash());
        block.getTransactionList().parallelStream().forEach(
                parallelTx -> {
                    if (!parallelTx.isSerial()) {
                        for (WorldStateModifyRecord record : parallelTx.getModifiedWorldStateList()) {
                            String key = record.getOldState() == null ? record.getNewState().getKey() : record.getOldState().getKey();
                            snapshoot.put(key, record);
                        }
                        for (ChannelModifyRecord modifyRecord : parallelTx.getModifiedChannelRecordList()) {
                            if (modifyRecord != null) {
                                snapshoot.addChangesToChannel(modifyRecord.getNewChannel().getChannelChange(), modifyRecord.getNewChannel());
                            }
                        }
                    }
                }
        );
        for (ExecutedTransaction tx : block.getTransactionList()) {
            if (tx.isSerial()) {
                for (WorldStateModifyRecord record : tx.getModifiedWorldStateList()) {
                    snapshoot.put(record.getOldState() == null ? record.getNewState().getKey() : record.getOldState().getKey(), record);
                }
                for (ChannelModifyRecord modifyRecord : tx.getModifiedChannelRecordList()) {
                    if (modifyRecord != null) {
                        snapshoot.addChangesToChannel(modifyRecord.getNewChannel().getChannelChange(), modifyRecord.getNewChannel());
                    }
                }
            }
        }
        return snapshoot;
    }

    /**
     * 缓存执行的池子，并返回
     *
     * @param block            区块
     * @param consensusContext 共识上下文
     * @return ExecutedTransactionPool
     */
    synchronized public ExecutedTransactionPool insertExecutionPool(Block block, ConsensusContext consensusContext) {
        if (null == executedTransactionPoolMap.get(block.getHash())) {
            ExecutedTransactionPool pool = new ExecutedTransactionPool(block, consensusContext);
            executedTransactionPoolMap.put(pool.getBlockBase().getHash(), pool);
            return pool;
        } else {
            return executedTransactionPoolMap.get(block.getHash());
        }
    }

    synchronized public WaitPackageTxPool insertWaitPackagePool(Block block) {
        if (null == waitPackageTxPoolMap.get(block.getHash())) {
            WaitPackageTxPool pool = new WaitPackageTxPool(block);
            waitPackageTxPoolMap.put(block.getHash(), pool);
            return pool;
        } else {
            return waitPackageTxPoolMap.get(block.getHash());
        }
    }


    /**
     * verify the correctness of execution of txs in block in genericMsg,
     * before updating genericQC in genericMsg.
     *
     * @param block
     * @return
     */
    public boolean verifyBlockForExecution(Block block) {
        if (block.getTransactionList().size() == 0) {
            return true;
        }
        if (!checkParallelOfBlock(block)) {
            log.info(ModuleClassification.TxM_TxECC_.toString() + "," + block.getBlockHeader().getChannelId() + " ,Wrong parallel transaction list in block:" + block.getHash());
            return false;
        }
        if (!executeTransactionIfNotYet(block)) {
            log.info(ModuleClassification.TxM_TxECC_.toString() + "," + block.getBlockHeader().getChannelId() + " ,executeTransactionIfNotYet failed in block:" + block.getHash());
            return false;
        }
        if (!checkParallelTransactionOfBlock(block)) {
            log.info(ModuleClassification.TxM_TxECC_.toString() + "," + block.getBlockHeader().getChannelId() + " ,checkParallelTransactionOfBlock failed in block:" + block.getHash());
            return false;
        }
        if (!checkSerialTransactionOfBlock(block)) {
            log.info(ModuleClassification.TxM_TxECC_.toString() + "," + block.getBlockHeader().getChannelId() + " ,checkSerialTransactionOfBlock failed in block:" + block.getHash());
            return false;
        }
        return true;

    }

    public boolean checkParallelOfBlock(Block block) {
        Set<String> keySet = new HashSet<>();
        Set<String> txHashSet = new HashSet<>();
        Set<String> actionSet = new HashSet<>();
        for (ExecutedTransaction tx : block.getTransactionList()) {
            for (WorldStateModifyRecord record : tx.getModifiedWorldStateList()) {
                String key = record.getOldState() == null ? record.getNewState().getKey() : record.getOldState().getKey();
                if (keySet.contains(key)) {
                    txHashSet.add(tx.getSDKTransactionHash());
                } else {
                    keySet.add(key);
                }
            }
            for (ChannelModifyRecord channelRecord : tx.getModifiedChannelRecordList()) {
                ChannelChange channelChange = channelRecord.getNewChannel().getChannelChange();
                Object action = channelChange.getActionData();
                if (action instanceof Member) {
                    //如果是member，并行操作同一个member会出现问题，需要特殊处理，其余的系统合约交易并行也没关系
                    String memberKey = ((Member) action).getPublicKey() + "-" + ((Member) action).getChannelId();
                    if (actionSet.contains(memberKey)) {
                        txHashSet.add(tx.getSDKTransactionHash());
                    } else {
                        actionSet.add(memberKey);
                    }
                }
            }
        }

        return block.getTransactionList().parallelStream().allMatch(
                tx -> {
                    if (txHashSet.contains(tx.getSDKTransactionHash()) && !tx.isSerial()) {
                        return false;
                    }
                    return true;
                }
        );
    }

    /**
     * find worldStateSnapShoot by previous block,if miss
     *
     * @param includedBlock
     * @return
     */
    private Boolean findWorldStateSnapShootForExecution(Block includedBlock, List<BlockChangesSnapshots> snapshoots) {
        Long numberOfWorldStateSnapShoot = includedBlock.getBlockHeader().getHeight() - consensusContext.getPersistedBlockHeight();
        while (numberOfWorldStateSnapShoot > 0) {
            BlockChangesSnapshots snapshoot = LedgerThreadLocalContext.blockChangesMap.getNeedSnapShootByBlockHash(includedBlock.getHash());
            if (null == snapshoot) {
                //for debug
                log.info(ModuleClassification.TxM_TxECC_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + includedBlock.getBlockHeader().getChannelId() + ",cannot find snapshoot of block {}", includedBlock.getHash());
                snapshoot = generateWorldStateSnapShootOfBlock(includedBlock);
                LedgerThreadLocalContext.blockChangesMap.addSnap(snapshoot);
            }
            snapshoots.add(snapshoot);
            //TODO
            String channelId = includedBlock.getBlockHeader().getChannelId();
            String prevBlockHash = includedBlock.getBlockHeader().getPrevBlockHash();
            includedBlock = consensusContext.getBlock(includedBlock.getBlockHeader().getPrevBlockHash());
            if (includedBlock == null) {
                log.error(ModuleClassification.TxM_TxECC_.toString() + "," + channelId + " ,misses preblock " + prevBlockHash);
                return false;
            }
            numberOfWorldStateSnapShoot--;
        }
        return true;
    }

    private boolean executeTransactionIfNotYet(Block block) {
        Block prevBlock = consensusContext.getBlock(block.getPrevBlockHash());
        if (prevBlock == null) {
            log.info(ModuleClassification.TxM_TxECC_.toString() + "," + block.getBlockHeader().getChannelId() + " Can't find prevBlock in executeTransactionIfNotYet of block:" + block.getHash());
            return false;
        }
        ExecutedTransactionPool pool = executedTransactionPoolMap.get(prevBlock.getHash());
        List<SDKTransaction> pooledTransactions = null;
        if (pool == null) {
            pooledTransactions = block.getSDKTransactions();
        } else {
            pooledTransactions = pool.findFreshPooledTransaction(block.getTransactionList());
            //这里先注释了，添加节点正常交易被判断为重复交易并最终未被执行
            for (ExecutedTransaction extx : block.getTransactionList()) {
                String transactionHash = tranasctionMgr.tryAddTransaction(extx.getSdkTransaction());
                if (transactionHash != null) {
                    consensusMsgProcessor.addNewTransaction(extx.getSdkTransaction().getChannelId(), transactionHash);
                }else{
                    log.info("交易hash为null，区块中的交易重新尝试addNewTransaction失败");
                }
            }
        }
        if (pooledTransactions.size() != 0) {
            executePooledTransactionBasedOnBlock(pooledTransactions, prevBlock.getHash());
        }
        return true;
    }

    private boolean checkParallelTransactionOfBlock(Block block) {
        try {
            List<ExecutedTransaction> executedTransactionsInBlock = block.getTransactionList();
            String hashOfPrevBlock = block.getPrevBlockHash();
            //WaitPackageTxPool pool = waitPackageTxPoolMap.get(hashOfPrevBlock);
            ExecutedTransactionPool pool = executedTransactionPoolMap.get(hashOfPrevBlock);
            //for debug
            if (pool == null) {
                if (block.getTransactionList().size() > 0) {
                    log.error(ModuleClassification.TxM_TxECC_.toString() + "," + block.getBlockHeader().getChannelId() + " ,Can't find executed transaction pool of block {}", block.getHash());
                    return false;
                } else {
                    return true;
                }
            }

            return executedTransactionsInBlock.parallelStream().allMatch(
                    tx -> {
                        if (!tx.isSerial()) {
                            ExecutedTransaction localTx = pool.getTx(tx.getSDKTransactionHash());
                            if (!tx.equals(localTx)) {
                                log.info(ModuleClassification.TxM_TxECC_ + "," + block.getBlockHeader().getChannelId() + " Parallel transaction {} in given block {} is conflict with local execution", tx.getSdkTransaction().getHash(), block.getHash());
                                return false;
                            }
                            return true;
                        }
                        return true;
                    }
            );
        } catch (Exception ex) {
            log.error(ModuleClassification.TxM_TxECC_.toString() + LogModuleCodes.SYSTEM_PLANTFORM_ACTION.toString() + "," + block.getBlockHeader().getChannelId() +
                    " ,Exception in checkParallelTransactionOfBlock of block " + block.getHash(), ex);
            return false;
        }
    }

    private boolean checkSerialTransactionOfBlock(Block block) {
        List<ExecutedTransaction> executedTransactionsInBlock = block.getTransactionList();
        List<ExecutedTransaction> serialExecutedTransactionsInBlock = new ArrayList<>();
        List<SDKTransaction> serialSDKTransactions = new ArrayList<>();
        for (ExecutedTransaction transaction : executedTransactionsInBlock) {
            if (transaction.isSerial()) {
                serialExecutedTransactionsInBlock.add(transaction);
                serialSDKTransactions.add(transaction.getSdkTransaction());
            }
        }
        if (serialExecutedTransactionsInBlock.isEmpty()) {
            return true;
        }
        Block prevBlock = consensusContext.getBlock(block.getPrevBlockHash());
        List<BlockChangesSnapshots> snapshoots = new ArrayList<>();
        if (!findWorldStateSnapShootForExecution(prevBlock, snapshoots)) {
            log.error(ModuleClassification.TxM_TxECC_.toString() + "," + block.getBlockHeader().getChannelId() + " ,failed to findWorldStateSnapShootForExecution for block " + block.getHash());
            return false;
        }
        List<ExecutedTransaction> executedTransactionsLocal = preExecuteFlowPooledTransaction(serialSDKTransactions, snapshoots,
                StateAccessModeEnum.TRANS_CREATE_FOR_STEPQC_VERIFY);
        for (int i = 0; i < serialExecutedTransactionsInBlock.size(); i++) {
            if (serialExecutedTransactionsInBlock.get(i).equals(executedTransactionsLocal.get(i)) == false) {
                log.info(ModuleClassification.TxM_TxECC_.toString() + "," + block.getBlockHeader().getChannelId() + " ,serial tx {} in block {} is conflict with local execution", serialExecutedTransactionsInBlock.get(i).getSDKTransactionHash(), block.getHash());
                return false;
            }
        }
        return true;
    }

    public void updateWorldStateSnapShootList(Block block) {
        BlockChangesSnapshots snapshoot = generateWorldStateSnapShootOfBlock(block);
        LedgerThreadLocalContext.blockChangesMap.addSnap(snapshoot);
    }


    public List<List<ExecutedTransaction>> packageExecutedTx4NewBlock() {
        Block blockBase = consensusContext.getBlock(consensusContext.getHighestQC().getBlockHash());
        List<ExecutedTransaction> txsBeforeSerialProcess = packageExecutedTransaction(blockBase);
        List<BlockChangesSnapshots> snapshootsList = new ArrayList<>();
        findWorldStateSnapShootForExecution(blockBase, snapshootsList);
        List<List<ExecutedTransaction>> txsAfterSerialProcess = processSerialTx(txsBeforeSerialProcess, snapshootsList);
        return txsAfterSerialProcess;
    }

    private List<ExecutedTransaction> packageExecutedTransaction(Block blockBase) {
        WaitPackageTxPool pool = waitPackageTxPoolMap.get(blockBase.getHash());
        if (pool == null) {
            log.info(ModuleClassification.TxM_TxECC_.toString() + blockBase.getBlockHeader().getChannelId() +
                    "no pool");
            return new ArrayList<>();
        }
        Long blockMaxSize = consensusContext.getChannel().getBlockMaxSize();
        List<ExecutedTransaction> outPutTransactions = pool.getTransactionMap(blockMaxSize);
        List<ExecutedTransaction> validTx = filterRepeatedTx(outPutTransactions);
        log.info(ModuleClassification.TxM_TxECC_.toString() + blockBase.getBlockHeader().getChannelId() +
                " output " + outPutTransactions.size() + " tx, with " + validTx.size() + " valid tx, based on block " +
                blockBase.getHash());
        return validTx;
    }

    /**
     * 过滤重复的交易
     *
     * @param transactionList 交易list
     * @return 不重复的交易列表
     */
    private List<ExecutedTransaction> filterRepeatedTx(List<ExecutedTransaction> transactionList) {
        List<ExecutedTransaction> transactions = new ArrayList<>();
        for (ExecutedTransaction transaction : transactionList) {
            if (ledgerMgr.ifTxExist(transaction.getSDKTransactionHash()) == false) {
                transactions.add(transaction);
            }
        }
        return transactions;
    }

    private List<List<ExecutedTransaction>> processSerialTx(List<ExecutedTransaction> transactionList, List<BlockChangesSnapshots> blockChangesSnapShots) {
        //mark serial
        Map<String, List<ExecutedTransaction>> keyMap = new HashMap<>();
        for (ExecutedTransaction tx : transactionList) {
            for (WorldStateModifyRecord record : tx.getModifiedWorldStateList()) {
                String key = record.getOldState() == null ? record.getNewState().getKey() : record.getOldState().getKey();
                if (keyMap.containsKey(key)) {
                    keyMap.get(key).add(tx);
                } else {
                    List<ExecutedTransaction> conflictTxs = new ArrayList<>();
                    conflictTxs.add(tx);
                    keyMap.put(key, conflictTxs);
                }
            }
            for (ChannelModifyRecord channelRecord : tx.getModifiedChannelRecordList()) {
                Object actionData = channelRecord.getNewChannel().getChannelChange().getActionData();
                if (actionData instanceof Member) {
                    String memberKey = ((Member) actionData).getPublicKey() + "-" + ((Member) actionData).getChannelId();
                    if (keyMap.containsKey(memberKey)) {
                        keyMap.get(memberKey).add(tx);
                    } else {
                        List<ExecutedTransaction> conflictTxs = new ArrayList<>();
                        conflictTxs.add(tx);
                        keyMap.put(memberKey, conflictTxs);
                    }
                }
            }
        }

        for (Map.Entry<String, List<ExecutedTransaction>> entry : keyMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                for (ExecutedTransaction tx : entry.getValue()) {
                    tx.setSerial(true);
                }
            }
        }
        List<SDKTransaction> needSerialProcessTx = new ArrayList<>();
        List<ExecutedTransaction> parallelTx = new ArrayList<>();
        for (ExecutedTransaction tx : transactionList) {
            if (tx.isSerial()) {
                needSerialProcessTx.add(tx.getSdkTransaction());
            } else {
                parallelTx.add(tx);
            }
        }
        List<ExecutedTransaction> serialTx = preExecuteFlowPooledTransaction(needSerialProcessTx, blockChangesSnapShots,
                StateAccessModeEnum.TRANS_CREATE_FOR_STEPQC_VERIFY);
        List<List<ExecutedTransaction>> txSet = new ArrayList<>();
        txSet.add(parallelTx);
        txSet.add(serialTx);
        return txSet;
    }

    /**
     * 执行一组写智能合约
     *
     * @param inputTransList
     * @return
     */
    public List<ExecutedTransaction> execute(List<SDKTransaction> inputTransList, StateAccessModeEnum stateAccessModeEnum, List<BlockChangesSnapshots> snapshots) {
        List<ExecutedTransaction> executedTransactionList = new ArrayList<>();
        if (CollectionUtils.isEmpty(inputTransList)) return executedTransactionList;

        // 如果是固定世界状态，可以并行处理，否则只能串行处理
        if (StateAccessModeEnum.TRANS_CREATE_FOR_FIXQC_VERIFY.equals(stateAccessModeEnum)) {
            //won't change in parallel execution. only copy once.
            long starttime = System.currentTimeMillis();
            List<CompletableFuture<ExecutedTransaction>> futures = inputTransList.stream().map(t -> CompletableFuture.supplyAsync(() -> {
                // 全部并行处理最长为其中一个任务的最长时间
                ExecutedTransaction rettx = new ExecutedTransaction();

                FutureTask<ExecutedTransaction> futureTX = new FutureTask<>(new Callable<ExecutedTransaction>() {
                    @Override
                    public ExecutedTransaction call() throws Exception {
                        try {
                            LedgerThreadLocalContext.blockChangesSnapshots.set(snapshots);
                            LedgerThreadLocalContext.stateAccessMode.set(stateAccessModeEnum);
                            LedgerThreadLocalContext.initValue();
                            ExecutedTransaction tx = executeTrans(t);
                            return tx;
                        } catch (Exception e) {
                            LedgerThreadLocalContext.stateAccessMode.set(StateAccessModeEnum.UNDEFINED);
                            log.debug("FutureTask LedgerThreadLocalContext.stateAccessMode.set(StateAccessModeEnum.UNDEFINED);{}", e.toString());
                            return new ExecutedTransaction();
                        }
                    }
                });
                new Thread(futureTX).start();

                try {
                    //先把超时的注释掉
                    //rettx = futureTX.get(channelTimeOut, TimeUnit.MILLISECONDS);
                    rettx = futureTX.get();
                } /*catch (TimeoutException e) {
                    futureTX.cancel(false);
                    rettx = buildExecutedTx(t, System.currentTimeMillis(), "ExecuteTime expired", "0");
                    log.info("futrueTask executeTrans time out------>process tx timeout: {}, \ntx:{}", channelTimeOut, rettx.toString());
                }*/catch (SandboxException e){
                    futureTX.cancel(false);
                    rettx = buildExecutedTx(t, System.currentTimeMillis(), e.getMessage(), "0");
                    log.error("futrueTask ex:", e);
                } catch (Exception e) {
                    futureTX.cancel(false);
                    rettx = buildExecutedTx(t, System.currentTimeMillis(), "交易执行异常", "0");
                    log.error("futrueTask ex:", e);
                }
                return rettx;
            }, executorTX)).collect(Collectors.toList());

            List<ExecutedTransaction> result = futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
            long endtime = System.currentTimeMillis();
            log.info("并行，在 {} (ms)时间内处理完 {} 个交易任务", endtime - starttime, inputTransList.size());

            for (ExecutedTransaction exTX : result) {
                executedTransactionList.add(exTX);
            }
        } else {
            List<BlockChangesSnapshots> snapshoots = BlockChangesSnapshots.clone(snapshots);
            LedgerThreadLocalContext.blockChangesSnapshots.set(snapshoots);
            long starttime = System.currentTimeMillis();

            for (SDKTransaction sdkTransaction : inputTransList) {
                // 串行不需要用线程池，直接增加get超时阻塞即可
                ExecutedTransaction rettx = new ExecutedTransaction();
                FutureTask<ExecutedTransaction> futureTX = new FutureTask<>(new Callable<ExecutedTransaction>() {
                    @Override
                    public ExecutedTransaction call() throws Exception {
                        try {
                            LedgerThreadLocalContext.stateAccessMode.set(stateAccessModeEnum);
                            LedgerThreadLocalContext.initValue();
                            ExecutedTransaction tx = executeTrans(sdkTransaction);
                            tx.setSerial(true);
                            return tx;
                        } catch (Exception ex) {
                            log.debug("{},{},Failed to execute transaction:CusCompiler{}", ModuleClassification.TxM_TxECC_.toString(), LogModuleCodes.SYSTEM_PLANTFORM_ACTION, ex.toString());
                            return new ExecutedTransaction();
                        }
                    }
                });
                new Thread(futureTX).start();
                try {
                    //先把外围超时注释掉
                    //rettx = futureTX.get(channelTimeOut, TimeUnit.MILLISECONDS);

                    rettx = futureTX.get();
                } /*catch (TimeoutException e) {
                    futureTX.cancel(false);
                    rettx = buildExecutedTx(sdkTransaction, System.currentTimeMillis(), "ExecuteTime expired", "0");
                    rettx.setSerial(true);
                    log.info("futrueTask executeTrans time out------>process tx timeout: {}, \ntx:{}", channelTimeOut, rettx.toString());
                }*/ catch (SandboxException e) {
                    futureTX.cancel(false);
                    rettx = buildExecutedTx(sdkTransaction, System.currentTimeMillis(), e.getMessage(), "0");
                    log.error("futrueTask ex: {}", e.toString());
                } catch (Exception e) {
                    futureTX.cancel(false);
                    rettx = buildExecutedTx(sdkTransaction, System.currentTimeMillis(), "交易执行异常", "0");
                    rettx.setSerial(true);
                    log.error("futrueTask ex: {}", e.toString());
                }
                executedTransactionList.add(rettx);
            }
            LedgerThreadLocalContext.stateAccessMode.set(StateAccessModeEnum.UNDEFINED);
            long endtime = System.currentTimeMillis();
            log.info("串行，在 {} (ms)时间内处理完 {} 个交易任务", endtime - starttime, inputTransList.size());
        }

        return executedTransactionList;
    }

    public void addPooledTxToPool(PooledTransaction transaction) {
        waitExecuteTxPool.add(transaction);
    }

    public void removeTxFromTransactionPool(Block committedBlock) {
        waitExecuteTxPool.remove(committedBlock);
    }

    public void removeStalePool(Block block) {
        log.info(ModuleClassification.TxM_TxECC_.toString() + consensusContext.getChannel().getChannelId() +
                " before removing stale pool, executedTransactionPoolMap size {}, waitPackageTxPoolMap size {}", executedTransactionPoolMap.size(), waitPackageTxPoolMap.size());
        Iterator<Map.Entry<String, ExecutedTransactionPool>> iterator = executedTransactionPoolMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ExecutedTransactionPool> poolEntry = iterator.next();
            if (poolEntry.getValue().getBlockBase().getBlockHeader().getHeight() < block.getBlockHeader().getHeight()) {
                iterator.remove();
            }
        }
        Iterator<Map.Entry<String, WaitPackageTxPool>> iterator1 = waitPackageTxPoolMap.entrySet().iterator();
        while (iterator1.hasNext()) {
            Map.Entry<String, WaitPackageTxPool> poolEntry = iterator1.next();
            if (poolEntry.getValue().getBlockBase().getBlockHeader().getHeight() < block.getBlockHeader().getHeight()) {
                iterator1.remove();
            }
        }
        log.info(ModuleClassification.TxM_TxECC_.toString() + consensusContext.getChannel().getChannelId() +
                " after removing stale pool, executedTransactionPoolMap size {}, waitPackageTxPoolMap size {}", executedTransactionPoolMap.size(), waitPackageTxPoolMap.size());

    }


    public List<SDKTransaction> outOfTransactionPool(Block block) {
        List<SDKTransaction> sdkTransactions = block.getSDKTransactions().parallelStream().filter(
                Tx -> !waitExecuteTxPool.ifExist(Tx.getHash())
        ).collect(Collectors.toList());
        return sdkTransactions;
    }
}
