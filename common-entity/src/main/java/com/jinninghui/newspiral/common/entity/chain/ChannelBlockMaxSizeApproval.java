package com.jinninghui.newspiral.common.entity.chain;

import com.jinninghui.newspiral.common.entity.policy.NewSpiralPolicyToken;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @version V1.0
 * @Title: PeerCertificateStateApproval
 * @Package com.jinninghui.newspiral.common.entity.chain
 * @Description:
 * @author: xuxm
 * @date: 2020/4/14 10:45
 */
@ApiModel(description = "通道区块最大限制修改请求体")
@Data
public class ChannelBlockMaxSizeApproval extends NewSpiralPolicyToken {

    /**
     * 通道块大小修改
     */
    @ApiModelProperty("通道区块最大限制请求体")
    private ChannelBlockMaxSizeRequest channelBlockMaxSizeRequest;
}
