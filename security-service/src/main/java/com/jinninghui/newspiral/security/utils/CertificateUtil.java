package com.jinninghui.newspiral.security.utils;

import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * @version V1.0
 * @Title: MyCertifacate
 * @Package com.jinninghui.newspiral.security.utils
 * @Description:
 * @author: xuxm
 * @date: 2019/12/19 14:02
 */
@Slf4j
public class CertificateUtil {

    /**
     * X.509标准的证书
     */
    public static final String CERT_TYPE = "X.509";

    /**
     * 加载密钥库，与Properties文件的加载类似，都是使用load方法
     *
     * @throws IOException
     */
    public static KeyStore getKeyStore(String storepass, String keystorePath)
            throws IOException {
        InputStream inputStream = null;
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            inputStream = new FileInputStream(keystorePath);
            keyStore.load(inputStream, storepass.toCharArray());
            return keyStore;
        } catch (KeyStoreException | NoSuchAlgorithmException
                | CertificateException | IOException e) {
            // TODO Auto-generated catch block
            log.error(ModuleClassification.SM_CU_ +"TError"+"CertificateUtil.getKeyStore is error：", e);
        } finally {
            if (null != inputStream) {
                inputStream.close();
            }
        }
        return null;
    }

    /**
     * @param storepass
     * @param keyStoreByte
     * @return
     * @throws IOException
     */
    public static KeyStore getKeyStore(String storepass, byte[] keyStoreByte)
            throws IOException {
        InputStream inputStream = null;
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            inputStream = new ByteArrayInputStream(keyStoreByte);
            ;
            keyStore.load(inputStream, storepass.toCharArray());
            return keyStore;
        } catch (KeyStoreException | NoSuchAlgorithmException
                | CertificateException | IOException e) {
            // TODO Auto-generated catch block
            log.error(ModuleClassification.SM_CU_ +"TError"+"CertificateUtil.getKeyStore is error:", e);
        } finally {
            if (null != inputStream) {
                inputStream.close();
            }
        }
        return null;
    }

    /**
     * 获取私钥
     *
     * @param keyStore
     * @param alias
     * @param password
     * @return
     */
    public static PrivateKey getPrivateKey(KeyStore keyStore, String alias,
                                           String password) {
        try {
            return (PrivateKey) keyStore.getKey(alias, password.toCharArray());
        } catch (UnrecoverableKeyException | KeyStoreException
                | NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            log.error(ModuleClassification.SM_CU_ +"TError"+"CertificateUtil.getPrivateKey is error:", e);
        }
        return null;
    }

    /**
     * 获取公钥
     *
     * @param certificate
     * @return
     */
    public static PublicKey getPublicKey(Certificate certificate) {
        return certificate.getPublicKey();
    }

    /**
     * 通过密钥库获取数字证书，不需要密码，因为获取到Keystore实例
     *
     * @param keyStore
     * @param alias
     * @return
     */
    public static X509Certificate getCertificateByKeystore(KeyStore keyStore,
                                                           String alias) {
        try {
            return (X509Certificate) keyStore.getCertificate(alias);
        } catch (KeyStoreException e) {
            // TODO Auto-generated catch block
            log.error(ModuleClassification.SM_CU_ +"TError"+"CertificateUtil.getCertificateByKeystore is error:", e);
        }
        return null;
    }

    /**
     * 通过证书路径生成证书，与加载密钥库差不多，都要用到流。
     *
     * @param path
     * @param certType
     * @return
     * @throws IOException
     */
    public static X509Certificate getCertificateByCertPath(String path,
                                                           String certType) throws IOException {
        InputStream inputStream = null;
        try {
            // 实例化证书工厂
            CertificateFactory factory = CertificateFactory
                    .getInstance(certType);
            // 取得证书文件流
            inputStream = new FileInputStream(path);
            // 生成证书
            Certificate certificate = factory.generateCertificate(inputStream);
            return (X509Certificate) certificate;
        } catch (CertificateException | IOException e) {
            // TODO Auto-generated catch block
            log.error(ModuleClassification.SM_CU_ +"TError"+"CertificateUtil.getCertificateByCertPath is error:", e);
        } finally {
            if (null != inputStream) {
                inputStream.close();
            }
        }
        return null;
    }

    public static X509Certificate getCertificateByCertByte(byte[] certByte,
                                                           String certType) throws IOException {
        InputStream inputStream = null;
        try {
            // 实例化证书工厂
            CertificateFactory factory = CertificateFactory
                    .getInstance(certType);
            // 取得证书文件流
            inputStream = new ByteArrayInputStream(certByte);
            // 生成证书
            Certificate certificate = factory.generateCertificate(inputStream);
            return (X509Certificate) certificate;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.error(ModuleClassification.SM_CU_ +"TError"+"CertificateUtil.getCertificateByCertByte is error:", e);
        } finally {
            if (null != inputStream) {
                inputStream.close();
            }
        }
        return null;
    }

    /**
     * 从证书中获取加密算法，进行签名
     *
     * @param certificate
     * @param privateKey
     * @param plainText
     * @return
     */
    public static byte[] sign(X509Certificate certificate,
                              PrivateKey privateKey, byte[] plainText) {
        try {
            Signature signature = Signature.getInstance(certificate
                    .getSigAlgName());
            signature.initSign(privateKey);
            signature.update(plainText);
            return signature.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException
                | SignatureException e) {
            // TODO Auto-generated catch block
            log.error(ModuleClassification.SM_CU_ +"TError"+"CertificateUtil.sign is error:", e);
        }
        return null;
    }

    /**
     * 验签，公钥包含在证书里面
     *
     * @param certificate
     * @param decodedText
     * @param receivedignature
     * @return
     */
    public static boolean verify(X509Certificate certificate,
                                 byte[] decodedText, final byte[] receivedignature) {
        try {
            Signature signature = Signature.getInstance(certificate
                    .getSigAlgName());
            /** 注意这里用到的是证书，实际上用到的也是证书里面的公钥 */
            signature.initVerify(certificate);
            signature.update(decodedText);
            return signature.verify(receivedignature);
        } catch (NoSuchAlgorithmException | InvalidKeyException
                | SignatureException e) {
            // TODO Auto-generated catch block
            log.error(ModuleClassification.SM_CU_ +"TError"+"CertificateUtil.verify is error:", e);
            return false;
        }
    }

    /**
     * 加密。注意密钥是可以获取到它适用的算法的。
     *
     * @param plainText
     * @param privateKey
     * @return
     */
    public static byte[] encode(byte[] plainText, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            return cipher.doFinal(plainText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            // TODO Auto-generated catch block
            log.error(ModuleClassification.SM_CU_ +"TError"+"CertificateUtil.encode is error:", e);
        }
        return null;

    }

    /**
     * 解密，注意密钥是可以获取它适用的算法的。
     *
     * @param encodedText
     * @param publicKey
     * @return
     */
    public static byte[] decode(byte[] encodedText, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            return cipher.doFinal(encodedText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            // TODO Auto-generated catch block
            log.error(ModuleClassification.SM_CU_ +"TError"+"CertificateUtil.decode is error:", e);
        }
        return null;
    }

    /**
     * @param str
     * @return
     */
    public static byte[] hexStrToByteArray(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return new byte[0];
        }
        byte[] byteArray = new byte[str.length() / 2];
        for (int i = 0; i < byteArray.length; i++) {
            String subStr = str.substring(2 * i, 2 * i + 2);
            byteArray[i] = ((byte) Integer.parseInt(subStr, 16));
        }
        return byteArray;
    }

    /**
     * @param byteArray
     * @return
     */
    public static String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * root cert is self signed
     *
     * @param cert is X509Certificate that will be tested
     * @return true if cert is self signed, false otherwise
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public static boolean isSelfSigned(X509Certificate cert)
            throws CertificateException, NoSuchAlgorithmException,
            NoSuchProviderException {
        try {
            PublicKey key = cert.getPublicKey();
            cert.verify(key);
            return true;
        } catch (SignatureException sigEx) {
            log.error(ModuleClassification.SM_CU_ +"TError"+"CertificateUtil.isSelfSigned is error:", sigEx);
            return false;
        } catch (InvalidKeyException keyEx) {
            log.error(ModuleClassification.SM_CU_ +"TError"+"CertificateUtil.isSelfSigned is error:", keyEx);
            return false;
        }
    }

    /**
     * 验证此证书是否是根证书签发的
     *
     * @param userCert  用户证书
     * @param publicKey 根证书公钥
     * @return
     */
    public static boolean checkVerifyUserCert(X509Certificate userCert, PublicKey publicKey) {
        boolean pass = false;
        try {
            //假设userCert是用户证书，publicKey是根证书的公钥
            userCert.verify(publicKey);
            pass = true;
        } catch (Exception e) {
            pass = false;
            log.error(ModuleClassification.SM_CU_ +"TError"+"CertificateUtil.checkVerifyUserCert is error:", e);
        }
        return pass;
    }

    /** 验证用户证书是否有效
     * @param userCert      用户证书
     * @param caCertificate CA证书
     * @param authType      作者类型
     * @throws CertificateException
     */
    public void checkServerTrusted(X509Certificate userCert, X509Certificate caCertificate, String authType) throws CertificateException {

        if (caCertificate == null) {
            throw new IllegalArgumentException("caCertificate is null");
        }
        if (userCert == null) {
            throw new IllegalArgumentException("userCert is null");
        }
        if (authType == null || authType.length() == 0) {
            throw new IllegalArgumentException("null or zero-length authentication type");
        }
        //Check if certificate send is your CA's
        if (!userCert.equals(caCertificate)) {
            try {   //Not your CA's. Check if it has been signed by your CA
                userCert.verify(caCertificate.getPublicKey());
            } catch (Exception e) {
                throw new CertificateException("Certificate not trusted", e);
            }
        }
        //If we end here certificate is trusted. Check if it has expired.
        try {
            userCert.checkValidity();
        } catch (Exception e) {
            throw new CertificateException("Certificate not trusted. It has expired", e);
        }
    }

    /**
     * 校验证书是否过期
     * @param certificate
     * @return
     */
    public static boolean verifyCertificateValidity(X509Certificate certificate) {
        boolean valid = true;
        try {
            certificate.checkValidity();
        } catch (Exception e) {
            log.error(ModuleClassification.SM_CU_ +"TError"+"CertificateUtil.verifyCertificateValidity error:",e);
            valid = false;
        }
        return valid;
    }

    /**
     * 使用getPublicKey得到公钥,返回类型为PublicKey
     *
     * @param key base64 String to PublicKey: GMCertificateUtil.encryptBASE64(publicKey.getEncoded())
     * @throws Exception
     */
    public static PublicKey getPublicKey(String key) throws Exception {
        byte[] keyBytes;
        //keyBytes = (new BASE64Decoder()).decodeBuffer(key);
        keyBytes = GMCertificateUtil.decryptBASE64(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        //密钥的算法，例如 RSA
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }

    /**
     * 使用getPrivateKey得到私钥,返回类型为PrivateKey
     * @param key base64 String to PrivateKey: GMCertificateUtil.encryptBASE64(privateKey.getEncoded())
     * @return
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(String key) throws Exception {
        byte[] keyBytes;
        //keyBytes = (new BASE64Decoder()).decodeBuffer(key);
        keyBytes = GMCertificateUtil.decryptBASE64(key);
        PKCS8EncodedKeySpec  keySpec = new PKCS8EncodedKeySpec(keyBytes);
        //密钥的算法，例如 RSA
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

}
