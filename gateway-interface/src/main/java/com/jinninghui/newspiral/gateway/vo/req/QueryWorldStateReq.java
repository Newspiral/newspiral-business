package com.jinninghui.newspiral.gateway.vo.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @version V1.0
 * @Title: QueryWorldStateReq
 * @Package com.jinninghui.newspiral.gateway.vo.req
 * @Description:
 * @author: xuxm
 * @date: 2020/2/8 11:11
 */
@ApiModel(description = "查询世界状态请求体")
@Data
public class QueryWorldStateReq extends RPCParam {

    /**
     * 世界状态key
     */
    @ApiModelProperty(value = "世界状态key")
    @NotBlank
    private     String  key;


    @ApiModelProperty(value = "是否查询附属信息的表示")
    private boolean attachFlag;

}
