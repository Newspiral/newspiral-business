package com.jinninghui.newspiral.common.entity.chain;

import com.jinninghui.newspiral.common.entity.identity.IdentityKey;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@ApiModel("节点冻结解冻")
@Data
public class PeerFrozenUnFrozen {

    /**
     * 通道ID
     */
    @ApiModelProperty("通道ID")
    @NotBlank
    String channelId;

    /**
     * 节点本身的身份标识
     */
    @ApiModelProperty("节点本身的身份标识")
    @Valid
    @NotNull
    IdentityKey peerId;

    /**
     * 节点冻结解冻
     */
    @ApiModelProperty("冻结解冻，0 解冻，1冻结")
    @NotBlank
    @Length(min = 1, max = 1, message = "状态的长度必须是1")
    String flag;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PeerFrozenUnFrozen)) {
            return false;
        }

        PeerFrozenUnFrozen newPeerFrozenUnFrozen = (PeerFrozenUnFrozen) obj;
        if(!newPeerFrozenUnFrozen.getChannelId().equals(this.channelId)
                ||!newPeerFrozenUnFrozen.getFlag().equals(this.getFlag())
                ||!newPeerFrozenUnFrozen.peerId.equals(this.peerId))
        {
            return false;
        }
        return true;
    }

    public PeerFrozenUnFrozen clone(){
        PeerFrozenUnFrozen peerFrozenUnFrozen = new PeerFrozenUnFrozen();
        peerFrozenUnFrozen.setChannelId(this.getChannelId());
        peerFrozenUnFrozen.setPeerId(this.getPeerId());
        peerFrozenUnFrozen.setFlag(this.getFlag());
        return peerFrozenUnFrozen;
    }
}
