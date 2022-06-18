package com.jinninghui.newspiral.ledger.mgr;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.jinninghui.newspiral.common.entity.chain.ChannelModifyRecord;
import com.jinninghui.newspiral.common.entity.state.WorldStateModifyRecord;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author lida
 * @date 2019/9/26 10:17
 * 账本持久化所需使用的ThreadLocalContext
 * TODO 也要同步处理节点初始化
 */
public class LedgerThreadLocalContext {
    public static ThreadLocal<StateAccessModeEnum> stateAccessMode = new TransmittableThreadLocal<>();
    /**
     * key:the key of worldstate
     * value:the worldstateModifyRecord
     * when txexecutor execute a transaction, all the worldstateModifyRecord will be store here by its key.
     */
    public static ThreadLocal<Map<String, WorldStateModifyRecord>> currTransModifiedStateMap = new TransmittableThreadLocal<>();


    public static ThreadLocal<Queue<ChannelModifyRecord>> currTransModifiedChannels = TransmittableThreadLocal.withInitial(ConcurrentLinkedQueue::new);

    /**
     * keys of worldstates changed by a transaction
     */
    public static ThreadLocal<Set<String>> executeWorldStateKeyList = new TransmittableThreadLocal<>();


    /**
     * all the worldState snapShoot of cache blocks, which are needed when preExecuting transaction.
     */
    public static BlockChangesMap blockChangesMap = new BlockChangesMap();

    /**
     * snapShoots on which transaction is executed.
     */
    public static ThreadLocal<List<BlockChangesSnapshots>> blockChangesSnapshots = new TransmittableThreadLocal<>();


    /**
     * 与wholeTransModifiedStateMap作用类似，用于系统交易修改channel
     * 系统交易需要共识的，都需要通过修改Channel,而一个通道只有一个Channel，所以不需要Map了
     * key为channelId
     * //todo:检查这个结构体是否有用，检查当前地通道管理机制是否有问题。
     */
    //public static ThreadLocal<ChannelModifyRecord> wholeTransModifiedChannel = new ThreadLocal<>();

    /**
     * 与currTransModifiedStateMap作用类似，用于系统交易修改channel
     * key为channelId
     */
    //public static ThreadLocal<ChannelModifyRecord> currTransModifiedChannel = new ThreadLocal<>();

    public static ThreadLocal<String> currChannelId = new TransmittableThreadLocal<>();

    /**
     * 静态语句块初始化各个对象
     */
    /*static {
        currTransModifiedStateMap.set(new HashMap<>());
        modifiedWorldStateKeyList.set(new HashSet<>());
        wholeTransModifiedChannel.set(null);
        currTransModifiedChannel.set(null);
        currChannelId.set(null);
        executedWorldStateSnapShoots.set(null);
    }*/
    public static ThreadLocal<Map<String, WorldStateModifyRecord>> getCurrTransModifiedStateMap() {
        if (currTransModifiedStateMap.get() == null) {
            currTransModifiedStateMap.set(new ConcurrentHashMap<>());
        }
        return currTransModifiedStateMap;
    }

    public static ThreadLocal<Queue< ChannelModifyRecord>> getCurrTransModifiedChannelsQueue() {
        if (currTransModifiedStateMap.get() == null) {
            currTransModifiedStateMap.set(new ConcurrentHashMap<>());
        }
        return currTransModifiedChannels;
    }

    public static ThreadLocal<Set<String>> getExecuteWorldStateKeyList() {
        if (executeWorldStateKeyList.get() == null) {
            executeWorldStateKeyList.set(new CopyOnWriteArraySet<>());
        }
        return executeWorldStateKeyList;
    }

    public static void initValue(){
        if (currTransModifiedStateMap.get() == null) {
            currTransModifiedStateMap.set(new ConcurrentHashMap<>());
        }
        if (currTransModifiedStateMap.get() == null) {
            currTransModifiedStateMap.set(new ConcurrentHashMap<>());
        }
        if (executeWorldStateKeyList.get() == null) {
            executeWorldStateKeyList.set(new CopyOnWriteArraySet<>());
        }
    }
}
