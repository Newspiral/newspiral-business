package com.jinninghui.newspiral.ledger.mgr;

import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.chain.ChannelChange;
import com.jinninghui.newspiral.common.entity.common.persist.PersistConstant;
import com.jinninghui.newspiral.common.entity.state.WorldStateModifyRecord;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author lida
 * @date 2019/10/9 17:06
 * 世界状态快照
 * 持久化的世界状态+本类中的所有世界状态取值，就得到的最新的世界状态
 */
@ToString
public class BlockChangesSnapshots {

    /**
     * 此快照为blockHashStr对应的区块执行后的快照
     */
    @Getter @Setter
    String blockHashStr;

    /**
     * 不能直接缓存WorldState，因为存在删除State的情况，当然也可以删除单独存储，使用WorldStateModifyRecord便于复用Block中的数据结构
     * key为世界状态的key
     */
    @Getter @Setter
    Map<String, WorldStateModifyRecord> worldStateModifyRecordMap = new ConcurrentHashMap<>();

    /**
     * 用于存放通道的动作标签
     */
    @Getter @Setter
    public ConcurrentLinkedQueue<Pair<PersistConstant.PersistTarget, Channel>> channelChanges = new ConcurrentLinkedQueue<>();
    /**
     * 通道变更
     */
/*    @Getter @Setter
    ChannelModifyRecord newModifyRecord= new ChannelModifyRecord();*/
    public static BlockChangesSnapshots createEmptyInstance()
    {
        BlockChangesSnapshots inst = new BlockChangesSnapshots();
        inst.setBlockHashStr(null);
        return inst;
    }

    public WorldStateModifyRecord get(String key)
    {
        return worldStateModifyRecordMap.get(key);
    }

    public void put(String key,WorldStateModifyRecord record)
    {
        worldStateModifyRecordMap.put(key,record);
    }

    public void remove(String key)
    {
        worldStateModifyRecordMap.remove(key);

    }

    public Set<String> keySet()
    {
        return worldStateModifyRecordMap.keySet();
    }

    /**
     * 半深拷贝：map新弄了一个，map中的元素也重新加一遍，但是map中的value对象并不需要深拷贝
     * @return
     */
    public BlockChangesSnapshots clone()
    {
        BlockChangesSnapshots newInstance = new BlockChangesSnapshots();
        newInstance.getWorldStateModifyRecordMap().putAll(this.getWorldStateModifyRecordMap());
        newInstance.setChannelChanges(this.getChannelChanges());
        newInstance.setBlockHashStr(this.getBlockHashStr());
        return newInstance;
    }
    public static List<BlockChangesSnapshots> clone(List<BlockChangesSnapshots> snapshoots) {
        List<BlockChangesSnapshots> copy = new ArrayList<>();
        for (BlockChangesSnapshots snapshoot : snapshoots) {
            copy.add(snapshoot.clone());
        }
        return copy;
    }

    public void addChangesToChannel(ChannelChange channelChange, Channel channel) {
        channelChanges.offer(new Pair<>(channelChange.getActionTag(), channel));
    }
}
