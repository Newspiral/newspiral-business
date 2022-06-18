package com.jinninghui.newspiral.security.utils;

import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * @version V1.0
 * @Title: CertificateTest
 * @Package com.jinninghui.newspiral.security.utils
 * @Description:
 * @author: xuxm
 * @date: 2019/12/28 16:27
 */
@Slf4j
public class CertificateTest {
    private static final String STORE_PASS = "123456";
    private static final String ALIAS = "myCertificate";
    private static final String KEYSTORE_PATH = "D:\\Java\\jdk1.8.0_60\\bin\\myKeystore.keystore";
    private static final String CERT_PATH = "D:\\Java\\jdk1.8.0_60\\bin\\myCer.cer";

    /**
     * 国密文件
     **/
    //private static final String privateKey_PATH = "E:\\XUXM\\product\\GMCA-master\\custPrivateKey";
   // private static final String GMCERT_PATH = "E:\\XUXM\\product\\GMCA-master\\custCert.cer";

    //private static final String privateKey_PATH = "E:\\keys\\peer10cakey.pem";
    //private static final String GMCERT_PATH = "E:\\keys\\peer10servercert.pem";

    //private static final String privateKey_PATH = "E:\\keys\\peer\\peer5cakey.pem";
    //private static final String GMCERT_PATH = "E:\\keys\\peer\\peer5servercert.pem";

    //private static final String privateKey_PATH = "E:\\keys\\application\\ApplicationPrivateKey.key";
    //private static final String GMCERT_PATH = "E:\\keys\\application\\ApplicationCert.pem";

    private static final String privateKey_PATH = "E:\\keys\\group\\testapp-应用证书20201014-191653\\testapp-AppPrivateKey.key";
    private static final String GMCERT_PATH = "E:\\keys\\group\\testapp-应用证书20201014-191653\\testapp-AppCert.pem";

    private static final String GMCERT_PATH4 = "E:\\test\\NS自动化测试-RootCert.pem";

    private static final String GMCERT_PATH2 = "E:\\keys\\peer49.pem";

    private static final String GMCERT_org = "E:\\keys\\aa\\zuzhi1.pem";

    private static final String GMCERT_peer = "E:\\keys\\aa\\yingyong1.pem";


    //private static final String PLAIN_TEXT = "MANUTD is the most greatest club in the world.";
    private static final String PLAIN_TEXT = "ni hao a ";

    public static void main(String[] args) throws IOException {
        //System.out.println( CertificateTest.class.getName());

       //String aa="[{\"methodName\":\"query\",\"methodArgs\":\"com.jinninghui.newspiral.common.entity.smartcontract.SmartContractCallInstnace\",\"remark\":\"查询\",\"state\":\"1\",\"strategy\":{}}]";
        //System.out.println(System.getProperty("os.name"));
        //System.out.println(System.getProperty("os.arch"));
        //GMCertificateUtil();
        //veryFy();
/*        String key49="2fef998049929f7514a51ce66170b9040e43bfcf59fd4702ee09ecee3e76ec262e3d7179c36ad3fa2df2e3c896d99e9f422d14e41bc3817284db22b99c2511dedafae9de8ead07aabf70f15f31190b46e841c1b7a2cdb4afcdcfa61add0844cf21d02dee03b761d117eb2d4435908f65b3ce09830f10abb87187d684a5dd408a100d32b5c234dbb59dff1e25f1a097bee2a369ec92212768475c6c2b88b7887a38f028adce3ba83e598d7afb010a578c07f98a3895bc531f4930eea1fbeb7202b5429ed7b3b33a5ab5bc4bd84ad764db70fc6b01e8a65b265ef444ea2d9603191d61f9dda4e471db160b4e1de4962c406ca08e76abc332977b769417ff7ab56851ab0b884b87138ab869c58a68611da6";
        String encryptByte = Crypto.decrypt(key49);
        System.out.println(encryptByte);

        try {
            //生成证书
            X509Certificate receivedCertificate = GMCertificateUtil.getGMCertificateByCertByte(
                    readFile(GMCERT_PATH2));
            System.out.println( GMCertificateUtil.encryptBASE64(receivedCertificate.getPublicKey().getEncoded())+":"+(GMCertificateUtil.encryptBASE64(receivedCertificate.getPublicKey().getEncoded())).length());

        }
        catch (Exception e)
        {

        }*/

/*        String key7="2fef998049929f7514a51ce66170b9040e43bfcf59fd4702ee09ecee3e76ec262e3d7179c36ad3fa2df2e3c896d99e9f422d14e41bc3817284db22b99c2511de248af870fe9a16772e9cb850b4210e41d9d00fecb55cbcbcaff72ee753f65ea9c4cad720aed9f22f0876dbae8006581e9386f90f0cd924391634f675919540708d58f2c5d5be6faad222c4f517054302ac96c9c7a3cb90e86af3125b207e091713dcd0c919e437ef82326a447bae22c78a3d644b2311e0ae7a8e398d79c7b6c6a120eb04e1f214e05174f606ee32500b3fe221ee1b3e3ff1c6bef86623d85a7054a13d559c67b75c9287e0d2854f31c36ca08e76abc332977b769417ff7ab56851ab0b884b87138ab869c58a68611da6";
        String encryptByte = Crypto.decrypt(key7);
        System.out.println(encryptByte);

        try {
            //生成证书
            X509Certificate receivedCertificate = GMCertificateUtil.getGMCertificateByCertByte(
                    readFile(GMCERT_PATH3));
            System.out.println( GMCertificateUtil.encryptBASE64(receivedCertificate.getPublicKey().getEncoded())+":"+(GMCertificateUtil.encryptBASE64(receivedCertificate.getPublicKey().getEncoded())).length());

        }
        catch (Exception e)
        {

        }*/

        CertificateTest();

    }

