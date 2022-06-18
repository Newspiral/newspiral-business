package com.jinninghui.newspiral.common.entity.task;

public enum LoggerEnum {
    ERROR("ERROR"),
    WARN("WARN"),
    INFO("INFO"),
    DEBUG("DEBUG");

    private String level;
    LoggerEnum(String level){
        this.level = level;
    }

    public String getLevel() {
        return level;
    }
}
