package com.jinninghui.newspiral.common.entity.chain;

import com.alibaba.fastjson.JSON;

/**
 * @author lida
 * @date 2019/7/11 17:50
 * 节点共识阶段
 */
public enum PeerStageEnum {

    PEER_STOP("PEER_STOP","节点停止中"),
    PEER_RUN("PEER_RUN", "节点运行中");
    public final String code;
    public final String message;
    PeerStageEnum(String code, String message) {
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
