package com.jinninghui.newspiral.security.cert;
 
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import com.jinninghui.newspiral.common.entity.cert.BaseCertConfig;
import com.jinninghui.newspiral.common.entity.cert.CertData;
import com.jinninghui.newspiral.common.entity.cert.RootCertData;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.misc.NetscapeCertType;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.X509KeyUsage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.encoders.Base64;

import javax.security.auth.x500.X500Principal;

/**
 * SM2 X.509签名制作
 */
public class SM2CertDemo {
 
	/**
	 * BouncyCastle算法提供者
	 */
	private static final Provider BC = new BouncyCastleProvider();

	public static final String BEGIN_CERT_PRIVATE_SM2 = "-----BEGIN PRIVATE KEY-----";
	public static final String END_CERT_PRIVATE_SM2 = "-----END PRIVATE KEY-----";
	public static final String BEGIN_CERT_PUBLIC_SM2 = "-----BEGIN PUBLIC KEY-----";
	public static final String END_CERT_PUBLIC_SM2 = "-----END PUBLIC KEY-----";

	public static final String BEGIN_CERT_PRIVATE_RSA = "-----BEGIN RSA PRIVATE KEY-----";
	public static final String END_CERT_PRIVATE_RSA = "-----END RSA PRIVATE KEY-----";
	public static final String BEGIN_CERT_PUBLIC_RSA = "-----BEGIN RSA PUBLIC KEY-----";
	public static final String END_CERT_PUBLIC_RSA = "-----END RSA PUBLIC KEY-----";

	public static final String MD5WITHRSA = "MD5withRSA";
	public static final String SHA256withRSA = "SHA256withRSA";
	public static final String SM3WITHSM2 = "SM3withSM2";
 
	/**
	 * 获取扩展密钥用途
	 *
	 * @return 增强密钥用法ASN.1对象
	 * @author Cliven
	 * @date 2018-12-21 16:04:58
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
 
	/**
	 * 生成证书文件
	 *
	 * @param x509Certificate
	 *            X.509格式证书
	 * @param savePath
	 *            证书保存路径
	 * @throws CertificateEncodingException
	 * @throws IOException
	 */
	public static void makeCertFile(X509Certificate x509Certificate, Path savePath)
			throws CertificateEncodingException, IOException {
		if (Files.exists(savePath)) {
			// 删除已存在文件
			Files.deleteIfExists(savePath);
		}
		// 创建文件
		Files.createFile(savePath);
 
		// 获取ASN.1编码的证书字节码
		byte[] asn1BinCert = x509Certificate.getEncoded();
		// 编码为BASE64 便于传输
		byte[] base64EncodedCert = Base64.encode(asn1BinCert);
		// 写入文件
		Files.write(savePath, base64EncodedCert);
	}

	public static void makeCertFile(byte[] byteCert, Path savePath)
			throws CertificateEncodingException, IOException {
		if (Files.exists(savePath)) {
			// 删除已存在文件
			Files.deleteIfExists(savePath);
		}
		// 创建文件
		Files.createFile(savePath);

		// 写入文件
		Files.write(savePath, byteCert);
	}
 
	public static void main(String[] args) throws Exception {
//		createCert();
//		createCSR();
		CertData certData = new CertData(null, null, null, "测试", null);
		RootCertData rootCert = CertGenerateFactory.createRootCert(certData, certData, new BaseCertConfig());
		byte[] root = CertBaseUtil.convertToBase64PEMByte(rootCert.getRootCert());
		CertBaseUtil.makeCertFilePEM(root,Paths.get("caKey.pem"));
		byte[] encoded = CertBaseUtil.convertToBase64KeyByte(rootCert.getPrivateKey(),false);
		CertBaseUtil.makeCertFilePEM(encoded,Paths.get("privateKey1.pem"));
		String privateKey = BEGIN_CERT_PRIVATE_SM2 +"\n" +  Base64.toBase64String(rootCert.getPrivateKey().getEncoded()).replaceAll("(.{64})", "$1\n")+"\n" + END_CERT_PRIVATE_SM2;
		makeCertFile(privateKey.getBytes(),Paths.get("privateKey2.pem"));
//		CertGenerateFactory.createCert();
//		CertGenerateFactory.createUserCert(readFile("csr.pem"),readFile("caKey.pem"),readFile("privateKey.pem"),new BaseCertConfig());
	}

