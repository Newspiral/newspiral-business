package com.jinninghui.newspiral.common.entity.chain;

import com.jinninghui.newspiral.common.entity.VerifiableData;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author lida
 * @date 2019/9/11 19:27
 * 管理平台调用节点的“把本节点加入某个通道”接口的请求
 * 需使用组织根证书签名
 */
@ToString
public class AddMySelfToChannelRequest implements VerifiableData {
    @Getter @Setter
    String hash;
    /**
     * 系统交易，节点将该交易发送给通道中的其他节点即可触发一次对本节点加入的交易执行和共识流程
     */
    @Getter @Setter
    SDKTransaction newMemberTransaction;
    /**
     * 通道中已经存在的节点的服务地址，地址要包含使用的协议，如：h2://192.168.0.45:12200
     */
    @Getter @Setter
    List<String> serviceUrlForPeerList;

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
