package com.jinninghui.newspiral.common.entity.chain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.security.PrivateKey;
import java.security.PublicKey;
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
public class PeerCertificateCipher {
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

    /**
     * 别名
     */
    private String CertificateAlias;

    /**
     * 密钥库密钥
     */
    private String CertificateStorePass;

/*    public void setCertificateKeyStoreFile(byte[] CertificateKeyStoreFile) {
        if (null != CertificateKeyStoreFile) {
            try {
                //解密私钥
                byte[] encryptByte = (Crypto.decrypt(new String(CertificateKeyStoreFile, "UTF-8"))).getBytes();
                this.CertificateKeyStoreFile = encryptByte;
                //log.info("decrypt,peer={},key={}",this.getPeerId(),Crypto.decrypt(new String(CertificateKeyStoreFile, "UTF-8")));
            } catch (Exception e) {
            }
        } else {
            this.CertificateKeyStoreFile = CertificateKeyStoreFile;
        }

    }*/

    /**
     * X509
     */
     private X509Certificate x509Certificate;
    /**
     * 公钥
     */
    private PublicKey publicKey;
    /**
     * 私钥
     */
    private PrivateKey privateKey;

}
