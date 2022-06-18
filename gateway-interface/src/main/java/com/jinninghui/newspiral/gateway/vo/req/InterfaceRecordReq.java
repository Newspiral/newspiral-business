package com.jinninghui.newspiral.gateway.vo.req;

import com.jinninghui.newspiral.common.entity.identity.IdentityTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 *
 */
@Data
public class InterfaceRecordReq extends RPCParam{


    /**
     * 开始时间戳：毫秒为单位，必选
     */
    @ApiModelProperty("开始时间戳：毫秒为单位，必选")
    @NotNull
    private Long startTime;

    /**
     * 结束时间戳：毫秒为单位，必选
     */
    @ApiModelProperty("结束时间戳：毫秒为单位，必选")
    @NotNull
    private Long endTime;

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
    @ApiModelProperty("一页包含记录最大数(可空，默认50，最大1000)")
    @Min(1)
    @Max(1000)
    private int pageSize = 50;

    /**
     * 被调用的接口名称
     */
    @ApiModelProperty("被调用的接口名称,可选")
    private String methodName;

    /**
     * 调用合约别名
     */
    @ApiModelProperty("调用合约别名")
    private String scAlisa;


    /**
     * 调用结果，1：Success:；0：Failed
     */
    @ApiModelProperty("调用结果，1：Success:；0：Failed")
    private Integer successed;


    /**
     * 调用方身份，IdentityTypeEnum key
     */
    @ApiModelProperty("身份标识类型")
    private IdentityTypeEnum identityType;

    /**
     * 调用方身份，vulue
     */
    @ApiModelProperty("身份标识类型值")
    private String identityValue;

    /**
     * 调用所属通道ID
     */
    @ApiModelProperty("调用所属通道ID")
    private String channelId;

    /**
     * 合约方法名
     */
    @ApiModelProperty("合约方法名")
    private String scMethodName;
}
