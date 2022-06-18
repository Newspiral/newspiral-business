package com.jinninghui.newspiral.common.entity.smartcontract;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @version V1.0
 * @Title: QuerySmartContractReq
 * @Package com.jinninghui.newspiral.common.entity.smartcontract
 * @Description:
 * @author: xuxm
 * @date: 2020/8/27 18:38
 */
@ApiModel(description = "查询智能合约列表请求体")
@Data
public class QuerySmartContractListReq {
    /**
     * 通道编号
     */
    @ApiModelProperty("通道Id")
    private String channelId;

    /**
     * 状态，1正常；2冻结；3销毁
     */
    @ApiModelProperty("合约状态，1正常；2冻结；3销毁")
    private String flag;

    /**
     * 页数
     * @return
     */
    @ApiModelProperty("页数")
    @Min(value = 1)
    @NotNull
    private Integer pageNo=1;

    /**
     * 每页数量
     */
    @ApiModelProperty("每页数量，默认为10")
    @Min(value = 1)
    @NotNull
    private Integer pageSize=10;
}
