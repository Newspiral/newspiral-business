package com.jinninghui.newspiral.common.entity.transaction;

import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContractCallInstnace;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * SDKTransaction的基本参数，用于计算transHash
 */
@Data
public class SDKTransactionBasicParam {

    @ApiModelProperty(value = "交易相关参数的hash值")
    String hash;

    /**
     * 交易所属的通道ID
     */
    @ApiModelProperty(value = "交易所属通道ID")
    @NotBlank
    String channelId;

    /**
     * 客户端时间戳，可用于防止重放攻击，UTC时间1970年到发送时刻的毫秒数
     */
    @ApiModelProperty(value = "客户端时间戳")
    @NotNull
    Long clientTimestamp;

    /**
     * 业务程序生成的交易ID, 需保证该客户下全局唯一。调用方使用此交易ID查询交易执行结果
     */
    @ApiModelProperty(value = "交易客户端ID")
    @NotBlank
    @Length(min = 1, max = 128, message = "交易ID必须在1-128长度之间")
    String clientTxId;

    /**
     * 交易数据结构版本号
     */
    @ApiModelProperty(value = "交易数据结构版本号")
    @NotBlank
    @Length(min = 1, max = 10, message = "交易版本号必须在1-10长度之间")
    String version;

    /**
     * 智能合约方法调用实例
     */
    @ApiModelProperty(value = "智能合约方法调用实例")
    @Valid
    SmartContractCallInstnace smartContractCallInstnace;

    /**
     * 交易发起者的身份（含签名），如此可使得交易不可否认
     */
    @ApiModelProperty(value = "交易发起者身份标识")
    SignerIdentityKey signerIdentityKey;

}
