package com.jinninghui.newspiral.gateway.vo.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @version V1.0
 * @Title: createChannelResp
 * @Package com.jinninghui.newspiral.gateway.vo.resp
 * @Description:
 * @author: xuxm
 * @date: 2020/8/5 17:55
 */
@Data
public class CreateChannelResp {

    /**
     * 通道Id
     */
    @ApiModelProperty("通道Id")
    private String channelId;

    /**
     * 成员身份ID
     */
    @ApiModelProperty("成员身份ID")
    private String memberId;

    /**
     * 创建时间戳
     */
    @ApiModelProperty("创建时间戳")
    private Long createTimestamp;

}
