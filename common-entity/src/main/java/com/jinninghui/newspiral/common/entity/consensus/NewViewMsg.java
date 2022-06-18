package com.jinninghui.newspiral.common.entity.consensus;

import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lida
 * @date 2019/7/20 9:33
 * HotStuff的NewView消息
 * 按照HotStuff的设计，NewView消息会携带本地的GenericQC
 * 这里不使用@Data注解，需要自行定义equals和hashcode方法
 */
@ApiModel(description = "NewViewMsg消息，会携带本地的GenericQC")
@Data
public class NewViewMsg extends HotStuffMsg {
    @ApiModelProperty(value = "",hidden = true)
    String hash;

    String channelId;

    /**
     * viewNo是当前view的编号
     */

    /**
     * 发送者本地的GenericQC
     */
    @ApiModelProperty(value = "本地的genericQc证书")
    GenericQC justify;

    /**
     * 发送者（含签名）
     */
    @ApiModelProperty(value = "发送者身份标识")
    SignerIdentityKey signerIdentityKey;



    public SignerIdentityKey getSignerIdentityKey() {
        return signerIdentityKey;
    }


    public void setSignerIdentityKey(SignerIdentityKey identity) {
        signerIdentityKey = identity;
    }


    public void setHash(String hash) {
        this.hash=hash;
    }


    public String getHash() {
        return hash;
    }

    public boolean equals(NewViewMsg newViewMsg) {
        if (null == newViewMsg) {
            return false;
        }
        if (viewNo.equals(newViewMsg.viewNo) &&
            justify.getBlockHash().equals(newViewMsg.getJustify().getBlockHash()) &&
            signerIdentityKey.getIdentityKey().equals(newViewMsg.getSignerIdentityKey().getIdentityKey())) {
            return true;
        }
        return false;

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"justify\":")
                .append(justify);
        sb.append(",\"signerIdentityKey\":")
                .append(signerIdentityKey);
        sb.append(",\"viewNo\":")
                .append(viewNo);
        sb.append('}');
        return sb.toString();
    }

    public String getBussinessKey() {
        return viewNo.toString()+channelId+(signerIdentityKey!=null?signerIdentityKey.getIdentityKey():null);
    }

}
