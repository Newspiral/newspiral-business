package com.jinninghui.newspiral.common.entity.chain;

import com.alibaba.fastjson.JSON;

/**
 * @author lida
 * @date 2019/7/11 17:50
 *  某个通道的元数据（例如各种控制策略、通道最大节点数等）的修改控制策略
 */
public enum ChannelModifyStrategyEnum {

    MAJORITY_AGREE("MAJORITY_AGREE", "通道中已有过半数节点同意即可"),
    ABSOLUTE_MAJORITY_AGREE("ABSOLUTE_MAJORITY_AGREE", "通道中已有节点中2/3以上同意即可"),
    ALL_AGREE("ALL_AGREE", "通道中已有节点全部同意即可");
    public final String code;
    public final String message;
    ChannelModifyStrategyEnum(String code, String message) {
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
