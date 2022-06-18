package com.jinninghui.newspiral.gateway.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @ClassName TransAlreadyConsensusByTimeReq
 * @Author owen
 * @Date 2021-04-23 2021-04-23 15:50
 * @Version 1.0
 * @Description 根据时间维度查询已经共识的通道中交易列表
 **/
@Data
public class TransAlreadyConsensusByTimeReq extends RPCParam {

    @ApiModelProperty(value = "开始时间, eg: System.currentTimeMillis()")
    @NotNull
    private Long startTime;

    @ApiModelProperty(value = "结束时间, eg:  System.currentTimeMillis()")
    @NotNull
    private Long endTime;
}
