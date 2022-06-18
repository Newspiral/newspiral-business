package com.jinninghui.newspiral.common.entity.member;

/**
 * @author whj
 * @date
 */
public enum RoleStateEnum {
    NORMOR(1,"正常"),
    DELETE(2,"业务删除");

    private final Integer code;
    private final String message;


    RoleStateEnum(Integer roleSate,String message) {
        this.code = roleSate;
        this.message = message;

    }
    public Integer getCode() {
        return code;
    }

    public String getMessage(){return message;}
}
