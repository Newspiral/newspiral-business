package com.jinninghui.newspiral.common.entity.identity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author lida
 * @date 2019/7/5 19:13
 * 身份Key，在系统间交互时使用
 * 例如使用ECC公钥证书，则证书本身是身份，而公钥则是身份Key
 */
@Data
@Slf4j
public class IdentityKey {

    @ApiModelProperty(value = "身份标识类型(CHINA_PKI：基于国密的非对称加密体系)")
    @NotNull
    IdentityTypeEnum type;

    /**
     * 与具体IdentityTypeEnum相关
     */
    @ApiModelProperty(value = "身份标识类型值(成员公钥字符串)")
    @NotBlank
    String value;

    /**
     * 当且仅当表示通道绑定的身份时，该字段不为空，如通道成员，
     */
    @ApiModelProperty(value = "通道id，用于该身份绑定通道通道id，用于该身份绑定通道")
    String channelId;

    public IdentityKey clone() {
        IdentityKey key = new IdentityKey();
        key.setValue(this.getValue());
        key.setType(this.getType());
        key.setChannelId(this.getChannelId());
        return key;
    }


    /**
     * 身份标识是否有效
     * @return
     */
    public Boolean valid() {
        if (null == type || null == value || value.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"type\":")
                .append(type);
        sb.append(",\"value\":\"")
                .append(value).append('\"');
        sb.append(",\"channelId\":\"")
                .append(channelId).append('\"');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IdentityKey)) {
            return false;
        }
        if(!valid())
        {
            return false;
        }
        IdentityKey newIdentity = (IdentityKey) obj;
        if (newIdentity.getType().equals(this.type) && newIdentity.getValue().equals(this.value)) {
            return true;
        }
        return false;
    }
}
