package com.jinninghui.newspiral.common.entity.chain;

import com.jinninghui.newspiral.common.entity.VerifiableData;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lida
 * @date 2019/7/15 9:54
 * 创建一条链所需要指定的参数
 */
public class ChannelInitParams extends ChannelBasicParams implements VerifiableData {
    @Setter @Getter
    String hash;

/*    @Setter @Getter
    String publicKey;*/


    /**
     * 预留字段，除本地节点外的其他需要加入的节点
     */
    @Getter @Setter
    List<Peer> addPeerList=new ArrayList<>();

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
