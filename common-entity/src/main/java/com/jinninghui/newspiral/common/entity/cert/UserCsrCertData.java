package com.jinninghui.newspiral.common.entity.cert;

import lombok.Data;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @version V1.0
 * @Title: CertBCStyle
 * @Package com.jinninghui.newspiral.common.entity.cert
 * @Description:
 * @author: xuxm
 * @date: 2021/2/3 11:46
 */
@Data
public class UserCsrCertData {

    /**
     * 用户请求证书（CSR）
     */
    private PKCS10CertificationRequest userCSR;
    /**
     * 私钥
     */
    private PrivateKey privateKey;
    /**
     * 公钥
     */
    private PublicKey publicKey;

}
