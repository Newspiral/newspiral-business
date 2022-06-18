package com.jinninghui.newspiral.security.cert;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.ECPublicKey;
import java.security.spec.*;

/**
 * @author
 * @date
 * 密钥对工厂
 */
@Slf4j
public class KeyGenerateFactory {

    private static final Logger logger = LoggerFactory.getLogger(KeyGenerateFactory.class);
    /**
     * BouncyCastle算法提供者
     */
    private static final Provider BCProvider = new BouncyCastleProvider();
    /**
     * 初始化
     */
    static {
        Security.addProvider(BCProvider);
    }

    /**
     * 公私钥算法
     */
    public enum KeyAlgorithm{
        BC_SM2P256V1("sm2p256v1","国密SM2算法"),
        EC_SECP256R1("secp256r1","EC算法R"),
        EC_SECP256K1("secp256k1","EC算法K"),
        RSA("RSA","RSA算法"),
        ;

        private String code;
        private String message;

        KeyAlgorithm(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * 生成新的密钥对（SM2类型）
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     */
    public static KeyPairGenerator generatorKeySM2() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        // 获取SM2椭圆曲线的参数
        final ECGenParameterSpec sm2Spec = new ECGenParameterSpec(KeyAlgorithm.BC_SM2P256V1.getCode());
        // 获取一个椭圆曲线类型的密钥对生成器
        final KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", new BouncyCastleProvider());
        // 使用SM2参数初始化生成器
        kpg.initialize(sm2Spec);
        return kpg;
    }

    public static PublicKey loadPublicKey(byte[] encodedKey, String algorithm)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 生成新的密钥对（RSA类型）
     * @param size
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static KeyPairGenerator generatorKeyRSA(int size) throws NoSuchAlgorithmException {
        if(size<=0){
            size = 2048;
        }
        // 使用RSA参数初始化生成器
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyAlgorithm.RSA.getCode());
        kpg.initialize(size);
        return kpg;
    }

    /**
     * 通过私钥文件字节获取私钥（SM2类型）
     * @param privateByte
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static PrivateKey getPrivateKeySM2(byte[] privateByte) {
        //去掉密码之外的字符，并进行转码操作
        String privateKeyPEM=new String(privateByte);
        privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "");
        privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");
        privateKeyPEM = privateKeyPEM.replace("-----BEGIN EC PRIVATE KEY-----", "");
        privateKeyPEM = privateKeyPEM.replace("-----END EC PRIVATE KEY-----", "");
        privateKeyPEM = privateKeyPEM.replaceAll("\n", "");
        privateKeyPEM = privateKeyPEM.replaceAll("\r", "");
        privateKeyPEM = privateKeyPEM.replace("\r\n", "");
        privateKeyPEM = privateKeyPEM.replaceAll(" ", "");
        return getPrivateKey(privateKeyPEM,"EC");
    }

    /**
     * 通过私钥文件字节获取私钥（RSA类型）
     * @param privateByte
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static PrivateKey getPrivateKeyRSA(byte[] privateByte) {
        //去掉密码之外的字符，并进行转码操作
        String privateKeyPEM=new String(privateByte);
        privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "");
        privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");
        privateKeyPEM = privateKeyPEM.replace("-----BEGIN RSA PRIVATE KEY-----", "");
        privateKeyPEM = privateKeyPEM.replace("-----END RSA PRIVATE KEY-----", "");
        privateKeyPEM = privateKeyPEM.replaceAll("\n", "");
        privateKeyPEM = privateKeyPEM.replaceAll("\r", "");
        privateKeyPEM = privateKeyPEM.replaceAll(" ", "");
        return getPrivateKey(privateKeyPEM,"RSA");
    }

    public static PublicKey getPublicKeySM2(byte[] privateByte) {
        //去掉密码之外的字符，并进行转码操作
        String getPublicKeyPEM=new String(privateByte);
        getPublicKeyPEM = getPublicKeyPEM.replace("-----BEGIN PUBLIC KEY-----", "");
        getPublicKeyPEM = getPublicKeyPEM.replace("-----END PUBLIC KEY-----", "");
        getPublicKeyPEM = getPublicKeyPEM.replace("-----BEGIN EC PUBLIC KEY-----", "");
        getPublicKeyPEM = getPublicKeyPEM.replace("-----END EC PUBLIC KEY-----", "");
        getPublicKeyPEM = getPublicKeyPEM.replaceAll("\n", "");
        getPublicKeyPEM = getPublicKeyPEM.replaceAll("\r", "");
        getPublicKeyPEM = getPublicKeyPEM.replace("\r\n", "");
        getPublicKeyPEM = getPublicKeyPEM.replaceAll(" ", "");
        return getPublicKey(getPublicKeyPEM,"EC");
    }

    /**
     * BASE64Encoder 加密
     * @param data
     * @return
     */
    public static String encryptBASE64(byte[] data) {
        // 从JKD 9开始rt.jar包已废除，从JDK 1.8开始使用java.util.Base64.Encoder
        java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
        String encode = encoder.encodeToString(data);
        return encode;
    }

