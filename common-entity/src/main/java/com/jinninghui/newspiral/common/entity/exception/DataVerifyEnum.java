package com.jinninghui.newspiral.common.entity.exception;

import com.alibaba.fastjson.JSON;

public enum DataVerifyEnum {
    UNSTRAT_CHECKING("UNSTRAT_CHECKING","此通道该高度的数据未开始校验"),
    START_CHECKING("START_CHECKING","开始校验"),
    IN_CHECKING("IN_CHECKING","正在校验"),
    COMPLETE_CHECKING("COMPLETE_CHECKING","校验完毕,数据正常"),
    ERROR_IN_CHECKING("ERROR_IN_CHECKING","校验出错"),
    UPTO_MAX_PROCESSORNUM("UPTO_MAX_PROCESSORNUM","当前校验并行数已达到上限，请稍后进行该校验");




    public final String code;
    public final String message;
    DataVerifyEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
    public String getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }


    public static String getMessage(String code) {
        DataVerifyEnum[] dataVerifyEnums = values();
        for (DataVerifyEnum dataVerifyEnum : dataVerifyEnums) {
            if (dataVerifyEnum.code.equals(code)) {
                return dataVerifyEnum.message;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
