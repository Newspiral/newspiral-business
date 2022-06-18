package com.jinninghui.newspiral.gateway.vo.req;

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
@Data
public class TransClientTxIdReq extends RPCParam {
    /**
     *客户端交易ID值
     */
    @ApiModelProperty(value = "客户端交易ID值")
    @NotBlank
    private String clientTxId;

}
