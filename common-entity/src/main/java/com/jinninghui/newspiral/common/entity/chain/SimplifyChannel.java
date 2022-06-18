package com.jinninghui.newspiral.common.entity.chain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@ApiModel(description = "通道精简数据实体")
@Data
public class SimplifyChannel {

    @ApiModelProperty(value = "通道id")
    private String channelId;

    @ApiModelProperty(value = "通道名称")
    private String channelName;

    @ApiModelProperty(value = "通道中最高的块高度")
    private long maxBlockId;
}
