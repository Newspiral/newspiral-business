package com.jinninghui.newspiral.security.utils;

import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * @version V1.0
 * @Title: RsaUtil
 * @Package com.jinninghui.newspiral.security.utils
 * @Description:
 * @author: xuxm
 * @date: 2019/12/24 15:22
 */
@Slf4j
public class RSAUtil {
    /** 编码格式 **/
    private  static final String CHARSET = "UTF-8";
    /** 签名算法**/
    private  static final String ALGORITHM = "SHA256withRSA";


    /**
     * 请求报文签名
     * @param privateKey  机构私钥字符串
     * @param content     签名原文
     * @return            签名密文
     * @throws Exception
     */
    public static String sign(String privateKey, String content)throws Exception {
        Signature signature = Signature.getInstance(ALGORITHM);
        signature.initSign(convertPrivateKey(privateKey));
        signature.update(content.getBytes(CHARSET));
        return Base64.encodeBase64String(signature.sign());
    }

    /**
     * 返回报文验签
     * @param publicKey   公钥字符串
     * @param content     验签原文报文
     * @param signStr     返回签名字符串
     * @return            验签结果
     * @throws Exception
     */
    public static boolean vertify(String publicKey, String content, String signStr)throws Exception {
        Signature signature = Signature.getInstance(ALGORITHM);
        signature.initVerify(convertPublicKey(publicKey));
        signature.update(content.getBytes(CHARSET));
        return signature.verify(Base64.decodeBase64(signStr.getBytes(CHARSET)));
    }


    /**
     * 非对称密钥公钥加密
     * @param publicKey  公钥字符串
     * @return           加密密文
     * @throws Exception
     */
    public static String encryptByPublicKey(String publicKey, String plaintextKey)throws Exception {
        String result = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, convertPublicKey(publicKey));
            byte[] encoded = cipher.doFinal(plaintextKey.getBytes(CHARSET));
            result = Base64.encodeBase64String(encoded);
        } catch (Exception e) {
            log.error(ModuleClassification.SM_RSAU_ +"TError"+"encryptByPublicKey error:",e);
        }
        return result;
    }

    /**
     * 非对称密钥密文解密
     * @param privateKey    私钥字符串
     * @param content       非对称密钥密文
     * @return              非对称密钥明文
     * @throws Exception
     */
    public static String decryptByPrivateKey(String privateKey, String content)throws Exception {
        String result = null;
        try {

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, convertPrivateKey(privateKey));
            byte[] encoded = cipher.doFinal(Base64.decodeBase64(content.getBytes(CHARSET)));
            result = new String(encoded,CHARSET);
        } catch (Exception e) {
            log.error(ModuleClassification.SM_RSAU_ +"TError"+"decryptByPrivateKey error:",e);
        }
        return result;
    }

    /**
     *
     * @param keyStr
     * @return
     * @throws Exception
     */
    protected static PrivateKey convertPrivateKey(String keyStr)throws Exception {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(keyStr.getBytes(CHARSET)));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);

    }

    /**
     *
     * @param keyStr
     * @return
     * @throws Exception
     */
    protected  static PublicKey convertPublicKey(String keyStr)throws Exception {

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
        Certificate certificate = certificateFactory.generateCertificate(new ByteArrayInputStream(Base64.decodeBase64(keyStr.getBytes(CHARSET))));
        return certificate.getPublicKey();
    }

    /**
     *
     * @param publicKeyPath
     * @return
     */
    public static String getPublicKey(String publicKeyPath){
        if(publicKeyPath==null){
            throw new RuntimeException("请设置公钥证书路径！");
        }
        return getKeyFromKeyPath(publicKeyPath);
    }

    /**
     *
     * @param privateKeyPath
     * @return
     */
    public static String getPrivateKey(String privateKeyPath){
        if(privateKeyPath==null){
            throw new RuntimeException("请设置私钥证书路径！");
        }
        return getKeyFromKeyPath(privateKeyPath);
    }


    /**
     *
     * @param keyPath
     * @return
     */
    protected static String getKeyFromKeyPath(String keyPath) {
        File file = new File(keyPath);
        if(!file.exists()){
            throw new RuntimeException("证书文件不存在:"+keyPath);
        }
        BufferedReader reader = null;
        StringBuffer keyString = new StringBuffer();
        String tempString = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            while ((tempString = reader.readLine()) != null) {
                if(tempString.startsWith("--")){
                    continue;
                }
                keyString.append(tempString);
            }
            reader.close();
        } catch (IOException e) {
            log.error(ModuleClassification.SM_RSAU_ +"TError"+"getKeyFromKeyPath error:",e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error(ModuleClassification.SM_RSAU_ +"TError"+"getKeyFromKeyPath error:",e);
                }
            }
        }
        return keyString.toString();

    }

/*    public static void main(String[] args) {
        try {
            String sign = sign("MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAONGdZEmPHLMwsliHydvedLd2Po5QHY3DL2qJDd/SC/uU04kWjTrwtlksSeZ7k4qN8Ri5rqx6umEZ1YEpaf9AnCjbeReyWWjhB8SYphwJ7RqguWsc9O/Bw2/dHHhV44hFTu5MvTqr39n/PX8F4JYEmfQcTozRD+ckxwDELS0dMZnAgMBAAECgYAt1OegfbVy8RfWxJLDwRcwDszSqPj23eMT3FuxVVmZUNY7y9jlEyYX8NZIxiHoYVsdn1QoEfjtXmX4UreVSNBUxVOc1VhYRt9+/EqnR7K7r+38xMFMCZGU2vzLB9QSYOMx2zInGTLVbdrffQ662N8rJOnAL3Xzl3FlRzMfO2egAQJBAPVkSMuZQdttLlPxuQxGwUsBXl91Cwg5h/mStfvgfQCbVPM2tRH0bcHIDRm5isFjRwtB3neIG/aoDytunYR3KYECQQDtGa6d72k4cVNh5VYL+a2l07klXK064MTBM3AvPsW37DYlXYWrmVA9wVu2s6n1o0Xh6dX56VRbcGHn5Ye4OdPnAkEAlcm1NbJDJPGsSclPL1MxQduAA7yqE0cq3QjU1P7ezrQCUsOeG2ucY6D9fipnrEwxXYnDhisrBpDnUZefxvAGAQJAIIynCS2Fz2bXYJggUPKM4TgIsdVgPrX+rNtH6mR+tjoqUMTfBei9OE0kLxfJHFy2ykXUx0M9/nOWLUS4dhUSPwJASjo7vt95mbDD+8ccNuOFM6rp0BJ0l4H4onB7cWmOdX+kwPKXFvE2L7+LMOw9cAV6Di2HkI4c+GFBsXGN2gwKCQ==","MaterialOne");
            System.out.println(sign);
            vertify(
                    "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDjRnWRJjxyzMLJYh8nb3nS3dj6OUB2Nwy9qiQ3f0gv7lNOJFo068LZZLEnme5OKjfEYua6serphGdWBKWn/QJwo23kXsllo4QfEmKYcCe0aoLlrHPTvwcNv3Rx4VeOIRU7uTL06q9/Z/z1/BeCWBJn0HE6M0Q/nJMcAxC0tHTGZwIDAQAB",
                    "MaterialOne",sign);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
