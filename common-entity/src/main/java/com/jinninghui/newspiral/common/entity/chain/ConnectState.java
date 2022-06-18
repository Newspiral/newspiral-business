package com.jinninghui.newspiral.common.entity.chain;

import lombok.Data;

/**
 * @author lida
 * @date 2019/7/15 15:59
 *
 * 连接状态
 */
@Data
public class ConnectState {

    /**
     * 连接状态
     */
    ConnectStateEnum state;

    Long lastBusinessCallTimestamp;

    Long lastHeartbeatTimestamp;

    Long sendBytes;

    Long receivedBytes;

    /**
     * 当前连接状态持续秒数
     */
    Long currentStateDuration;

}
