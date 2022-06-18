package com.jinninghui.newspiral.common.entity.member;

import com.jinninghui.newspiral.common.entity.common.base.BaseApproval;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @version V1.0
 * @Title: MemberAddApproval
 * @Package com.jinninghui.newspiral.common.entity.member
 * @Description:
 * @author: xuxm
 * @date: 2020/1/13 15:53
 */
@ApiModel(description = "成员新增请求体")
@Data
public class MemberAddApproval extends BaseApproval implements Serializable {
    /**
     * 新加入的成员信息
     */
    @ApiModelProperty("新增成员信息")
    private MemberInfo newMemberInfo;

    public MemberAddApproval clone()
    {
        MemberAddApproval memberAddApproval=new MemberAddApproval();
        memberAddApproval.setNewMemberInfo(this.newMemberInfo.clone());
        memberAddApproval.setSignerIdentityKey(this.getSignerIdentityKey());
        memberAddApproval.setChannelId(this.getChannelId());
        memberAddApproval.setHash(this.getHash());
        return memberAddApproval;
    }
}
