package com.jinninghui.newspiral.common.entity.ddos;

public enum DDOSEnum {
    BATCH_MODIFY_LIMIT_PARAM_SUCCESS("batch modify limit param success","批量修改限流参数成功了"),
    BY_CLASS_MODIFY_LIMIT_PARAM_SUCCESS("by class modify limit param success","按照类名批量修改限流参数成功了"),
    ADD_IP_INTO_IP_CONSTRAINT_LIST_SECCESS("add ip into ip constraint list success","添加限制ip操作信息成功了"),
    MODIFY_IP_IN_IP_CONSTRAINT_LIST_SECCESS("modify ip in ip constraint list success","修改限制ip操作信息成功了");
    //add ip into ip_constraint_list success  modify ip in ip_constraint_list success
    private String code;
    private String message;
    DDOSEnum(String code,String message){
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
