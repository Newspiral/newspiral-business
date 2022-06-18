package com.jinninghui.newspiral.common.entity.chain;

import com.jinninghui.newspiral.common.entity.VerifiableData;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import lombok.Data;

import java.util.List;

/**
 * @author lida
 * @date 2019/9/11 19:27
 * 管理平台调用节点的“把本节点加入某个通道”接口的请求
 * 需使用组织根证书签名
 */

@Data
public class UpdateChannelPeerPrivateKeyRequest implements VerifiableData {
    String hash;

    /**
     * 通道
     */
    String channelId;

    /**
     *
     */
    List<Peer> channelPeerList;
    /**
     * 这个签名用于本地节点验签
     */
    SignerIdentityKey signerIdentityKey;

    public SignerIdentityKey getSignerIdentityKey() {
        return signerIdentityKey;
    }

    public void setSignerIdentityKey(SignerIdentityKey identity) {
        signerIdentityKey = identity;
    }

    public void setHash(String hash) {
        this.hash=hash;
    }

    public String getHash() {
        return hash;
    }
}
