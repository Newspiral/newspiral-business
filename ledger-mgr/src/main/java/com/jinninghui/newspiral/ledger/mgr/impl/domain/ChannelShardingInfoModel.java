package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import com.jinninghui.newspiral.common.entity.config.ChannelShardingInfo;
import lombok.Data;

@Data
public class ChannelShardingInfoModel {
    private String channelId;

    private String channelName;

    private long maxHeightInEachDs;

    public ChannelShardingInfo toChannelShardingInfo() {
        ChannelShardingInfo info = new ChannelShardingInfo();
        info.setChannelId(this.channelId);
        info.setChannelName(this.channelName);
        info.setMaxHeightInEachDs(this.maxHeightInEachDs);
        return info;
    }

    public static ChannelShardingInfoModel createInstance(ChannelShardingInfo info) {
        ChannelShardingInfoModel model = new ChannelShardingInfoModel();
        model.setChannelId(info.getChannelId());
        model.setChannelName(info.getChannelName());
        model.setMaxHeightInEachDs(info.getMaxHeightInEachDs());
        return model;
    }
}
