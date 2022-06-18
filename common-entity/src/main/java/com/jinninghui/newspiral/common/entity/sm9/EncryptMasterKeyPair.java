package com.jinninghui.newspiral.common.entity.sm9;

import lombok.Data;

/**
 * @version V1.0
 * @Title: EncryptMasterKeyPair
 * @Package com.jinninghui.newspiral.common.entity.sm9
 * @Description:
 * @author: xuxm
 * @date: 2019/11/7 9:48
 */
@Data
public class EncryptMasterKeyPair {
    /**
     * 加密的主公钥字符串
     */
    private String encryptMasterPublicKey;

    /**
     * 加密的主私钥字符串
     */
    private String encryptMasterPrivateKey;
}
