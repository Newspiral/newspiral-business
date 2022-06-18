package com.jinninghui.newspiral.ledger.mgr.impl.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.LocalConfigModel;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface LocalConfigModelMapper {

    String selectByKey(@Param("key") String key);

    List<LocalConfigModel> selectByType(@Param("type") String type);

    @MapKey("key")
    Map<String,LocalConfigModel> selectLikeKey(@Param("key") String key);

    List<LocalConfigModel> selectAll();

    int insert(@Param("record") LocalConfigModel model);

    int update(@Param("record") LocalConfigModel model);

    int insertBatch(@Param("records") List<LocalConfigModel> modelList);

    int queryJdbcCount(@Param("value") String value);

    @DS("sharding")
    @MapKey("CHANNEL_ID")
    HashMap<String,HashMap> queryChannelMaxBlockId();


}
