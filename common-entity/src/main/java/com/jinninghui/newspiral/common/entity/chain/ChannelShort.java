package com.jinninghui.newspiral.common.entity.chain;

import lombok.Data;

/**
 * @version V1.0
 * @Title: ChannelShort
 * @Package com.jinninghui.newspiral.gateway.vo.resp
 * @Description:
 * @author: xuxm
 * @date: 2020/7/29 10:27
 */
@Data
public class ChannelShort {
    //通道ID
    private String channelId;
    //当前高度
    private Long height;
    //交易大小
    private Long transactionSize;


}
