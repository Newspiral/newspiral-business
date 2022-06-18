package com.jinninghui.newspiral.security.cert;
 
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.jinninghui.newspiral.common.entity.cert.CertData;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author
 * @date
 */
public class CertBaseUtil {

	private static final Logger logger = LoggerFactory.getLogger(CertBaseUtil.class);

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

	public static final String BEGIN_CERT_PRIVATE_SM2 = "-----BEGIN PRIVATE KEY-----";
	public static final String END_CERT_PRIVATE_SM2 = "-----END PRIVATE KEY-----";
	public static final String BEGIN_CERT_PUBLIC_SM2 = "-----BEGIN PUBLIC KEY-----";
	public static final String END_CERT_PUBLIC_SM2 = "-----END PUBLIC KEY-----";

	public static final String BEGIN_CERT_PRIVATE_RSA = "-----BEGIN RSA PRIVATE KEY-----";
	public static final String END_CERT_PRIVATE_RSA = "-----END RSA PRIVATE KEY-----";
	public static final String BEGIN_CERT_PUBLIC_RSA = "-----BEGIN RSA PUBLIC KEY-----";
	public static final String END_CERT_PUBLIC_RSA = "-----END RSA PUBLIC KEY-----";

	/**
	 * 签名算法
	 */
	public enum SignAlgorithm{
		SM3_WITH_SM2("SM3withSM2","国密SM2算法"),
		SHA256_WITH_ECDSA("SHA256withECDSA","哈希算法"),
		SHA1_WITH_RSA("SHA1withRSA","RSA算法")
		;

		private String code;
		private String message;

