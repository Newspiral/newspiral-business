package com.jinninghui.newspiral.common.entity.member;

import com.jinninghui.newspiral.common.entity.common.base.BaseApproval;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @version V1.0
 * @Title: RoleAddApproval
 * @Package com.jinninghui.newspiral.common.entity.member
 * @Description:
 * @author: xuxm
 * @date: 2020/1/13 15:53
 */
@ApiModel(description = "角色新增请求体")
@Data
public class RoleAddApproval extends BaseApproval {
    /**
     * 新加入的自定义RoleAdd
     */
    @ApiModelProperty("角色新增参数")
    private RoleAdd roleAdd;
}
