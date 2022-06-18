package com.jinninghui.newspiral.common.entity.smartcontract;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 智能合约信息：外部管理平台部署合约时需且仅需传入此对象，由Newspiral将其转换为SmartContract
 */
@ApiModel(description = "智能合约信息：外部管理平台部署合约时需且仅需传入此对象，由Newspiral将其转换为SmartContract")
@Data
public class SmartContractInfo {
    @ApiModelProperty(value = "部署的目标通道ID")
    private String channelId;

    @ApiModelProperty(value = "智能合约的源码内容")
    private String sourceContent;

    @ApiModelProperty(value = "智能合约版本号")
    private String version;

    @ApiModelProperty(value = "智能合约操作状态：安装，升级")
    private SmartContractOperationTypeEnum operationType;

    @ApiModelProperty(value = "扩展属性")
    private Map<String, String> extendedData = new HashMap<String, String>();

    @ApiModelProperty(value = "智能合约别名")
    private String alisa;

    @ApiModelProperty(value = "智能合约状态,1正常；2冻结；3销毁，新部署合约时，不用设置值，默认会设置为正常")
    private SmartContractStateEnum state;


    public SmartContractInfo clone()
    {
        SmartContractInfo smartContractInfo=new SmartContractInfo();
        smartContractInfo.setAlisa(this.alisa);
        smartContractInfo.setSourceContent(this.sourceContent);
        smartContractInfo.setOperationType(this.operationType);
        smartContractInfo.setChannelId(this.channelId);
        smartContractInfo.setExtendedData(this.extendedData);
        smartContractInfo.setState(this.state);
        smartContractInfo.setVersion(this.version);
        return smartContractInfo;

    }

}
