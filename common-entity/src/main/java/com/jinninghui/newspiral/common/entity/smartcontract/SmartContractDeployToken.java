package com.jinninghui.newspiral.common.entity.smartcontract;

import com.jinninghui.newspiral.common.entity.common.base.BaseApproval;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 部署和升级合约均可使用使用此Token
 */
@ApiModel(description = "部署和升级合约均可使用使用此Token")
@Data
public class SmartContractDeployToken extends BaseApproval implements Serializable {
    /**
     * the smart contract to be deployed.
     */
    @ApiModelProperty("智能合约信息：外部管理平台部署合约时需且仅需传入此对象，由Newspiral将其转换为SmartContract")
   private SmartContractInfo smartContractInfo;


    public SmartContractDeployToken clone()
    {
        SmartContractDeployToken smartContractDeployToken=new SmartContractDeployToken();
        smartContractDeployToken.setSmartContractInfo(this.smartContractInfo);
        smartContractDeployToken.setChannelId(this.getChannelId());
        smartContractDeployToken.setHash(this.getHash());
        smartContractDeployToken.setSignerIdentityKey(this.getSignerIdentityKey().clone());
        return smartContractDeployToken;
    }
}