	/**
	 * 生成公私钥文件
	 */
	public static void keyPairtoFile(KeyPair keyPair,String privateFile , String publicFile , String type) throws IOException, CertificateEncodingException {
		if("SM2".equals(type)){
			String privateKey = BEGIN_CERT_PRIVATE_SM2 +"\n" +  Base64.toBase64String(keyPair.getPrivate().getEncoded()).replaceAll("(.{64})", "$1\n")+"\n" + END_CERT_PRIVATE_SM2;
			makeCertFile(privateKey.getBytes(),Paths.get(privateFile));
			String publicKey = BEGIN_CERT_PUBLIC_SM2 +"\n" +  Base64.toBase64String(keyPair.getPublic().getEncoded()).replaceAll("(.{64})", "$1\n")+"\n" + END_CERT_PUBLIC_SM2;
			makeCertFile(publicKey.getBytes(),Paths.get(publicFile));
		}else if("RSA".equals(type)){
			String privateKey = BEGIN_CERT_PRIVATE_RSA +"\n" +  Base64.toBase64String(keyPair.getPrivate().getEncoded()).replaceAll("(.{64})", "$1\n")+"\n" + END_CERT_PRIVATE_RSA;
			makeCertFile(privateKey.getBytes(),Paths.get(privateFile));
			String publicKey = BEGIN_CERT_PUBLIC_RSA +"\n" +  Base64.toBase64String(keyPair.getPublic().getEncoded()).replaceAll("(.{64})", "$1\n")+"\n" + END_CERT_PUBLIC_RSA;
			makeCertFile(publicKey.getBytes(),Paths.get(publicFile));
		}
	}

