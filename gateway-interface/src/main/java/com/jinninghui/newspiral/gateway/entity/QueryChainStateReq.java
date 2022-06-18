package com.jinninghui.newspiral.gateway.entity;

import com.jinninghui.newspiral.common.entity.VerifiableData;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class QueryChainStateReq implements VerifiableData {
    @Getter @Setter
    String hash;
    @Getter @Setter
    String channelId;
    @Getter @Setter
    Long blockHeight;

    SignerIdentityKey signerIdentityKey;
    public SignerIdentityKey getSignerIdentityKey() {
        return signerIdentityKey;
    }
    public void setSignerIdentityKey(SignerIdentityKey identity){
        signerIdentityKey = identity;
    }
    public void setHash(String hash) {
        this.hash=hash;
    }

    public String getHash() {
        return hash;
    }
}
