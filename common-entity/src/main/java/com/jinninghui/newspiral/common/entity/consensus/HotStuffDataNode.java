package com.jinninghui.newspiral.common.entity.consensus;

import com.jinninghui.newspiral.common.entity.block.Block;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lida
 * @date 2019/7/23 15:21
 *
 */
@ApiModel(description = "本地节点")
public class HotStuffDataNode {
    /**
     * 父Node，以其Block的hash作为key
     */
    @ApiModelProperty(value = "父节点的hash值")
    @Getter @Setter
    String parentNodeHashStr;
    /**
     * 等同于HotStuff中的cmd
     */
    @ApiModelProperty(value = "本节点的block")
    @Getter @Setter
    Block block;

    /**
     * 证明此block的父亲是合法的，注意并不是证明此Block合法
     */
    @ApiModelProperty(value = "投票证书，证明此block的父区块是合法的")
    @Getter @Setter
    GenericQC justify;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"parentNodeHashStr\":\"")
                .append(parentNodeHashStr).append('\"');
        sb.append(",\"block\":")
                .append(block.getHash());
        sb.append(",\"justify\":")
                .append(justify.toString());
        sb.append('}');
        return sb.toString();
    }

    public String toStringForLog()
    {
        return "HotStuffDataNode(parentNodeHashStr="+parentNodeHashStr+",本DataNode携带的blockHash="+block.getHash()+",justify共识的区块Hash="+justify.getBlockHash();
    }
}
