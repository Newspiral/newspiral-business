package com.jinninghui.newspiral.common.entity.Enum;

public enum VersionEnum {

    FABRIC("Fabric1.0"),
    NEWSPIRAL("Version1.0");

    private VersionEnum(String code){
        this.code = code;
    };

    private final String code;

    public String getCode() {
        return this.code;
    }

}
