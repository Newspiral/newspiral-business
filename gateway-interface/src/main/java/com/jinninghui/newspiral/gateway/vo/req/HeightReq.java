package com.jinninghui.newspiral.gateway.vo.req;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @version V1.0
 * @Title: ChannelIdReq
 * @Package com.jinninghui.newspiral.gateway.vo.req
 * @Description:
 * @author: xuxm
 * @date: 2019/12/7 11:48
 */
@Data
public class HeightReq {
    /**
     * 通道ID
     */
    @NotBlank
    private     String channelId;
    @NotNull
    @Min(value = 0,message = "高度不能小于零")
    private     Long height;
}
