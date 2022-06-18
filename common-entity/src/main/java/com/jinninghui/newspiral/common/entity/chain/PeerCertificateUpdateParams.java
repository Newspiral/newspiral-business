package com.jinninghui.newspiral.common.entity.chain;

import com.jinninghui.newspiral.common.entity.identity.IdentityKey;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @version V1.0
 * @Title: PeerCertificateUpdateParams
 * @Package com.jinninghui.newspiral.common.entity.chain
 * @Description:
 * @author: xuxm
 * @date: 2021/3/12 13:26
 */
@ApiModel(description = "修改节点证书参数")
@Data
public class PeerCertificateUpdateParams {

    @ApiModelProperty(value = "通道Id")
    private String channelId;
    /**
     * 节点本身的身份标识
     */
    @ApiModelProperty(value = "节点本身的身份标识")
    @Valid
    @NotNull
    IdentityKey peerId;
    /**
     * ca证书
     */
    @ApiModelProperty(value = "ca证书")
    private byte[] certificateCerFile=null;

    /**
     * 密钥库
     */
    @ApiModelProperty(value = "密钥库")
    private byte[] certificateKeyStoreFile=null;
}
