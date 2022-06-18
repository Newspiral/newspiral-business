package com.jinninghui.newspiral.common.entity.member;

import com.jinninghui.newspiral.common.entity.common.base.BaseApproval;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @version V1.0
 * @Title: MemberUpdateApproval
 * @Package com.jinninghui.newspiral.common.entity.member
 * @Description:
 * @author: xuxm
 * @date: 2020/1/13 15:53
 */
@ApiModel(description = "成员修改请求体")
@Data
public class MemberUpdateApproval extends BaseApproval {
    /**
     * 需要修改的成员信息
     */
    @ApiModelProperty("成员修改信息")
    private MemberUpdateInfo memberUpdateInfo;
}
