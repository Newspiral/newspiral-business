package com.jinninghui.newspiral.common.entity.member;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用来删除成员信息
 */
@ApiModel(description = "用来删除成员信息")
@Data
public class MemberDeleteInfo {

    /**
     * 数字证书公钥
     */
    @ApiModelProperty("数字证书公钥")
    String publicKey;

    /**
     * 通道Id
     */
    @ApiModelProperty("通道Id")
    String channelId;
}
