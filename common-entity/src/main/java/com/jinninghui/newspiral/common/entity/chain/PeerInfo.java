package com.jinninghui.newspiral.common.entity.chain;

import com.jinninghui.newspiral.common.entity.identity.IdentityKey;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;


/**
 * 创建节点时所需要的节点信息结构提炼
 * 原则：
 * 该信息需要给到其他组织进行背书签名，所以不可以有任何需保密的信息，例如私钥
 */
@ApiModel(description = "节点信息")
@Data
public class PeerInfo implements Serializable {
    /**
     * 节点所属组织的根身份标识
     */
    @ApiModelProperty(value = "节点所属组织的标识")
    IdentityKey orgId;


    /**
     * 节点本身的身份标识
     */
    @ApiModelProperty(value = "节点本身的身份标识")
    @Valid
    @NotNull
    IdentityKey peerId;


    /**
     * 本节点提供的服务URL
     */
    @ApiModelProperty(value = "本节点提供的服务URL")
    @Valid
    @NotNull
    PeerServiceUrls serviceUrls;

    /**
     * ca证书
     */
    @ApiModelProperty(value = "节点ca证书")
    private byte[] certificateCerFile=null;

    /**
     * 别名
     */
    @ApiModelProperty(value = "证书别名")
    private String certificateAlias;

    /**
     * 组织
     */
    @ApiModelProperty(value = "组织",hidden = true)
    private PeerOrganization peerOrganization;
    /**
     * 复制一份
     * @return
     */
    public PeerInfo clone()
    {
        PeerInfo peerInfo = new PeerInfo();
        peerInfo.setOrgId(orgId.clone());
        peerInfo.setPeerId(peerId.clone());
        peerInfo.setCertificateAlias(certificateAlias);
        peerInfo.setCertificateCerFile(certificateCerFile.clone());
        peerInfo.setPeerOrganization(peerOrganization.clone());
        peerInfo.setServiceUrls(serviceUrls.clone());
        return peerInfo;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o )
            return true;
        if(o instanceof PeerInfo)
        {
            return this.getPeerId().equals(((Peer) o).getPeerId());
        }
        else {
            return false;
        }

    }

    @Override
    public int hashCode() {
        return this.getPeerId().hashCode();
    }
}