	/**
	 * 创建CSR（用户请求证书）
	 */
	public static void createCSR() throws Exception {
		// 创建密钥对
		KeyPairGenerator keyPairGenerator = SM2KeyGenerateFactory.generator();
		KeyPair pair = keyPairGenerator.generateKeyPair();
		PrivateKey privateKey = pair.getPrivate();
		PublicKey publicKey = pair.getPublic();
		keyPairtoFile(pair,"privateKeyCSR.pem","publicKeyCSR.pem","SM2");

		// 创建 CSR 对象
		X500Principal subject = new X500Principal("C=CN, ST=STName, L=LName, O=OName, OU=OUName, CN=南海分节点, EMAILADDRESS=Name@gmail.com");
		JcaContentSignerBuilder jcaContentSB = new JcaContentSignerBuilder("SM3withSM2");
		jcaContentSB.setProvider(BC);
		PKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(subject, publicKey);
		ContentSigner signGen = jcaContentSB.build(privateKey);
		// 添加 SAN 扩展
		ExtensionsGenerator extensionsGenerator = new ExtensionsGenerator();
		GeneralNames generalNames = new GeneralNames(new GeneralName[]{new GeneralName(GeneralName.rfc822Name, "ip=6.6.6.6"), new GeneralName(GeneralName.rfc822Name, "email=666@gmail.com")});
		extensionsGenerator.addExtension(Extension.subjectAlternativeName, false,generalNames);
		builder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, extensionsGenerator.generate());
		// build csr
		PKCS10CertificationRequest csr = builder.build(signGen);
		// 输出 PEM 格式的 CSR;
		StringWriter sw = new StringWriter();
		try (JcaPEMWriter pw = new JcaPEMWriter(sw)) {
			pw.writeObject(csr);
		}
		makeCertFile(sw.toString().getBytes(),Paths.get("csr.pem"));
		System.out.println("生成CSR请求证书成功!");
		createUserCert(sw.toString().getBytes());
	}


	public static void createUserCert(byte[] pem)throws Exception{
		Security.addProvider(BC);
		// 解析 PEM 格式的 CSR
		PKCS10CertificationRequest pkcs10CertificationRequest = null;
		ByteArrayInputStream pemStream = new ByteArrayInputStream(pem);
		Reader pemReader = new BufferedReader(new InputStreamReader(pemStream));
		PEMParser pemParser = new PEMParser(pemReader);

		Object parsedObj = pemParser.readObject();
		if (parsedObj instanceof PKCS10CertificationRequest) {
			pkcs10CertificationRequest = (PKCS10CertificationRequest) parsedObj;
		}



		// 私钥用来前面
		PrivateKey issuePriveteKey = KeyGenerateFactory.getPrivateKeySM2(readFile("privateKey.pem"));
		JcaContentSignerBuilder jcaContentSB = new JcaContentSignerBuilder(SM3WITHSM2);
		jcaContentSB.setProvider(BC);
		ContentSigner sigGen = jcaContentSB.build(issuePriveteKey);
		// 利用公钥创建根证书，来签发用户证书
		X509Certificate rootCert = CertBaseUtil.getCertificate(readFile("caKey.pem"));
//		X509Certificate rootCert = GMCertificateUtil.getGMCertificateByCertByte(readFile("caKey.cer"));
		X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(
				new X500Name(rootCert.getSubjectDN().getName()),
				BigInteger.valueOf(666666666L),
				new Date(),
				new Date(System.currentTimeMillis() + 1000 * 86400 * 365L),
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
		String userKey = convertToBase64PEMString(cert);
		makeCertFile(userKey.getBytes(),Paths.get("userKey.pem"));
		// 保存为证书文件
		makeCertFile(cert, Paths.get("userKey.cer"));
		System.out.println("生成用户证书成功");
	}


	/**
	 * 构建CA根证书证书
	 */
	public static void createCert()throws InvalidAlgorithmParameterException, NoSuchAlgorithmException,OperatorCreationException, IOException, CertificateException {
		// 生成密钥生成器, 产生密钥对
		KeyPairGenerator keyPairGenerator = SM2KeyGenerateFactory.generator();
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		keyPairtoFile(keyPair,"privateKey.pem","publicKey.pem","SM2");

		// 证书签名实现类 附加了 SM3WITHSM2 和 PrivateKey
		JcaContentSignerBuilder jcaContentSB = new JcaContentSignerBuilder(SM3WITHSM2);
		jcaContentSB.setProvider(BC);
		ContentSigner sigGen = jcaContentSB.build(keyPair.getPrivate());

		// 准备证书信息
		BigInteger sn = BigInteger.valueOf(System.currentTimeMillis());
		X500Name issuer = createX500Name("CA可信机构");
		X500Name subject = createX500Name("佛山警察局");
		Date notBefore = new Date();
		Date notAfter = new Date(System.currentTimeMillis() + 60 * 60 * 1000);
		PublicKey publickey = keyPair.getPublic();

		// 构造证书信息
		JcaX509v3CertificateBuilder jcaX509v3Cert = new JcaX509v3CertificateBuilder(issuer, sn, notBefore, notAfter,
				subject, publickey);
		jcaX509v3Cert.addExtension(Extension.keyUsage, false,
				new X509KeyUsage(X509KeyUsage.digitalSignature | X509KeyUsage.nonRepudiation));
		jcaX509v3Cert.addExtension(Extension.extendedKeyUsage, false, extendedKeyUsage());
	jcaX509v3Cert.addExtension(MiscObjectIdentifiers.netscapeCertType, false,
			new NetscapeCertType(NetscapeCertType.sslClient));

		// 构造X.509 第3版的证书构建者
		X509v3CertificateBuilder x509v3Cert = jcaX509v3Cert;

		// 将证书构造参数装换为X.509证书对象
		X509Certificate certificate = new JcaX509CertificateConverter().setProvider(BC)
				.getCertificate(x509v3Cert.build(sigGen));

		String caKey = convertToBase64PEMString(certificate);
		makeCertFile(caKey.getBytes(),Paths.get("caKey.pem"));
		// 保存为证书文件
		makeCertFile(certificate, Paths.get("caKey.cer"));
		System.out.println("生成公钥、私钥、根证书证书成功!");

	}

	public static byte[] readFile(String filePath) throws IOException {

		InputStream in = new FileInputStream(filePath);
		byte[] data = toByteArray(in);
		in.close();

		return data;
	}
	private static byte[] toByteArray(InputStream in) throws IOException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024 * 4];
		int n = 0;
		while ((n = in.read(buffer)) != -1) {
			out.write(buffer, 0, n);
		}
		return out.toByteArray();
	}




	// 构造 主题名称
	public static X500Name createX500Name(String cn) {
		X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
		// 国家代码
		builder.addRDN(BCStyle.C, "CN");
		// 组织
		builder.addRDN(BCStyle.O, "info");
		// 省份
		builder.addRDN(BCStyle.ST, "beijing");
		// 地区
		builder.addRDN(BCStyle.L, "beijing");
		// 身份
		builder.addRDN(BCStyle.CN, cn);

		X500Name subject = builder.build();
 
		return subject;
	}


	/**
	 * 将CRT {@link X509Certificate} 转换成PEM格式 (PEM format).
	 *
	 * @param x509Cert A X509 Certificate instance
	 * @return PEM formatted String
	 * @throws CertificateEncodingException
	 */
	public static String convertToBase64PEMString(X509Certificate x509Cert) throws IOException {
		StringWriter sw = new StringWriter();
		try (JcaPEMWriter pw = new JcaPEMWriter(sw)) {
			pw.writeObject(x509Cert);
		}
		return sw.toString();
	}
 
}