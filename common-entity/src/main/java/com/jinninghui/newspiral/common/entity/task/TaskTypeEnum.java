package com.jinninghui.newspiral.common.entity.task;

import com.alibaba.fastjson.JSON;

/**
 * @author lida
 * @date 2019/9/10 20:23
 */
public enum TaskTypeEnum implements CodeEnum {
    LOCAL_PEER_ADD_TO_CHANNEL_RESULT_QUERY("LOCAL_PEER_ADD_TO_CHANNEL_RESULT_QUERY", "本地节点加入某个通道的结果查询任务"),
    REMOVE_PEER_FROM_CHANNEL_RESULT_QUERY("REMOVE_PEER_FROM_CHANNEL_RESULT_QUERY", "从通道中删除节点的结果查询任务"),
    DEFAULT_TYPE("DEFAULT_TYPE","初始化类型，默认会不处理");
    public final String code;
    public final String message;
    TaskTypeEnum(String code, String message) {
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
