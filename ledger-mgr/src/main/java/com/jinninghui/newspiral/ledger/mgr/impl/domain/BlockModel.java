package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import com.alibaba.fastjson.JSON;
import com.jinninghui.newspiral.common.entity.ConsensusAlgorithmEnum;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.block.BlockHeader;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import lombok.ToString;

import java.util.Date;

@ToString
public class BlockModel {
    private Long id;

    private Long blockId;

    private String version;

    private String hash;

    private String prevBlockHash;

    private Long prevBlockHeight;

    private String blockBuilderId;

    private String channelId;

    private Date packTimestamp;

    private String blockConsensus;

    private String blockWitness;

    private Date consensusTimeStamp;

    private Date persistenceTimeStamp;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBlockId() {
        return blockId;
    }

    public void setBlockId(Long blockId) {
        this.blockId = blockId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPrevBlockHash() {
        return prevBlockHash;
    }

    public void setPrevBlockHash(String prevBlockHash) {
        this.prevBlockHash = prevBlockHash;
    }

    public Long getPrevBlockHeight() {
        return prevBlockHeight;
    }

    public void setPrevBlockHeight(Long prevBlockHeight) {
        this.prevBlockHeight = prevBlockHeight;
    }

    public String getBlockBuilderId() {
        return blockBuilderId;
    }

    public void setBlockBuilderId(String blockBuilderId) {
        this.blockBuilderId = blockBuilderId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public Date getPackTimestamp() {
        return packTimestamp;
    }


    public void setPackTimestamp(Date packTimestamp) {
        this.packTimestamp = packTimestamp;
    }

    public String getBlockConsensus() {
        return blockConsensus;
    }

    public void setBlockConsensus(String blockConsensus) {
        this.blockConsensus = blockConsensus;
    }

    public String getBlockWitness() {
        return blockWitness;
    }

    public void setBlockWitness(String blockWitness) {
        this.blockWitness = blockWitness;
    }

    public Date getConsensusTimeStamp() {
        return consensusTimeStamp;
    }

    public void setConsensusTimeStamp(Date consensusTimeStamp) {
        this.consensusTimeStamp = consensusTimeStamp;
    }

    public Date getPersistenceTimeStamp() {
        return persistenceTimeStamp;
    }

    public void setPersistenceTimeStamp(Date persistenceTimeStamp) {
        this.persistenceTimeStamp = persistenceTimeStamp;
    }

    public BlockHeader toBlockHeader() {
        BlockHeader header = new BlockHeader();
        header.setHash(this.getHash());
        header.setConsensusAlgorithm(JSON.parseObject(this.getBlockConsensus(),ConsensusAlgorithmEnum.class));
        header.setChannelId(this.getChannelId());
        header.setHeight(this.getBlockId());
        header.setPrevBlockHash(this.getPrevBlockHash());
        header.setTimestamp(this.getPackTimestamp().getTime());
        header.setVersion(this.getVersion());
        SignerIdentityKey caller = JSON.parseObject(this.getBlockBuilderId(), SignerIdentityKey.class);
        header.setPackagerAndSign(caller);
        header.setWitness(this.getBlockWitness());

        header.setPersistenceTimestamp(this.getPersistenceTimeStamp()!=null?this.getPersistenceTimeStamp().getTime():null);
        header.setConsensusTimestamp(this.getConsensusTimeStamp()!=null?this.getConsensusTimeStamp().getTime():null);
        return header;
    }

    public static BlockModel createInstance(Block block)
    {
        BlockModel model = new BlockModel();
        model.setBlockId(block.getBlockHeader().getHeight());
        model.setBlockBuilderId(JSON.toJSONString(block.getSignerIdentityKey()));
        model.setBlockConsensus(JSON.toJSONString(block.getBlockHeader().getConsensusAlgorithm()));
        model.setHash(block.getHash());
        model.setPrevBlockHash(block.getPrevBlockHash());
        model.setPackTimestamp(new Date(block.getBlockHeader().getTimestamp()));
        model.setChannelId(block.getBlockHeader().getChannelId());
        model.setPrevBlockHeight(block.getBlockHeader().getHeight() - 1);
        model.setVersion(block.getBlockHeader().getVersion());
        model.setBlockWitness(block.getBlockHeader().getWitness());
        model.setConsensusTimeStamp(block.getBlockHeader().getConsensusTimestamp()!=null?new Date(block.getBlockHeader().getConsensusTimestamp()):null);
        model.setPersistenceTimeStamp(block.getBlockHeader().getPersistenceTimestamp()!=null?new Date(block.getBlockHeader().getPersistenceTimestamp()):null);
        return model;
    }
}
