package com.jinninghui.newspiral.gateway.vo.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @version V1.0
 * @Title: QueryTxReq
 * @Package com.jinninghui.newspiral.gateway.vo.req
 * @Description:
 * @author: xuxm
 * @date: 2020/5/28 15:37
 */
@ApiModel(description = "交易账本查询请求体")
@Data
public class QueryTxReq extends RPCParam {
    /**
     * 账本key
     */
    @ApiModelProperty(value = "账本key")
    @NotBlank
    private  String  key;

}
