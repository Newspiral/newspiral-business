package com.jinninghui.newspiral.gateway.entity;

import com.jinninghui.newspiral.common.entity.VerifiableData;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class QueryHistoryQCReq implements VerifiableData {
    @Getter @Setter
    String hash;
    @Getter @Setter
    String channelId;
    @Getter @Setter
    Long fromHeight;
    @Getter @Setter
    Long toHeight;
    SignerIdentityKey signerIdentityKey;
    @Override
    public SignerIdentityKey getSignerIdentityKey() { return signerIdentityKey;}

    @Override
    public void setSignerIdentityKey(SignerIdentityKey identity) {
        signerIdentityKey = identity;
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
