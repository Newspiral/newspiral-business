package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.state.WorldState;
import com.jinninghui.newspiral.common.entity.state.WorldStateResp;
import lombok.Data;

import java.util.Date;

@Data
public class StateModel {

    private Long id;

    private String key;

    private byte[] value;

    private String channelId;

    private Date packTimestamp;

    private String latestBlockHash;

    private Long latestBlockHeight;

    private String latestTransHash;

    public static StateModel createInstance(WorldState newState, String channelId, Block block, String latestTransHash) {
        StateModel model = new StateModel();
        model.setKey(newState.getKey());
        model.setValue(newState.getValue());
        model.setChannelId(channelId);
        model.setPackTimestamp(new Date());
        model.setLatestBlockHash(block.getHash());
        model.setLatestBlockHeight(block.getBlockHeader().getHeight());
        model.setLatestTransHash(latestTransHash);
        return model;
    }


    public WorldState toWorldState()
    {
        WorldState state =new WorldState();
        state.setKey(this.getKey());
        state.setValue(this.getValue()==null?"".getBytes():this.getValue());
        state.setLatestBlockHash(this.getLatestBlockHash());
        state.setLatestBlockHeight(this.getLatestBlockHeight());
        state.setLatestTransHash(this.getLatestTransHash());
        state.setUpdateTime(this.getPackTimestamp().getTime());
        return state;
    }

    public static WorldStateResp createWorldStateRespInstance(StateModel stateModel)
    {
        WorldStateResp worldStateResp=new WorldStateResp();
        worldStateResp.setValue(stateModel.getValue());
        worldStateResp.setKey(stateModel.getKey());
        worldStateResp.setUpdateTime(stateModel.getPackTimestamp().getTime());
        worldStateResp.setLatestBlockHash(stateModel.getLatestBlockHash());
        worldStateResp.setLatestBlockHeight(stateModel.getLatestBlockHeight());
        worldStateResp.setLatestTransHash(stateModel.getLatestTransHash());
        return worldStateResp;
    }

}