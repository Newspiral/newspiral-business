package com.jinninghui.newspiral.common.entity.chain;

import com.jinninghui.newspiral.common.entity.VerifiableData;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author
 * @date
 *
 *
 */
@ToString
public class ExistMySelfFromChannelRequest implements VerifiableData {
    @Getter @Setter
    String hash;
    /**
     *
     */
    @Getter @Setter
    SDKTransaction exitTransaction;

    @Getter @Setter
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
