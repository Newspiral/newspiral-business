package com.jinninghui.newspiral.common.entity.smartcontract;

import com.alibaba.fastjson.JSON;

/**
 *
 *
 * 智能合约操作状态
 */
public enum SmartContractOperationTypeEnum {

    SMARTCONTRACT_INSTALL("SMARTCONTRACT_INSTALL", "合约安装"),
    SMARTCONTRACT_UPGRADE("SMARTCONTRACT_UPGRADE", "合约升级");
    public final String code;
    public final String message;
    SmartContractOperationTypeEnum(String code, String message) {
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
