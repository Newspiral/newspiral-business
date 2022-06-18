package com.jinninghui.newspiral.gateway.vo.req;

import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @version V1.0
 * @Title: SDKTransactionReq
 * @Package com.jinninghui.newspiral.gateway.vo.req
 * @Description:
 * @author: xuxm
 * @date: 2019/12/10 16:11
 */
@ApiModel(description = "SDK侧交易请求体")
@Data
public class SDKTransactionReq extends RPCParam implements Serializable {


    @ApiModelProperty("SDK业务交易")
    @NotNull
    @Valid
    private SDKTransaction sdkTransaction;
}
