package com.jinninghui.newspiral.security.utils;

import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.util.encoders.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import java.util.Base64.Encoder;
import java.util.Base64.Decoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version V1.0
 * @Title: GMCertificateUtil
 * @Package com.jinninghui.newspiral.security.utils
 * @Description:
 * @author: xuxm
 * @date: 2019/12/28 16:22
 */
@Slf4j
public class GMCertificateUtil {

    //static ThreadLocal<Signature> signatureThreadLocal = new ThreadLocal<>();

    //static ThreadLocal<KeyFactory> keyFactoryThreadLocal = new ThreadLocal<>();

    private static BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();

    private static Map<String,PublicKey> publicKeyMap=new ConcurrentHashMap<>();

    private static Map<String,PrivateKey> privateKeyMap=new ConcurrentHashMap<>();

    /**
     *  初始化
     */
    static {
        Security.addProvider(new BouncyCastleProvider());
/*        BouncyCastleProvider bc = new BouncyCastleProvider();
        Set<Provider.Service> services = bc.getServices();
        for (Provider.Service s:services){
            if (s.toString().toUpperCase().contains("CIPHER")) System.out.println(s.toString());
        }*/
    }

    /**
     * 通过私钥文件字节获取私钥
     *
     * @param privateByte
     * @return
     */
    public static PrivateKey getPrivateKey(byte[] privateByte) {
        try {
            //去掉密码之外的字符，并进行转码操作
            String privateKeyPEM=new String(privateByte);
            privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "");
            privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");
            privateKeyPEM = privateKeyPEM.replaceAll("\n", "");
            privateKeyPEM = privateKeyPEM.replaceAll("\r", "");
            privateKeyPEM = privateKeyPEM.replaceAll(" ", "");
            byte[] keyData = Base64.decode(privateKeyPEM);
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyData);
            KeyFactory keyFactory = KeyFactory.getInstance("EC",bouncyCastleProvider);
            return keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.error(ModuleClassification.SM_GMCU_ +"TError"+"GMCertificateUtil.getPrivateKey is error:", e);
        }
        return null;
    }

    /**
     * 通过证书文件字节获取证书
     *
     * @param certByte
     * @return
     * @throws IOException
     */
    public static X509Certificate getGMCertificateByCertByte(byte[] certByte) throws IOException {
        InputStream inputStream = null;
        try {
                // 实例化证书工厂
            CertificateFactory factory = CertificateFactory.getInstance("X509", "BC");

            // 取得证书文件流
            inputStream = new ByteArrayInputStream(certByte);
            //new FileInputStream(certByte);
            // 生成证书
            X509Certificate certificate = (X509Certificate) factory.generateCertificate(inputStream);
            return certificate;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.error(ModuleClassification.SM_GMCU_ +"TError"+"GMCertificateUtil.getGMCertificateByCertByte is error:", e);
        } finally {
            if (null != inputStream) {
                inputStream.close();
            }
        }
        return null;
    }

    /**
     * GM签名
     *
     * @param privateKey
     * @param plainText
     * @return
     */
    public static byte[] signByGM(PrivateKey privateKey, byte[] plainText) {
        try {
            ECPrivateKeyParameters priKeyParameters = BCECUtil.convertPrivateKeyToParameters((BCECPrivateKey)privateKey);
            SM2Signer signer = new SM2Signer();
            ParametersWithRandom pwr = new ParametersWithRandom(priKeyParameters, new SecureRandom());
            signer.init(true, pwr);
            signer.update(plainText, 0, plainText.length);
            return signer.generateSignature();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.error(ModuleClassification.SM_GMCU_ +"TError"+"GMCertificateUtil.signByGM is error:", e);
        }
        return null;
    }
