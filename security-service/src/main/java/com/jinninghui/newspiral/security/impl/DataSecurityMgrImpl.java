package com.jinninghui.newspiral.security.impl;

import com.alibaba.fastjson.JSONObject;
import com.jinninghui.newspiral.common.entity.Hashable;
import com.jinninghui.newspiral.common.entity.VerifiableData;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.common.logmodule.LogModuleCodes;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import com.jinninghui.newspiral.common.entity.member.Member;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.security.DataSecurityMgr;
import com.jinninghui.newspiral.common.entity.util.OsccaCinpher;
import com.jinninghui.newspiral.security.utils.CertificateUtil;
import com.jinninghui.newspiral.security.utils.GMCertificateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import java.security.PublicKey;
import java.security.cert.X509Certificate;

/**
 * @version V1.0
 * @Title: DataSecurityMgrImpl
 * @Package com.jinninghui.newspiral.security.impl
 * @Description:
 * @author: xuxm
 * @date: 2020/3/23 14:20
 */
@Slf4j
public class DataSecurityMgrImpl implements DataSecurityMgr {

    @Override
    public void hash(Hashable param) {
        param.setHash(null);
        byte[] bytes = JSONObject.toJSON(param).toString().getBytes();
        byte[] hashBytes = "".getBytes();
        //synchronized(this) {
            hashBytes= OsccaCinpher.calHashBySM3(bytes);
        //}
        param.setHash(Hex.encodeHexString(hashBytes, false));
    }

    @Override
    public void hash(VerifiableData param) {
        param.setHash(calcHash(param));
    }

    public String calcHash(VerifiableData param) {
        String hash = param.getHash();
        param.setHash(null);
        SignerIdentityKey signerIdentityKey = param.getSignerIdentityKey();
        param.setSignerIdentityKey(null);
        byte[] bytes = JSONObject.toJSON(param).toString().getBytes();
        byte[] hashBytes = "".getBytes();
        //synchronized(this) {
            hashBytes= OsccaCinpher.calHashBySM3(bytes);
        //}
        param.setHash(hash);
        param.setSignerIdentityKey(signerIdentityKey);
        return Hex.encodeHexString(hashBytes, false);
    }

    @Override
    public String hash(byte[] content) {
        byte[] hashBytes;
        //synchronized (this) {
            hashBytes = OsccaCinpher.calHashBySM3(content);
        //}
        return Hex.encodeHexString(hashBytes, false);
    }


    @Override
    public void signByGMCertificateKey(VerifiableData verifiableData, String privateKeyStr) {
        //log.info(ModuleClassification.SM_DSMI+"DataSecurityMgrImpl.signByGMCertificateKey.start,verifiableData:{} privateKeyStr:{}", verifiableData,privateKeyStr);
        //SignerIdentityKey sender = this.getMyCallerIdentity();
        SignerIdentityKey sender = verifiableData.getSignerIdentityKey();
        if (StringUtils.isEmpty(privateKeyStr)) {
            log.error(ModuleClassification.SM_DSMI_ +"DataSecurityMgrImpl.signByGMCertificateKey：privateKeyStr is empty!");
            return;
        }
        try {
            //私钥生成
            java.security.PrivateKey privateKey = GMCertificateUtil.getPrivateKey(privateKeyStr);
            //生成签名
            byte[] signature = GMCertificateUtil.signByGM(privateKey, Block.hexStringToByte(verifiableData.getHash()));
            sender.setSignature(Hex.encodeHexString(signature, false));
            //log.info(ModuleClassification.SM_DSMI+"DataSecurityMgrImpl.signByGMCertificateKey.textHash:{},signature:{}", verifiableData.getHash().toString(), sender.getSign());
            verifiableData.setSignerIdentityKey(sender);
        } catch (Exception e) {
            log.error(ModuleClassification.SM_DSMI_ +"TError"+"DataSecurityMgrImpl.signByGMCertificateKey：Certificate produce sign is error:", e);
        }
        //log.info(ModuleClassification.SM_DSMI+"DataSecurityMgrImpl.signByGMCertificateKey.end:outpara {}", verifiableData);
    }

