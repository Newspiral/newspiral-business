package com.jinninghui.newspiral.common.entity.chain;

import com.alibaba.fastjson.JSON;

/**
 * @author lida
 * @date 2019/7/11 17:50
 * 节点加入链的策略枚举
 */
public enum ConnectStateEnum {

    CONNECTED("CONNECTED", "正常连接"),
    DIS_CONNECTED("DIS_CONNECTED", "连接断开");
    public final String code;
    public final String message;
    ConnectStateEnum(String code, String message) {
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
