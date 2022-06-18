package com.jinninghui.newspiral.ledger.mgr.impl.mapper;

import com.jinninghui.newspiral.common.entity.smartcontract.QuerySmartContractListReq;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.SmartContractModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.SmartContractModelKey;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.SmartContractModelWithBLOBs;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SmartContractModelMapper {
    int deleteByPrimaryKey(SmartContractModelKey key);

    int insert(SmartContractModelWithBLOBs record);

    int insertSelective(SmartContractModelWithBLOBs record);

    SmartContractModelWithBLOBs selectByPrimaryKey(SmartContractModelKey key);

    SmartContractModelWithBLOBs selectActiveByPrimaryKey(SmartContractModelKey key);

    SmartContractModelWithBLOBs selectByName(@Param("scName") String name);

    int updateByPrimaryKeySelective(SmartContractModelWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(SmartContractModelWithBLOBs record);

    int updateByPrimaryKey(SmartContractModel record);

    List<SmartContractModelWithBLOBs> selectAll();

    List<SmartContractModelWithBLOBs> selectByChannelId(@Param("channelId")String channelId);

    int deleteByChannelId(@Param("channelId")String channelId);

    List<SmartContractModelWithBLOBs> getAllList(@Param("vo") QuerySmartContractListReq vo);

    int fetchAllCount(@Param("vo") QuerySmartContractListReq vo);

    int destructionSmartCintract(@Param("scName")String scName, @Param("scChannelId")String scChannelId);

}