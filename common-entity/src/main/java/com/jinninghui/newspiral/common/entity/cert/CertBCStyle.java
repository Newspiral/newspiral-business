package com.jinninghui.newspiral.common.entity.cert;

/**
 * @version V1.0
 * @Title: CertBCStyle
 * @Package com.jinninghui.newspiral.common.entity.cert
 * @Description:
 * @author: xuxm
 * @date: 2021/2/3 11:46
 */
public enum CertBCStyle {

    C("C","国家代码"),
    ST("ST","省份"),
    L("L","地区"),
    STREET("STREET","街道"),
    O("O","组织"),
    OU("OU","组织名称"),
    CN("CN","身份"),
    T("T","主题"),
    SERIALNUMBER("SERIALNUMBER","设备序列号"),
    TELEPHONE_NUMBER("TelephoneNumber","电话号码"),
    ORGANIZATION_IDENTIFIER("2.5.4.97","组织标识符"),
    NAME("Name","名字"),
    TYPE("TYPE","类型"),
    ;
    public final String code;
    public final String message;

    CertBCStyle(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }
}
