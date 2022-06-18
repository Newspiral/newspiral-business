package com.jinninghui.newspiral.common.entity.util;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.digests.SM3Digest;

/**
 * need jars:
 * bcpkix-jdk15on-160.jar
 * bcprov-jdk15on-160.jar
 *
 * ref:
 * https://tools.ietf.org/html/draft-shen-sm2-ecdsa-02
 * http://gmssl.org/docs/oid.html
 * http://www.jonllen.com/jonllen/work/164.aspx
 *
 * 用BC的注意点：
 * 这个版本的BC对SM3withSM2的结果为asn1格式的r和s，如果需要直接拼接的r||s需要自己转换。下面rsAsn1ToPlainByteArray、rsPlainByteArrayToAsn1就在干这事。
 * 这个版本的BC对SM2的结果为C1||C2||C3，据说为旧标准，新标准为C1||C3||C2，用新标准的需要自己转换。下面changeC1C2C3ToC1C3C2、changeC1C3C2ToC1C2C3就在干这事。
 */
@Slf4j
public class OsccaCinpher {
/*    private static ThreadLocal<MessageDigest> digestThreadLocal = new ThreadLocal<>();
    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SM3", "BC");
            if (digest != null) {
                digestThreadLocal.set(digest);
            }
        } catch (NoSuchAlgorithmException e) {
            log.info(ModuleClassification.SM_OC_ +"TError"+"缺少SM3算法",e);
        } catch (NoSuchProviderException e) {
            log.info(ModuleClassification.SM_OC_ +"TError"+"缺少BC提供者",e);
        }
    }*/


/*    public static byte[] calHashBySM3_old(byte[] input) {
        MessageDigest digest1 = digestThreadLocal.get();
        if (digest1 == null) {
            try {
                digest1 = MessageDigest.getInstance("SM3", "BC");
            }
            catch (Exception e)
            {
                log .warn("calHashBySM3.digest,warn");
            }
            digestThreadLocal.set(digest1);
        }
        if (digest1 != null) {
            return digest1.digest(input);
        }
        log.error("calHashBySM3 return null");
        return null;
    }*/


/*    public  byte[] calHashBySM3(byte[] input) {
        try {
            MessageDigest digest1=MessageDigest.getInstance("SM3", "BC");
            return digest1.digest(input);
        }
        catch (Exception e)
        {
            log .warn("calHashBySM3.digest,warn");
        }
        log.error("calHashBySM3 return null");
        return null;
    }*/


    public static byte[] calHashBySM3(byte[] srcData) {
        SM3Digest digest = new SM3Digest();
        digest.update(srcData, 0, srcData.length);
        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);
        return hash;
    }

