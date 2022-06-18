package com.jinninghui.newspiral.common.entity.state;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @version V1.0
 * @Title: WorldStateResp
 * @Package com.jinninghui.newspiral.gateway.vo.resp
 * @Description:
 * @author: xuxm
 * @date: 2020/2/10 9:54
 */
@ApiModel(description = "世界状态信息")
@Slf4j
@Data
public class WorldStateResp {
    /**
     * 世界状态key
     */
    @ApiModelProperty(value = "世界状态key")
    private String key;

    /**
     * value
     */
    @ApiModelProperty(value = "世界状态value")
    private byte[] value;

    /**
     * 时间
     */
    @ApiModelProperty(value = "时间戳",hidden = true)
    private Long updateTime;


    @ApiModelProperty(value="最新更新的区块hash")
    private String latestBlockHash;


    @ApiModelProperty(value="最新更新的区块高度")
    private Long latestBlockHeight;

    @ApiModelProperty(value="最新更新的交易hash")
    private String latestTransHash;

    @ApiModelProperty(value="通道名称")
    private String channelName;

    @ApiModelProperty(value="通道Id")
    private String channelId;

    /**
     * 世界状态实体转化
     * @param worldState
     * @return
     */
    public static WorldStateResp transferWorldState(WorldState worldState) {
        WorldStateResp worldStateResp = new WorldStateResp();
        worldStateResp.setKey(worldState.getKey());
        worldStateResp.setLatestBlockHash(worldState.getLatestBlockHash());
        worldStateResp.setUpdateTime(worldState.getUpdateTime());
        worldStateResp.setLatestTransHash(worldState.getLatestTransHash());
        worldStateResp.setLatestBlockHeight(worldState.getLatestBlockHeight());
        worldStateResp.setValue(worldState.getValue());
        return worldStateResp;
    }

}
