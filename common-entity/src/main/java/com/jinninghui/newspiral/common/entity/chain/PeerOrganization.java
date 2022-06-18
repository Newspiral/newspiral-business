package com.jinninghui.newspiral.common.entity.chain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @version V1.0
 * @Title: PeerOrganization
 * @Package com.jinninghui.newspiral.common.entity.chain
 * @Description:
 * @author: xuxm
 * @date: 2020/8/7 21:26
 */
@ApiModel(description = "节点组织")
@Data
public class PeerOrganization {
    @ApiModelProperty(value = "组织ID")
    private String organizationId;
    @ApiModelProperty(value = "组织Name")
    private String organizationName;
    @ApiModelProperty(value = "公钥")
    private String publicKey;
    @ApiModelProperty(value = "证书")
    private String certificateCerFile;

    public PeerOrganization clone()
    {
        PeerOrganization peerOrganization=new PeerOrganization();
        peerOrganization.setOrganizationId(this.organizationId);
        peerOrganization.setOrganizationName(this.organizationName);
        peerOrganization.setCertificateCerFile(this.getCertificateCerFile());
        peerOrganization.setPublicKey(this.publicKey);
        return peerOrganization;
    }
}
