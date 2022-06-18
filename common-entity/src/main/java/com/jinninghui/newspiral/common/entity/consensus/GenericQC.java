package com.jinninghui.newspiral.common.entity.consensus;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author lida
 * @date 2019/7/19 17:50
 * 由多个Vote组成的QC
 */
@ApiModel(description = "由多个Vote组成的Qc")
@Data
@Slf4j
public class GenericQC {
    @ApiModelProperty(value = "版本号")
    private String version = "1.0";

    /**
     * 本区块的Hash值
     */
    @ApiModelProperty(value = "genericQc的hash值")
    private String hash;
    /**
     * QC所共识的Block的Hash值
     */
    @ApiModelProperty(value = "QC所共识的Block的Hash值")
    String blockHash;
    /**
     * QC所共识的Block的父区块的hash值
     */
    @ApiModelProperty(value = "QC所共识的Block的父区块的hash值")
    String prevBlockHash;

    /**
     * block生成的viewNo
     */
    @ApiModelProperty(value = "block生成的viewNo")
    Long blockViewNo;

    /**
     * block的高度
     */
    @ApiModelProperty(value = "block的高度")
    Long height;

    /**
     * block创建的时间戳
     * 非共识算法所必须，便于计算两个BLock之间的时间长度用于决定是否需要创建一个新的Block
     * 新链设置为ConsensusContext的初始化时刻，等同于该链的创建时刻
     */
    @ApiModelProperty(value = "block创建的时间戳")
    Long blockCreateTimestamp;

    /**
     * 当前通道中的state=0(显示正常)的节点总数
     */
    @ApiModelProperty(value = "当前Qc所对应的通道中的正常节点总数")
    Integer PeerCnt;


    /**
     * 针对该Block的vote Map，应当为n-f个
     * key为BlockVoteMsg的业务键，即如果业务键相同，则BlockVoteMsg也相同
     */
    @ApiModelProperty(value = "针对该Block的vote Map，应当为n-f个，key为BlockVoteMsg的业务键")
    LinkedHashMap<String, BlockVoteMsg> voteMap;

    /**
     * blockViewNo越大则说明GenericQC越新
     * @param otherQC
     * @return
     */
    public boolean newerThan(GenericQC otherQC)
    {
        if (height.longValue() > otherQC.getHeight().longValue()) {
            return true;
        } else {
            return blockViewNo.longValue() > otherQC.getBlockViewNo().longValue();
        }
    }


    public boolean basicCheck() {
        String blockStr = null;
        List<BlockVoteMsg> list = new ArrayList<>();
        for (BlockVoteMsg msg : this.getVoteMap().values()) {

            if (blockStr == null) {
                blockStr = msg.getBlockHash();
            } else {
                if (false == blockStr.equals(msg.getBlockHash())) {
                    log.warn("inconsistent voting block");
                    return false;
                }
            }
            for(BlockVoteMsg already: list)
            {
                if(already.equals(msg))
                {
                    log.warn("repeated blockVoteMsg in QC " + getBlockHash());
                    return false;
                }
            }
            list.add(msg);
        }

        return true;
    }

    public static GenericQC clone(GenericQC genericQC)
    {
        GenericQC form=new GenericQC();
        form.setBlockCreateTimestamp(genericQC.getBlockCreateTimestamp());
        form.setBlockHash(genericQC.getBlockHash());
        form.setBlockViewNo(genericQC.getBlockViewNo());
        LinkedHashMap<String, BlockVoteMsg> voteMap=new LinkedHashMap<>();
        voteMap.putAll(genericQC.getVoteMap());
        form.setVoteMap(voteMap);
        form.setHeight(genericQC.getHeight());
        form.setPrevBlockHash(genericQC.getPrevBlockHash());
        form.setPeerCnt(genericQC.getPeerCnt());
        form.setHash(genericQC.getHash());
        return form;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"blockHash\":\"")
                .append(blockHash).append('\"');
        sb.append(",\"prevBlockHash\":\"")
                .append(prevBlockHash).append('\"');
        sb.append(",\"blockViewNo\":")
                .append(blockViewNo);
        sb.append(",\"height\":")
                .append(height);
        sb.append(",\"blockCreateTimestamp\":")
                .append(blockCreateTimestamp);
        sb.append('}');
        return sb.toString();
    }
}
