package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import java.util.Date;

public class StateAttachModel {


    private String channelId;

    private String stateKey;

    private Date latedtUpdateTime;

    private String latestBlockHash;


    public static StateAttachModel createAttach(StateModel stateModel,String blockHash){
        StateAttachModel model = new StateAttachModel();
        model.setChannelId(stateModel.getChannelId());
        model.setStateKey(stateModel.getKey());
        model.setLatestBlockHash(blockHash);
        model.setLatedtUpdateTime(new Date());
        return model;
    }


    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getStateKey() {
        return stateKey;
    }

    public void setStateKey(String stateKey) {
        this.stateKey = stateKey;
    }

    public Date getLatedtUpdateTime() {
        return latedtUpdateTime;
    }

    public void setLatedtUpdateTime(Date latedtUpdateTime) {
        this.latedtUpdateTime = latedtUpdateTime;
    }

    public String getLatestBlockHash() {
        return latestBlockHash;
    }

    public void setLatestBlockHash(String latestBlockHash) {
        this.latestBlockHash = latestBlockHash;
    }
}