/*    public static void main(String[] args) throws IOException {

      System.out.println(Hex.encodeHexString(calHashBySM3("38F16D3976492FDD354325CBBCD94E8160E7B01EC87844FA60A3B42A1E56B1BD".getBytes()), false));
        //System.out.println(Hex.encodeHexString(calHashBySM3_old("38F16D3976492FDD354325CBBCD94E8160E7B01EC87844FA60A3B42A1E56B1BD".getBytes()), false));
    }*/

  /*  *//**
     *
     * @param msg
     * @param userId
     * @param privateKey
     * @return r||s，直接拼接byte数组的rs
     *//*
    public static byte[] signSm3WithSm2(byte[] msg, byte[] userId, PrivateKey privateKey){
        return rsAsn1ToPlainByteArray(signSm3WithSm2Asn1Rs(msg, userId, privateKey));
    }

    *//**
     *
     * @param msg
     * @param userId
     * @param privateKey
     * @return rs in <b>asn1 format</b>
     *//*
    public static byte[] signSm3WithSm2Asn1Rs(byte[] msg, byte[] userId, PrivateKey privateKey){
        try {
            SM2ParameterSpec parameterSpec = new SM2ParameterSpec(userId);
            Signature signer = Signature.getInstance("SM3withSM2", "BC");
            signer.setParameter(parameterSpec);
            signer.initSign(privateKey, new SecureRandom());
            signer.update(msg, 0, msg.length);
            byte[] sig = signer.sign();
            return sig;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    *//**
     *
     * @param msg
     * @param userId
     * @param rs r||s，直接拼接byte数组的rs
     * @param publicKey
     * @return
     *//*
    public static boolean verifySm3WithSm2(byte[] msg, byte[] userId, byte[] rs, PublicKey publicKey){
        return verifySm3WithSm2Asn1Rs(msg, userId, rsPlainByteArrayToAsn1(rs), publicKey);
    }

    *//**
     *
     * @param msg
     * @param userId
     * @param rs in <b>asn1 format</b>
     * @param publicKey
     * @return
     *//*
    public static boolean verifySm3WithSm2Asn1Rs(byte[] msg, byte[] userId, byte[] rs, PublicKey publicKey){
        try {
            SM2ParameterSpec parameterSpec = new SM2ParameterSpec(userId);
            Signature verifier = Signature.getInstance("SM3withSM2", "BC");
            verifier.setParameter(parameterSpec);
            verifier.initVerify(publicKey);
            verifier.update(msg, 0, msg.length);
            return verifier.verify(rs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    *//**
     * bc加解密使用旧标c1||c2||c3，此方法在加密后调用，将结果转化为c1||c3||c2
     * @param c1c2c3
     * @return
     *//*
    private static byte[] changeC1C2C3ToC1C3C2(byte[] c1c2c3) {
        final int c1Len = (x9ECParameters.getCurve().getFieldSize() + 7) / 8 * 2 + 1; //sm2p256v1的这个固定65。可看GMNamedCurves、ECCurve代码。
        final int c3Len = 32; //new SM3Digest().getDigestSize();
        byte[] result = new byte[c1c2c3.length];
        System.arraycopy(c1c2c3, 0, result, 0, c1Len); //c1
        System.arraycopy(c1c2c3, c1c2c3.length - c3Len, result, c1Len, c3Len); //c3
        System.arraycopy(c1c2c3, c1Len, result, c1Len + c3Len, c1c2c3.length - c1Len - c3Len); //c2
        return result;
    }


    *//**
     * bc加解密使用旧标c1||c3||c2，此方法在解密前调用，将密文转化为c1||c2||c3再去解密
     * @param c1c3c2
     * @return
     *//*
    private static byte[] changeC1C3C2ToC1C2C3(byte[] c1c3c2) {
        final int c1Len = (x9ECParameters.getCurve().getFieldSize() + 7) / 8 * 2 + 1; //sm2p256v1的这个固定65。可看GMNamedCurves、ECCurve代码。
        final int c3Len = 32; //new SM3Digest().getDigestSize();
        byte[] result = new byte[c1c3c2.length];
        System.arraycopy(c1c3c2, 0, result, 0, c1Len); //c1: 0->65
        System.arraycopy(c1c3c2, c1Len + c3Len, result, c1Len, c1c3c2.length - c1Len - c3Len); //c2
        System.arraycopy(c1c3c2, c1Len, result, c1c3c2.length - c3Len, c3Len); //c3
        return result;
    }

    *//**
     * c1||c3||c2
     * @param data
     * @param key
     * @return
     *//*
    public static byte[] sm2Decrypt(byte[] data, PrivateKey key){
        return sm2DecryptOld(changeC1C3C2ToC1C2C3(data), key);
    }

    *//**
     * c1||c3||c2
     * @param data
     * @param key
     * @return
     *//*

    public static byte[] sm2Encrypt(byte[] data, PublicKey key){
        return changeC1C2C3ToC1C3C2(sm2EncryptOld(data, key));
    }

    *//**
     * c1||c2||c3
     * @param data
     * @param key
     * @return
     *//*
    public static byte[] sm2EncryptOld(byte[] data, PublicKey key){
        BCECPublicKey localECPublicKey = (BCECPublicKey) key;
        ECPublicKeyParameters ecPublicKeyParameters = new ECPublicKeyParameters(localECPublicKey.getQ(), ecDomainParameters);
        SM2Engine sm2Engine = new SM2Engine();
        sm2Engine.init(true, new ParametersWithRandom(ecPublicKeyParameters, new SecureRandom()));
        try {
            return sm2Engine.processBlock(data, 0, data.length);
        } catch (InvalidCipherTextException e) {
            throw new RuntimeException(e);
        }
    }

    *//**
     * c1||c2||c3
     * @param data
     * @param key
     * @return
     *//*
    public static byte[] sm2DecryptOld(byte[] data, PrivateKey key){
        BCECPrivateKey localECPrivateKey = (BCECPrivateKey) key;
        ECPrivateKeyParameters ecPrivateKeyParameters = new ECPrivateKeyParameters(localECPrivateKey.getD(), ecDomainParameters);
        SM2Engine sm2Engine = new SM2Engine();
        sm2Engine.init(false, ecPrivateKeyParameters);
        try {
            return sm2Engine.processBlock(data, 0, data.length);
        } catch (InvalidCipherTextException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] sm4Encrypt(byte[] keyBytes, byte[] plain){
        if(keyBytes.length != 16) throw new RuntimeException("err key length");
        if(plain.length % 16 != 0) throw new RuntimeException("err data length");

        try {
            Key key = new SecretKeySpec(keyBytes, "SM4");
            Cipher out = Cipher.getInstance("SM4/ECB/NoPadding", "BC");
            out.init(Cipher.ENCRYPT_MODE, key);
            return out.doFinal(plain);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] sm4Decrypt(byte[] keyBytes, byte[] cipher){
        if(keyBytes.length != 16) throw new RuntimeException("err key length");
        if(cipher.length % 16 != 0) throw new RuntimeException("err data length");

        try {
            Key key = new SecretKeySpec(keyBytes, "SM4");
            Cipher in = Cipher.getInstance("SM4/ECB/NoPadding", "BC");
            in.init(Cipher.DECRYPT_MODE, key);
            return in.doFinal(cipher);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    *//**
     * @param bytes
     * @return
     *//*
    public static byte[] sm3(byte[] bytes) {
        SM3Digest sm3 = new SM3Digest();
        sm3.update(bytes, 0, bytes.length);
        byte[] result = new byte[sm3.getDigestSize()];
        sm3.doFinal(result, 0);
        return result;
    }

    private final static int RS_LEN = 32;

    private static byte[] bigIntToFixexLengthBytes(BigInteger rOrS){
        // for sm2p256v1, n is 00fffffffeffffffffffffffffffffffff7203df6b21c6052b53bbf40939d54123,
        // r and s are the result of mod n, so they should be less than n and have length<=32
        byte[] rs = rOrS.toByteArray();
        if(rs.length == RS_LEN) return rs;
        else if(rs.length == RS_LEN + 1 && rs[0] == 0) return Arrays.copyOfRange(rs, 1, RS_LEN + 1);
        else if(rs.length < RS_LEN) {
            byte[] result = new byte[RS_LEN];
            Arrays.fill(result, (byte)0);
            System.arraycopy(rs, 0, result, RS_LEN - rs.length, rs.length);
            return result;
        } else {
            throw new RuntimeException("err rs: " );
        }
    }

    *//**
     * BC的SM3withSM2签名得到的结果的rs是asn1格式的，这个方法转化成直接拼接r||s
     * @param rsDer rs in asn1 format
     * @return sign result in plain byte array
     *//*
    private static byte[] rsAsn1ToPlainByteArray(byte[] rsDer){
        ASN1Sequence seq = ASN1Sequence.getInstance(rsDer);
        byte[] r = bigIntToFixexLengthBytes(ASN1Integer.getInstance(seq.getObjectAt(0)).getValue());
        byte[] s = bigIntToFixexLengthBytes(ASN1Integer.getInstance(seq.getObjectAt(1)).getValue());
        byte[] result = new byte[RS_LEN * 2];
        System.arraycopy(r, 0, result, 0, r.length);
        System.arraycopy(s, 0, result, RS_LEN, s.length);
        return result;
    }

    *//**
     * BC的SM3withSM2验签需要的rs是asn1格式的，这个方法将直接拼接r||s的字节数组转化成asn1格式
     * @param sign in plain byte array
     * @return rs result in asn1 format
     *//*
    private static byte[] rsPlainByteArrayToAsn1(byte[] sign){
        if(sign.length != RS_LEN * 2) throw new RuntimeException("err rs. ");
        BigInteger r = new BigInteger(1, Arrays.copyOfRange(sign, 0, RS_LEN));
        BigInteger s = new BigInteger(1, Arrays.copyOfRange(sign, RS_LEN, RS_LEN * 2));
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new ASN1Integer(r));
        v.add(new ASN1Integer(s));
        try {
            return new DERSequence(v).getEncoded("DER");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static KeyPair generateKeyPair(){
        try {
            KeyPairGenerator kpGen = KeyPairGenerator.getInstance("EC", "BC");
            kpGen.initialize(ecParameterSpec, new SecureRandom());
            KeyPair kp = kpGen.generateKeyPair();
            return kp;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

*//*    public static BCECPrivateKey getPrivatekeyFromD(BigInteger d){
        ECPrivateKeySpec ecPrivateKeySpec = new ECPrivateKeySpec(d, ecParameterSpec);
        return new BCECPrivateKey("EC", ecPrivateKeySpec, BouncyCastleProvider.CONFIGURATION);
    }

    public static BCECPublicKey getPublickeyFromXY(BigInteger x, BigInteger y){
        ECPublicKeySpec ecPublicKeySpec = new ECPublicKeySpec(x9ECParameters.getCurve().createPoint(x, y), ecParameterSpec);
        return new BCECPublicKey("EC", ecPublicKeySpec, BouncyCastleProvider.CONFIGURATION);
    }

    public static PublicKey getPublickeyFromX509File(File file){
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
            FileInputStream in = new FileInputStream(file);
            X509Certificate x509 = (X509Certificate) cf.generateCertificate(in);
//            System.out.println(x509.getSerialNumber());
            return x509.getPublicKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*//*

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, CertPathBuilderException, InvalidKeyException, SignatureException, CertificateException {

//        // 随便看看 ---------------------
//        System.out.println("GMNamedCurves: ");
//        for(Enumeration e = GMNamedCurves.getNames(); e.hasMoreElements();) {
//            System.out.println(e.nextElement());
//        }
//        System.out.println("sm2p256v1 n:"+x9ECParameters.getN());
//        System.out.println("sm2p256v1 nHex:"+Hex.toHexString(x9ECParameters.getN().toByteArray()));


        // 生成公私钥对 ---------------------
        KeyPair kp = generateKeyPair();

       // System.out.println(Hex.toHexString(kp.getPrivate().getEncoded()));
       // System.out.println(Hex.toHexString(kp.getPublic().getEncoded()));

        System.out.println(kp.getPrivate().getAlgorithm());
        System.out.println(kp.getPublic().getAlgorithm());

        System.out.println(kp.getPrivate().getFormat());
        System.out.println(kp.getPublic().getFormat());

        System.out.println("private key d: " + ((BCECPrivateKey)kp.getPrivate()).getD());
        System.out.println("public key q:" + ((BCECPublicKey)kp.getPublic()).getQ()); //{x, y, zs...}

        byte[] msg = "message digest".getBytes();
        byte[] userId = "userId".getBytes();
        byte[] sig = signSm3WithSm2(msg, userId, kp.getPrivate());
       // System.out.println(Hex.toHexString(sig));
        System.out.println(verifySm3WithSm2(msg, userId, sig, kp.getPublic()));


//        // 由d生成私钥 ---------------------
//        BigInteger d = new BigInteger("097b5230ef27c7df0fa768289d13ad4e8a96266f0fcb8de40d5942af4293a54a", 16);
//        BCECPrivateKey bcecPrivateKey = getPrivatekeyFromD(d);
//        System.out.println(bcecPrivateKey.getParameters());
//        System.out.println(Hex.toHexString(bcecPrivateKey.getEncoded()));
//        System.out.println(bcecPrivateKey.getAlgorithm());
//        System.out.println(bcecPrivateKey.getFormat());
//        System.out.println(bcecPrivateKey.getD());
//        System.out.println(bcecPrivateKey instanceof java.security.interfaces.ECPrivateKey);
//        System.out.println(bcecPrivateKey.getParameters());


//        公钥X坐标PublicKeyXHex: 59cf9940ea0809a97b1cbffbb3e9d96d0fe842c1335418280bfc51dd4e08a5d4
//        公钥Y坐标PublicKeyYHex: 9a7f77c578644050e09a9adc4245d1e6eba97554bc8ffd4fe15a78f37f891ff8
//        PublicKey publicKey = getPublickeyFromX509File(new File("/Users/xxx/Downloads/xxxxx.cer"));
//        System.out.println(publicKey);
//        PublicKey publicKey1 = getPublickeyFromXY(new BigInteger("59cf9940ea0809a97b1cbffbb3e9d96d0fe842c1335418280bfc51dd4e08a5d4", 16), new BigInteger("9a7f77c578644050e09a9adc4245d1e6eba97554bc8ffd4fe15a78f37f891ff8", 16));
//        System.out.println(publicKey1);
//        System.out.println(publicKey.equals(publicKey1));
//        System.out.println(publicKey.getEncoded().equals(publicKey1.getEncoded()));
//
//
//
        // sm2 encrypt and decrypt test ---------------------
        KeyPair kp2 = generateKeyPair();
        PublicKey publicKey2 = kp2.getPublic();
        PrivateKey privateKey2 = kp2.getPrivate();
        byte[]bs = sm2Encrypt("s".getBytes(), publicKey2);
      //  System.out.println(Hex.toHexString(bs));
        bs = sm2Decrypt(bs, privateKey2);
        System.out.println(new String(bs));
//
//
//
        // sm4 encrypt and decrypt test ---------------------
        //0123456789abcdeffedcba9876543210 + 0123456789abcdeffedcba9876543210 -> 681edf34d206965e86b3e94f536e4246
        byte[] plain = Hex.decode("0123456789abcdeffedcba98765432100123456789abcdeffedcba98765432100123456789abcdeffedcba9876543210");
        byte[] key = Hex.decode("0123456789abcdeffedcba9876543210");
        byte[] cipher = Hex.decode("595298c7c6fd271f0402f804c33d3f66");
        byte[] bs4 = sm4Encrypt(key, plain);
      //  System.out.println(Hex.toHexString(bs4));;
        bs = sm4Decrypt(key, bs4);
        //System.out.println(Hex.toHexString(bs4));
    }*/
}
