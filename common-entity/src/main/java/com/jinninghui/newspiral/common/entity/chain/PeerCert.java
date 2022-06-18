package com.jinninghui.newspiral.common.entity.chain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @version V1.0
 * @Title: PeerCertificateBlacklist
 * @Package com.jinninghui.newspiral.common.entity.chain
 * @Description:
 * @author: xuxm
 * @date: 2020/4/14 11:27
 */
@ApiModel(value = "节点证书")
@Data
public class PeerCert implements Serializable {
    /**
     * 主键
     */
    @ApiModelProperty(value = "主键")
    private Long id;
    /**
     * 节点ID
     */
    @ApiModelProperty(value = "节点ID")
    private String peerId;
    /**
     * ca证书
     */
    @ApiModelProperty(value = "ca证书")
    private byte[] certificateCerFile=null;

    /**
     * 证书hash
     */
    @ApiModelProperty(value = "证书hash")
    private String certificateHash;

    /**
     * 状态，0正常；1冻结；2吊销
     */
    @ApiModelProperty(value = "节点状态，0正常；1冻结；2吊销")
    private String flag;
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Long createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间")
    private Long updateTime;

    /**
     *
     */
    @ApiModelProperty(value = "区块高度")
    private Long blockHeight;

    @ApiModelProperty(value = "通道ID")
    private String channelId;

    @ApiModelProperty(value = "是否为本地节点")
    private Integer isLocalPeer;

    /**
     * 密钥库
     */
    @ApiModelProperty(value = "秘钥库")
    private byte[] certificateKeyStoreFile=null;

    public static void clonePeerCers(Peer peer,String channelId,String certificateHash)
    {
        List<PeerCert> peerCerts=new ArrayList<>();
        PeerCert peerCert=new PeerCert();
        peerCert.setIsLocalPeer(peer.getIsLocalPeer()?1:0);
        peerCert.setCertificateKeyStoreFile(peer.getCertificateKeyStoreFile());
        peerCert.setChannelId(channelId);
        peerCert.setBlockHeight(0L);
        peerCert.setPeerId(peer.getPeerId().getValue());
        peerCert.setFlag("0");
        peerCert.setCertificateCerFile(peer.getCertificateCerFile());
        peerCert.setCertificateHash(certificateHash);
        peerCert.setCreateTime(System.currentTimeMillis());
        peerCerts.add(peerCert);
        peer.setPeerCert(peerCerts);
    }

    public PeerCert clone()
    {
        PeerCert peerCert=new PeerCert();
        peerCert.setIsLocalPeer(this.getIsLocalPeer());
        peerCert.setCertificateKeyStoreFile(this.getCertificateKeyStoreFile());
        peerCert.setChannelId(this.getChannelId());
        peerCert.setBlockHeight(this.getBlockHeight());
        peerCert.setPeerId(this.getPeerId());
        peerCert.setFlag(this.flag);
        peerCert.setCertificateCerFile(this.getCertificateCerFile());
        peerCert.setCertificateHash(this.certificateHash);
        peerCert.setCreateTime(this.getCreateTime());
        return peerCert;
    }

    public static List<PeerCert> clones(List<PeerCert> peerCerts)
    {
        List<PeerCert> peerCertList=new ArrayList<>();
        for(PeerCert peerCert:peerCerts)
        {
            peerCertList.add(peerCert.clone());
        }
        return peerCertList;
    }
}
