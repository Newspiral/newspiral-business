package com.jinninghui.newspiral.security.impl;

import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.security.utils.PropertyConstants;
import com.jinninghui.newspiral.security.utils.SM4Util;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @version V1.0
 * @Title: GMCrypto
 * @Package com.jinninghui.newspiral.security.impl
 * @Description:加解密的方法（密钥需要加密后入库，使用之前先解密）
 * @author: xuxm
 * @date:
 */
@Slf4j
public class GMCrypto {

    /**
     * 自定义 KEY
     */
/*    private static byte[] keybytes = { 0x31, 0x32, 0x33, 0x34, 0x35, 0x50,
            0x37, 0x38, 0x39, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46 };*/

    /*配置文件中的一部分*/
    private static final String ENCODING = "UTF-8";
    static final String KEY_PART_CONFIG = PropertyConstants.getPropertiesKey("sm4.key.prefix");
    /*代码中一部分*/
    static final String KEY_PART_CONSTANT = "f0289a2c607befcbcc440";
    private static byte[] keyBytes;

    static {
        try {
            //decryptBASE64(KEY_PART_CONFIG + KEY_PART_CONSTANT);
            keyBytes = ByteUtils.fromHexString(KEY_PART_CONFIG + KEY_PART_CONSTANT);
        } catch (Exception e) {
            log.error("init peer privateKey crypto key is error,e={}", e);
        }
    }

    public static void main(String[] args) {

        //test();
        try {
            //String aa=new String(keybytes, "UTF-8");
            //System.out.println(aa);
            //log.info(ModuleClassification.SM_CPO+"解密：{}",keybytes);
            //log.info(ModuleClassification.SM_CPO+"解密：{}",aa.getBytes());
            //String bb = KEY_PART_CONFIG + KEY_PART_CONSTANT;
            //log.info(ModuleClassification.SM_CPO_ +"bb：{}",bb);
            //log.info(ModuleClassification.SM_CPO_ +"bb：{}",bb.getBytes());
            //log.info(encryptBASE64(SM4Util.generateKey()));

/*            byte [] aa=SM4Util.generateKey();
            log.info("{}",aa);
            log.info("{}",aa.toString());
            log.info(new String(aa));
            log.info(encryptBASE64(aa));
            log.info(ByteUtils.toHexString(aa));*/

           String aa="-----BEGIN PRIVATE KEY-----\n" +
                   "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgDvv/dKuwHCr4Hbpe\n" +
                   "hfKfDQL4GNH3avQKVAlj/cbj+J2gCgYIKoEcz1UBgi2hRANCAATmMHe0QKZ2lT64\n" +
                   "/CkNwJatpdrHf6QtlJU5mvZVxvrq2nIkC0Zw15wEMpPRWxFLqV7kM9TxCOmHAcgX\n" +
                   "2G++KRT9\n" +
                   "-----END PRIVATE KEY-----";
           String enStr=encrypt(aa);
           String deStr=decrypt(enStr);
           if(aa.equals(deStr))
           {
               log.info("encrypt is true");
               log.info("enStr={}",enStr);
           }
        } catch (Exception e) {

        }

    }

    private static void test() {
        BufferedReader reader;
        try {
            String st = "";
            do {
                if ("".equals(st)) {
                    log.info(ModuleClassification.SM_CPO_ + "AES加密与解密操作:");
                    log.info(ModuleClassification.SM_CPO_ + "\"E\":加密 \t\"D\":解密\t\t\"Q\":退出");
                    log.info(ModuleClassification.SM_CPO_ + "请输入操作代码:");
                }
                reader = new BufferedReader(new InputStreamReader(System.in));
                st = reader.readLine();
                if ("E".equalsIgnoreCase(st)) {
                    log.info(ModuleClassification.SM_CPO_ + "请输入待加密字符串:");
                    st = reader.readLine();
                    if (!"".equals(st.trim())) {
                        log.info(ModuleClassification.SM_CPO_ + "加密前:" + st.trim());
                        log.info(ModuleClassification.SM_CPO_ + "加密后:" + encrypt(st.trim()) + "\n\n");
                    }
                    st = "";
                } else if ("D".equalsIgnoreCase(st)) {
                    log.info(ModuleClassification.SM_CPO_ + "请输入待解密字符串:");
                    st = reader.readLine();
                    if (!"".equals(st.trim())) {
                        log.info(ModuleClassification.SM_CPO_ + "解密前:" + st.trim());
                        log.info(ModuleClassification.SM_CPO_ + "解密后:" + decrypt(st.trim()) + "\n\n");
                    }
                    st = "";
                }
            } while (!st.equalsIgnoreCase("Q"));
        } catch (Exception e) {
            log.error(ModuleClassification.SM_CPO_ + "TError" + e.getMessage(), e);
        }
    }


    /**
     * @param @param  value
     * @param @return 设定文件
     * @return String    返回类型
     * @throws
     * @Title: encrypt
     * @Description: 加密
     */
    public static String encrypt(String value) {

        String s = null;
        try {
            byte[] srcData = value.getBytes(ENCODING);
            byte[] outBytes = SM4Util.encrypt_ECB_Padding(keyBytes,srcData);
            s = ByteUtils.toHexString(outBytes);;
        } catch (Exception e) {
            log.error(ModuleClassification.SM_CPO_ + "TError" + e.getMessage(), e);
        }
        return s;
    }


    /**
     * @param @param  value
     * @param @return 设定文件
     * @return String    返回类型
     * @throws
     * @Title: decrypt
     * @Description: 解密
     */
    public static String decrypt(String value) {
        String s = null;
        try {
            byte[] cipherData = ByteUtils.fromHexString(value);
            byte[] outBytes = SM4Util.decrypt_ECB_Padding(keyBytes, cipherData);
            s = new String(outBytes, ENCODING);;
        } catch (Exception e) {
            log.error(ModuleClassification.SM_CPO_ + "TError" + e.getMessage(), e);
        }
        return s;
    }


}
