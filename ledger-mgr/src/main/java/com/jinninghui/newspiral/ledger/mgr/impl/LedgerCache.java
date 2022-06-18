package com.jinninghui.newspiral.ledger.mgr.impl;

import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.state.WorldState;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.ChannelModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.StateModel;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.ChannelModelMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.StateModelMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class LedgerCache {

    @Autowired
    private ChannelModelMapper channelModelMapper;
    @Autowired
    private StateModelMapper stateModelMapper;
    private int cnt = 8;
    Map<String, List<Map<String, byte[]> > > stateMap = new ConcurrentHashMap<>();
    public void init() {
        log.info("###################账本管理开始初始化#################");
        List<Channel> channels = readAllChannels();
        for (Channel channel : channels) {
            List<Map<String, byte[]>> mapList = new ArrayList<>();
            for (int i = 0 ; i < cnt; i++) {
                Map<String, byte[]> map = new ConcurrentHashMap<>();
                mapList.add(map);
            }
            stateMap.put(channel.getChannelId(), mapList);
            readAllState(channel.getChannelId());
        }
        log.info("###################账本管理初始化完毕#################");
    }

    private List<Channel> readAllChannels() {
        List<ChannelModel> channelModels = channelModelMapper.selectAll();
        List<Channel> channels = new ArrayList<>();
        for (ChannelModel model : channelModels) {
            channels.add(model.toChannel());
        }
        return channels;
    }
    private void readAllState(String channelId) {
        log.info("###################开始读取所有世界状态#################");
        List<WorldState> worldStates = selectAllState(channelId);
        List<Map<String, byte[]>> mapList = stateMap.get(channelId);
        if (null == mapList) {
            return;
        }
        log.info("###################读取所有世界状态完毕#################,数量:{}",worldStates.size());
        int i=0;
        for(WorldState worldState: worldStates)
        {
            int index = Math.abs(worldState.getKey().hashCode()) % cnt;
            i++;
            mapList.get(index).put(worldState.getKey(), worldState.getValue());
        }


        log.info("###################存储世界状态完毕，初始化{}个世界状态#################",i);


    }
    private List<WorldState> selectAllState(String channelId) {
        List<StateModel> stateModels = stateModelMapper.selectAll(channelId);
        List<WorldState> worldStates = new ArrayList<>();
        for (StateModel stateModel : stateModels) {
            worldStates.add(stateModel.toWorldState());
        }
        return worldStates;
    }

    public void putState(String channelId, WorldState worldState) {
        List<Map<String, byte[]>> mapList = stateMap.get(channelId);
        if (null == mapList) {
            return;
        }
        mapList.get(Math.abs(worldState.getKey().hashCode()) % cnt).put(worldState.getKey(),worldState.getValue());
    }

    public void batchUpdateState(String channelId, List<WorldState> worldStates) {
        List<Map<String, byte[]>> mapList = stateMap.get(channelId);
        if (null == mapList) {
            return;
        }
        for (WorldState state : worldStates) {
               mapList.get(Math.abs(state.getKey().hashCode()) % cnt).put(state.getKey(), state.getValue());
        }
    }

    public byte[] getState(String channelId, String key) {
        List<Map<String, byte[]>> mapList = stateMap.get(channelId);
        if (null == mapList) {
            return null;
        }
        return mapList.get(Math.abs(key.hashCode()) % cnt).get(key);
    }
    public void deleteState(String channelId, String key) {
        List<Map<String, byte[]>> mapList = stateMap.get(channelId);
        if (null == mapList) {
            return;
        }
        mapList.get(Math.abs(key.hashCode()) % cnt).remove(key);
    }

    public void batchDeleteState(String channelId, List<WorldState> worldStates) {
        List<Map<String, byte[]>> mapList = stateMap.get(channelId);
        if (null == mapList) {
            return;
        }
        for (WorldState state : worldStates) {
            mapList.get(Math.abs(state.getKey().hashCode()) % cnt).remove(state.getKey(), state.getValue());
        }
    }
}