    private static void test1()
    {
        Map<Long, String> genericMsgMapbackup=new HashMap<>();
        genericMsgMapbackup.put(1L,"123");

        Map<Long, String> genericMsgMap=new HashMap<>();
        genericMsgMap.put(2L,"123");

        Map<Long, String> temp=new HashMap<>();
        //1 123
        System.out.println(genericMsgMapbackup);
        //2 123
        System.out.println(genericMsgMap);
        //{}
        System.out.println(temp);

        temp=genericMsgMap;
        genericMsgMap=genericMsgMapbackup;
        genericMsgMapbackup=new HashMap<>();
        temp.clear();
        //{}
        System.out.println(genericMsgMapbackup);
        //1 123
        System.out.println(genericMsgMap);
        //{}
        System.out.println(temp);

    }

    private static void veryFy()
    {
        try {
            X509Certificate receivedCertificate = GMCertificateUtil.getGMCertificateByCertByte(
                    readFile(GMCERT_PATH2));
            if (CertificateUtil.verifyCertificateValidity(receivedCertificate)) {
                System.out.println("X509 有效期格式正确");
            }
            else
            {
                System.out.println("X509 有效期格式不正确");
            }
        }
        catch (Exception e)
        {

        }

    }

    private static void GMCertificateUtil() {
        try {
            //私钥生成
            java.security.PrivateKey privateKey = GMCertificateUtil.getPrivateKey(readFile(privateKey_PATH));
            //生成签名
           byte[] signature = GMCertificateUtil.signByGM(privateKey, PLAIN_TEXT.getBytes());
            //生成证书
            X509Certificate receivedCertificate = GMCertificateUtil.getGMCertificateByCertByte(
                    readFile(GMCERT_PATH));
            boolean flag = GMCertificateUtil.verifyByGM(receivedCertificate.getPublicKey(), PLAIN_TEXT.getBytes(), signature);
            //System.out.println(GMCertificateUtil.encryptBASE64((receivedCertificate.getPublicKey().getEncoded()));
            System.out.println("-----------------签名 start--------------------");
            System.out.println("text.string:" + PLAIN_TEXT);
            System.out.println("signature.string:" + Base64.toBase64String(signature));
            System.out.println("Signature is:" + flag);
            System.out.println("-----------------签名 end--------------------");
            //待加密数据
             byte[] ebs = "123sssss测试".getBytes("UTF-8");

            String enText = GMCertificateUtil.encode(ebs, receivedCertificate.getPublicKey());
            String deText = GMCertificateUtil.decode(enText, privateKey);
            System.out.println("-----------------加解密 start--------------------");
            System.out.println("text.string:123sssss测试");
            System.out.println("enText.string:" + enText);
            System.out.println("deText.string:" + deText);
            System.out.println("-----------------加解密 end--------------------");


        System.out.println("版本号 "+receivedCertificate.getVersion());
        System.out.println("序列号 "+receivedCertificate.getSerialNumber().toString(16));
        System.out.println("全名 "+receivedCertificate.getSubjectDN());
        System.out.println("签发者全名n"+receivedCertificate.getIssuerDN());
        System.out.println("有效期起始日 "+receivedCertificate.getNotBefore());
        System.out.println("有效期截至日 "+receivedCertificate.getNotAfter());
        System.out.println("签名算法 "+receivedCertificate.getSigAlgName());
        System.out.println("签发者 "+ receivedCertificate.getIssuerX500Principal().getName());
        byte[] sig=receivedCertificate.getSignature();
        System.out.println("签名n"+new BigInteger(sig).toString(16));
        PublicKey pk=receivedCertificate.getPublicKey();
        System.out.println("签发者备注公钥n"+receivedCertificate.getIssuerAlternativeNames());
        byte[ ] pkenc=pk.getEncoded();
/*        System.out.println("公钥");
        for(int i=0;i< pkenc.length;i++){
                System.out.print(pkenc[i]+",");
    }*/

            System.out.println("-----------------公钥 start--------------------");
            System.out.println( GMCertificateUtil.encryptBASE64(receivedCertificate.getPublicKey().getEncoded())+":"+(GMCertificateUtil.encryptBASE64(receivedCertificate.getPublicKey().getEncoded())).length());
            System.out.println("-----------------------------------------------");
            PublicKey publicKey2=GMCertificateUtil.getPublicKey(GMCertificateUtil.encryptBASE64(receivedCertificate.getPublicKey().getEncoded()));
            System.out.println(GMCertificateUtil.encryptBASE64(publicKey2.getEncoded()));
            System.out.println("-----------------公钥 end--------------------");
            System.out.println("-----------------私钥 start--------------------");
            System.out.println(GMCertificateUtil.encryptBASE64(privateKey.getEncoded()));
            System.out.println("-----------------------------------------------");
            PrivateKey privateKey2=GMCertificateUtil.getPrivateKey(GMCertificateUtil.encryptBASE64(privateKey.getEncoded()));
            System.out.println(GMCertificateUtil.encryptBASE64(privateKey2.getEncoded()));
            System.out.println("-----------------私钥 end--------------------");


            if (CertificateUtil.verifyCertificateValidity(receivedCertificate)) {
                System.out.println("X509 有效期格式正确");
            }
            else
            {
                System.out.println("X509 有效期格式不正确");
            }

        } catch (Exception e) {
            log.error(ModuleClassification.SM_CT_ +"TError"+"e:", e);
        }
    }

