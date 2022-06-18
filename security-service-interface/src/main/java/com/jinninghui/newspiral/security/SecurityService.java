package com.jinninghui.newspiral.security;

import com.jinninghui.newspiral.common.entity.Hashable;
import com.jinninghui.newspiral.common.entity.VerifiableData;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.chain.PeerCert;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;

import java.util.List;

/**
 * @author lida
 * @date 2019/7/12 19:51
 * 安全服务，提供一组匹配的安全相关接口
 * 每一个Security实例会支持一组匹配的且固定的哈希、签名、验签、加密算法
 */
public interface SecurityService {

    /**
     * 安全服务接口组的名称
     * @return
     */
    String getName();

    /**
     * 返回本地调用者身份标识，签名未设置，一般用于发送消息中的签名部分
     * @return
     */
    SignerIdentityKey getMyCallerIdentity();


    /**
     * 计算输入字节数组的Hash值，以字节数组方式返回
     * @param contentBytes
     * @return
     */
    byte[] calcHashBytes(byte[] contentBytes);

    /**
     * 计算输入对象的Hash值并调用setHash()方法设置，要求输入对象的getHash返回为空
     * @param param
     */
    void hash(Hashable param);

    /**
     * calc the hash of block
     * @param block
     */
    String hashBlock(Block block);

    /**
     * 验证Hash是否正确，与void hash(Hashable param)配套使用
     * @param param
     */
    boolean  verifyHash(Hashable param);

  /*  byte[] merkle(ArrayList<byte[]> tree);*/



    /**
     * 国密证书签名
     * @param verifiableData
     */
    void  signByGMCertificate(VerifiableData verifiableData, String channelId);

    /**
     * 国密证书验签
     * @param verifiableData
     * @return
     */
    boolean verifySignatureByGMCertificate(VerifiableData verifiableData, String channelId);

    /**
     * 只用于区块以及genericMsg的验签
     * @param verifiableData
     * @return
     */
    boolean verifySignatureWithoutHashCheck(VerifiableData verifiableData, String channelId);

    /**
     * 同步历史区块时国密证书验签
     * @param verifiableData
     * @return
     */
    boolean syncHistoryBlockVerifySignatureByGMCertificate(VerifiableData verifiableData, List<PeerCert> peerCert);


    /**
     * 清除证书
     * @param peerId
     */
    void clearPeerCertificateCipherMap(String peerId,String channelId);

    boolean verifySignature(String hash, SignerIdentityKey signerIdentityKey);

    boolean verifySignatureByPublicKey(String hash, SignerIdentityKey signerIdentityKey);
}
