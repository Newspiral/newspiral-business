package com.jinninghui.newspiral.common.entity.common.logmodule;

/**
 * @version V1.0
 * @Title: LogModuleCodes
 * @Package com.jinninghui.newspiral.common.entity.common.logmodule
 * @Description:
 * @author: xuxm
 * @date: 2020/4/7 10:10
 */
public enum LogModuleCodes {
    SYSTEM_PLANTFORM_ACTION("001", "SYSTEM_PLANTFORM_ACTION"),
    SYSTEM_USER_ACTION("002", "SYSTEM_USER_ACTION"),
    SYSTEM_SMART_CONTRACT_ACTION("003", "SYSTEM_SMART_CONTRACT_ACTION"),
    BUSINESS_SMART_CONTRACT_ACTION("004", "SANDBOX_BUSINESS_CONTRACT_ACTION");

    private final String code;
    private final String msg;

    private LogModuleCodes(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }
}
