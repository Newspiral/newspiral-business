package com.jinninghui.newspiral.ledger.mgr.impl.mapper;

import com.jinninghui.newspiral.ledger.mgr.impl.domain.StateModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StateModelMapper {
    /**
     * 查询该通道内的所有世界状态
     * @param channelId
     * @return
     */
    List<StateModel> selectAllWorldState(@Param("channelId") String channelId, @Param("from") Long from, @Param("to")Long to);

    int deleteByPrimaryKey(@Param("channelId") String channelId, @Param("key") String key);
    int batchDeleteByPrimaryKey(@Param("list") List<StateModel> list);

    int insert(@Param("record") StateModel record);
    int batchInsert(@Param("list") List<StateModel> list);

    StateModel selectByPrimaryKey(@Param("channelId") String channelId,@Param("key") String key,@Param("flag") Boolean flag);

    int updateByPrimaryKey(@Param("record") StateModel record);
    int batchUpdateByPrimaryKey(@Param("list") List<StateModel> list);
    int deleteByChannelId(@Param("channelId") String channelId);


    int selectCountByTime(@Param("channelId") String channelId,
                                  @Param("startTime") String startTime,
                                  @Param("endTime") String endTime);

    List<StateModel> selectByTime(@Param("channelId") String channelId,
                                  @Param("startTime") String startTime,
            @Param("endTime") String endTime);
    List<StateModel> selectAll(@Param("channelId") String channelId);

    void deleteByBlockHeight(@Param("channelId") String channelId, @Param("height") Long height);
}
