package com.jinninghui.newspiral.common.entity.smartcontract;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @version V1.0
 * @Title: SmartContractShort
 * @Package com.jinninghui.newspiral.common.entity.smartcontract
 * @Description:
 * @author: xuxm
 * @date: 2020/8/31 15:48
 */
@ApiModel(description = "简化智能合约")
@Data
public class SmartContractShort {
    //别名
    @ApiModelProperty(value = "别名")
    private String alisa;

    //版本号
    @ApiModelProperty(value = "版本号")
    private String version;
}
