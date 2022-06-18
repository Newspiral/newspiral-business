package com.jinninghui.newspiral.common.entity.state;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lida
 * @date 2019/9/26 11:01
 */
@ApiModel(description = "世界状态变更记录")
public class WorldStateModifyRecord {
    /**
     * oldState为空，newState不为空表示新建
     */
    @ApiModelProperty(value = "旧的世界状态")
    @Setter @Getter
    WorldState oldState;
    /**
     * newState为空，oldState不为空表示删除
     */
    @ApiModelProperty(value = "新的世界状态")
    @Setter @Getter
    WorldState newState;

    /**
     * 该变更记录的最新更新viewNo
     * 用于清除不需要的变更记录
     * 不用blockHash是因为新建的时候，其实还不知道BlockHash
     */
    @ApiModelProperty(value = "变更记录的最新更新viewNo")
    @Getter @Setter
    long latestUpdateViewNo;

}
