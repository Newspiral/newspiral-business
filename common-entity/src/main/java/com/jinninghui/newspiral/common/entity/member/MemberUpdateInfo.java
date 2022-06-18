package com.jinninghui.newspiral.common.entity.member;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用来更新成员信息
 */
@ApiModel(description = "成员修改信息")
@Data
public class MemberUpdateInfo {
    /**
     * 成员修改信息
     */
    @ApiModelProperty("成员修改信息")
    MemberInfo memberInfo;
    /**
     * 成员状态
     */
    @ApiModelProperty("成员状态")
    MemberStateEnum stateEnum;
}
