package com.jinninghui.newspiral.common.entity.member;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel(description = "查询角色列表")
@Data
public class QueryRoleListParams {

    @ApiModelProperty(value = "通道ID")
    private String channelId;

    @ApiModelProperty(value = "状态（1正常 2业务删除）")
    private List<RoleStateEnum> state;

}
