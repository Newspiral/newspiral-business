package com.jinninghui.newspiral.common.entity.member;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class Auth implements Serializable {

    @ApiModelProperty(value = "权限id")
    private  Integer authId;

    @ApiModelProperty(value = "权限名称")
    private String authName;

    @ApiModelProperty(value = "权限编码")
    private String authCode;

    @ApiModelProperty(value = "权限描述")
    private String authDescription;


    public Auth clone()
    {
        Auth auth=new Auth();
        auth.setAuthId(this.authId);
        auth.setAuthCode(this.authCode);
        auth.setAuthDescription(this.authDescription);
        auth.setAuthName(this.authName);
        return auth;
    }

}