		SignAlgorithm(String code, String message) {
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
	 * 证书信息字段
	 */
	public enum CertBCStyle{
		C("C","国家代码"),
		ST("ST","省份"),
		L("L","地区"),
		STREET("STREET","街道"),
		O("O","组织"),
		OU("OU","组织名称"),
		CN("CN","身份"),
		T("T","主题"),
		SERIALNUMBER("SERIALNUMBER","设备序列号"),
		TELEPHONE_NUMBER("TelephoneNumber","电话号码"),
		ORGANIZATION_IDENTIFIER("2.5.4.97","组织标识符"),
		NAME("Name","名字"),
		TYPE("TYPE","类型"),
		;
		public final String code;
		public final String message;

		CertBCStyle(String code, String message) {
			this.code = code;
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

		public String getCode() {
			return code;
		}
	}

	/**
	 * 证书信息字段
	 */
	private final static Supplier<Map<String, BiConsumer<CertData,String>>> BC_STYLES = () -> {
		Map<String, BiConsumer<CertData,String>> bcStyle = new HashMap<>();
		bcStyle.put(CertBCStyle.C.code,(certData, value) -> certData.setCountry(value));
		bcStyle.put(CertBCStyle.ST.code,(certData, value) -> certData.setState(value));
		bcStyle.put(CertBCStyle.L.code,(certData, value) -> certData.setLocality(value));
		bcStyle.put(CertBCStyle.O.code,(certData, value) -> certData.setOrganization(value));
		bcStyle.put(CertBCStyle.CN.code,(certData, value) -> certData.setCommonName(value));
		bcStyle.put(CertBCStyle.OU.code,(certData, value) -> certData.setOrganizationalUnit(value));
		bcStyle.put(CertBCStyle.SERIALNUMBER.code,(certData, value) -> certData.setSerialNumber(value));
		bcStyle.put(CertBCStyle.T.code,(certData, value) -> certData.setTitle(value));
		bcStyle.put(CertBCStyle.STREET.code,(certData, value) -> certData.setStreet(value));
		bcStyle.put(CertBCStyle.TELEPHONE_NUMBER.code,(certData, value) -> certData.setTelephoneNumber(value));
		bcStyle.put(CertBCStyle.NAME.code,(certData, value) -> certData.setName(value));
		bcStyle.put(CertBCStyle.ORGANIZATION_IDENTIFIER.code,(certData, value) -> certData.setOrganizationIdentifier(value));
		return bcStyle;
	};

	/**
	 * 解析证书实体对象
	 * @param der
	 * @return
	 */
	public static X509Certificate getCertificate(byte[] der){
		try (ByteArrayInputStream bIn = new ByteArrayInputStream(der)) {
			CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
			if(cf==null){
				return null;
			}
			X509Certificate cert = (X509Certificate) cf.generateCertificate(bIn);
			return cert;
		} catch (CertificateException e) {
			logger.error("解析证书实体对象异常",e);
		} catch (IOException e) {
			logger.error("解析证书实体对象异常",e);
		} catch (NoSuchProviderException e) {
			logger.error("解析证书实体对象异常",e);
		}
		return null;
	}

	/**
	 * 获取全部可信机构证书
	 * @return
	 */
	public static List<X509Certificate> getTrustedCA(){
		List<X509Certificate> trustedCA = new ArrayList<>();
		// 加载JDK的cacerts keystore文件
		String filename = System.getProperty("java.home") + "/lib/security/cacerts".replace('/', File.separatorChar);
		KeyStore keystore;
		try (FileInputStream is = new FileInputStream(filename)) {
			keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(is, "changeit".toCharArray());
			//此类用于从keystore遍历出受信的CA
			PKIXParameters params = new PKIXParameters(keystore);
			// 获取包含受信的CA证书的一组信任锚点
			Iterator it = params.getTrustAnchors().iterator();
			while( it.hasNext() ) {
				TrustAnchor ta = (TrustAnchor)it.next();
				// 获取证书
				X509Certificate cert = ta.getTrustedCert();
				trustedCA.add(cert);
				System.out.println(cert.getSubjectDN().getName());
			}
		}catch (Exception e){

		}
		return trustedCA;
	}

	/**
	 * 解析证书字段 -> {@link CertData}
	 * @param DN
	 * @return
	 */
	public static CertData toCertData(String DN){
		CertData certData = new CertData();
		String[] split = DN.split(",");
		for (String field : split) {
			String[] keyValue = field.split("=", 2);
			if(keyValue.length==2 && BC_STYLES.get().containsKey(keyValue[0])){
				BC_STYLES.get().get(keyValue[0]).accept(certData,keyValue[1]);
			}
		}
		return certData;
	}

	/**
	 * 将证书解析成CRT格式
	 * @param x509Certificate
	 * @return
	 */
	public static byte[] convertToBase64CRTByte(X509Certificate x509Certificate){
		try{
			// 获取ASN.1编码的证书字节码
			byte[] asn1BinCert = x509Certificate.getEncoded();
			// 编码为BASE64 便于传输
			byte[] base64EncodedCert = Base64.encode(asn1BinCert);
			return base64EncodedCert;
		}catch (Exception e){
			logger.error("将证书解析成CRT格式异常",e);
			return null;
		}
	}

	/**
	 * 将私钥、公钥、证书转换成PEM格式 (PEM format)字符串或字节.
	 * @param code
	 * @return
	 */
	public static String convertToBase64PEMString(Object code) {
		try{
			StringWriter sw = new StringWriter();
			try (JcaPEMWriter pw = new JcaPEMWriter(sw)) {
				pw.writeObject(code);
			}
			return sw.toString();
		}catch (Exception e){
			logger.error("将私钥、公钥、证书转换成PEM格式 (PEM format)字符串或字节.异常",e);
			return null;
		}
	}

	public static byte[] convertToBase64PEMByte(Object code) {
		try{
			String data = convertToBase64PEMString(code);
			if(StringUtils.isEmpty(data)){
				return null;
			}
			return data.getBytes();
		}catch (Exception e){
			logger.error("将私钥、公钥、证书转换成PEM格式 (PEM format)字符串或字节.异常",e);
			return null;
		}
	}

	/**
	 * 生成证书文件（.crt格式）
	 * @param x509Certificate
	 * @param savePath 保存路径
	 * @throws CertificateEncodingException
	 * @throws IOException
	 */
	public static void makeCertFileCRT(X509Certificate x509Certificate, Path savePath) {
		try{
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
		}catch (Exception e){

		}
	}

	/**
	 * 生成证书文件（.pem格式）
	 * @param byteCert
	 * @param savePath 保存路径
	 * @throws CertificateEncodingException
	 * @throws IOException
	 */
	public static void makeCertFilePEM(byte[] byteCert, Path savePath){
		try{
			if (Files.exists(savePath)) {
				// 删除已存在文件
				Files.deleteIfExists(savePath);
			}
			// 创建文件
			Files.createFile(savePath);
			// 写入文件
			Files.write(savePath, byteCert);
		}catch (Exception e){

		}
	}

	public static byte[] convertToBase64KeyByte(Key keyPair,boolean isPublicKey)  {
		String temkey = "";
		if(isPublicKey){
			temkey = BEGIN_CERT_PUBLIC_SM2 +"\n" +  Base64.toBase64String(keyPair.getEncoded()).replaceAll("(.{64})", "$1\n")+"\n" + END_CERT_PUBLIC_SM2;
		}else {
			temkey = BEGIN_CERT_PRIVATE_SM2 +"\n" +  Base64.toBase64String(keyPair.getEncoded()).replaceAll("(.{64})", "$1\n")+"\n" + END_CERT_PRIVATE_SM2;
		}
		return temkey.getBytes();
	}

	/**
	 * 构造主题信息
	 * @param certData
	 * @return
	 */
	public static X500Name createX500Name(CertData certData) {
		X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
		// 国家代码
		if(!StringUtils.isEmpty(certData.getCountry())){
			builder.addRDN(BCStyle.C, certData.getCountry());
		}
		// 组织
		if(!StringUtils.isEmpty(certData.getOrganization())){
			builder.addRDN(BCStyle.O, certData.getOrganization());
		}
		// 省份
		if(!StringUtils.isEmpty(certData.getState())){
			builder.addRDN(BCStyle.ST, certData.getState());
		}
		// 地区
		if(!StringUtils.isEmpty(certData.getLocality())){
			builder.addRDN(BCStyle.L, certData.getLocality());
		}
		// 身份
		if(!StringUtils.isEmpty(certData.getCommonName())){
			builder.addRDN(BCStyle.CN, certData.getCommonName());
		}
		// 主题
		if(!StringUtils.isEmpty(certData.getTitle())){
			builder.addRDN(BCStyle.T, certData.getTitle());
		}
		//电话号码
		if(!StringUtils.isEmpty(certData.getTelephoneNumber())){
			builder.addRDN(BCStyle.TELEPHONE_NUMBER, certData.getTelephoneNumber());
		}
		//组织ID
		if(!StringUtils.isEmpty(certData.getOrganizationIdentifier())){
			builder.addRDN(BCStyle.ORGANIZATION_IDENTIFIER, certData.getOrganizationIdentifier());
		}
		//
		if(!StringUtils.isEmpty(certData.getName())){
			builder.addRDN(BCStyle.NAME, certData.getName());
		}
		X500Name subject = builder.build();
		return subject;
	}

	/**
	 * 生成签名(自定义算法)
	 * @param privateKey
	 * @param plainText
	 * @param algorithm
	 * @return
	 */
	public static byte[] signWithAlgorithm(PrivateKey privateKey, byte[] plainText,SignAlgorithm algorithm) {
		try {
			Signature signature = null;
			if(algorithm == SignAlgorithm.SM3_WITH_SM2){
				signature = Signature.getInstance(algorithm.getCode(),"BC");
			}else{
				signature = Signature.getInstance(algorithm.getCode());
			}
			signature.initSign(privateKey);
			signature.update(plainText);
			return signature.sign();
		} catch (Exception e) {
			logger.error("生成签名异常",e);
			return null;
		}
	}

	/**
	 * 验证签名(自定义算法)
	 * @param publicKey
	 * @param decodedText
	 * @param receivedignature
	 * @param algorithm
	 * @return
	 */
	public static boolean verifyWithAlgorithm(PublicKey publicKey, byte[] decodedText, byte[] receivedignature,SignAlgorithm algorithm) {
		try {
			Signature signature = null;
			if(algorithm == SignAlgorithm.SM3_WITH_SM2){
				signature = Signature.getInstance(algorithm.getCode(),"BC");
			}else{
				signature = Signature.getInstance(algorithm.getCode());
			}
			signature.initVerify(publicKey);
			signature.update(decodedText);
			boolean verify = signature.verify(receivedignature);
			return verify;
		} catch (Exception e) {
			logger.error("验证签名异常",e);
			return false;
		}
	}

	/**
	 * 根据公钥加密
	 * @param text      需要加密的文本
	 * @param publicKey 公钥
	 * @return Base64.toBase64String 的加密字符串
	 */
	public static String encodeByPublicKey(byte[] text, PublicKey publicKey) {
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
			return org.apache.commons.codec.binary.Base64.encodeBase64String(bs);
		} catch (Exception e) {
			logger.error("根据公钥加密异常:", e);
		}
		return null;
	}

	/**
	 * 根据私钥解密
	 * @param text 需要解密的文本
	 * @param privateKey 私钥
	 * @return 明文
	 */
	public static String decodeByPrivateKey(String text, PrivateKey privateKey) {
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
			byte[] bs = Base64.decode(text.getBytes(StandardCharsets.UTF_8));
			byte[] b = sm2DcEngine.processBlock(bs, 0, bs.length);
			return new String(b);
		} catch (Exception e) {
			logger.error("根据私钥解密异常:", e);
		}
		return null;
	}

	private static boolean checkCertAndPrivateKey(X509Certificate certificate , PrivateKey privateKey){
		String decodeText = "EasySpiral";
		byte[] encodeText = CertBaseUtil.signWithAlgorithm(privateKey, decodeText.getBytes(),SignAlgorithm.SM3_WITH_SM2);
		return CertBaseUtil.verifyWithAlgorithm(certificate.getPublicKey(),decodeText.getBytes(),encodeText,SignAlgorithm.SM3_WITH_SM2);
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
 
}