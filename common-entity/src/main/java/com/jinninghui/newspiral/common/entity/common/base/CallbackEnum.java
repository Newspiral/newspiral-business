package com.jinninghui.newspiral.common.entity.common.base;

/**
 * @version V1.0
 * @Title: CallbackEnum
 * @Package com.jinninghui.newspiral.common.entity.common.base
 * @Description:
 * @author: xuxm
 * @date: 2021/2/18 13:20
 */
public enum CallbackEnum {
    TRANSACTION("transaction");

    private CallbackEnum(String code){
        this.code = code;
    };

    private final String code;

    public String getCode() {
        return this.code;
    }
}
