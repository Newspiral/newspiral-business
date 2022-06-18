package com.jinninghui.newspiral.common.entity.state;

import lombok.Data;

/**
 *
 *
 */
@Data
public class WorldStateModifyRecordResp {
    /**
     * oldState为空，newState不为空表示新建
     */
    WorldStateResp oldState;
    /**
     * newState为空，oldState不为空表示删除
     */
    WorldStateResp newState;

    /**
     * 该变更记录的最新更新viewNo
     * 用于清除不需要的变更记录
     * 不用blockHash是因为新建的时候，其实还不知道BlockHash
     */
    long latestUpdateViewNo;

}
