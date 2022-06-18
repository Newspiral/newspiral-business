package com.jinninghui.newspiral.common.entity.record;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author whj
 * @data 2020/11/22
 * 接口调用记录汇总实体
 */
@Data
public class InterfaceRecordSummary {

    /**
     * 自增主键
     */
    @NotNull
    @ApiModelProperty("自增主键")
    private Long id;

    /**
     * 记录的hash值，用来标识每条记录的唯一性，唯一索引
     */
    @NotNull
    @ApiModelProperty("自增主键")
    private String recordHash;

    /**
     * 被调用的接口名称
     */
    @NotNull
    @ApiModelProperty("被调用的接口名称")
    private String methodName;

    /**
     * 被调用的接口协议类型
     */
    @NotNull
    @ApiModelProperty("被调用的接口协议类型")
    private String protocolName;

    /**
     * 调用所属通道ID
     */
    @ApiModelProperty("调用所属通道ID")
    private String channelId;

    /**
     * 调用结果，1：Success:；0：Failed
     */
    @ApiModelProperty("调用结果，1：Success:；0：Failed")
    private Integer successed;

    /**
     * 调用错误信息
     */
    @ApiModelProperty("调用错误信息")
    private String errorMsg;

    /**
     * 调用合约别名
     */
    @ApiModelProperty("调用合约别名")
    private String scAlisa;

    /**
     * 合约方法名
     */
    @ApiModelProperty("合约方法名")
    private String scMethodName;

    /**
     * 调用合约版本号
     */
    @ApiModelProperty("调用合约版本号")
    private String scVersion;

    /**
     * 调用总次数
     */
    @NotNull
    @ApiModelProperty("调用总次数")
    private Long totalCalls;

    /**
     * 调用总耗时，毫秒
     */
    @NotNull
    @ApiModelProperty("调用总耗时，毫秒")
    private Long totalCallTime;

}
