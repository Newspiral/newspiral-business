package com.jinninghui.newspiral.common.entity.consensus;

import com.jinninghui.newspiral.common.entity.block.BlockResp;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 用于查询的时候对外展示hotstuff节点信息
 */
public class HotStuffDataNodeResp {

    /**
     * 父Node，以其Block的hash作为key
     */
    @ApiModelProperty(value = "父节点的hash值")
    @Getter
    @Setter
    String parentNodeHashStr;

    /**
     * 证明此block的父亲是合法的，注意并不是证明此Block合法
     */
    @ApiModelProperty(value = "投票证书，证明此block的父区块是合法的")
    @Getter @Setter
    GenericQC justify;

    /**
     * 区块基本信息，此字段仅仅在查询展示的时候使用
     */
    @Getter @Setter
    BlockResp blockResp;
}
