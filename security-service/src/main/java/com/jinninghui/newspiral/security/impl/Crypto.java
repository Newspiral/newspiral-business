package com.jinninghui.newspiral.security.impl;

import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.security.utils.PropertyConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * @version V1.0
 * @Title: Crypto
 * @Package com.jinninghui.newspiral.security.impl
 * @Description:加解密的方法（密钥需要加密后入库，使用之前先解密）
 * @author: xuxm
 * @date: 2019/11/4 17:08
 */
@Slf4j
public class Crypto {

    /**
     * 自定义 KEY
     */
/*    private static byte[] keybytes = { 0x31, 0x32, 0x33, 0x34, 0x35, 0x50,
            0x37, 0x38, 0x39, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46 };*/

    /*配置文件中的一部分*/
    static final String KEY_PART_CONFIG = PropertyConstants.getPropertiesKey("peer.key.part");
    /*代码中一部分*/
    static final String  KEY_PART_CONSTANT="@ABCDEF";
    public static void main(String[] args) {

        //test();
        try {
            //String aa=new String(keybytes, "UTF-8");
            //System.out.println(aa);
            //log.info(ModuleClassification.SM_CPO+"解密：{}",keybytes);
            //log.info(ModuleClassification.SM_CPO+"解密：{}",aa.getBytes());
            //String bb=KEY_PART_CONFIG+KEY_PART_CONSTANT;
            //log.info(ModuleClassification.SM_CPO_ +"bb：{}",bb);
            //log.info(ModuleClassification.SM_CPO_ +"bb：{}",bb.getBytes());
        }
        catch (Exception e)

        {

        }

    }

    private static void test()
    {
        BufferedReader reader;
        try {
            String st = "";
            do{
                if("".equals(st)) {
                    log.info(ModuleClassification.SM_CPO_ +"AES加密与解密操作:");
                    log.info(ModuleClassification.SM_CPO_ +"\"E\":加密 \t\"D\":解密\t\t\"Q\":退出");
                    log.info(ModuleClassification.SM_CPO_ +"请输入操作代码:");
                }
                reader = new BufferedReader(new InputStreamReader(System.in));
                st = reader.readLine();
                if("E".equalsIgnoreCase(st)) {
                    log.info(ModuleClassification.SM_CPO_ +"请输入待加密字符串:");
                    st = reader.readLine();
                    if(!"".equals(st.trim())) {
                        log.info(ModuleClassification.SM_CPO_ +"加密前:" + st.trim());
                        log.info(ModuleClassification.SM_CPO_ +"加密后:" + encrypt(st.trim()) + "\n\n");
                    }
                    st = "";
                }else if("D".equalsIgnoreCase(st)) {
                    log.info(ModuleClassification.SM_CPO_ +"请输入待解密字符串:");
                    st = reader.readLine();
                    if(!"".equals(st.trim())) {
                        log.info(ModuleClassification.SM_CPO_ +"解密前:" + st.trim());
                        log.info(ModuleClassification.SM_CPO_ +"解密后:" + decrypt(st.trim()) + "\n\n");
                    }
                    st = "";
                }
            } while(!st.equalsIgnoreCase("Q"));
        } catch (Exception e) {
            log.error(ModuleClassification.SM_CPO_ +"TError"+e.getMessage(),e);
        }
    }


    /**
     * @Title: encrypt
     * @Description: 加密
     * @param @param value
     * @param @return    设定文件
     * @return String    返回类型
     * @throws
     */
    public static String encrypt(String value) {

        String s = null;
        int mode = Cipher.ENCRYPT_MODE;
        try {
            Cipher cipher = initCipher(mode);
            byte[] outBytes = cipher.doFinal(value.getBytes());
            s = String.valueOf(Hex.encodeHex(outBytes));
        } catch (Exception e) {
            log.error(ModuleClassification.SM_CPO_ +"TError"+e.getMessage(),e);
        }
        return s;
    }


    /**
     * @Title: decrypt
     * @Description: 解密
     * @param @param value
     * @param @return    设定文件
     * @return String    返回类型
     * @throws
     */
    public static String decrypt(String value) {
        String s = null;

        int mode = Cipher.DECRYPT_MODE;
        try {
            Cipher cipher = initCipher(mode);

            byte[] outBytes = cipher
                    .doFinal(Hex.decodeHex(value.toCharArray()));
            s = new String(outBytes);
        } catch (Exception e) {
            log.error(ModuleClassification.SM_CPO_ +"TError"+e.getMessage(),e);
        }
        return s;
    }


    /**
     * @Title: initCipher
     * @author xuxm
     * @Description: 初始化密码
     * @param @param mode
     * @param @return
     * @param @throws NoSuchAlgorithmException
     * @param @throws NoSuchPaddingException
     * @param @throws InvalidKeyException    设定文件
     * @return Cipher    返回类型
     * @throws
     */
    private static Cipher initCipher(int mode) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        byte[] keyBytes=(KEY_PART_CONFIG+KEY_PART_CONSTANT).getBytes();
        Key key = new SecretKeySpec(keyBytes, "AES");
        cipher.init(mode, key);
        return cipher;
    }

}
