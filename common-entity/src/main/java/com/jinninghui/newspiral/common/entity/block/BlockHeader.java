package com.jinninghui.newspiral.common.entity.block;

import com.jinninghui.newspiral.common.entity.ConsensusAlgorithmEnum;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author lida
 * @date 2019/7/11 16:36
 * 区块头
 */
@ToString
public class BlockHeader {

    @ApiModelProperty(value = "Version1.0")
    public static final String VERSION_1_0 = "Version1.0";
    /**
     * 数据结构版本号
     */
    @ApiModelProperty(value = "数据结构版本号")
    @Getter @Setter
    private String version;
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
     * 本区块的Hash值
     */
    @ApiModelProperty(value = "本区块Hash值")
    @Getter @Setter
    private String hash;
    /**
     * 所属通道ID
     */
    @ApiModelProperty(value = "所属通道ID")
    @Getter @Setter
    private String channelId;

    /**
     * 区块构建完成时间戳
     */
    @ApiModelProperty(value = "区块构建完成时间戳")
    @Getter @Setter
    private Long timestamp;


    @ApiModelProperty(value = "区块共识时间")
    @Getter @Setter
    private Long consensusTimestamp;


    @ApiModelProperty(value = "区块持久化时间")
    @Getter @Setter
    private Long persistenceTimestamp;

    /**
     * 证明本区块合法的证据，与具体的共识算法相关，使用String各种证据类型
     */
    @ApiModelProperty("证明本区块合法的证据，与具体的共识算法相关，可使用String数据类型的各种证据")
    @Getter @Setter
    private String witness;


    /**
     * 证明本区块世界状态合法的依据
     */
    @ApiModelProperty(value = "证明本区块世界状态合法的依据")
    @Getter @Setter
    private byte[] worldStateHash;

    /**
     * merkle root of tx
     */
    @ApiModelProperty("默克尔根")
    @Getter @Setter
    private String merkleRoot = "";

    /**
     * 区块共识算法
     */
    @ApiModelProperty(value = "区块共识算法")
    @Getter @Setter
    private ConsensusAlgorithmEnum consensusAlgorithm;

    @ApiModelProperty(value = "区块创建者身份标识和相关参数签名信息")
    @Getter @Setter
    private SignerIdentityKey packagerAndSign;

    public BlockHeader clone() {
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setWitness(witness);
        blockHeader.setPackagerAndSign(packagerAndSign);
        blockHeader.setHash(hash);
        blockHeader.setChannelId(channelId);
        blockHeader.setConsensusAlgorithm(consensusAlgorithm);
        blockHeader.setHeight(height);
        blockHeader.setConsensusTimestamp(consensusTimestamp);
        blockHeader.setMerkleRoot(merkleRoot);
        blockHeader.setPersistenceTimestamp(persistenceTimestamp);
        blockHeader.setPrevBlockHash(prevBlockHash);
        blockHeader.setVersion(version);
        blockHeader.setWorldStateHash(worldStateHash);
        blockHeader.setTimestamp(timestamp);
        return blockHeader;
    }

}
