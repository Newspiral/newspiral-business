package com.jinninghui.newspiral.ledger.mgr.impl.mapper;

import com.jinninghui.newspiral.ledger.mgr.impl.domain.MessageModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MessageModelMapper {
    int insert(@Param("record")MessageModel messageModel);

    List<MessageModel> selectByViewNo (@Param("channelId") String channelId, @Param("viewNo") Long viewNo);

    void deleteByViewNo (@Param("channelId") String channelId, @Param("viewNo") Long viewNo);
}
