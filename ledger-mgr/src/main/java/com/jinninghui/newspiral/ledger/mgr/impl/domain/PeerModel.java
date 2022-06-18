package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jinninghui.newspiral.common.entity.chain.Peer;
import com.jinninghui.newspiral.common.entity.chain.PeerServiceUrls;
import com.jinninghui.newspiral.common.entity.identity.IdentityKey;
import com.jinninghui.newspiral.common.entity.identity.IdentityTypeEnum;
import org.springframework.util.StringUtils;

public class PeerModel {

    private Long id;

    private String peerIdValue;

    private String peerIdType;

    private String serviceUrls;

    private String organizationIdType;

    private String organizationIdValue;

    private Integer isLocalPeer;
    /**
     * ca证书
     */
    private String certificateCerFile=null;

    /**
     * 密钥库
     */
    private String certificateKeyStoreFile=null;

    /**
     * 别名
     */
    private String certificateAlias;

    /**
     * 密钥库密钥
     */
    private String certificateStorePass;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getPeerIdValue() {
        return peerIdValue;
    }

    public void setPeerIdValue(String peerIdValue) {
        this.peerIdValue = peerIdValue;
    }

    public String getPeerIdType() {
        return peerIdType;
    }

    public void setPeerIdType(String peerIdType) {
        this.peerIdType = peerIdType;
    }

    public String getServiceUrls() {
        return serviceUrls;
    }

    public void setServiceUrls(String serviceUrls) {
        this.serviceUrls = serviceUrls;
    }

    public String getOrganizationIdType() {
        return organizationIdType;
    }

    public void setOrganizationIdType(String organizationIdType) {
        this.organizationIdType = organizationIdType;
    }

    public String getOrganizationIdValue() {
        return organizationIdValue;
    }

    public void setOrganizationIdValue(String organizationIdValue) {
        this.organizationIdValue = organizationIdValue;
    }

    public Integer getIsLocalPeer() {
        return isLocalPeer;
    }

    public void setIsLocalPeer(Integer isLocalPeer) {
        this.isLocalPeer = isLocalPeer;
    }

    public String getCertificateCerFile() {
        return certificateCerFile;
    }

    public void setCertificateCerFile(String certificateCerFile) {
        this.certificateCerFile = certificateCerFile;
    }

    public String getCertificateKeyStoreFile() {
        return certificateKeyStoreFile;
    }

    public void setCertificateKeyStoreFile(String certificateKeyStoreFile) {
        this.certificateKeyStoreFile = certificateKeyStoreFile;
    }

    public String getCertificateAlias() {
        return certificateAlias;
    }

    public void setCertificateAlias(String certificateAlias) {
        this.certificateAlias = certificateAlias;
    }

    public String getCertificateStorePass() {
        return certificateStorePass;
    }

    public void setCertificateStorePass(String certificateStorePass) {
        this.certificateStorePass = certificateStorePass;
    }

    public Peer toPeer() {
        Peer peer = new Peer();
        peer.setServiceUrls(JSON.parseObject(this.getServiceUrls(), PeerServiceUrls.class));
        IdentityKey peerId = new IdentityKey();
        peerId.setType(JSON.parseObject(this.getPeerIdType(), IdentityTypeEnum.class));
        peerId.setValue(this.getPeerIdValue());
        peer.setPeerId(peerId);

        IdentityKey orgId = new IdentityKey();
        orgId.setType(JSON.parseObject(this.getOrganizationIdType(), IdentityTypeEnum.class));
        orgId.setValue(this.getOrganizationIdValue());
        peer.setOrgId(orgId);

        if(this.getIsLocalPeer()>=1)
            peer.setIsLocalPeer(true);
        else
            peer.setIsLocalPeer(false);

        if(StringUtils.isEmpty(this.certificateKeyStoreFile)) {
            peer.setCertificateKeyStoreFile(null);
        }
        else
        {
            peer.setCertificateKeyStoreFile(this.certificateKeyStoreFile.getBytes());
        }
        peer.setCertificateCerFile(this.certificateCerFile==null?"".getBytes():this.getCertificateCerFile().getBytes());
        peer.setCertificateAlias(this.getCertificateAlias());
        peer.setCertificateStorePass(this.certificateStorePass);
        return peer;
    }



    public static PeerModel createInstance(Peer peer)
    {
        PeerModel model = new PeerModel();
        if(null!=peer.getIsLocalPeer()) {
            model.setIsLocalPeer(peer.getIsLocalPeer() ? 1 : 0);
        }
        if(null!=peer.getOrgId()) {
            model.setOrganizationIdType(JSON.toJSONString(peer.getOrgId().getType()));
            model.setOrganizationIdValue(peer.getOrgId().getValue());
        }
        if(null!=peer.getPeerId()) {
            model.setPeerIdType(JSON.toJSONString(peer.getPeerId().getType()));
            model.setPeerIdValue(peer.getPeerId().getValue());
        }
        if(null!=peer.getServiceUrls()) {
            model.setServiceUrls(JSON.toJSONString(peer.getServiceUrls(), new SerializerFeature[]{SerializerFeature.DisableCircularReferenceDetect}));
        }
        String str1 = "";
        String str2 = "";
        try {
            str1 = new String(peer.getCertificateCerFile(), "UTF-8");
            str2 = new String(peer.getCertificateKeyStoreFile(), "UTF-8");
        } catch (Exception ex) {

        }
        model.setCertificateCerFile(str1);
        model.setCertificateKeyStoreFile(str2);
        model.setCertificateAlias(peer.getCertificateAlias());
        model.setCertificateStorePass(peer.getCertificateStorePass());
        return model;
    }
}