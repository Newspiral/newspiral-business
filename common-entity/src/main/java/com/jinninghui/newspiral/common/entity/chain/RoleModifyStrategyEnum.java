package com.jinninghui.newspiral.common.entity.chain;

import com.alibaba.fastjson.JSON;

/**
 *
 *
 * 角色修改的策略枚举
 */
public enum RoleModifyStrategyEnum {

    MAJORITY_AGREE("MAJORITY_AGREE", "通道中已有过半数节点同意即可"),
    ABSOLUTE_MAJORITY_AGREE("ABSOLUTE_MAJORITY_AGREE", "通道中已有节点中2/3以上同意即可"),
    ALL_AGREE("ALL_AGREE", "通道中已有节点全部同意即可"),
    MANAGER_AGREE("MANAGER_AGREE", "通道的管理员同意即可");
    public final String code;
    public final String message;
    RoleModifyStrategyEnum(String code, String message) {
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
