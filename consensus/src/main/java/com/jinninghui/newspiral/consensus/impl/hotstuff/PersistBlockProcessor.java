package com.jinninghui.newspiral.consensus.impl.hotstuff;

import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.ledger.mgr.LedgerMgr;
import com.jinninghui.newspiral.transaction.mgr.TransactionMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class PersistBlockProcessor extends TimerTask {
    /**
     * this is a reference of Map of NewSpiralHotStuffProcessors managed by ConsensusMsgProcessorImpl.
     * When a new channel is created, this map will be changed and can be sensed by this thread.
     * So this thread depends on the map of NewSpiralHotStuffProcessors
     */
    public Map<String, NewSpiralHotStuffProcessor> newSpiralHotStuffHashMap;

    private Map<String, Long> lastPersistCachedHeightMap = new HashMap<>();

    private Map<String, Long> lastPersistCommitHeightMap = new HashMap<>();

    @SofaReference
    private TransactionMgr transactionMgr;

    @SofaReference
    private LedgerMgr ledgerMgr;


    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void init(Map<String, NewSpiralHotStuffProcessor> newSpiralHotStuffHashMap) {
        this.newSpiralHotStuffHashMap = newSpiralHotStuffHashMap;
    }

    @Override
    public void run() {
        try {
            for (Map.Entry<String, NewSpiralHotStuffProcessor> entry : newSpiralHotStuffHashMap.entrySet()) {
                NewSpiralHotStuffProcessor processor = entry.getValue();
                if (hasAvailableBlockToPersist(processor)) {
                    ConsensusContext context = processor.consensusContext;
                    updatePersistHeight(context);
                    executorService.execute(() ->
                            {
                                insertCachedBlock(context);
                                insertCommittedBlock(context);
                            }

                    );
                }
            }
        } catch (Exception ex) {
            log.error("insert block error", ex);
        }
    }

    private void insertCachedBlock(ConsensusContext consensusContext) {
        Map<String, Block> cachedBlockMap = consensusContext.getCachedBlockMap();
        int size = cachedBlockMap.size();
        if (size > 0) {
            Map<String, Block> map = new HashMap<>(size);
            map.putAll(cachedBlockMap);
            map.entrySet().stream()
                    .sorted(comparator)
                    .forEach(entry -> {
                        Block value = entry.getValue();
                        ledgerMgr.insertCacheBlock(value.getBlockHeader().getChannelId(), value);
                        //cachedBlockMap.remove(entry.getKey());
                    });
            map.clear();
        }
    }


    private void insertCommittedBlock(ConsensusContext consensusContext) {
        Map<String, Block> committedBlockMap = consensusContext.getCommittedBlockMap();
        int size = committedBlockMap.size();
        if (size > 0) {
            Map<String, Block> map = new HashMap<>(size);
            map.putAll(committedBlockMap);
            map.entrySet().stream()
                    .sorted(comparator)
                    .forEach(entry -> {
                        Block block = entry.getValue();
                        ledgerMgr.persistBlock(block);
                        transactionMgr.processBlockConsensusSuccess(block);
                        if (consensusContext.getPersistedBlockHeight() < block.getBlockHeader().getHeight()) {
                            consensusContext.setPersistedBlockHeight(block.getBlockHeader().getHeight());
                        }
                        if (consensusContext.getBlockHeight() != consensusContext.getPersistedBlockHeight()) {
                            log.info(ModuleClassification.LedM_LMI_.toString() + "," + consensusContext.getChannel().getChannelId() + " ,asynchronized block insertion:blockheight " + consensusContext.getBlockHeight() +
                                    " persisted block height " + consensusContext.getPersistedBlockHeight());
                        }
                        committedBlockMap.remove(entry.getKey());
                        consensusContext.cleanUnusedDataAfterPersist(block);
                    });
            map.clear();

        }
    }

    private boolean hasAvailableBlockToPersist(NewSpiralHotStuffProcessor newSpiralHotStuffProcessor) {
        ConsensusContext consensusContext = newSpiralHotStuffProcessor.consensusContext;

        if (null == lastPersistCachedHeightMap.get(consensusContext.getChannel().getChannelId())) {
            try {
                lastPersistCachedHeightMap.put(consensusContext.getChannel().getChannelId(), 0L);
                lastPersistCommitHeightMap.put(consensusContext.getChannel().getChannelId(), 0L);
            } catch (Exception ex) {
                log.error(ModuleClassification.LedM_PBP_.toString() + consensusContext.getChannel().getChannelId() +
                        " unable to initial map.");
                //todo:优雅关闭
                System.exit(0);
            }
            return true;
        } else {
            Long lastPersistCachedBlockHeight = lastPersistCachedHeightMap.get(consensusContext.getChannel().getChannelId());
            Long lastPersistCommittedBlockHeight = lastPersistCommitHeightMap.get(consensusContext.getChannel().getChannelId());
            if (lastPersistCachedBlockHeight < consensusContext.getCachedBlockHeight()) {
                return true;
            }
            return lastPersistCommittedBlockHeight < consensusContext.getBlockHeight();
        }
    }

    private void updatePersistHeight(ConsensusContext consensusContext) {
        lastPersistCachedHeightMap.put(consensusContext.getChannel().getChannelId(), consensusContext.getCachedBlockHeight());
        lastPersistCommitHeightMap.put(consensusContext.getChannel().getChannelId(), consensusContext.getBlockHeight());
    }

    /*private void insertCachedBlock(ConsensusContext consensusContext) {
        List<Map.Entry<String, Block>> list = new ArrayList<>(consensusContext.getCachedBlockMap().entrySet());
        Collections.sort(list, comparator);
        List<String> hashSet = new ArrayList<>();
        for (Map.Entry<String, Block> entry : list) {
            Block value = entry.getValue();
            ledgerMgr.insertCacheBlock(value.getBlockHeader().getChannelId(), value);
            hashSet.add(entry.getKey());
        }
        for (String hash : hashSet) {
            consensusContext.getCachedBlockMap().remove(hash);
        }
        list.clear();
        hashSet.clear();
    }*/

    /*private void insertCommittedBlock(ConsensusContext consensusContext) {
        Set<Map.Entry<String, Block>> set = consensusContext.getCommittedBlockMap().entrySet();
        List<Map.Entry<String, Block>> list = new ArrayList<>(set);
        Collections.sort(list, comparator);
        List<String> hashSet = new ArrayList<>();
        for (Map.Entry<String, Block> entry : consensusContext.getCommittedBlockMap().entrySet()) {
            String hashStr = entry.getKey();
            Block block = entry.getValue();
                *//*try {
                    executorService.submit(
                            () -> persistBlock(entry.getValue(), ledgerMgr, consensusContext)
                    ).get();
                } catch (Exception ex) {
                    log.error(ModuleClassification.LedM_LMI_ + "Exception in persist block:" + entry.getValue().getHash() + ",exception:", ex);
                }*//*
            ledgerMgr.persistBlock(block);
            //ledgerMgr.cacheTxShort(entry.getValue());
            transactionMgr.processBlockConsensusSuccess(block);
            hashSet.add(hashStr);
            if (consensusContext.getPersistedBlockHeight() < block.getBlockHeader().getHeight()) {
                consensusContext.setPersistedBlockHeight(block.getBlockHeader().getHeight());
            }
            //for debug
            if (consensusContext.getBlockHeight() != consensusContext.getPersistedBlockHeight()) {
                log.info(ModuleClassification.LedM_LMI_.toString() + "," + consensusContext.getChannel().getChannelId() + " ,asynchronized block insertion:blockheight " + consensusContext.getBlockHeight() +
                        " persisted block height " + consensusContext.getPersistedBlockHeight());
            }
        }
        for (String hash : hashSet) {
            consensusContext.getCommittedBlockMap().remove(hash);
        }
        set.clear();
        list.clear();
        hashSet.clear();
    }*/

    private Comparator<Map.Entry<String, Block>> comparator = new Comparator<Map.Entry<String, Block>>() {
        @Override
        public int compare(Map.Entry<String, Block> o1, Map.Entry<String, Block> o2) {
            return o1.getValue().getBlockHeader().getHeight() < o2.getValue().getBlockHeader().getHeight() ? -1 : 1;
        }
    };
}
