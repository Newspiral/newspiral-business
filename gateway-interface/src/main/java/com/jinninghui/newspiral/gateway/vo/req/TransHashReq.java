package com.jinninghui.newspiral.gateway.vo.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @version V1.0
 * @Title: TransHashReq
 * @Package com.jinninghui.newspiral.gateway.vo.req
 * @Description:
 * @author: xuxm
 * @date: 2019/12/7 14:58
 */
@ApiModel(description = "交易Hash请求体")
@Data
public class TransHashReq extends RPCParam {
    /**
     *交易hash值
     */
    @ApiModelProperty(value = "交易Hash值")
    @NotBlank
    private String transHash;

}
