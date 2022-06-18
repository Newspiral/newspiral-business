package com.jinninghui.newspiral.common.entity.member;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@ApiModel(description = "角色查询参数")
@Data
public class QueryRoleParams {

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

    /**
     * 名称
     */
    @ApiModelProperty(value = "角色名称")
    private String name;

    /**
     * 英文名称
     */
    @ApiModelProperty(value = "角色英文名称")
    private String shortName;

}
