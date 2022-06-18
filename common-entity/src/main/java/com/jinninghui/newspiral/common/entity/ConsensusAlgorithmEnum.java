package com.jinninghui.newspiral.common.entity;

import com.alibaba.fastjson.JSON;

/**
 * @author lida
 * @date 2019/7/11 17:26
 */
public enum ConsensusAlgorithmEnum {

    NEWSPIRAL_HOT_STUFF("NewSpiralHotStuff", "NewSpiral工程化后的HotStuff共识算法");
    public final String code;
    public final String message;
    ConsensusAlgorithmEnum(String code, String message) {
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
