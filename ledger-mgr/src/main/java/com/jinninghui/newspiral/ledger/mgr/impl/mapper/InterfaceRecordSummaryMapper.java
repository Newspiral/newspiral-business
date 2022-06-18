package com.jinninghui.newspiral.ledger.mgr.impl.mapper;

import com.jinninghui.newspiral.ledger.mgr.impl.domain.InterfaceRecordSummaryModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface InterfaceRecordSummaryMapper {
    /**
     * 没有则插入，有则更新指定字段
     * @param interfaceRecordSummaryModelList
     */
    void duplicateKeyInsertAndUpdate(@Param("list") List<InterfaceRecordSummaryModel> interfaceRecordSummaryModelList);

    /**
     * 查询所有的接口调用记录
     * @return
     */
    List<InterfaceRecordSummaryModel> selectInterfaceRecordSummary();
}
