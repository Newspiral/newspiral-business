package com.jinninghui.newspiral.gateway.entity;

import com.jinninghui.newspiral.common.entity.VerifiableData;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import lombok.Data;

@Data
public class QueryHistoryBlockReq implements VerifiableData {
    String hash;
    String channelId;
    SignerIdentityKey signerIdentityKey;
    String fromHash;
    String toHash;
    Long count;
    Long fromHeight;
    Long toHeight;
    @Override
    public SignerIdentityKey getSignerIdentityKey() {
        return signerIdentityKey;
    }

    @Override
    public void setSignerIdentityKey(SignerIdentityKey identity){
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
