package com.jinninghui.newspiral.common.entity.chain;

import com.jinninghui.newspiral.common.entity.VerifiableData;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import lombok.Data;

/**
 * @author lida
 * @date 2019/7/15 10:33
 * 同意某个节点加入通道的批准信息
 * 即supporterIdendity在timestamp时刻同意newMemberIdentity加入区块通道channelId
 */
@Data
public class PeerAddChannelApproval implements VerifiableData {
    String hash;
    /**
     * 加入的通道ID
     */
    String channelId;
    /**
     * 32字符的随机数
     */
    String nonceStr;

    /**
     * 同意加入的时间戳
     */
    Long timestamp;

    /**
     * 新加入的节点
     */
    Peer newMemberPeer;

    /**
     * 同意该申请的组织的身份标识，应当使用管理员证书签名
     */
    SignerIdentityKey supporterIdendity;

    @Override
    public SignerIdentityKey getSignerIdentityKey() {
        return supporterIdendity;
    }

    @Override
    public void setSignerIdentityKey(SignerIdentityKey identity) {
        supporterIdendity = identity;
    }
    @Override
    public void setHash(String hash) {
        this.hash=hash;
    }

    @Override
    public String getHash() {
        return hash;
    }

}
