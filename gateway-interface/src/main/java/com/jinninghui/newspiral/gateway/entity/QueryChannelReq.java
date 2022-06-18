package com.jinninghui.newspiral.gateway.entity;

import com.jinninghui.newspiral.common.entity.VerifiableData;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author lida
 * @date 2019/9/10 19:23
 */
@ToString
public class QueryChannelReq implements VerifiableData {
    @Getter @Setter
    String hash;
    @Getter @Setter
    String channelId;
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
