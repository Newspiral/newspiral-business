package com.jinninghui.newspiral.common.entity.Enum;

public enum  PeerActionTypeEnum {

    IN_OUT("IN_OUT","进入退出通道操作"),
    FROZEN("FROZEN","冻结解冻节点操作");


    private String code;
    private String message;

    PeerActionTypeEnum(String code,String message){
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
