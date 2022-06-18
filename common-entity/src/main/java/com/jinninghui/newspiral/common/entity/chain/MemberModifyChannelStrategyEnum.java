package com.jinninghui.newspiral.common.entity.chain;

import com.alibaba.fastjson.JSON;

/**
 *
 *
 * 成员修改的通道的策略枚举
 */
public enum MemberModifyChannelStrategyEnum {

    MAJORITY_AGREE("MAJORITY_AGREE", "通道中已有过半数节点同意即可"),
    ABSOLUTE_MAJORITY_AGREE("ABSOLUTE_MAJORITY_AGREE", "通道中已有节点中2/3以上同意即可"),
    ALL_AGREE("ALL_AGREE", "通道中已有节点全部同意即可"),
    MANAGER_AGREE("MANAGER_AGREE", "通道的管理员同意即可"),
    PARENT_AGREE("PARENT_AGREE", "父成员同意即可");
    public final String code;
    public final String message;
    MemberModifyChannelStrategyEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
    public String getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
