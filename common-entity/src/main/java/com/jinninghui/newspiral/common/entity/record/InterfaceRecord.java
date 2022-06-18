package com.jinninghui.newspiral.common.entity.record;

import com.jinninghui.newspiral.common.entity.Hashable;
import com.jinninghui.newspiral.common.entity.identity.IdentityTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author：whj
 * @data 2020/11/22
 * 接口调用记录实体
 */

@Data
public class InterfaceRecord implements Hashable{
    /**
     * 用于标识interface的唯一性
     */
    private String hash;
    /**
     * 自增主键
     */
    @ApiModelProperty("自增主键")
    @NotNull
    private Long Id;

    /**
     * 被调用的接口名称
     */
    @ApiModelProperty("被调用的接口名称")
    @NotNull
    private String methodName;


    /**
     * 被调用的接口协议类型
     */
    @ApiModelProperty("被调用的接口协议类型")
    @NotNull
    private String protocolName;

    /**
     * 调用方身份，IdentityTypeEnum key
     */
    @ApiModelProperty("身份标识类型")
    @NotBlank
    private IdentityTypeEnum identityType;

    /**
     * 调用方身份，vulue
     */
    @ApiModelProperty("身份标识类型值")
    @NotBlank
    private String identityValue;

    /**
     * 调用所属通道ID
     */
    @ApiModelProperty("调用所属通道ID")
    private String channelId;

    /**
     * 调用发生时间戳，毫秒
     */
    @ApiModelProperty("调用发生时间戳，毫秒")
    @NotNull
    private Long startTime;

    /**
     * 调用完成时间戳，毫秒
     */
    @ApiModelProperty("调用完成时间戳，毫秒")
    @NotNull
    private Long endTime;

    /**
     * 接口处理时长，毫秒
     */
    @ApiModelProperty("接口处理时长，毫秒")
    @NotNull
    private Long callTime;

    /**
     * 调用结果，1：Success:；0：Failed
     */
    @ApiModelProperty("调用结果，1：Success:；0：Failed")
    @NotNull
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

    @Override
    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String getHash() {
        return this.hash;
    }
}
