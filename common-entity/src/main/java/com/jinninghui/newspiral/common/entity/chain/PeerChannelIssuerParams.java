package com.jinninghui.newspiral.common.entity.chain;

import lombok.Data;

/**
 * @ClassName PeerChannelIssuerParams
 * @Author owen
 * @Date 2021-04-20 2021-04-20 19:35
 * @Version 1.0
 * @Description 在相同通道和相同组织中的节点
 **/
@Data
public class PeerChannelIssuerParams {
    /**
     * 几点所在通道
     */
    private String channelId;
    /**
     * 节点ID
     */
    private String peerId;
    /**
     * 节点状态
     */
    private String flag;
    /**
     * 节点url
     */
    private String serviceUrls;
    /**
     * 节点所在组织ID
     */
    private String issuerId;
    /**
     * 节点所在组织公钥
     */
    private String issuerPublickey;
    /**
     * 节点所在组织私钥；此变量为空
     */
    private String issuerPrivatekey;
}
