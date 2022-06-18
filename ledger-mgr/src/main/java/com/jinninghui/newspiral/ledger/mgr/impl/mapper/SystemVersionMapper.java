package com.jinninghui.newspiral.ledger.mgr.impl.mapper;

import com.jinninghui.newspiral.ledger.mgr.impl.domain.SystemVersionModel;

import java.util.List;

public interface SystemVersionMapper {

    List<SystemVersionModel> selectAll();

    SystemVersionModel selectMaxId();

}

