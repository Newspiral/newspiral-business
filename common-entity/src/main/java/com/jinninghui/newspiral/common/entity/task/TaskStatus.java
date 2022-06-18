package com.jinninghui.newspiral.common.entity.task;

import com.alibaba.fastjson.JSON;

/**
 * @author lida
 * @date 2019/9/11 11:03
 */
public enum TaskStatus implements CodeEnum {
    PROCESSING("PROCESSING", "处理中"),
    WAIT_EXEXUTE("WAIT_EXEXUTE", "等待执行开始时刻点到达"),
    SUCCESS("SUCCESS", "成功，终态"),
    FAIL("FAIL", "业务失败，终态"),
    OVERTIME("OVERTIME", "超出时间，终态");
    public final String code;
    public final String message;
    TaskStatus(String code, String message) {
        this.code = code;
        this.message = message;
    }
    public String getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }

    /**
     * 是否是终态，如果是，则不需要再执行
     * @return
     */
    public boolean isFianlStatus()
    {
        return this == TaskStatus.SUCCESS || this==TaskStatus.FAIL
                ||this==TaskStatus.OVERTIME;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
