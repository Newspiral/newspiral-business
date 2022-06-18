package com.jinninghui.newspiral.common.entity.chain;

import com.jinninghui.newspiral.common.entity.identity.IdentityKey;
import com.jinninghui.newspiral.common.entity.policy.NewSpiralPolicyToken;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@ApiModel(description = "通道移除节点投票")
@Data
public class RemovePeerFromChannelToken extends NewSpiralPolicyToken {

    @ApiModelProperty("节点身份标识")
    @Valid
    @NotNull
    IdentityKey peerId;


    public RemovePeerFromChannelToken clone()
    {
        RemovePeerFromChannelToken removePeerFromChannelToken=new RemovePeerFromChannelToken();
        removePeerFromChannelToken.setPeerId(this.peerId.clone());
        removePeerFromChannelToken.setChannelId(this.getChannelId());
        removePeerFromChannelToken.setHash(this.getHash());
        removePeerFromChannelToken.setSignerIdentityKey(getSignerIdentityKey().clone());
        Map<String, String> info = new HashMap<>();
        for (Map.Entry<String, String> entry : this.getMetaInfo().entrySet()) {
            info.put(entry.getKey(), entry.getValue());
        }
        removePeerFromChannelToken.setMetaInfo(info);
        return removePeerFromChannelToken;
    }
}
