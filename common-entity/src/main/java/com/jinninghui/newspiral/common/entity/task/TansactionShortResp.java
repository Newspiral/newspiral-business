package com.jinninghui.newspiral.common.entity.task;

import lombok.Data;

/**
 * @version V1.0
 * @Title: TansactionShortResp
 * @Package com.jinninghui.newspiral.gateway.signTest
 * @Description:
 * @author: xuxm
 * @date: 2020/7/13 10:22
 */
@Data
public class TansactionShortResp {
    private String transHash;

    private Long timeStamp;
}
