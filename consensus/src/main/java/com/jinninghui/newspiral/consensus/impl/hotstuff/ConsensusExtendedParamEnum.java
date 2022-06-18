package com.jinninghui.newspiral.consensus.impl.hotstuff;

import com.alibaba.fastjson.JSON;

/**
 * @author lida
 * @date 2019/7/11 17:50
 * 节点共识阶段
 */
public enum ConsensusExtendedParamEnum {

    VIEW_TIMEOUT_MS("viewTimeoutMs","View的超时时间");
    public final String code;
    public final String message;
    ConsensusExtendedParamEnum(String code, String message) {
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
