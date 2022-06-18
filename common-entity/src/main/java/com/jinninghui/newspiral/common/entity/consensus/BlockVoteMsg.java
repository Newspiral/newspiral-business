package com.jinninghui.newspiral.common.entity.consensus;


import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @version V1.0
 * @Title: BlockVoteMsg
 * @Package com.jinninghui.newspiral.consensus.hotstuff
 * @Description:针对某个BlockMsg的Vote，标识某个副本支持该Block，其中的viewNo是投票者本地的viewNo,一定会等于Block的ViewNo
 * @author lida
 * @date 2019/7/19 17:52
 */
@ApiModel(description = "投票消息")
@Data
public class BlockVoteMsg extends HotStuffMsg {
    @ApiModelProperty(value = "区块Hash值",hidden = true)
    String hash;

    /**
     * 通道Id
     */
    @ApiModelProperty(value = "通道Id")
    String channelId;

    /**
     * 区块Hash
     */
    @ApiModelProperty(value = "区块Hash值")
    String blockHash;

    /**
     * 投票者身份
     */
    @ApiModelProperty(value = "投票者身份标识")
    SignerIdentityKey signerIdentityKey;


    public SignerIdentityKey getSignerIdentityKey() {
        return signerIdentityKey;
    }

    public void setSignerIdentityKey(SignerIdentityKey identity) {
        signerIdentityKey = identity;
    }


    public boolean equals(BlockVoteMsg voteMsg) {
        if (null == voteMsg) {
            return false;
        }
        if (channelId.equals(voteMsg.getChannelId()) &&
            blockHash.equals(voteMsg.getBlockHash()) &&
            viewNo.equals(voteMsg.getViewNo()) &&
            signerIdentityKey.getIdentityKey().equals(voteMsg.getSignerIdentityKey().getIdentityKey())) {
            return true;
        }
        return false;
    }

    public void setHash(String hash) {
        this.hash=hash;
    }

    public String getHash() {
        return hash;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"channelId\":\"")
                .append(channelId).append('\"');
        sb.append(",\"blockHash\":\"")
                .append(blockHash).append('\"');
        sb.append(",\"signerIdentityKey\":")
                .append(signerIdentityKey);
        sb.append(",\"viewNo\":")
                .append(viewNo);
        sb.append('}');
        return sb.toString();
    }


    public String getBussinessKey() {
        return viewNo.toString()+blockHash+channelId+(signerIdentityKey!=null?signerIdentityKey.getIdentityKey():null)  ;
    }



}
