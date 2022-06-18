package com.jinninghui.newspiral.common.entity.record;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class InterfaceRecordBO {
    /**
     * 开始时间戳：毫秒为单位，必选
     */
    @ApiModelProperty("开始时间戳：毫秒为单位，必选")
    @NotNull
    private String startTime;

    /**
     * 结束时间戳：毫秒为单位，必选
     */
    @ApiModelProperty("结束时间戳：毫秒为单位，必选")
    @NotNull
    private String endTime;

    /**
     * 当前页序号
     */
    private Long curPage;

    /**
     * 开始序号
     */
    private Long start;

    /**
     * 每页查询数
     */

    private Integer pageSize;

    /**
     * 被调用的接口名称
     */
    private String methodName;

    /**
     * 调用合约别名
     */
    private String scAlisa;


    /**
     * 调用结果，1：Success:；0：Failed
     */
    private Integer successed;


    /**
     * 调用方身份，IdentityTypeEnum key
     */
    private String identityType;

    /**
     * 调用方身份，vulue
     */
    private String identityValue;

    /**
     * 调用所属通道ID
     */
    private String channelId;

    /**
     * 合约方法名
     */
    private String scMethodName;
}
