package com.jinninghui.newspiral.gateway.vo.req;

import com.jinninghui.newspiral.common.entity.VerifiableData;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @version V1.0
 * @Title: RPCParamImpl
 * @Package com.jinninghui.newspiral.gateway.vo.req
 * @Description:
 * @author: xuxm
 * @date: 2020/10/21 15:55
 */
@Data
public class RPCParam implements VerifiableData {

    /**
     *入参参数除身份之外字段的hash
     */
    @ApiModelProperty(value = "入参参数除身份之外字段的hash",hidden = true)
    private String hash;
    /**
     * 调用者身份
     */
    @ApiModelProperty(value = "调用者身份标识")
    @NotNull
    @Valid
    private SignerIdentityKey signerIdentityKey;
}
