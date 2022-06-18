package com.jinninghui.newspiral.ledger.mgr.impl.mapper;

import com.jinninghui.newspiral.ledger.mgr.impl.domain.StateAttachModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StateAttachModelMapper {
    int batchInsert(@Param("list") List<StateAttachModel> list);
    int batchUpdateByPrimaryKey(@Param("list") List<StateAttachModel> list);
    int batchDeleteByPrimaryKey(@Param("list") List<StateAttachModel> list);
}
