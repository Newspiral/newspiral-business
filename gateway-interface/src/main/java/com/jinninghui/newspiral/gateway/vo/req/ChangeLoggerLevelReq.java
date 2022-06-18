package com.jinninghui.newspiral.gateway.vo.req;

import com.jinninghui.newspiral.common.entity.task.LoggerEnum;
import lombok.Data;


@Data
public class ChangeLoggerLevelReq extends RPCParam {
    /**
     * 日志修改级别
     */
    private LoggerEnum logLevel;
}
