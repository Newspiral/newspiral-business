package com.jinninghui.newspiral.common.entity.transaction;

import com.jinninghui.newspiral.common.entity.VerifiableData;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import com.jinninghui.newspiral.common.entity.policy.NewSpiralPolicyToken;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContractCallInstnace;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lida
 * @date 2019/7/15 17:15
 * SDK侧（即业务程序侧）生成的交易
 */
@ApiModel(description = "SDK侧（即业务程序侧）生成的交易")
@Data
public class SDKTransaction implements VerifiableData {

    @ApiModelProperty(value = "交易相关参数的hash值")
    String hash;

    /**
     * 交易数据结构版本号
     */
    @ApiModelProperty(value = "交易数据结构版本号")
    @NotBlank
    @Length(min = 1, max = 10, message = "交易版本号必须在1-10长度之间")
    String version;

    /**
     * 客户端时间戳，可用于防止重放攻击，UTC时间1970年到发送时刻的毫秒数
     */
    @ApiModelProperty(value = "客户端时间戳")
    @NotNull
    Long clientTimestamp;

    /**
     * 交易所属的通道ID
     */
    @ApiModelProperty(value = "交易所属通道ID")
    @NotBlank
    String channelId;

    /**
     * 业务程序生成的交易ID, 需保证该客户下全局唯一。调用方使用此交易ID查询交易执行结果
     */
    @ApiModelProperty(value = "业务程序生成的交易ID, 需保证该客户下全局唯一。调用方使用此交易ID查询交易执行结果，交易ID必须在1-128长度之间")
    @NotBlank
    @Length(min = 1, max = 128, message = "交易ID必须在1-128长度之间")
    String clientTxId;

    /**
     * 智能合约方法调用实例
     */
    @ApiModelProperty(value = "智能合约方法调用实例")
    @Valid
    SmartContractCallInstnace smartContractCallInstnace;


    @ApiModelProperty(value = "智能合约调用凭证列表：针对管理类交易一般需要通道中的组织对该交易进行背书即提供token，对于普通业务交易调用一般为空")
    List<NewSpiralPolicyToken> tokenList = new ArrayList<>();

    /**
     * 交易发起者的身份（含签名），如此可使得交易不可否认
     */
    @ApiModelProperty(value = "交易发起者身份标识")
    SignerIdentityKey signerIdentityKey;

    public SignerIdentityKey getSignerIdentityKey() {
        return signerIdentityKey;
    }

    public void setSignerIdentityKey(SignerIdentityKey identity) {
        this.signerIdentityKey = identity;
    }

    public SDKTransaction()
    {
        this.version = "V1.0";
    }

    public void setHash(String hash) {
        this.hash=hash;
    }

    public String getHash() {
        return hash;
    }

    @ApiModelProperty(value = "",hidden = true)
    public long getSize() {
        return 1000L;
    }

    /**
     * 复制一份
     * @return
     */
    public SDKTransaction clone()
    {
        SDKTransaction sdkTransaction = new SDKTransaction();
        sdkTransaction.setSmartContractCallInstnace(this.getSmartContractCallInstnace().clone());
        sdkTransaction.setHash(this.getHash());
        sdkTransaction.setSignerIdentityKey(this.getSignerIdentityKey());
        sdkTransaction.setChannelId(this.getChannelId());
        sdkTransaction.setClientTimestamp(this.getClientTimestamp());
        sdkTransaction.setClientTxId(this.getClientTxId());
        sdkTransaction.setVersion(this.getVersion());
        List<NewSpiralPolicyToken> tokens = new ArrayList<>();
        for (NewSpiralPolicyToken token : tokenList) {
            tokens.add(token.clone());
        }
        sdkTransaction.setTokenList(tokens);
        return sdkTransaction;
    }
}
