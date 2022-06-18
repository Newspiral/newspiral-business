package com.jinninghui.newspiral.ledger.mgr;

import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.common.persist.PersistConstant;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author lida
 * @date 2019/10/9 17:11
 * 账本模块需要缓存多个WorldStateSnapshoot,使用本对象封装
 * 这个类的各个方法，应该不会有并发调用的情况，稳妥起见，加个锁吧，没有竞争JVM会优化它的
 */
@Slf4j
public class BlockChangesMap {

    //不给Get和Set方法避免瞎用
    Map<String, BlockChangesSnapshots> snapshootMap =  new ConcurrentHashMap<>();;

    /**
     * 获得最新的快照，一般用于构建更新的快照
     * @return
     */
    synchronized public BlockChangesSnapshots getNeedSnapShootByBlockHash(String hashStr)
    {
        BlockChangesSnapshots snapshoot = this.snapshootMap.get(hashStr);
        return snapshoot;
    }



    /**
     * 直接在最后追加一个新快照
     * @param blockChangesSnapShots
     */
    synchronized public void addSnap(BlockChangesSnapshots blockChangesSnapShots) {
        if (snapshootMap.containsKey(blockChangesSnapShots.getBlockHashStr())) {
            log.info("try to add snapshoot twice");
        }
        snapshootMap.put(blockChangesSnapShots.getBlockHashStr(), blockChangesSnapShots);
    }


    synchronized public void removeSnap(String blockHashStr) {
        this.snapshootMap.remove(blockHashStr);
    }


    public List<Channel> findInMap(PersistConstant.PersistTarget persistTarget) {
        List<Channel> channels = new ArrayList<>();
        for (BlockChangesSnapshots snapshots: snapshootMap.values()) {
            ConcurrentLinkedQueue<Pair<PersistConstant.PersistTarget, Channel>> channelChanges = snapshots.getChannelChanges();
            for (int i = 0; i < channelChanges.size(); i++) {
                Pair<PersistConstant.PersistTarget, Channel> pair = channelChanges.peek();
                if (pair.getKey().equals(persistTarget)) {
                    channels.add(pair.getValue());
                }
            }
        }
        return channels;
    }
}
