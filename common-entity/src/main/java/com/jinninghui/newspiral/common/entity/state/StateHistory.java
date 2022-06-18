package com.jinninghui.newspiral.common.entity.state;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class StateHistory {

    //单例对象,用于记录通道对应的转化高度
    private final static Map<String,Long> channelTransferHeight = new ConcurrentHashMap<>();
    //单例对象，用来记录需要删除的通道集合
    private final static Set<String> deleteChannelIdSet = new ConcurrentSkipListSet<>();
    //用于记录最新的blockId
    private final static Map<String,Long> channelLatestBlockId = new ConcurrentHashMap<>();
    // 用于记录删通道之后，已经解析的高度,如果下次该通道重新加入，则使用这个高度
    private final static Map<String,Long> markDelChannelParseHeight = new ConcurrentHashMap<>();

    /**
     * 用于给和addMyselfTochannel调用
     */
    public static Map<String, Long> getChannelTransferHeight() {
        return channelTransferHeight;
    }

    public static Set<String> getDeleteChannelIdSet(){
        return deleteChannelIdSet;
    }

    public static Map<String,Long> getChannelLatestBlockId(){return channelLatestBlockId;}

    public static Map<String,Long> getMarkDelChannelParseHeight(){return markDelChannelParseHeight;}

}
