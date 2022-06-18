package com.jinninghui.newspiral.common.entity.chain;

import com.jinninghui.newspiral.common.entity.identity.IdentityKey;
import lombok.Data;

import java.util.Map;

/**
 * @author lida
 * @date 2019/7/15 14:48
 * 某个节点在某个通道内的连接状态
 */
@Data
public class ChannelConnectState {
    /**
     * 本节点本身的身份标识
     */
    IdentityKey localPeerId;

    /**
     * key为某个对端节点的身份标识
     */
    Map<IdentityKey,ConnectState> connectStateMap;
}
