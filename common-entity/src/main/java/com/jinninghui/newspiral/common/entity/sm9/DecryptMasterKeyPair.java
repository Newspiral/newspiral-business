package com.jinninghui.newspiral.common.entity.sm9;

import lombok.Data;

/**
 * @version V1.0
 * @Title: DecryptMasterKeyPair
 * @Package com.jinninghui.newspiral.common.entity.sm9
 * @Description:
 * @author: xuxm
 * @date: 2019/11/7 10:14
 */
@Data
public class DecryptMasterKeyPair {

    /**
     * 解密后的主公钥字符串
     */
    private String  decryptMasterPublicKey;

    /**
     * 解密后的主私钥字符串
     */
    private String decryptMasterPrivateKey;
}
