package com.jinninghui.newspiral.common.entity.chain;

import com.jinninghui.newspiral.common.entity.identity.IdentityKey;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lida
 * @date 2019/7/15 14:42
 * 某个节点的与网络的连接状态
 */
@Data
public class NetworkConnectState {

    /**
     * 本节点本身的身份标识
     */
    IdentityKey localPeerId;


    /**
     * key为通道ID
     */
    Map<String,ChannelConnectState> channelConnStateMap  = new HashMap<>();


}
