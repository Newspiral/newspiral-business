package com.jinninghui.newspiral.common.entity.chain;

import com.jinninghui.newspiral.common.entity.VerifiableData;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class RemovePeerFromChannelRequest implements VerifiableData {

    @ApiModelProperty(value = "hash值",hidden = true)
    @Getter @Setter
    String hash;

    @ApiModelProperty("系统交易，从通道中移除节点(removeOnePeer)")
    @Getter @Setter
    SDKTransaction rmPeerTransaction;

    @ApiModelProperty("调用者身份标识")
    SignerIdentityKey signerIdentityKey;

    @Override
    public SignerIdentityKey getSignerIdentityKey() { return this.signerIdentityKey; }

    @Override
    public void setSignerIdentityKey(SignerIdentityKey identitKey) {
        signerIdentityKey = identitKey;
    }

    @Override
    public String getHash() {
        return hash;
    }

    @Override
    public void setHash (String hash) {
        this.hash = hash;
    }

}
