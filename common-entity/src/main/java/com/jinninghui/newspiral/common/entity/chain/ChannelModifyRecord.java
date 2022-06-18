package com.jinninghui.newspiral.common.entity.chain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lida
 * @date 2019/9/29 14:24
 */
@ApiModel(description = "通道变更记录")
@Data
public class ChannelModifyRecord implements Serializable {

    /**
     * newChannel为空，oldChannel不为空表示删除
     */
    @ApiModelProperty(value = "新的通道信息")
    Channel newChannel;
    @ApiModelProperty(value = "旧的通道信息")
    Channel oldChannel;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChannelModifyRecord)) {
            return false;
        }

        ChannelModifyRecord newChannelModifyRecord = (ChannelModifyRecord) obj;
        return newChannelModifyRecord.newChannel.equals(newChannel);
    }
}
