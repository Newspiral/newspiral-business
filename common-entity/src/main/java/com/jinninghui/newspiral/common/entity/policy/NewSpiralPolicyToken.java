package com.jinninghui.newspiral.common.entity.policy;

import com.jinninghui.newspiral.common.entity.common.base.BaseApproval;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Token is used for the Policy control mechanism,
 * actually, token is a approval with extra information.
 */
@ApiModel(description = "NewSpiral投票数据结构")
@Data
public class NewSpiralPolicyToken extends BaseApproval implements Serializable {


     /**
     * extended data is used here,because we can't predict what will be
      *  packed into as part of approval.
     */
     @ApiModelProperty(value = "元信息（使用扩展属性数据）")
    private Map<String, String> metaInfo;

    public NewSpiralPolicyToken(){
        this.metaInfo = new HashMap<>();
    }
    public void addMetaInfo(String key, String value) {
        this.metaInfo.put(key, value);
    }

    public NewSpiralPolicyToken clone() {
        NewSpiralPolicyToken token = new NewSpiralPolicyToken();
        Map<String, String> info = new HashMap<>();
        for (Map.Entry<String, String> entry : metaInfo.entrySet()) {
            info.put(entry.getKey(), entry.getValue());
        }
        token.setMetaInfo(info);
        token.setChannelId(getChannelId());
        token.setHash(getHash());
        token.setSignerIdentityKey(getSignerIdentityKey().clone());
        return token;
    }

}
