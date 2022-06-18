package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import com.alibaba.fastjson.JSON;
import com.jinninghui.newspiral.common.entity.consensus.GenericMsg;


public class MessageModel {
    private Long id;

    private Long viewNumber;

    private String channelId;

    private String content;

    public Long getId () { return id; }

    public void setId (Long id) {
        this.id = id;
    }

    public Long getViewNumber () { return viewNumber; }

    public void setViewNumber (Long viewNumber) {
        this.viewNumber = viewNumber;
    }

    public String getChannelId() { return channelId;}

    public void setChannelId(String channelId) { this.channelId=channelId;}

    public String getContent () { return content; }

    public void setContent (String content) {
        this.content = content;
    }

    public GenericMsg toGenericMsg() {
        GenericMsg genericMsg = JSON.parseObject(this.content, GenericMsg.class);
        return genericMsg;
    }
}
