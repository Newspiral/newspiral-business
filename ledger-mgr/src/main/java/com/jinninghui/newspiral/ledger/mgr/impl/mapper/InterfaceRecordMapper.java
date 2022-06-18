package com.jinninghui.newspiral.ledger.mgr.impl.mapper;

import com.jinninghui.newspiral.common.entity.record.InterfaceRecord;
import com.jinninghui.newspiral.common.entity.record.InterfaceRecordBO;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.InterfaceRecordModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface InterfaceRecordMapper {
    /**
     * 批量插入接口详细调用记录
     * @param interfaceRecordModelList
     */
    void batchInsert(@Param("list")List<InterfaceRecordModel> interfaceRecordModelList);

    /**
     * 按照条件查询总数
     * @param interfaceRecordBO
     * @return
     */
    Long slectTotalCount(@Param("interfaceBo") InterfaceRecordBO interfaceRecordBO);

    /**
     * 根据条件查询总记录数
     * @param interfaceRecordBO
     * @return
     */
    List<InterfaceRecord> selectByPage(@Param("interfaceBo") InterfaceRecordBO interfaceRecordBO);

    /**
     * 删除指定日志之前的所有记录
     * @param beforeDayTime
     */
    void deleteFromDayTime(@Param("beforeDayTime") String beforeDayTime);


}
