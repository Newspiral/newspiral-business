package com.jinninghui.newspiral.common.entity.member;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @version V1.0
 * @Title: QueryMemberReq
 * @Package com.jinninghui.newspiral.common.entity.member
 * @Description:
 * @author: xuxm
 * @date: 2020/1/21 9:45
 */
@ApiModel(description = "查询成员请求体")
@Data
public class QueryMemberReq {
    /**
     * 通道编号(必传)
     */
    @ApiModelProperty("通道Id")
    private String channelId;

    /**
     * 数字证书的公钥
     */
    @ApiModelProperty(value = "数字证书公钥")
    private String publicKey;
}
