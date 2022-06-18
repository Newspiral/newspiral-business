package com.jinninghui.newspiral.common.entity.chain;

import com.alibaba.fastjson.JSON;

/**
 * @author lida
 * @date 2019/7/11 17:50
 * 通道变更枚举
 */
public enum ChannelChangeEnum {

    DEFALUT("DEFALUT", "默认"),
    CHANNEL_PEER_ADD("CHANNEL_PEER_ADD", "通道节点新增"),
    CHANNEL_PEER_DEL("CHANNEL_PEER_DEL", "通道节点删除"),
    CHANNEL_PEER_CERT_FLAG_CHANGE("CHANNEL_PEER_CERT_FLAG_CHANGE", "通道节点证书状态变化");
    public final String code;
    public final String message;
    ChannelChangeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
    public String getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
