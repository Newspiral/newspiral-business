package com.jinninghui.newspiral.common.entity.chain;

import com.jinninghui.newspiral.common.entity.policy.NewSpiralPolicyToken;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@ApiModel(description = "添加节点投票")
@Data
public class AddPeer2ChannelToken extends NewSpiralPolicyToken {
    /**
     * peer to be added
     */
    @ApiModelProperty("节点信息")
    PeerInfo newPeer;

    public AddPeer2ChannelToken clone()
    {
        AddPeer2ChannelToken addPeer2ChannelToken=new AddPeer2ChannelToken();
        addPeer2ChannelToken.setNewPeer(this.newPeer.clone());
        addPeer2ChannelToken.setChannelId(this.getChannelId());
        addPeer2ChannelToken.setHash(this.getHash());
        addPeer2ChannelToken.setSignerIdentityKey(getSignerIdentityKey().clone());
        Map<String, String> info = new HashMap<>();
        for (Map.Entry<String, String> entry : this.getMetaInfo().entrySet()) {
            info.put(entry.getKey(), entry.getValue());
        }
        addPeer2ChannelToken.setMetaInfo(info);
        return addPeer2ChannelToken;
    }
}
