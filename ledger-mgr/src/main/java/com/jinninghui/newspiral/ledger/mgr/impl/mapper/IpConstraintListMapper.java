package com.jinninghui.newspiral.ledger.mgr.impl.mapper;

import com.jinninghui.newspiral.ledger.mgr.impl.domain.IpConstraintListModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IpConstraintListMapper {


    public void batchInsertForIpConstraintList(@Param("list") List<IpConstraintListModel> ipConstraintListModels);

    void updateForIpConstraintList(@Param("list") List<IpConstraintListModel> ipConstraintListModels);

    List<String> selectBlackList();

    List<String> selectByIpAddrAndConstraintTyep(@Param("ipAddr") String ipAddr, @Param("constraintType") String constraintType);

    int selectInterfaceByClassName(@Param("className") String className);

    int selectInterfaceByMethodName(@Param("methodName") String methodName);

    List<String> selectWhiteList();
}
