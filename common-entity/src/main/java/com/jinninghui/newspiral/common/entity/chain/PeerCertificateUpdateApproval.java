package com.jinninghui.newspiral.common.entity.chain;

import com.jinninghui.newspiral.common.entity.policy.NewSpiralPolicyToken;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @version V1.0
 * @Title: PeerCertificateUpdateApproval
 * @Package com.jinninghui.newspiral.common.entity.chain
 * @Description:
 * @author: xuxm
 * @date: 2020/1/20 13:53
 */
@ApiModel(description = "节点证书修改请求体")
@Data
public class PeerCertificateUpdateApproval extends NewSpiralPolicyToken {
    /**
     * 需要修改peer节点的证书
     */
/*
    private byte[] certificateCerFile;*/

    /**
     * 需要修改peer节点的证书
     */
    @ApiModelProperty("节点证书修改参数")
    private PeerCertificateUpdateParams updatePeerCertificate;


}
