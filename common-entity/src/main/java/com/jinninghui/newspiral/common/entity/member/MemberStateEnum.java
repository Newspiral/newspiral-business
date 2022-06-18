package com.jinninghui.newspiral.common.entity.member;

import com.alibaba.fastjson.JSON;

public enum MemberStateEnum {
    VALID(0, "正常"),
    INVALID_CERTIFICATE(1, "证书过期"),
    FROZEN(2,"业务冻结"),
    DELETED(3, "业务删除");
    public final Integer code;
    public final String message;
    MemberStateEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    public Integer getCode() {
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
