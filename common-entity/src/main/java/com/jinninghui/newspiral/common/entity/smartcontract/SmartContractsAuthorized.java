package com.jinninghui.newspiral.common.entity.smartcontract;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @version V1.0
 * @Title: QuerySmartContractReq
 * @Package com.jinninghui.newspiral.common.entity.smartcontract
 * @Description:
 * @author: xuxm
 * @date: 2020/8/27 18:38
 */
@ApiModel(description = "智能合约授权")
@Data
public class SmartContractsAuthorized {

    /**
     * 通道编号
     */
    @ApiModelProperty(value = "通道编号")
    private String channelId;
    /**
     * 智能合约
     */
    @ApiModelProperty(value = "简化智能合约列表")
    private List<SmartContractShort> smartContractShorts=new ArrayList<>();
    /**
     * 授权应用
     */
    @ApiModelProperty(value = "授权应用")
    private List<String> authorizedMember=new ArrayList<>();
    /**
     * 授权角色
     */
    @ApiModelProperty(value = "授权角色")
    private List<String> authorizedRole=new ArrayList<>();

    public SmartContractsAuthorized clone()
    {
        if(this==null) return null;
        SmartContractsAuthorized smartContractsAuthorized=new SmartContractsAuthorized();
        smartContractsAuthorized.setChannelId(this.getChannelId());
        return smartContractsAuthorized;
    }

}
