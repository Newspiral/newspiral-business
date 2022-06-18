package com.jinninghui.newspiral.common.entity.cert;

import lombok.Data;

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
public class UserCsrAndCertData extends UserCsrCertData {

    /**
     * 用户证书
     */
    private X509Certificate userCert;

}
