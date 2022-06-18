package com.jinninghui.newspiral.common.entity.cert;

import lombok.Data;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

/**
 * @version V1.0
 * @Title: CertBCStyle
 * @Package com.jinninghui.newspiral.common.entity.cert
 * @Description:
 * @author: xuxm
 * @date: 2021/2/3 11:46
 */
@Data
public class RootCertData {

    /**
     * 根证书
     */
    private X509Certificate rootCert;
    /**
     * 私钥
     */
    private PrivateKey privateKey;
    /**
     * 公钥
     */
    private PublicKey publicKey;

}
