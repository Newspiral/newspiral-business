package com.jinninghui.newspiral.ledger.mgr.impl.mapper;

import com.jinninghui.newspiral.common.entity.task.TimeConsuming;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.TimeConsumingModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TimeConsumingModelMapper {
    int insert(@Param("record") TimeConsumingModel messageModel);

    List<TimeConsumingModel> selectALL();

    int beachInsert(@Param("list") List<TimeConsuming> list);

}
