package com.jinninghui.newspiral.ledger.mgr.impl.mapper;

import com.jinninghui.newspiral.ledger.mgr.impl.domain.IdentityModel;

import java.util.List;

public interface IdentityModelMapper {
    int deleteByPrimaryKey(String identityId);

    int insert(IdentityModel record);

    int insertSelective(IdentityModel record);

    IdentityModel selectByPrimaryKey(String identityId);

    List<IdentityModel> selectAll();

    int updateByPrimaryKeySelective(IdentityModel record);

    int updateByPrimaryKey(IdentityModel record);
}