    @Override
    public boolean verifySignatureByGMCertificateKey(VerifiableData verifiableData, String publicKeyStr) {
        //log.info(ModuleClassification.SM_DSMI+"MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",DataSecurityMgrImpl.verifySignatureByGMCertificateKey.start,verifiableData:{} publicKeyStr:{}", verifiableData,publicKeyStr);
        //节点身份判空
        if (null == verifiableData.getSignerIdentityKey()) {
            log.error(ModuleClassification.SM_DSMI_ +"DataSecurityMgrImpl.verifySignatureByGMCertificateKey：SignerIdentityKey is empty! ");
            return false;
        }
        //重新hash一遍用于验签使用
        hash(verifiableData);
        //公钥判空
        if (StringUtils.isEmpty(publicKeyStr)) {
            log.error(ModuleClassification.SM_DSMI_ +"DataSecurityMgrImpl.verifySignatureByGMCertificateKey：publicKeyStr is empty! ");
            return false;
        }
        try {
            //生成公钥
            PublicKey publicKey=GMCertificateUtil.getPublicKey(publicKeyStr);
            //log.info(ModuleClassification.SM_DSMI+"DataSecurityMgrImpl.verifySignatureByGMCertificateKey.textHash:{},signature{}", verifiableData.getHash().toString(), verifiableData.getSignerIdentityKey().getSign());
            return GMCertificateUtil.verifyByGM(publicKey, Block.hexStringToByte(verifiableData.getHash()), Hex.decodeHex(verifiableData.getSignerIdentityKey().getSignature()));
        } catch (Exception e) {
            log.error(ModuleClassification.SM_DSMI_ +"TError"+"MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",DataSecurityMgrImpl.verifySignatureByGMCertificateKey：CertificateVerifySignature is error:", e);
            return false;
        }
    }

    @Override
    public  String getHash(Object object)
    {
        byte[] bytes = JSONObject.toJSON(object).toString().getBytes();
        String str="";
        /*synchronized(this) {
            str=bytesToHexString(OsccaCinpher.calHashBySM3(bytes));
        }*/
        str=bytesToHexString(OsccaCinpher.calHashBySM3(bytes));
        return str;
    }

    public static String bytesToHexString(byte[] src){
        return Hex.encodeHexString(src,false);
    }

    public byte[]  getHash(Hashable param) {

        // 序列化对象
        param.setHash(null);
        //byte[] bytes = serializer.serialize(param, param.getClass());
        // byte[] bytes = KryoUtil.writeToByteArray(param);
        byte[] bytes = JSONObject.toJSON(param).toString().getBytes();
        //log.info("DataSecurityMgrImpl.bytes=" +bytes.toString());
        // 计算hash值
        byte[] bytes1="".getBytes();
        /*synchronized(this) {
            bytes1= OsccaCinpher.calHashBySM3(bytes);
        }*/
        bytes1= OsccaCinpher.calHashBySM3(bytes);
       return bytes1;
    }

     public boolean verifyHash(Hashable param) {
        log.info(ModuleClassification.SM_DSMI_ +"DataSecurityMgrImpl.verifyHash:");
        String bytesHash = param.getHash();
        log.info(ModuleClassification.SM_DSMI_ +"DataSecurityMgrImpl.verifyHash,bytesHash1={}" , bytesHash);
        param.setHash(null);
        //byte[] bytes = KryoUtil.writeToByteArray(param);
        byte[] bytes = JSONObject.toJSON(param).toString().getBytes();
        //log.info(ModuleClassification.SM_DSMI+"DataSecurityMgrImpl.verifyHash,bytes={}" ,bytes);
         String verifyBytesHash;
         //synchronized(this) {
              verifyBytesHash = Block.bytesToHexString(OsccaCinpher.calHashBySM3(bytes));
         //}

        log.info(ModuleClassification.SM_DSMI_ +"DataSecurityMgrImpl.verifyHash,bytesHash2={}" , bytesHash);
        log.info(ModuleClassification.SM_DSMI_ +"DataSecurityMgrImpl.verifyHash,verifyBytesHash={}" , verifyBytesHash);
        if (bytesHash.equals(verifyBytesHash)) {
            return true;
        }
        return false;
    }

    @Override
    public String getCertificatePublicKey(String certificateFile)
    {
        String publicKey="";
        try {
            //生成证书
            X509Certificate receivedCertificate = GMCertificateUtil.getGMCertificateByCertByte(
                    certificateFile.getBytes());
            publicKey=GMCertificateUtil.encryptBASE64(receivedCertificate.getPublicKey().getEncoded());
        }
        catch (Exception e)
        {
            log.error(ModuleClassification.SM_DSMI_ +"TError"+"MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",DataSecurityMgrImpl.getCertificatePublicKey：certificateFile is error:", e);
        }

        return publicKey;
    }
    @Override
    public  boolean processMemberCertificate(Member member)
    {
        try {
            X509Certificate receivedCertificate = GMCertificateUtil.getGMCertificateByCertByte(
                    member.getCertificateCerFile().getBytes());
            //判断证书是否有效
            if (!CertificateUtil.verifyCertificateValidity(receivedCertificate)) {
                log.error(ModuleClassification.SM_DSMI_ +"TError"+"_processMemberCertificate：member certificate is invalid");
                return false;
            }
            member.setPublicKey(GMCertificateUtil.encryptBASE64(receivedCertificate.getPublicKey().getEncoded()));
            member.setName(receivedCertificate.getSubjectDN().getName());
            member.setIssuerId(receivedCertificate.getIssuerDN().getName());
            member.setSignAlgorithm(receivedCertificate.getSigAlgName());
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }

}
