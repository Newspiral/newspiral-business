package com.jinninghui.newspiral.common.entity.block;

import com.jinninghui.newspiral.common.entity.ConsensusAlgorithmEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * blockHeader的基本参数，用于计算blockhash,和createBlock中的blockHeader设置参数对应
 * @Data 2020/11/9
 */
@ToString
@Data
public class BlockHeaderBasicParam {

    /**
     * 所属通道ID
     */
    @ApiModelProperty(value = "所属通道ID")
    @Getter
    @Setter
    private String channelId;

    /**
     * 区块共识算法
     */
    @ApiModelProperty(value = "区块共识算法")
    @Getter @Setter
    private ConsensusAlgorithmEnum consensusAlgorithm;

    /**
     * 区块高度，从0开始
     */
    @ApiModelProperty(value = "区块高度")
    @Getter @Setter
    private Long height;

    /**
     * 前序区块的Hash值
     */
    @ApiModelProperty(value = "前序区块Hash值")
    @Getter @Setter
    private String prevBlockHash;

    /**
     * 区块构建完成时间戳
     */
    @ApiModelProperty(value = "区块构建完成时间戳")
    @Getter @Setter
    private Long timestamp;

    /**
     * 数据结构版本号
     */
    @ApiModelProperty(value = "数据结构版本号")
    @Getter @Setter
    private String version;

    /**
     * merkle root of tx
     */
    @ApiModelProperty("默克尔根")
    @Getter @Setter
    private String merkleRoot = "";
}
