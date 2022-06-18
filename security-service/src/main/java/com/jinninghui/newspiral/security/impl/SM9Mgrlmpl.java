package com.jinninghui.newspiral.security.impl;

import com.jinninghui.newspiral.common.entity.sm9.*;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @version V1.0
 * @Title: SM9Mgrlmpl
 * @Package com.jinninghui.newspiral.security.impl
 * @Description:
 * @author: xuxm
 * @date: 2019/11/7 9:33
 */
@Slf4j
@Component
public class SM9Mgrlmpl {


    /**
     * 加密后的签名主密钥对
     * @return
     */
    public  static EncryptMasterKeyPair getEncryptSignMasterKeyPair()
    {
        log.info("SM9Mgrlmpl.getEncryptSignMasterKeyPair:");
        EncryptMasterKeyPair encryptMasterKeyPair=new EncryptMasterKeyPair();
        SM9Curve sm9Curve = new SM9Curve();
        KGC kgc = new KGC(sm9Curve);

        //生成签名主密钥对
        MasterKeyPair signMasterKeyPair = kgc.genSignMasterKeyPair();
        encryptMasterKeyPair.setEncryptMasterPublicKey(Crypto.encrypt(signMasterKeyPair.getPublicKey().toString()));
        encryptMasterKeyPair.setEncryptMasterPrivateKey(Crypto.encrypt(signMasterKeyPair.getPrivateKey().toString()));

        return  encryptMasterKeyPair;
    }

    /**
     *
     * @param encryptMasterKeyPair 加密的公私钥对字符串
     * @return 解密的公私钥对字符串
     */
    public static DecryptMasterKeyPair getDecryptSignMasterKeyPair(EncryptMasterKeyPair encryptMasterKeyPair)
    {
        log.info(ModuleClassification.SM_SM9MI_ +"SM9Mgrlmpl.getDecryptSignMasterKeyPair:");
        DecryptMasterKeyPair decryptMasterKeyPair=new DecryptMasterKeyPair();
        //解密公钥私钥
        decryptMasterKeyPair.setDecryptMasterPublicKey(Crypto.decrypt(encryptMasterKeyPair.getEncryptMasterPublicKey()));
        decryptMasterKeyPair.setDecryptMasterPrivateKey(Crypto.decrypt(encryptMasterKeyPair.getEncryptMasterPrivateKey()));
       return decryptMasterKeyPair;
    }

    /**
     *
     * @param masterPrivateKeyStr master private key string
     * @param id user ID
     * @return 加密后的用户私钥
     */
    public  static String getEncryptPrivateKey(String masterPrivateKeyStr,String id)
    {
        log.info(ModuleClassification.SM_SM9MI_ +"SM9Mgrlmpl.getEncryptPrivateKey:");
        String encryptPrivate="";
        SM9Curve sm9Curve = new SM9Curve();
        KGC kgc = new KGC(sm9Curve);
        MasterPrivateKey masterPrivateKey=MasterPrivateKey.fromByteArray(masterPrivateKeyStr);
        try {
            PrivateKey signPrivateKey = kgc.genPrivateKey(masterPrivateKey, id, PrivateKeyType.KEY_SIGN);
            //生成用户私钥 并加密
            encryptPrivate=Crypto.encrypt(signPrivateKey.toString());
        }
        catch (Exception e)
        {
          log.error(ModuleClassification.SM_SM9MI_ +"TError"+"SM9Mgrlmpl.getEncryptPrivateKey，生成私钥失败："+e.getMessage(),e);
        }
       return encryptPrivate;
    }

}
