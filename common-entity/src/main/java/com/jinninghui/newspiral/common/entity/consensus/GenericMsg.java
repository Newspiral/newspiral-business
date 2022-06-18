package com.jinninghui.newspiral.common.entity.consensus;

import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lida
 * @date 2019/7/19 18:03
 * 即HotSutff中的MSG(GENERIC,curProposal,null)产生的消息，该消息不需要jusity字段
 */
@ApiModel(description = "genericMsg,leader节点发送的消息")
@Data
public class GenericMsg extends HotStuffMsg  {
    @ApiModelProperty(value = "",hidden = true)
    String hash;

    /**
     * 通道Id
     */
    @ApiModelProperty(value = "通道Id")
    String channelId;

    @ApiModelProperty(value = "节点信息，其中包括leader提议的新的Block")
    HotStuffDataNode hotStuffDataNode;
    /**
     * 发送者（含签名），此字段为扩展字段，用于标识发送者身份
     */
    @ApiModelProperty(value = "发送者身份标识")
    SignerIdentityKey signerIdentityKey;



    /**
     * Node的parent使用输入justify的block
     * 注意并没有设置签名
     * @param block
     * @param justify
     * @param viewNo
     */
    public GenericMsg(Block block, GenericQC justify, Long viewNo,String channelId)
    {
        hotStuffDataNode = new HotStuffDataNode();
        hotStuffDataNode.setBlock(block);
        hotStuffDataNode.setJustify(justify);
        if(null!=justify) {
            hotStuffDataNode.setParentNodeHashStr(justify.getBlockHash());
        }
        this.viewNo = viewNo;
        this.channelId = channelId;
    }


    public GenericMsg(String hash, HotStuffDataNode hotStuffDataNode, SignerIdentityKey signerIdentityKey, Long viewNo) {
        this.hash = hash;
        this.hotStuffDataNode = hotStuffDataNode;
        this.signerIdentityKey = signerIdentityKey;
        this.viewNo = viewNo;
    }

    /**
     * 一个简单的描述字符串，便于记录日志
     * @return
     */
    public String createSimpleDespStr()
    {
        return "viewNo:"+viewNo+",blockHash:"+ hotStuffDataNode.getBlock().getHash();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"channelId\":\"")
                .append(channelId).append('\"');
        sb.append(",\"hotStuffDataNode\":")
                .append(hotStuffDataNode.toString());
        sb.append(",\"signerIdentityKey\":")
                .append(signerIdentityKey);
        sb.append(",\"viewNo\":")
                .append(viewNo);
        sb.append('}');
        return sb.toString();
    }

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
}
