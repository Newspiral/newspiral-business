package com.jinninghui.newspiral.common.entity.chain;

import com.alibaba.fastjson.JSON;

/**
 * @author lida
 * @date 2019/7/11 17:50
 * 节点加入链的策略枚举
 */
public enum PeerServiceTypeEnum {

    FOR_PEER("FOR_PEER", "为其他节点提供的服务"),
    FOR_SDK("FOR_SDK", "为SDK提供的服务");
    public final String code;
    public final String message;
    PeerServiceTypeEnum(String code, String message) {
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
