package com.jinninghui.newspiral.gateway.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @data 2020/10/26
 * @author whj
 */
@Data
public class TransRegionBlockHashReq extends RPCParam {
    /**
     *区块hash值
     */
    @ApiModelProperty(value = "区块hash值")
    @NotBlank
    private String blockHash;

    /**
     * 交易index范围start
     */
    @ApiModelProperty(value = "交易在区块中的开始序号")
    private Integer TxIndexFrom;

    /**
     *交易index范围end
     */
    @ApiModelProperty(value = "交易在区块中的结束序号")
    private Integer TxIndexTo;
}
