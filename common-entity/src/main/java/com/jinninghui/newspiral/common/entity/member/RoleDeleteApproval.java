package com.jinninghui.newspiral.common.entity.member;

import com.jinninghui.newspiral.common.entity.common.base.BaseApproval;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @version V1.0
 * @Title: RoleDeleteApproval
 * @Package com.jinninghui.newspiral.common.entity.member
 * @Description:
 * @author: wuhuaijiang
 * @date: 2020/10/18 9:01
 */
@ApiModel(description = "角色删除请求体")
@Data
public class RoleDeleteApproval extends BaseApproval {
    /**
     * 需要删除的自定义角色
     */
    @ApiModelProperty("角色删除参数")
    private RoleDelete deleteRole;
}
