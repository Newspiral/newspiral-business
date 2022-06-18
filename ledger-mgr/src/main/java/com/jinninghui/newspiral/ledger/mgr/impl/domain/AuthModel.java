package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import com.jinninghui.newspiral.common.entity.member.Auth;
import lombok.Data;

import java.util.Date;

@Data
public class AuthModel {

    private Integer id;

    private String authName;

    private String authCode;

    private String authDescription;

    private Date createTime;

    private Date updateTime;

    public Auth toAuth(){
        Auth auth = new Auth();
        auth.setAuthId(this.getId());
        auth.setAuthCode(this.getAuthCode());
        auth.setAuthDescription(this.getAuthDescription());
        auth.setAuthName(this.getAuthName());
        return auth;
    }

    public void test(){
        System.out.println("123123");
    }


}
