package com.jinninghui.newspiral.security.cert;


import com.jinninghui.newspiral.common.entity.cert.BaseCertConfig;
import com.jinninghui.newspiral.common.entity.cert.CertData;
import com.jinninghui.newspiral.common.entity.cert.RootCertData;
import com.jinninghui.newspiral.common.entity.cert.UserCsrAndCertData;
import com.jinninghui.newspiral.common.entity.cert.UserCsrCertData;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.misc.NetscapeCertType;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.X509KeyUsage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author
 * @date
 * 证书生成工厂
 */
public class CertGenerateFactory {

    private static final Logger logger = LoggerFactory.getLogger(CertGenerateFactory.class);

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
     * 构建CA根证书证书
     * @param issuerCertData
     * @param subjectData
     * @return
     * @throws NoSuchAlgorithmException
     * @throws OperatorCreationException
     * @throws IOException
     * @throws CertificateException
     */
    public static RootCertData createRootCert(CertData issuerCertData, CertData subjectData , BaseCertConfig baseCertConfig) throws NoSuchAlgorithmException, OperatorCreationException, IOException, CertificateException, InvalidAlgorithmParameterException {
        RootCertData rootCertData = new RootCertData();
        // 生成密钥生成器, 产生密钥对
        KeyPairGenerator keyPairGenerator = KeyGenerateFactory.generatorKeySM2();
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        rootCertData.setPrivateKey(keyPair.getPrivate());
        rootCertData.setPublicKey(keyPair.getPublic());

        // 证书签名实现类 附加了 SM3WITHSM2 和 PrivateKey
		JcaContentSignerBuilder jcaContentSB = new JcaContentSignerBuilder(CertBaseUtil.SignAlgorithm.SM3_WITH_SM2.getCode());
        jcaContentSB.setProvider(BCProvider);
        ContentSigner sigGen = jcaContentSB.build(keyPair.getPrivate());

        // 准备证书信息
        X500Name issuer = CertBaseUtil.createX500Name(issuerCertData);
        X500Name subject = CertBaseUtil.createX500Name(subjectData);
        PublicKey publickey = keyPair.getPublic();

        // 构造证书信息
        JcaX509v3CertificateBuilder jcaX509v3Cert = new JcaX509v3CertificateBuilder(
                issuer,
                baseCertConfig.getSerial(),
                baseCertConfig.getNotBefore(),
                baseCertConfig.getNotAfter(),
                subject, publickey);
        jcaX509v3Cert.addExtension(Extension.keyUsage, false,
                new X509KeyUsage(X509KeyUsage.digitalSignature | X509KeyUsage.nonRepudiation ));
        jcaX509v3Cert.addExtension(Extension.extendedKeyUsage, false, extendedKeyUsage());
        jcaX509v3Cert.addExtension(MiscObjectIdentifiers.netscapeCertType, false,
                new NetscapeCertType(NetscapeCertType.sslClient));

        // 构造X.509 第3版的证书构建者
        X509v3CertificateBuilder x509v3Cert = jcaX509v3Cert;

        // 将证书构造参数装换为X.509证书对象
        X509Certificate certificate = new JcaX509CertificateConverter().setProvider(BCProvider)
                .getCertificate(x509v3Cert.build(sigGen));
        rootCertData.setRootCert(certificate);
        return rootCertData;
    }

