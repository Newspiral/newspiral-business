package com.jinninghui.newspiral.common.entity.chain;

import lombok.Data;

/**
 * @version V1.0
 * @Title:
 * @Package
 * @Description:
 * @author: xuxm
 * @date: 2020/8/5 17:55
 */
@Data
public class CreateChannel {

    /**
     * 通道Id
     */
    private String channelId;


    /**
     * 创建时间戳
     */
    private Long createTimestamp;
}
