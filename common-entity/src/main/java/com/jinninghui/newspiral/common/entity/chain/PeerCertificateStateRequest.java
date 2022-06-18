package com.jinninghui.newspiral.common.entity.chain;

import com.jinninghui.newspiral.common.entity.identity.IdentityKey;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @version V1.0
 * @Title: PeerCertificateStateRequest
 * @Package com.jinninghui.newspiral.common.entity.chain
 * @Description:
 * @author: xuxm
 * @date: 2020/4/14 10:37
 */
@ApiModel(description = "证书状态请求体")
@Data
public class PeerCertificateStateRequest {

    /**
     * 节点本身的身份标识
     */
    @ApiModelProperty("节点本身的身份标识")
    @Valid
    @NotNull
    IdentityKey peerId;

    /**
     * 证书状态，1冻结；2吊销;0恢复
     */
    @ApiModelProperty("证书状态，1冻结；2吊销;0恢复")
    @NotBlank
    @Length(min = 1, max = 1, message = "状态的长度必须是1")
    String flag;

    /**
     *通道ID
     */
    @ApiModelProperty("通道ID")
    @NotBlank
    String channelId;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PeerCertificateStateRequest)) {
            return false;
        }

        PeerCertificateStateRequest newChannel= (PeerCertificateStateRequest) obj;
        if(!newChannel.getChannelId().equals(this.channelId)
                ||!newChannel.getFlag().equals(this.getFlag())
                ||!newChannel.peerId.equals(this.peerId))
        {
            return false;
        }
        return true;
    }
}
