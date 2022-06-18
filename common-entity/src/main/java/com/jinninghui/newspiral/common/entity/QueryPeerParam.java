package com.jinninghui.newspiral.common.entity;

import com.jinninghui.newspiral.common.entity.chain.Peer;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import lombok.Data;

import java.util.List;

/**
 * @version V1.0
 * @Title: QueryPeerParam
 * @Package com.jinninghui.newspiral.gateway.entity
 * @Description:
 * @author: xuxm
 * @date: 2019/11/12 17:06
 */
@Data
public class QueryPeerParam {

    /**
     * 通道
     */
    String channelId;
    /**
     * 节点的信息，一般就是一条
     */
    List<Peer> peerList;
    /**
     * 身份信息
     */
    SignerIdentityKey signerIdentityKey;
}
