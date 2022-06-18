package com.jinninghui.newspiral.security;

import com.jinninghui.newspiral.common.entity.VerifiableData;
import com.jinninghui.newspiral.common.entity.Hashable;
import com.jinninghui.newspiral.common.entity.member.Member;

/**
 * @version V1.0
 * @Title: DataSecurityMgr
 * @Package com.jinninghui.newspiral.security
 * @Description:
 * @author: xuxm
 * @date: 2020/3/23 14:20
 */
public interface DataSecurityMgr {

    /**
     * 计算输入对象的Hash值并调用setHash()方法设置，要求输入对象的getHash返回为空
     * @param param
     */
    void hash(Hashable param);

    void hash(VerifiableData param);
    /**
     * calculte the hash of Verifyable object, without change it.
     * @param param
     */
    String calcHash(VerifiableData param);

    /**
     * calculate hash of the content
     * @param content
     */
    String hash(byte[] content);
    /**
     * 国密证书密钥签名
     * @param verifiableData
     */
    void  signByGMCertificateKey(VerifiableData verifiableData, String privateKeyStr);

    /**
     * 国密证书密钥验签
     * @param verifiableData
     * @return
     */
    boolean verifySignatureByGMCertificateKey(VerifiableData verifiableData, String publicKeyStr);

    /**
     *
     * @param object
     * @return
     */
    String getHash(Object object);

    /**
     * 根据证书获取公钥
     * @param certificateFile
     * @return
     */
    String getCertificatePublicKey(String certificateFile);

    /**
     * 通过证书解析成员其他信息
     * @param member
     * @return
     */
    boolean processMemberCertificate(Member member);
}
