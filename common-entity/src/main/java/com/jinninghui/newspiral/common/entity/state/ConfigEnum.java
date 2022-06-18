package com.jinninghui.newspiral.common.entity.state;

public enum ConfigEnum {

    NS_SYSCONTRACTID("NS_SYSCONTRACTID","com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor.SystemSmartContract");

    private String code;
    private String message;

    private ConfigEnum(String code, String message){
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
