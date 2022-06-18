package com.jinninghui.newspiral.common.entity.chain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @version V1.0
 * @Title: ChannelBlockMaxSizeRequest
 * @Package com.jinninghui.newspiral.common.entity.chain
 * @Description:
 * @author: xuxm
 * @date: 2020/4/15 17:12
 */
@ApiModel(description = "通道区块大小请求体")
@Data
public class ChannelBlockMaxSizeRequest {

    /**
     * 通道Id
     */
    @ApiModelProperty("通道Id")
    @NotBlank
    private String ChannelId;

    /**
     * 块大小
     */
    @ApiModelProperty("区块大小")
    @NotNull
    Long blockMaxSize;
}
