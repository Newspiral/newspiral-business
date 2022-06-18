package com.jinninghui.newspiral.common.entity.smartcontract;

import com.alibaba.fastjson.JSON;

/**
 *
 *
 * 智能合约操作状态
 */
public enum SmartContractStateEnum {

    SMARTCONTRACT_VALID("1", "正常"),
    SMARTCONTRACT_FROZEN("2", "冻结"),
    SMARTCONTRACT_DESTORIED("3", "销毁");
    public final String code;
    public final String message;
    SmartContractStateEnum(String code, String message) {
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
