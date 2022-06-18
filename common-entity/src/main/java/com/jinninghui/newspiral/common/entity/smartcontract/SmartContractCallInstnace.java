package com.jinninghui.newspiral.common.entity.smartcontract;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author lida
 * @date 2019/7/11 16:23
 * 智能合约调用实例，使用该类的对象，即可完成一次智能合约的调用
 */
@ApiModel(description = "智能合约调用实例，使用该类的对象，即可完成一次智能合约的调用")
@Data
public class SmartContractCallInstnace implements Serializable {
    /**
     * 智能合约ID，在某一个区块链中唯一，系统智能合约传全类名，业务智能合约传（channelId,version,alisa）拼接字符串
     */
    @ApiModelProperty(value = "智能合约ID，在某一个区块链中唯一，系统智能合约传全类名，业务智能合约传（channelId,version,alisa）拼接字符串")
    String smartContractId;

    /**
     * 方法参数的class名称列表
     */
    @ApiModelProperty(value = "方法参数的class名称列表")
    private String[] methodArgSigs;

    /**
     * 调用的智能合约的方法名
     */
    @ApiModelProperty(value = "调用的智能合约的方法名")
    @NotBlank
    String methodName;

    /**
     * 调用参数
     */
    @ApiModelProperty(value = "调用方法的入参")
    Object[] methodArgs;

    /**
     * 具体参数的class名
     */
    @ApiModelProperty(value = "具体参数的class名")
    private String[] methodArgClassNames;

    /**
     * 复制一份
     * @return
     */
    public SmartContractCallInstnace clone(){
        SmartContractCallInstnace smartContractCallInstnace=new SmartContractCallInstnace();
        smartContractCallInstnace.setMethodArgs(this.getMethodArgs());
        smartContractCallInstnace.setSmartContractId(this.getSmartContractId());
        smartContractCallInstnace.setMethodArgClassNames(this.getMethodArgClassNames());
        smartContractCallInstnace.setMethodArgSigs(this.getMethodArgSigs());
        smartContractCallInstnace.setMethodName(this.getMethodName());
        return smartContractCallInstnace;

    }

}
