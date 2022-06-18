package com.jinninghui.newspiral.common.entity.identity;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

/**
 * @author lida
 * @date 2019/7/5 19:13
 * 身份标识类型枚举
 */
public enum IdentityTypeEnum implements Serializable {
    CHINA_PKI("CHINA_PKI", "基于国密的非对称加密体系");
    public final String code;
    public final String message;
    IdentityTypeEnum(String code, String message) {
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