    /**
     * 构建CSR证书（用户请求证书）
     * @param subjectData
     * @return
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws OperatorCreationException
     * @throws IOException
     */
    public static UserCsrCertData createCsrCert(CertData subjectData, PrivateKey privateKey, PublicKey publicKey) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, OperatorCreationException, IOException {
        UserCsrCertData userCsrCertData = new UserCsrCertData();
        // 创建密钥对
        if(privateKey == null || publicKey == null){
            KeyPairGenerator keyPairGenerator = KeyGenerateFactory.generatorKeySM2();
            KeyPair pair = keyPairGenerator.generateKeyPair();
            privateKey = pair.getPrivate();
            publicKey = pair.getPublic();
        }
        userCsrCertData.setPrivateKey(privateKey);
        userCsrCertData.setPublicKey(publicKey);
        // 创建 CSR 对象
        X500Principal subject = new X500Principal(subjectData.toString());
        JcaContentSignerBuilder jcaContentSB = new JcaContentSignerBuilder("SM3withSM2");
        jcaContentSB.setProvider(BCProvider);
        PKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(subject, publicKey);
        ContentSigner signGen = jcaContentSB.build(privateKey);
        // 添加 SAN 扩展
        ExtensionsGenerator extensionsGenerator = new ExtensionsGenerator();
        GeneralNames generalNames = new GeneralNames(new GeneralName[]{new GeneralName(GeneralName.rfc822Name, "ip="+subjectData.getCommonName()), new GeneralName(GeneralName.rfc822Name, "use=EasySpiral")});
        extensionsGenerator.addExtension(Extension.subjectAlternativeName, false,generalNames);
        builder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, extensionsGenerator.generate());
        // build csr
        PKCS10CertificationRequest csr = builder.build(signGen);
        userCsrCertData.setUserCSR(csr);
        return userCsrCertData;
    }



    /**
     * 构建用户证书
     * @param crsCertByte
     * @param rootCertByte
     * @param rootPrivateKeyByte
     * @return
     * @throws Exception
     */
    public static X509Certificate createUserCert(byte[] crsCertByte , byte[] rootCertByte, byte[] rootPrivateKeyByte,BaseCertConfig baseCertConfig,CertData certData) throws IOException, OperatorCreationException, CertificateException {
        // 解析 PEM 格式的 CSR
        PKCS10CertificationRequest pkcs10CertificationRequest = null;
        ByteArrayInputStream pemStream = new ByteArrayInputStream(crsCertByte);
        Reader pemReader = new BufferedReader(new InputStreamReader(pemStream));
        PEMParser pemParser = new PEMParser(pemReader);

        Object parsedObj = pemParser.readObject();
        if (parsedObj instanceof PKCS10CertificationRequest) {
            pkcs10CertificationRequest = (PKCS10CertificationRequest) parsedObj;
        }

        // 私钥用来前面
        PrivateKey issuePriveteKey = KeyGenerateFactory.getPrivateKeySM2(rootPrivateKeyByte);
        JcaContentSignerBuilder jcaContentSB = new JcaContentSignerBuilder(CertBaseUtil.SignAlgorithm.SM3_WITH_SM2.getCode());
        jcaContentSB.setProvider(BCProvider);
        ContentSigner sigGen = jcaContentSB.build(issuePriveteKey);
        // 利用公钥创建根证书，来签发用户证书
        X509Certificate rootCert = CertBaseUtil.getCertificate(rootCertByte);
        CertData rootCertData = CertBaseUtil.toCertData(rootCert.getSubjectDN().getName());
        String title = Base64.toBase64String(rootCert.getPublicKey().getEncoded());
        if(!StringUtils.isEmpty(title)){
            rootCertData.setTitle(title);
        }
        rootCertData.setName(certData.getName());
        rootCertData.setTelephoneNumber(certData.getTelephoneNumber());
        rootCertData.setOrganizationIdentifier(certData.getOrganizationIdentifier());
        X500Name x500Name = CertBaseUtil.createX500Name(rootCertData);
        X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(
                x500Name,
                baseCertConfig.getSerial(),
                baseCertConfig.getNotBefore(),
                baseCertConfig.getNotAfter(),
                pkcs10CertificationRequest.getSubject(),
                pkcs10CertificationRequest.getSubjectPublicKeyInfo()
        );
        // 读取扩展信息
        Extensions extensions = null;
        for (Attribute attr : pkcs10CertificationRequest.getAttributes()) {
            if (PKCSObjectIdentifiers.pkcs_9_at_extensionRequest.equals(attr.getAttrType())) {
                extensions = Extensions.getInstance(attr.getAttributeValues()[0]);
                break;
            }
        }
        if (extensions != null) {
            // 添加 SAN 扩展
            certificateBuilder.addExtension(extensions.getExtension(Extension.subjectAlternativeName));
        }

        //添加crl扩展
        GeneralName[] names = new GeneralName[1];
        names[0] = new GeneralName(GeneralName.uniformResourceIdentifier, "http://www.ca.com/crl");
        GeneralNames gns = new GeneralNames(names);
        DistributionPointName pointName = new DistributionPointName(gns);
        GeneralNames crlIssuer = new GeneralNames(new GeneralName(new X500Name(rootCert.getSubjectDN().getName())));
        DistributionPoint[] points = new DistributionPoint[1];
        points[0] = new DistributionPoint(pointName, null, crlIssuer);
        certificateBuilder.addExtension(Extension.cRLDistributionPoints, false, new CRLDistPoint(points));

        //添加aia扩展
        AccessDescription[] accessDescriptions = new AccessDescription[2];
        accessDescriptions[0] = new AccessDescription(AccessDescription.id_ad_caIssuers, new GeneralName(GeneralName.uniformResourceIdentifier, "http://www.ca.com/root.crt"));
        accessDescriptions[1] = new AccessDescription(AccessDescription.id_ad_ocsp, new GeneralName(GeneralName.uniformResourceIdentifier, "http://ocsp.com/"));
        certificateBuilder.addExtension(Extension.authorityInfoAccess, false, new AuthorityInformationAccess(accessDescriptions));

        //添加其他扩展
        certificateBuilder.addExtension(Extension.keyUsage, false, new X509KeyUsage(X509KeyUsage.keyEncipherment ));
        BasicConstraints basicConstraints = new BasicConstraints(false);
        certificateBuilder.addExtension(Extension.basicConstraints, false, basicConstraints);
        //生成用户证书
        X509CertificateHolder holder = certificateBuilder.build(sigGen);
        X509Certificate cert = new JcaX509CertificateConverter()
                .setProvider(Security.getProvider( "BC")).getCertificate(holder);
        return cert;
    }

    /**
     * 生成CSR证书和用户证书
     * @param certData
     * @param rootCertByte
     * @param rootPrivateKeyByte
     * @return
     */
    public static UserCsrAndCertData createCsrAndUserCert(CertData certData, byte[] rootCertByte, byte[] rootPrivateKeyByte,BaseCertConfig baseCertConfig) {
        try{
            UserCsrCertData csrCert = createCsrCert(certData , null,null);
            byte[] csr = CertBaseUtil.convertToBase64PEMByte(csrCert.getUserCSR());
            if(csr == null){
                logger.error("生成CSR证书和用户证书异常,csr is null");
                return null;
            }
            X509Certificate userCert = createUserCert(csr, rootCertByte, rootPrivateKeyByte,baseCertConfig,certData);
            UserCsrAndCertData userCsrAndCertData = new UserCsrAndCertData();
            BeanUtils.copyProperties(csrCert,userCsrAndCertData);
            userCsrAndCertData.setUserCert(userCert);
            return userCsrAndCertData;
        }catch (Exception e){
            logger.error("生成CSR证书和用户证书异常",e);
           return null;
        }
    }

    /**
     * 更新CSR证书和用户证书
     * @param certData
     * @param rootCertByte
     * @param rootPrivateKeyByte
     * @return
     */
    public static UserCsrAndCertData updateCsrAndUserCert(CertData certData ,PrivateKey privateKey,PublicKey publicKey, byte[] rootCertByte, byte[] rootPrivateKeyByte,BaseCertConfig baseCertConfig) {
        try{
            UserCsrCertData csrCert = createCsrCert(certData , privateKey,publicKey);
            byte[] csr = CertBaseUtil.convertToBase64PEMByte(csrCert.getUserCSR());
            if(csr == null){
                logger.error("更新CSR证书和用户证书异常,csr is null");
                return null;
            }
            X509Certificate userCert = createUserCert(csr, rootCertByte, rootPrivateKeyByte,baseCertConfig,certData);
            UserCsrAndCertData userCsrAndCertData = new UserCsrAndCertData();
            BeanUtils.copyProperties(csrCert,userCsrAndCertData);
            userCsrAndCertData.setUserCert(userCert);
            return userCsrAndCertData;
        }catch (Exception e){
            logger.error("更新CSR证书和用户证书异常",e);
            return null;
        }
    }

    /**
     * 获取扩展密钥用途
     * @return 增强密钥用法ASN.1对象
     */
    public static DERSequence extendedKeyUsage() {
        // 构造容器对象
        ASN1EncodableVector vector = new ASN1EncodableVector();
        // 客户端身份认证
        vector.add(KeyPurposeId.id_kp_clientAuth);
        // 安全电子邮件
        vector.add(KeyPurposeId.id_kp_emailProtection);
        return new DERSequence(vector);
    }

    public static void main(String[] args) throws Exception {
        for (int i = 1; i < 2; i++) {
            gen(i);
        }
    }

    private static void gen(int count) throws Exception{
        byte[] rootCert = Files.readAllBytes(Paths.get("E:\\keys\\test\\南京金宁汇科技有限公司-RootCert.crt"));
        byte[] rootPrivateKey = Files.readAllBytes(Paths.get("E:\\keys\\test\\南京金宁汇科技有限公司-Root-PrivateKey.key"));
        X509Certificate certificate = CertBaseUtil.getCertificate(rootCert);
        CertData rootCertData = CertBaseUtil.toCertData(certificate.getSubjectDN().getName());
        CertData certData = new CertData();
        certData.setCountry(rootCertData.getCountry());
        certData.setState(rootCertData.getState());
        certData.setLocality(rootCertData.getLocality());
        certData.setOrganization("测试应用130-"+count);
        certData.setOrganizationIdentifier("758004163593371648");
        certData.setName("管理员"+count);
        certData.setTelephoneNumber("130000000"+count);
        BaseCertConfig baseCertConfig = new BaseCertConfig();
        baseCertConfig.setNotAfter(certificate.getNotAfter());
        baseCertConfig.setNotBefore(certificate.getNotBefore());
        UserCsrAndCertData csrAndUserCert = CertGenerateFactory.createCsrAndUserCert(certData, rootCert, rootPrivateKey, baseCertConfig);
        byte[] userCertCRT = CertBaseUtil.convertToBase64PEMByte(csrAndUserCert.getUserCert());
        byte[] userCertPEM = CertBaseUtil.convertToBase64PEMByte(csrAndUserCert.getUserCert());
        byte[] privateKeyPEM = CertBaseUtil.convertToBase64KeyByte(csrAndUserCert.getPrivateKey(),false);
        byte[] publicKeyPEM = CertBaseUtil.convertToBase64KeyByte(csrAndUserCert.getPublicKey(),true);
        SM2CertDemo.makeCertFile(userCertCRT,Paths.get("E:\\keys\\test\\test1\\"+certData.getOrganization() + "-AppCert.crt"));
        SM2CertDemo.makeCertFile(userCertPEM,Paths.get("E:\\keys\\test\\test1\\"+certData.getOrganization() + "-AppCert.pem"));
        SM2CertDemo.makeCertFile(privateKeyPEM,Paths.get("E:\\keys\\test\\test1\\"+certData.getOrganization() + "-PrivateKey.pem"));
        SM2CertDemo.makeCertFile(publicKeyPEM,Paths.get("E:\\keys\\test\\test1\\"+certData.getOrganization() + "-PublicKey.pem"));
    }

}
