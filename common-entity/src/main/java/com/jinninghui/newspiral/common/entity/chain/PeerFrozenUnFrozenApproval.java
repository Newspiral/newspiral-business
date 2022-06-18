package com.jinninghui.newspiral.common.entity.chain;

import com.jinninghui.newspiral.common.entity.policy.NewSpiralPolicyToken;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel(description = "冻结解冻节点")
@Data
public class PeerFrozenUnFrozenApproval extends NewSpiralPolicyToken {

    /**
     * 证书状态动作
     */
    @ApiModelProperty("节点冻结解冻请求体")
    private PeerFrozenUnFrozen peerFrozenUnFrozen;



}
