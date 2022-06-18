package com.jinninghui.newspiral.ledger.mgr.impl;

import com.jinninghui.newspiral.common.entity.config.LocalConfig;
import com.jinninghui.newspiral.common.entity.ddos.Interface;
import com.jinninghui.newspiral.common.entity.ddos.IpConstraintList;
import com.jinninghui.newspiral.common.entity.ddos.LimitParam;
import com.jinninghui.newspiral.common.entity.ddos.ClassLimitParam;
import com.jinninghui.newspiral.ledger.mgr.LocalConfigLedgerMgr;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.InterfaceModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.IpConstraintListModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.LocalConfigModel;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.BlockModelMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.InterfaceMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.InterfaceRecordMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.IpConstraintListMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.LocalConfigModelMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LocalConfigLedgerMgrImpl implements LocalConfigLedgerMgr {

    @Autowired
    private LocalConfigModelMapper configModelMapper;

    @Autowired
    private BlockModelMapper blockModelMapper;

    @Autowired
    private InterfaceRecordMapper interfaceRecordMapper;

    @Autowired
    private InterfaceMapper interfaceMapper;

    @Autowired
    private IpConstraintListMapper ipConstraintListMapper;

    @Override
    public Map<String, String> queryAll() {
        List<LocalConfigModel> localConfigModels = configModelMapper.selectAll();
        HashMap<String, String> map = new HashMap<>();
        localConfigModels.forEach(config -> map.put(config.getKey(), config.getValue()));
        return map;
    }

    @Override
    public Map<String, String> queryByType(String type) {
        List<LocalConfigModel> localConfigModels = configModelMapper.selectByType(type);
        HashMap<String, String> map = new HashMap<>();
        localConfigModels.forEach(config -> map.put(config.getKey(), config.getValue()));
        return map;
    }

    @Override
    public List<LocalConfig> queryConfigByType(String type) {
        List<LocalConfigModel> localConfigModels = configModelMapper.selectByType(type);
        return localConfigModels.stream().map(LocalConfigModel::toLocalConfig).collect(Collectors.toList());
    }

    @Override
    public boolean addConfig(LocalConfig config) {
        LocalConfigModel instance = LocalConfigModel.createInstance(config);
        return configModelMapper.insert(instance) > 0;
    }

    @Override
    public boolean modifyConfig(LocalConfig config) {
        LocalConfigModel instance = LocalConfigModel.createInstance(config);
        return configModelMapper.update(instance) > 0;
    }

    @Override
    public String queryByKey(String key) {
        return configModelMapper.selectByKey(key);
    }

    @Override
    public boolean addConfigs(List<LocalConfig> configs) {
        List<LocalConfigModel> models = configs.parallelStream().map(LocalConfigModel::createInstance).collect(Collectors.toList());
        return configModelMapper.insertBatch(models) == models.size();
    }

    public List<Long> queryMaxBlockId(){
        return blockModelMapper.queryMaxBlockId();
    }

    @Override
    public int queryConfigValue(String value) {
        return configModelMapper.queryJdbcCount(value);
    }

    public HashMap<String, HashMap> queryChannelMaxBlockId() {
        return configModelMapper.queryChannelMaxBlockId();
    }

    /**
     * 批量修改限流参数
     * @param limitParams
     */
    @Override
    public void modifyLimitRequestParam(List<LimitParam> limitParams){
        List<Interface> interfaces = limitParams.parallelStream().map(limitParam -> limitParam.toInterface()).collect(Collectors.toList());
        List<InterfaceModel> interfaceModels = new ArrayList<>();
        for (Interface anInterface : interfaces) {
            InterfaceModel interfaceModel = InterfaceModel.createInstance(anInterface);
            interfaceModels.add(interfaceModel);
        }
        interfaceMapper.batchInsertForLimitParam(interfaceModels);
    }

    /**
     * 按照接口所在类修改同一个类下所有需要限流接口的限流参数
     * @param classLimitParam
     */
    @Override
    public void modifyClassLimitRequestParam(ClassLimitParam classLimitParam){
        Interface anInterface = classLimitParam.toInterface();
        InterfaceModel interfaceModel = InterfaceModel.createInstance(anInterface);
        interfaceMapper.updateClassLimitParam(interfaceModel);
    }

    /**
     * 拿到需要限流的所有接口
     * @return
     */
    @Override
    public List<LimitParam> getLimitParamMethods(){
        List<InterfaceModel> interfaceModels = interfaceMapper.selectLimitParamMethods();
        List<LimitParam> limitParams = interfaceModels.parallelStream().map(interfaceModel -> interfaceModel.toLimitParam(interfaceModel)).collect(Collectors.toList());
        return limitParams;
    }

    /**
     * 根据类名查找该类下所有需要进行限流的方法
     * @param className
     * @return
     */
    @Override
    public List<LimitParam> getLimitParamMethodByClassName(String className){
        List<InterfaceModel> interfaceModels = interfaceMapper.selectLimitParamMethodByClassName(className);
        List<LimitParam> limitParams = interfaceModels.parallelStream().map(interfaceModel -> interfaceModel.toLimitParam(interfaceModel)).collect(Collectors.toList());
        return limitParams;
    }

    /**
     * 根据类名和方法名查找令牌桶的初始化参数
     * @param className
     * @param methodName
     * @return
     */
    public LimitParam getLimitParamMethodByClassNameAndMethodName(String className,String methodName){
        InterfaceModel interfaceModel = interfaceMapper.selectLimitParamMethodByClassNameAndMethodName(className,methodName);
        LimitParam limitParam = interfaceModel.toLimitParam(interfaceModel);
        return limitParam;
    }

    /**
     * 将限制操作的ip信息添加到ip限制表中
     * @param ipConstraintLists
     */
    public void addIpIntoIpConstraintList(List<IpConstraintList> ipConstraintLists){
        List<IpConstraintListModel> ipConstraintListModels = ipConstraintLists.parallelStream().map(ipConstraintList -> IpConstraintListModel.createInsantce(ipConstraintList)).collect(Collectors.toList());
        ipConstraintListMapper.batchInsertForIpConstraintList(ipConstraintListModels);
    }

    /**
     * 修改ip限制表中的ip参数
     * @param ipConstraintLists
     */
    public void modifyIpConstraintList(List<IpConstraintList> ipConstraintLists){
        List<IpConstraintListModel> ipConstraintListModels = ipConstraintLists.parallelStream().map(ipConstraintList -> IpConstraintListModel.createInsantce(ipConstraintList)).collect(Collectors.toList());
        ipConstraintListMapper.updateForIpConstraintList(ipConstraintListModels);
    }

    /**
     * 获取黑名单
     * @return
     */
    public List<String> getBlackList(){
        List<String> blackList = ipConstraintListMapper.selectBlackList();
        return blackList;
    }

    public List<String> getWhiteList(){
        List<String> whiteList = ipConstraintListMapper.selectWhiteList();
        return whiteList;
    }

    /**
     * 根据ipAddr和操作类型查询ip
     * @param ipAddr
     * @param constraintType
     * @return
     */
    public List<String> getIpsByIpAddrAndConstraintTyep(String ipAddr,String constraintType){
        List<String> ips = ipConstraintListMapper.selectByIpAddrAndConstraintTyep(ipAddr,constraintType);
        return ips;
    }

    public int getInterfaceByClassName(String className){
        return ipConstraintListMapper.selectInterfaceByClassName(className);
    }


    public int getInterfaceByMethodName(String methodName){
        return ipConstraintListMapper.selectInterfaceByMethodName(methodName);
    }

}
