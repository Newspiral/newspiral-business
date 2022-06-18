package com.jinninghui.newspiral.gateway.vo.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @version V1.0
 * @Title: TransHashReq
 * @Package com.jinninghui.newspiral.gateway.vo.req
 * @Description:
 * @author: xuxm
 * @date: 2019/12/7 14:58
 */
@ApiModel(description = "区块hash和交易序号请求体")
@Data
public class TransBlockHashReq extends RPCParam {

    /**
     *blockhash值
     */
    @ApiModelProperty(value = "区块hash值")
    @NotBlank
    private String blockHash;

    /**
     * 交易Index
     */
    @ApiModelProperty(value = "交易索引，不能小于1")
    @NotNull
    @Min(value = 1,message = "大小不能小于一")
    private Integer indexInBlock;

}
