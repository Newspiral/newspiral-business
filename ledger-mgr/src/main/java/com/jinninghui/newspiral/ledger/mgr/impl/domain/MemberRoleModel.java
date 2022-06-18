package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import lombok.Data;

import java.util.UUID;

/**
 * @version V1.0
 * @Title: MemberRoleModel
 * @Package com.jinninghui.newspiral.ledger.mgr.impl.domain
 * @Description:
 * @author: xuxm
 * @date: 2020/1/10 11:12
 */
@Data
public class MemberRoleModel {
    /**
     * 主键
     */
    private String id;
    /**
     * 成员Id
     */
    private long memberId;
    /**
     * 角色Id
     */
    private String roleId;
    /**
     * 通道编号
     */
    private String channelId;

    public static MemberRoleModel createInstance(long memberId,String roleId,String channelId)
    {
        MemberRoleModel memberRoleModel=new MemberRoleModel();
        memberRoleModel.setId(UUID.randomUUID().toString().replaceAll("-","").toUpperCase());
        memberRoleModel.setMemberId(memberId);
        memberRoleModel.setRoleId(roleId);
        memberRoleModel.setChannelId(channelId);
        return memberRoleModel;
    }
}
