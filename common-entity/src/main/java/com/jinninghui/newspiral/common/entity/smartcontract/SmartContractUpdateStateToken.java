package com.jinninghui.newspiral.common.entity.smartcontract;

import com.jinninghui.newspiral.common.entity.common.base.BaseApproval;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@ApiModel(description = "智能合约状态升级投票")
@Data
public class SmartContractUpdateStateToken extends BaseApproval implements Serializable {

    @ApiModelProperty(value = "智能合约版本号")
    private String version;

    @ApiModelProperty(value = "智能合约别名")
    private String alisa;

    @ApiModelProperty(value = "智能合约状态,1正常；2冻结；3销毁")
    private SmartContractStateEnum state;

    public  boolean equals(SmartContractUpdateStateToken other)
    {
        if(version.equals(other.getVersion()) && alisa.equals(other.getAlisa()) && state.equals(other.getState())
        && getChannelId().equals(other.getChannelId()))
        {
            return true;
        }
        return false;
    }

}
