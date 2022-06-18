package com.jinninghui.newspiral.gateway.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class StateHistoryReq extends RPCParam{
    /**
     * 通道id
     */
    @ApiModelProperty("通道id")
    @NotNull
    private String channelId;

    /**
     * 世界状态Key，必选
     */
    @ApiModelProperty("世界状态Key，必选")
    @NotNull
    private String stateKey;

    /**
     * 分页序号：整数，1为最小值；必选
     */
    @ApiModelProperty("分页序号：整数，1为最小值；必选")
    @Min(1)
    @NotNull
    private Long curPage;

    /**
     * 一页包含记录最大数(可空，默认50，最大1000)
     */
    @ApiModelProperty("一页包含记录最大数(可空，默认10，最大1000)")
    @Min(1)
    @Max(1000)
    private int pageSize = 10;

    /**
     * 开始时间戳
     */
    @ApiModelProperty("开始时间戳，单位毫秒,为空表示从查询从该通道的创建时刻开始的所有交易历史")
    private Long startTime;

    /**
     * 结束时间戳
     */
    @ApiModelProperty("结束时间戳，单位毫秒,为空取当前时间")
    private Long endTime;

}