    public static void CertificateTest()
    {
        try {
            //生成证书
            X509Certificate receivedCertificate = GMCertificateUtil.getGMCertificateByCertByte(
                    readFile(GMCERT_org));
        System.out.println("版本号 "+receivedCertificate.getVersion());
        System.out.println("序列号 "+receivedCertificate.getSerialNumber().toString(16));
        System.out.println("全名 "+receivedCertificate.getSubjectDN());
        System.out.println("签发者全名n"+receivedCertificate.getIssuerDN());
        System.out.println("有效期起始日 "+receivedCertificate.getNotBefore());
        System.out.println("有效期截至日 "+receivedCertificate.getNotAfter());
        System.out.println("签名算法 "+receivedCertificate.getSigAlgName());
        System.out.println("签发者 "+ receivedCertificate.getIssuerX500Principal().getName());
        byte[] sig=receivedCertificate.getSignature();
        System.out.println("签名n"+new BigInteger(sig).toString(16));
        PublicKey pk=receivedCertificate.getPublicKey();
        System.out.println("签发者备注公钥n"+receivedCertificate.getIssuerAlternativeNames());
            System.out.println("-----------------------------");
            X509Certificate peer = GMCertificateUtil.getGMCertificateByCertByte(
                    readFile(GMCERT_peer));
            System.out.println("版本号 "+peer.getVersion());
            System.out.println("序列号 "+peer.getSerialNumber().toString(16));
            System.out.println("全名 "+peer.getSubjectDN());
            System.out.println("签发者全名n"+peer.getIssuerDN());
            System.out.println("有效期起始日 "+peer.getNotBefore());
            System.out.println("有效期截至日 "+peer.getNotAfter());
            System.out.println("签名算法 "+peer.getSigAlgName());
            System.out.println("签发者 "+ peer.getIssuerX500Principal().getName());
            byte[] sig1=peer.getSignature();
            System.out.println("签名n"+new BigInteger(sig1).toString(16));
            PublicKey pk1=peer.getPublicKey();
            System.out.println("签发者备注公钥n"+peer.getIssuerAlternativeNames());
        }
        catch (Exception e)
        {

        }
    }


    public static String getKeyFromFile(String filename) throws IOException {
        File f = new File(filename);
        FileInputStream fis = new FileInputStream(f);
        DataInputStream dis = new DataInputStream(fis);
        byte[] keyBytes = new byte[(int) f.length()];
        dis.readFully(keyBytes);
        dis.close();

        String key = new String(keyBytes);

        return key;
    }


