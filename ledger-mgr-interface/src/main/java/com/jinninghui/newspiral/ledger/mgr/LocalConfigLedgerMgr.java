package com.jinninghui.newspiral.ledger.mgr;

import com.jinninghui.newspiral.common.entity.config.LocalConfig;
import com.jinninghui.newspiral.common.entity.ddos.IpConstraintList;
import com.jinninghui.newspiral.common.entity.ddos.LimitParam;
import com.jinninghui.newspiral.common.entity.ddos.ClassLimitParam;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于本地配置的账本模块
 */
public interface LocalConfigLedgerMgr {

    Map<String,String> queryAll();

    Map<String,String> queryByType(String type);

    List<LocalConfig> queryConfigByType(String type);

    boolean addConfig(LocalConfig config);

    boolean modifyConfig(LocalConfig config);

    String queryByKey(String key);

    boolean addConfigs(List<LocalConfig> configs);

    List<Long> queryMaxBlockId();

    int queryConfigValue(String value);

    HashMap<String,HashMap> queryChannelMaxBlockId();

    void modifyLimitRequestParam(List<LimitParam> limitParams);

    void modifyClassLimitRequestParam(ClassLimitParam classLimitParam);

    List<LimitParam> getLimitParamMethods();

    List<LimitParam> getLimitParamMethodByClassName(String className);

    LimitParam getLimitParamMethodByClassNameAndMethodName(String className,String methodName);

    void addIpIntoIpConstraintList(List<IpConstraintList> ipConstraintLists);

    void modifyIpConstraintList(List<IpConstraintList> ipConstraintLists);

    List<String> getBlackList();

    List<String> getWhiteList();

    List<String> getIpsByIpAddrAndConstraintTyep(String ipAddr,String constraintType);

    int getInterfaceByClassName(String className);

    int getInterfaceByMethodName(String methodName);


}
