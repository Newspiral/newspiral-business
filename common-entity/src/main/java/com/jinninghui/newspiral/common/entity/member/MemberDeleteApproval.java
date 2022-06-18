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
@ApiModel(description = "成员删除请求体")
@Data
public class MemberDeleteApproval extends BaseApproval {

    /**
     * 成员删除信息
     */
    @ApiModelProperty("成员删除信息")
    MemberDeleteInfo memberDeleteInfo;
}
