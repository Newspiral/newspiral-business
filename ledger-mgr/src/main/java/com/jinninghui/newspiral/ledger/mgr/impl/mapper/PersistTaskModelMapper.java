package com.jinninghui.newspiral.ledger.mgr.impl.mapper;

import com.jinninghui.newspiral.ledger.mgr.impl.domain.PersistTaskModel;

import java.util.List;

/**
 * @version V1.0
 * @Title: PersistTaskModelMapper
 * @Package com.jinninghui.newspiral.ledger.mgr.impl.mapper
 * @Description:
 * @author: xuxm
 * @date: 2019/10/17 15:48
 */
public interface PersistTaskModelMapper {

    int insert(PersistTaskModel record);

    int updateByPrimaryKey(PersistTaskModel record);

    List<PersistTaskModel> selectAllByStatus();
}