    private static byte[] readFile(String path) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(path);
        byte[] bytes = new byte[fileInputStream.available()];
        fileInputStream.read(bytes);
        return bytes;
    }

    private static void CertificateUtilTest() throws IOException {
        /**
         * 假设现在有这样一个场景 。A机器上的数据，需要加密导出，然后将导出文件放到B机器上导入。 在这个场景中，A相当于服务器，B相当于客户端
         */
        /** A */
        KeyStore keyStore = CertificateUtil.getKeyStore(STORE_PASS, KEYSTORE_PATH);
        PrivateKey privateKey = CertificateUtil.getPrivateKey(keyStore, ALIAS, STORE_PASS);

        X509Certificate certificate = CertificateUtil.getCertificateByKeystore(keyStore, ALIAS);

        /** 加密和签名 */
        byte[] encodedText = CertificateUtil.encode(PLAIN_TEXT.getBytes(), privateKey);
        byte[] signature = CertificateUtil.sign(certificate, privateKey, PLAIN_TEXT.getBytes());

        /** 现在B收到了A的密文和签名，以及A的可信任证书 */
        X509Certificate receivedCertificate = CertificateUtil.getCertificateByCertPath(
                CERT_PATH, CertificateUtil.CERT_TYPE);

        PublicKey publicKey = CertificateUtil.getPublicKey(receivedCertificate);
        byte[] decodedText = CertificateUtil.decode(encodedText, publicKey);
        System.out.println("Decoded Text : " + new String(decodedText));
        System.out.println("Signature is : "
                + CertificateUtil.verify(receivedCertificate, decodedText, signature));
        try {
            Date data = DateUtil.convert2Date("2029-11-01 16:12:12", "yyyy-MM-dd HH:mm:ss");
            certificate.checkValidity(data);
            System.out.println("checkValidity:" + true);
        } catch (Exception e) {
            System.out.println("checkValidity:" + false);
        }
/*        System.out.println("版本号 "+receivedCertificate.getVersion());
        System.out.println("序列号 "+receivedCertificate.getSerialNumber().toString(16));
        System.out.println("全名 "+receivedCertificate.getSubjectDN());
        System.out.println("签发者全名n"+receivedCertificate.getIssuerDN());
        System.out.println("有效期起始日 "+receivedCertificate.getNotBefore());
        System.out.println("有效期截至日 "+receivedCertificate.getNotAfter());
        System.out.println("签名算法 "+receivedCertificate.getSigAlgName());
        byte[] sig=receivedCertificate.getSignature();
        System.out.println("签名n"+new BigInteger(sig).toString(16));
        PublicKey pk=receivedCertificate.getPublicKey();
        byte[ ] pkenc=pk.getEncoded();
        System.out.println("公钥");
        for(int i=0;i< pkenc.length;i++){
                System.out.print(pkenc[i]+",");
    }*/

/*        try {
            PublicKey publicKey2 = keyStore.getCertificate(ALIAS).getPublicKey();
            System.out.println("-----------------密钥库公钥--------------------");
            //System.out.println(publicKey.getAlgorithm());
            //System.out.println(privateKey.getAlgorithm());
            System.out.println(GMCertificateUtil.encryptBASE64(publicKey2.getEncoded()));
            System.out.println("-----------------密钥库公钥--------------------");
            PublicKey publicKey3=   getPublicKey(GMCertificateUtil.encryptBASE64(publicKey2.getEncoded())) ;
            System.out.println("-----------------密钥库公钥-转化测试--------------------");
            System.out.println(GMCertificateUtil.encryptBASE64((publicKey3.getEncoded()));
            System.out.println("-----------------密钥库公钥-转化测试--------------------");

            System.out.println("-----------------密钥库私钥--------------------");
            System.out.println(GMCertificateUtil.encryptBASE64(privateKey.getEncoded()));
            System.out.println("-----------------密钥库私钥--------------------");
            PrivateKey privateKey1=   getPrivateKey(GMCertificateUtil.encryptBASE64((privateKey.getEncoded())) ;
            System.out.println("-----------------密钥库私钥转化测试--------------------");
            System.out.println(GMCertificateUtil.encryptBASE64(privateKey1.getEncoded()));
            System.out.println("-----------------密钥库私钥转化测试--------------------");
        }
        catch (Exception e)
        {

        }

        System.out.println("-----------------证书公钥--------------------");
        //System.out.println(publicKey.getAlgorithm());
        //System.out.println(privateKey.getAlgorithm());
        System.out.println(GMCertificateUtil.encryptBASE64(publicKey.getEncoded()));
        System.out.println("-----------------证书公钥--------------------");

        try {
            PublicKey publicKey1=   getPublicKey(GMCertificateUtil.encryptBASE64(publicKey.getEncoded())) ;
            System.out.println("-----------------证书公钥转化测试--------------------");
            System.out.println(GMCertificateUtil.encryptBASE64(publicKey1.getEncoded()));
            System.out.println("-----------------证书公钥转化测试--------------------");

        }
        catch (Exception e)
        {

        }*/
    }
}