/*    public static byte[] signByGM(PrivateKey privateKey, byte[] plainText) {
        try {
*//*            Signature signature = signatureThreadLocal.get();
            if (signature == null) {
                signature = Signature.getInstance("SM3withSM2", bouncyCastleProvider);
                //signature = Signature.getInstance("SM3withSM2");
                signatureThreadLocal.set(signature);
            }*//*

            Signature signature = Signature.getInstance("SM3withSM2", bouncyCastleProvider);
            signature.initSign(privateKey);
            signature.update(plainText);
            return signature.sign();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.error(ModuleClassification.SM_GMCU_ +"TError"+"GMCertificateUtil.signByGM is error:", e);
        }
        return null;
    }*/

    /**
     * 国密验签
     * @param publicKey 公钥
     * @param decodedText 解密之后的信息（hash）
     * @param receivedignature 签名（如果是对hash签名，则使用公钥解密之后就是hash值）
     * @return
     */
    public static boolean verifyByGM(PublicKey publicKey, byte[] decodedText, final byte[] receivedignature) {
        try {
            ECPublicKeyParameters pubKeyParameters = BCECUtil.convertPublicKeyToParameters((BCECPublicKey)publicKey);
            SM2Signer signer = new SM2Signer();

            signer.init(false, pubKeyParameters);
            signer.update(decodedText, 0, decodedText.length);
            return signer.verifySignature(receivedignature);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.error(ModuleClassification.SM_GMCU_ +"TError"+"GMCertificateUtil.verifyByGM is error:", e);
            return false;
        }
    }
