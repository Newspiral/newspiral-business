package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import com.alibaba.fastjson.JSON;
import com.jinninghui.newspiral.common.entity.identity.Identity;
import com.jinninghui.newspiral.common.entity.identity.IdentityKey;
import com.jinninghui.newspiral.common.entity.identity.IdentityKeyBrief;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class IdentityModel {

    @Getter @Setter
    private Long id;

    /**
     * 对应IdentityKey的type_value
     */
    @Getter @Setter
    private String identityId;


    @Getter @Setter
    private String parentId;

    @Getter @Setter
    private Date setupTimestamp;

    @Getter @Setter
    private String extendedProps;


    /**
     * 转换为Identity
     * @return
     */
    public Identity toIdentity() {
        Identity identity = new Identity();
        identity.setExtendedProps(extendedProps);
        identity.setKey(JSON.parseObject(this.identityId,IdentityKey.class));
        identity.setParentKey(JSON.parseObject(this.parentId,IdentityKey.class));
        return identity;
    }

    public static IdentityModel createInstance(Identity identity)
    {
        IdentityModel model = new IdentityModel();
        model.setIdentityId(JSON.toJSONString(transforIdentityKeyBrief(identity.getKey())));
        model.setParentId(JSON.toJSONString(transforIdentityKeyBrief(identity.getParentKey())));
        model.setExtendedProps(identity.getExtendedProps());
        return model;
    }


    public static IdentityKeyBrief transforIdentityKeyBrief(IdentityKey identityKey)
    {
        IdentityKeyBrief identityKeyBrief=new IdentityKeyBrief();
        identityKeyBrief.setType(identityKey.getType());
        identityKeyBrief.setValue(identityKey.getValue());
        return identityKeyBrief;
    }

}