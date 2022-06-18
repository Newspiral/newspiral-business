package com.jinninghui.newspiral.common.entity.state;

public enum SyncStateHistoryThreadEnum {
    SYNC("SYNC","线程同步中"),
    SYNC_ERROR("SYNC_ERROR","线程同步出错");

    private String code;
    private String message;

    private SyncStateHistoryThreadEnum(String code, String message){
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
