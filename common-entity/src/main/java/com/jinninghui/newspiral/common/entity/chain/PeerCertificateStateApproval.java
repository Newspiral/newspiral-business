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
@ApiModel(description = "修改节点证书状态投票")
@Data
public class PeerCertificateStateApproval extends NewSpiralPolicyToken {

    /**
     * 证书状态动作
     */
    @ApiModelProperty("证书状态请求体")
    private PeerCertificateStateRequest peerCertificateStateRequest;
}
