package com.jinninghui.newspiral.ledger.mgr.impl.mapper;

import com.jinninghui.newspiral.ledger.mgr.impl.domain.ChannelModel;

import java.util.List;

public interface ChannelModelMapper {
    int deleteByPrimaryKey(String channelId);

    int insert(ChannelModel record);

    int insertSelective(ChannelModel record);

    ChannelModel selectByPrimaryKey(String channelId);

    int updateByPrimaryKeySelective(ChannelModel record);

    int updateByPrimaryKey(ChannelModel record);

    List<ChannelModel> selectAll();

    ChannelModel selectByName(String name);

    int updateFlagByPrimaryKey(ChannelModel record);
}