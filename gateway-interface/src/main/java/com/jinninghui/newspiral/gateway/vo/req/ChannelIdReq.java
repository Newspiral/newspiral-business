package com.jinninghui.newspiral.gateway.vo.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @version V1.0
 * @Title: ChannelIdReq
 * @Package com.jinninghui.newspiral.gateway.vo.req
 * @Description:
 * @author: xuxm
 * @date: 2019/12/7 11:48
 */
@ApiModel(description = "通道ID请求体")
@Data
public class ChannelIdReq {
    /**
     * 通道ID
     */
    @ApiModelProperty(value = "通道ID")
    private String channelId;
}
