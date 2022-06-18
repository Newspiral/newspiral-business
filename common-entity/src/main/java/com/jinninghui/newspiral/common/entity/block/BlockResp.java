package com.jinninghui.newspiral.common.entity.block;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @version V1.0
 * @Title: BlockResp
 * @Package com.jinninghui.newspiral.gateway.vo.resp
 * @Description:
 * @author: xuxm
 * @date: 2019/12/7 11:50
 */
@ApiModel(description = "返回区块数据")
@Data
public class BlockResp {

    /**
     * 数据结构版本号
     */
    @ApiModelProperty(value = "数据结构版本号")
    private String version;
    /**
     * 区块高度
     */
    @ApiModelProperty(value = "区块高度")
    private Long height;
    /**
     * 前序区块的Hash值
     */
    @ApiModelProperty(value = "前序区块Hash值")
    private String prevBlockHash;

    /**
     * 本区块的Hash值
     */
    @ApiModelProperty(value = "本区块Hash值")
    private String hash;
    /**
     * 所属通道ID
     */
    @ApiModelProperty(value = "所属通道ID")
    private String channelId;

    /**
     * 区块构建完成时间戳
     */
    @ApiModelProperty(value = "区块构建完成时间戳")
    private Long timestamp;


    @ApiModelProperty(value = "块中交易数")
    private Integer transactionNum;


    @ApiModelProperty(value = "交易的hash列表")
    private List<String> transactionHashList;


    @ApiModelProperty(value = "共识时间")
    private Long consensusTime;


    @ApiModelProperty(value = "持久化时间")
    private Long persistenceTime;

    /**
     * 块大小
     */
    @ApiModelProperty(value = "区块字节数",hidden = true)
    private Integer blockSize;
    /**
     * 创建区块的节点
     */
    @ApiModelProperty(value = "创建区块的节点ID值",hidden = true)
    private String builderPeerId;

}
