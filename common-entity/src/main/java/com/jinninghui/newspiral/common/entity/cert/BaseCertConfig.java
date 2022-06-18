package com.jinninghui.newspiral.common.entity.cert;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

/**
 * @version V1.0
 * @Title: CertBCStyle
 * @Package com.jinninghui.newspiral.common.entity.cert
 * @Description:
 * @author: xuxm
 * @date: 2021/2/3 11:46
 */
@Data
public class BaseCertConfig {

    /**
     * 证书序列号
     */
    private BigInteger Serial = BigInteger.valueOf(System.currentTimeMillis());
    /**
     * 生效日期（默认为创建时间）
     */
    private Date NotBefore = new Date();
    /**
     * 失效日期（默认为创建时间后5年）
     */
    private Date NotAfter = new Date(System.currentTimeMillis()+1000L*86400L*365L*5);

}
