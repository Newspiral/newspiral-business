package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import com.jinninghui.newspiral.common.entity.chain.PeerCert;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * @version V1.0
 * @Title: PeerCertificateModel
 * @Package com.jinninghui.newspiral.ledger.mgr.impl.domain
 * @Description:
 * @author: xuxm
 * @date: 2020/4/8 14:50
 */
@Data
public class PeerCertificateModel {
    /**
     * 主键
     */
    private Long id;
    /**
     * 节点ID
     */
    private String peerId;
    /**
     * ca证书
     */
    private String certificateCerFile=null;

    /**
     * 证书hash
     */
    private String certificateHash;

    /**
     * 状态，0正常；1冻结；2吊销
     */
    private String flag;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     *
     */
    private Long blockHeight;

    private String channelId;

    private Integer isLocalPeer;

    /**
     * 密钥库
     */
    private String certificateKeyStoreFile=null;

/*    public String getCertificateHash()
    {
        return bytesToHexString(this.certificateCerFile);
    }
    public static String bytesToHexString(byte[] src){
        return Hex.encodeHexString(src,false);
    }*/

    public static PeerCertificateModel createInstance(PeerCert peerCert)
    {
        PeerCertificateModel peerCertificateModel =new PeerCertificateModel();
        peerCertificateModel.setId(peerCert.getId());

        try {
            peerCertificateModel.setCertificateCerFile(new String(peerCert.getCertificateCerFile(), "UTF-8"));
            peerCertificateModel.setCertificateKeyStoreFile(new String(peerCert.getCertificateKeyStoreFile(),"UTF-8"));
        }
        catch (Exception e)
        {
        }
        peerCertificateModel.setFlag(peerCert.getFlag());
        peerCertificateModel.setPeerId(peerCert.getPeerId());
        peerCertificateModel.setCertificateHash(peerCert.getCertificateHash());
        peerCertificateModel.setCreateTime(null!=peerCert.getCreateTime()?new Date(peerCert.getCreateTime()):new Date() );
        peerCertificateModel.setUpdateTime(null!=peerCert.getUpdateTime()?new Date(peerCert.getUpdateTime()):new Date());
        peerCertificateModel.setBlockHeight(peerCert.getBlockHeight());
        peerCertificateModel.setChannelId(peerCert.getChannelId());
        peerCertificateModel.setIsLocalPeer(peerCert.getIsLocalPeer());
        return peerCertificateModel;
    }

    public PeerCert toPeerCert()
    {
        PeerCert peerCert =new PeerCert();
        peerCert.setCertificateHash(this.certificateHash);
        peerCert.setId(this.id);
        peerCert.setFlag(this.flag);
        peerCert.setPeerId(this.peerId);
        peerCert.setCreateTime(null!=this.createTime?this.createTime.getTime():null);
        peerCert.setUpdateTime(null!=this.updateTime?this.updateTime.getTime():null);
        peerCert.setCertificateCerFile(this.certificateCerFile.getBytes());
        //peerCert.setCerFile(this.certificateCerFile);
        peerCert.setBlockHeight(this.getBlockHeight());
        peerCert.setChannelId(this.channelId);
        peerCert.setCertificateKeyStoreFile(StringUtils.isEmpty(this.certificateKeyStoreFile)?"".getBytes(): this.certificateKeyStoreFile.getBytes());
        peerCert.setIsLocalPeer(this.isLocalPeer);
        return peerCert;
    }
}
