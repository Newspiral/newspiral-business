package com.jinninghui.newspiral.common.entity.common.base;

import com.jinninghui.newspiral.common.entity.VerifiableData;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @version V1.0
 * @Title: BaseApproval
 * @Package com.jinninghui.newspiral.common.entity.common.base
 * @Description:
 * @author: xuxm
 * @date: 2020/1/20 15:12
 */
@ApiModel(description = "基础审核信息")
@Data
public class BaseApproval implements VerifiableData {
    @ApiModelProperty("投票参数相关Hash值")
    private String hash;
    /**
     * 加入的通道ID
     */
    @ApiModelProperty(value = "通道ID")
    private String channelId;

    /**
     * 同意该申请的组织的身份标识
     */
    @ApiModelProperty(value = "审核者的身份标识")
    private SignerIdentityKey signerIdentityKey;
}