    /**
     * BASE64Decoder 解密
     * @param data
     * @return
     */
    public static byte[] decryptBASE64(String data) {
        // 从JKD 9开始rt.jar包已废除，从JDK 1.8开始使用java.util.Base64.Decoder
        java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();
        byte[] buffer = decoder.decode(data);
        return buffer;
    }

    /**
     * 使用getPublicKey得到公钥,返回类型为PublicKey
     *
     * @param key base64 String to PublicKey: GMCertificateUtil.encryptBASE64(publicKey.getEncoded())
     * @throws Exception
     */
    public static PublicKey getPublicKey(String key,String type){
        try{
            byte[] keyBytes;
            keyBytes = decryptBASE64(key);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            //密钥的算法，例如 RSA
            KeyFactory keyFactory = KeyFactory.getInstance(type);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            return publicKey;
        }catch (NoSuchAlgorithmException e){
            log.error("KeyGenerateFactory.getPublicKey,e=",e);

        }catch (InvalidKeySpecException e){
            log.error("KeyGenerateFactory.getPublicKey,e=",e);
        }
       return null;
    }

    /**
     * 使用getPrivateKey得到私钥,返回类型为PrivateKey
     * @param key
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static PrivateKey getPrivateKey(String key,String type) {
        try{
            byte[] keyBytes;
            keyBytes = decryptBASE64(key);
            PKCS8EncodedKeySpec  keySpec = new PKCS8EncodedKeySpec(keyBytes);
            //密钥的算法，例如 RSA
            KeyFactory keyFactory = KeyFactory.getInstance(type);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            return privateKey;
        }catch (NoSuchAlgorithmException e){
            log.error("KeyGenerateFactory.getPrivateKey,e=",e);

        }catch (InvalidKeySpecException e){
            log.error("KeyGenerateFactory.getPrivateKey,e=",e);
        }
        return null;
    }

    /**
     * 解析私钥私钥，根据十六进制
     * @param hexPrivate
     * @param algorithm
     * @return
     */
    public static PrivateKey getPrivateKeyByHexWithAlgorithm(String hexPrivate, KeyAlgorithm algorithm) {
        try {
            BigInteger priv = new BigInteger(hexPrivate, 16);
            org.bouncycastle.jce.spec.ECParameterSpec ecParameterSpec = ECNamedCurveTable.getParameterSpec(algorithm.code);
            org.bouncycastle.jce.spec.ECPrivateKeySpec privateKeySpec = new org.bouncycastle.jce.spec.ECPrivateKeySpec(priv, ecParameterSpec);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
            return privateKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error("解析私钥异常：",e);
        }
        return null;
    }

    /**
     * 解析公钥，根据十六进制
     * @param hexPublic
     * @param algorithm
     * @return
     */
    public static PublicKey getPublicKeyByHexWithAlgorithm(String hexPublic, KeyAlgorithm algorithm) {
        try{
            byte[] pubKey = new BigInteger(hexPublic, 16).toByteArray();
            ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec(algorithm.code);
            KeyFactory kf = KeyFactory.getInstance("EC", new BouncyCastleProvider());
            ECNamedCurveSpec params = new ECNamedCurveSpec(algorithm.code, spec.getCurve(), spec.getG(), spec.getN());
            ECPoint point = ECPointUtil.decodePoint(params.getCurve(), pubKey);
            ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, params);
            ECPublicKey pk = (ECPublicKey) kf.generatePublic(pubKeySpec);
            return pk;
        }catch (Exception e){
            logger.error("解析公钥异常：",e);
        }
        return null;
    }

    /**
     * 根据种子（助记词）生成公私钥
     * @param algorithm
     * @param seed
     */
    public static KeyPair generatorKeyWithAlgorithm(KeyAlgorithm algorithm,String seed){
        try{
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA");
            ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec(algorithm.code);
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.setSeed(seed.getBytes());
            keyPairGenerator.initialize(ecGenParameterSpec, secureRandom);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            return keyPair;
        }catch (Exception e){
            logger.error("生成公私钥异常：",e);
        }
        return null;
    }

    /**
     * 字符数组转十六进制
     * @param bytes
     * @return
     */
    public static String byteToHex(byte[] bytes){
        String strHex = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < bytes.length; n++) {
            strHex = Integer.toHexString(bytes[n] & 0xFF);
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex); // 每个字节由两个字符表示，位数不够，高位补0
        }
        return sb.toString().trim();
    }

}