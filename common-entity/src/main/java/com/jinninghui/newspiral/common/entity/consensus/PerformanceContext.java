package com.jinninghui.newspiral.common.entity.consensus;

import lombok.Data;

/**
 * @version V1.0
 * @Title: PerformanceContext
 * @Package com.jinninghui.newspiral.consensus.hotstuff
 * @Description:
 * @author: xuxm
 * @date: 2020/7/3 11:21
 */
@Data
public class PerformanceContext {
    /**
     * current height of committed block
     */
    long blockHeight;
    /**
     * 共识数量
     */
    int nodeNumber;
}