/*    public static boolean verifyByGM(PublicKey publicKey, byte[] decodedText, final byte[] receivedignature) {
        try {

            Signature  signature = Signature.getInstance("SM3withSM2", bouncyCastleProvider);
            *//** 注意这里用到的是证书，实际上用到的也是证书里面的公钥 *//*
            signature.initVerify(publicKey);
            signature.update(decodedText);
            return signature.verify(receivedignature);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.error(ModuleClassification.SM_GMCU_ +"TError"+"GMCertificateUtil.verifyByGM is error:", e);
            return false;
        }
    }*/

    /**
     * @param text      需要加密的文本
     * @param publicKey 公钥
     * @return Base64.toBase64String 的加密字符串
     */
    public static String encode(byte[] text, PublicKey publicKey) {
        //获取加密参数
        BCECPublicKey localECPublicKey = (BCECPublicKey) publicKey;
        ECParameterSpec localECParameterSpec = localECPublicKey.getParameters();
        ECDomainParameters localECDomainParameters = new ECDomainParameters(
                localECParameterSpec.getCurve(), localECParameterSpec.getG(),
                localECParameterSpec.getN());
        ECPublicKeyParameters localECPublicKeyParameters = new ECPublicKeyParameters(localECPublicKey.getQ(),
                localECDomainParameters);
        //初始化加密引擎
        SM2Engine sm2EncEngine = new SM2Engine();
        sm2EncEngine.init(true, new ParametersWithRandom(localECPublicKeyParameters));
        //加密
        try {
            byte[] bs = sm2EncEngine.processBlock(text, 0, text.length);
            return Base64.toBase64String(bs);
        } catch (Exception e) {
            log.error(ModuleClassification.SM_GMCU_ +"TError"+"GMCertificateUtil.encode is error:", e);
        }
        return null;
    }

    /**
     *
     * @param text 需要解密的文本
     * @param privateKey 私钥
     * @return 明文
     */
    public static String decode(String text, PrivateKey privateKey) {
        BCECPrivateKey sm2PriK = (BCECPrivateKey) privateKey;
        ECParameterSpec ecParameterSpec = sm2PriK.getParameters();
        ECDomainParameters ecDomainParameters = new ECDomainParameters(
                ecParameterSpec.getCurve(), ecParameterSpec.getG(),
                ecParameterSpec.getN());
        ECPrivateKeyParameters localECPrivateKeyParameters = new ECPrivateKeyParameters(
                sm2PriK.getD(), ecDomainParameters);
        //初始化解密引擎
        SM2Engine sm2DcEngine = new SM2Engine();
        sm2DcEngine.init(false, localECPrivateKeyParameters);
        try {
            byte[] bs = Base64.decode(text.getBytes("Utf-8"));
            byte[] b = sm2DcEngine.processBlock(bs, 0, bs.length);
            return new String(b);
        } catch (Exception e) {
            log.error(ModuleClassification.SM_GMCU_ +"TError"+"GMCertificateUtil.decode is error:", e);
        }
        return null;
    }

    /**
     * 使用getPublicKey得到公钥,返回类型为PublicKey
     *
     * @param key base64 String to PublicKey: GMCertificateUtil.encryptBASE64(publicKey.getEncoded())
     * @throws Exception
     */
    public static PublicKey getPublicKey(String key) throws Exception {
        PublicKey publicKey=  publicKeyMap.get(key);
        if(null!=publicKey) return publicKey;
        byte[] keyBytes;
        //keyBytes = (new BASE64Decoder()).decodeBuffer(key);
        keyBytes = decryptBASE64(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        //密钥的算法，例如 RSA
        KeyFactory keyFactory = KeyFactory.getInstance("EC",bouncyCastleProvider);
/*        KeyFactory keyFactory = keyFactoryThreadLocal.get();
        if (keyFactory == null) {
            keyFactory = KeyFactory.getInstance("EC");
            keyFactoryThreadLocal.set(keyFactory);
        }*/
         publicKey = keyFactory.generatePublic(keySpec);
        publicKeyMap.put(key,publicKey);
        return publicKey;
    }

    /**
     * 使用getPrivateKey得到私钥,返回类型为PrivateKey
     * @param key base64 String to PrivateKey: GMCertificateUtil.encryptBASE64(privateKey.getEncoded())
     * @return
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(String key) throws Exception {
        PrivateKey privateKey=  privateKeyMap.get(key);
        if(null!=privateKey) return privateKey;
        byte[] keyBytes;
        //keyBytes = (new BASE64Decoder()).decodeBuffer(key);
        keyBytes = decryptBASE64(key);
        PKCS8EncodedKeySpec  keySpec = new PKCS8EncodedKeySpec(keyBytes);
        //密钥的算法，例如 RSA
        KeyFactory keyFactory = KeyFactory.getInstance("EC",bouncyCastleProvider);
/*        KeyFactory keyFactory = keyFactoryThreadLocal.get();
        if (keyFactory == null) {
            keyFactory = KeyFactory.getInstance("EC");
            keyFactoryThreadLocal.set(keyFactory);
        }*/
         privateKey = keyFactory.generatePrivate(keySpec);
        privateKeyMap.put(key,privateKey);
        return privateKey;
    }


    /**
     * BASE64Encoder 加密
     *
     * @param data
     *            要加密的数据
     * @return 加密后的字符串
     */
    public static String encryptBASE64(byte[] data) {
        // BASE64Encoder encoder = new BASE64Encoder();
        // String encode = encoder.encode(data);
        // 从JKD 9开始rt.jar包已废除，从JDK 1.8开始使用java.util.Base64.Encoder
        Encoder encoder = java.util.Base64.getEncoder();
        String encode = encoder.encodeToString(data);
        return encode;
    }
    /**
     * BASE64Decoder 解密
     *
     * @param data
     *            要解密的字符串
     * @return 解密后的byte[]
     * @throws Exception
     */
    public static byte[] decryptBASE64(String data) throws Exception {
        // BASE64Decoder decoder = new BASE64Decoder();
        // byte[] buffer = decoder.decodeBuffer(data);
        // 从JKD 9开始rt.jar包已废除，从JDK 1.8开始使用java.util.Base64.Decoder
        Decoder decoder = java.util.Base64.getDecoder();
        byte[] buffer = decoder.decode(data);
        return buffer;
    }
}
