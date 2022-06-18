package com.jinninghui.newspiral.common.entity.config;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "通道分片相关信息")
public class ChannelShardingInfo {

    @ApiModelProperty(value = "通道Id")
    private String channelId;

    @ApiModelProperty(value = "通道名称")
    private String channelName;

    @ApiModelProperty(value = "每个分库中通道最大的高度")
    private long maxHeightInEachDs;
}
