package com.jinninghui.newspiral.security;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.security.cert.X509Certificate;

/**
 * @version V1.0
 * @Title: PeerCertificateCipher
 * @Package com.jinninghui.newspiral.common.entity.chain
 * @Description:
 * @author: xuxm
 * @date: 2019/12/20 16:06
 */
@Data
@Slf4j
public class PeerCertificateCipherReq {
    /**
     * peer value
     */
    private String peerId;

    /**
     * ca证书
     */
    private byte[] CertificateCerFile = null;

    /**
     * 密钥库
     */
    private byte[] CertificateKeyStoreFile = null;

    private java.security.PrivateKey privateKey;

    private X509Certificate x509Certificate;


}
