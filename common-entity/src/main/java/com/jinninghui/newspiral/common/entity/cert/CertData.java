package com.jinninghui.newspiral.common.entity.cert;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;

/**
 * @author
 * @date
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CertData {
    /**
     * 国家 C
     */
    @ApiModelProperty(value = "国家")
    private String country;
    /**
     * 省份 ST
     */
    @ApiModelProperty(value = "省份")
    private String state;
    /**
     * 城市 L
     */
    @ApiModelProperty(value = "城市")
    private String locality;
    /**
     * 街道 STREET
     */
    @ApiModelProperty(value = "街道")
    private String street;
    /**
     * 组织 O
     */
    @NotBlank
    @ApiModelProperty(value = "组织")
    private String organization;
    /**
     * 组织名称 OU
     */
    @ApiModelProperty(value = "组织名称")
    private String organizationalUnit;
    /**
     * 身份 CN
     */
    @ApiModelProperty(value = "身份，如IP地址")
    private String commonName;
    /**
     * 主题 T
     */
    @ApiModelProperty(value = "主题")
    private String title;
    /**
     * 设备序列号 SERIALNUMBER
     */
    @ApiModelProperty(value = "设备序列号")
    private String serialNumber;
    /**
     * 电话号码
     */
    @ApiModelProperty(value = "电话号码")
    private String telephoneNumber;
    /**
     * 组织唯一标识符
     */
    @ApiModelProperty(value = "组织唯一标识符")
    private String organizationIdentifier;
    /**
     * 名字
     */
    @ApiModelProperty(value = "名字")
    private String name;
    @ApiModelProperty(value = "开始时间，默认创建时间")
    private Long notBefore;
    @ApiModelProperty(value = "结束时间，默认距离开始时间的后5年")
    private Long notAfter;

    public CertData(String country, String state, String locality, String organization, String commonName, String serialNumber) {
        this.country = country;
        this.state = state;
        this.locality = locality;
        this.organization = organization;
        this.commonName = commonName;
        this.serialNumber = serialNumber;
    }

    public CertData(String country, String state, String locality, String organization, String commonName) {
        this.country = country;
        this.state = state;
        this.locality = locality;
        this.organization = organization;
        this.commonName = commonName;
    }

    @Override
    public String toString() {
        StringBuffer string = new StringBuffer();
        setParam(string,CertBCStyle.C,country);
        setParam(string,CertBCStyle.ST,state);
        setParam(string,CertBCStyle.L,locality);
        setParam(string,CertBCStyle.STREET,street);
        setParam(string,CertBCStyle.OU,organizationalUnit);
        setParam(string,CertBCStyle.O,organization);
        setParam(string,CertBCStyle.T,title);
        setParam(string,CertBCStyle.CN,commonName);
        if(!StringUtils.isEmpty(string.toString())){
            return string.substring(0,string.lastIndexOf(","));
        }
        return string.toString();
    }

    public String toStringDetail() {
        StringBuffer string = new StringBuffer();
        setParam(string,CertBCStyle.C,country);
        setParam(string,CertBCStyle.ST,state);
        setParam(string,CertBCStyle.L,locality);
        setParam(string,CertBCStyle.STREET,street);
        setParam(string,CertBCStyle.OU,organizationalUnit);
        setParam(string,CertBCStyle.O,organization);
        setParam(string,CertBCStyle.CN,commonName);
        setParam(string,CertBCStyle.T,title);
        setParam(string,CertBCStyle.SERIALNUMBER,serialNumber);
        setParam(string,CertBCStyle.TELEPHONE_NUMBER,telephoneNumber);
        setParam(string,CertBCStyle.ORGANIZATION_IDENTIFIER,organizationIdentifier);
        setParam(string,CertBCStyle.NAME,name);
        if(!StringUtils.isEmpty(string.toString())){
            return string.substring(0,string.lastIndexOf(", "));
        }
        return string.toString();
    }

    public void setParam(StringBuffer string,CertBCStyle bcStyle , String bcValue){
        if(!StringUtils.isEmpty(bcValue)){
            string.append(bcStyle.code + "=" + bcValue + ", ");
        }
    }
}
