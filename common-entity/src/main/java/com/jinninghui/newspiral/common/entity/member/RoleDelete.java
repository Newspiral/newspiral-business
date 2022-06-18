package com.jinninghui.newspiral.common.entity.member;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "角色删除参数")
@Data
public class RoleDelete {
    /**
     * 通道Id
     */
    @ApiModelProperty(value = "通道ID")
    private String channelId;
    /**
     * 主键
     */
    @ApiModelProperty(value = "角色ID")
    private String roleId;
}
