package com.jinninghui.newspiral.ledger.mgr.impl.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.jinninghui.newspiral.common.entity.state.StateHistoryBO;
import com.jinninghui.newspiral.common.entity.state.StateHistoryModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StateHistoryModelMapper {

    @DS("sharding")
    Long selectTotalCount(@Param("stateHistoryBO") StateHistoryBO stateHistoryBO);

    @DS("sharding")
    List<StateHistoryModel> selectPageTransactionList(@Param("stateHistoryBO") StateHistoryBO stateHistoryBO);

    @DS("sharding")
    void deleteLatestBlockData(@Param("channelId") String channelId);

    @DS("sharding")
    Long selectLatestBlockId(@Param("channelId") String channelId);

    @DS("sharding")
    void batchInsert(@Param("list") List<StateHistoryModel> batchInsertList);

    @DS("sharding")
    Long selectLatestInsertVersion();
}
