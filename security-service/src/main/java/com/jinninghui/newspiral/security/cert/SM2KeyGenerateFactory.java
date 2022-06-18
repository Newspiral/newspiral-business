package com.jinninghui.newspiral.security.cert;
 
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.ECGenParameterSpec;
 
import org.bouncycastle.jce.provider.BouncyCastleProvider;
 
/**
 * SM2密钥对生成器
 *
 * @author Cliven
 * @date 2018-12-21 14:05
 */
public class SM2KeyGenerateFactory {
    /**
     * 获取SM2密钥对生成器
     *
     * @return SM2密钥对生成器
     * @throws NoSuchAlgorithmException
     */
    public static KeyPairGenerator generator() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        // 获取SM2椭圆曲线的参数
        final ECGenParameterSpec sm2Spec = new ECGenParameterSpec("sm2p256v1");
        // 获取一个椭圆曲线类型的密钥对生成器
        final KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", new BouncyCastleProvider());
        // 使用SM2参数初始化生成器
        kpg.initialize(sm2Spec);
        return kpg;
 
    }

    /**
     * 获取RSA密钥对生成器
     *
     * @return SM2密钥对生成器
     * @throws NoSuchAlgorithmException
     */
    public static KeyPairGenerator generatorRSA() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        return kpg;
    }
}