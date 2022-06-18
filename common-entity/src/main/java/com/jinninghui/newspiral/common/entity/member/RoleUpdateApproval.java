package com.jinninghui.newspiral.common.entity.member;

import com.jinninghui.newspiral.common.entity.common.base.BaseApproval;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @version V1.0
 * @Title: RoleUpdateApproval
 * @Package com.jinninghui.newspiral.common.entity.member
 * @Description:
 * @author: xuxm
 * @date: 2020/1/13 15:53
 */
@ApiModel(description = "角色修改请求体")
@Data
public class RoleUpdateApproval extends BaseApproval  {
    /**
     * 需要修改的自定义role
     */
    @ApiModelProperty("角色修改请求参数")
    private RoleUpdate updateRole;
}
