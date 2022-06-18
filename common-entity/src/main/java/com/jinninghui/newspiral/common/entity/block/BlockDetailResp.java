package com.jinninghui.newspiral.common.entity.block;

import com.jinninghui.newspiral.common.entity.ConsensusAlgorithmEnum;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import com.jinninghui.newspiral.common.entity.transaction.TransactionResp;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @data 2020/10/26
 * @author whj
 */
@Data
public class BlockDetailResp {
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
     * 创建区块节点的身份证明
     */
    @ApiModelProperty(value = "创建区块节点的身份证明")
    private SignerIdentityKey signerIdentityKey;

    /**
     * 区块共识所用的共识算法
     */
    @ApiModelProperty(value = "区块所用共识算法")
    private ConsensusAlgorithmEnum consensusAlgorithmEnum;

    /**
     * 区块合法性证明witness
     */
    @ApiModelProperty(value = "区块合法性证明")
    private String witness;

    /**
     * 交易列表
     */
    @ApiModelProperty(value = "交易列表")
    private List<TransactionResp> transactionList;

}
