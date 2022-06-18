package com.jinninghui.newspiral.common.entity.smartcontract;

import com.jinninghui.newspiral.common.entity.VerifiableData;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author lida
 * @date 2019/9/25 10:25
 * 部署智能合约的请求
 */
@ToString
public class SmartContractDepolyRequest implements VerifiableData {
    @Getter @Setter
    String hash;
    /**
     * 系统交易，节点将该交易发送给通道中的其他节点即可触发一次部署智能合约交易的执行和共识流程
     */
    @Getter @Setter
    SDKTransaction newMemberTransaction;


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
