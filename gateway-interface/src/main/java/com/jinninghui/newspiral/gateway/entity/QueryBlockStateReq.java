package com.jinninghui.newspiral.gateway.entity;

import com.jinninghui.newspiral.common.entity.VerifiableData;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class QueryBlockStateReq implements VerifiableData {
    @Getter @Setter
    String hash;
    @Getter @Setter
    String channelId;
    @Getter @Setter
    String blockHash;

    SignerIdentityKey callerIdentityKey;
    public SignerIdentityKey getSignerIdentityKey() {
        return callerIdentityKey;
    }
    public void setSignerIdentityKey(SignerIdentityKey identity){
        callerIdentityKey = identity;
    }
    public void setHash(String hash) {
        this.hash=hash;
    }

    public String getHash() {
        return hash;
    }
}
