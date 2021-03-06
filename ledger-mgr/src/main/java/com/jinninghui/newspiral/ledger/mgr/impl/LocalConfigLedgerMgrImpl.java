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
     * ????????????????????????
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
     * ?????????????????????????????????????????????????????????????????????????????????
     * @param classLimitParam
     */
    @Override
    public void modifyClassLimitRequestParam(ClassLimitParam classLimitParam){
        Interface anInterface = classLimitParam.toInterface();
        InterfaceModel interfaceModel = InterfaceModel.createInstance(anInterface);
        interfaceMapper.updateClassLimitParam(interfaceModel);
    }

    /**
     * ?????????????????????????????????
     * @return
     */
    @Override
    public List<LimitParam> getLimitParamMethods(){
        List<InterfaceModel> interfaceModels = interfaceMapper.selectLimitParamMethods();
        List<LimitParam> limitParams = interfaceModels.parallelStream().map(interfaceModel -> interfaceModel.toLimitParam(interfaceModel)).collect(Collectors.toList());
        return limitParams;
    }

    /**
     * ????????????????????????????????????????????????????????????
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
     * ?????????????????????????????????????????????????????????
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
     * ??????????????????ip???????????????ip????????????
     * @param ipConstraintLists
     */
    public void addIpIntoIpConstraintList(List<IpConstraintList> ipConstraintLists){
        List<IpConstraintListModel> ipConstraintListModels = ipConstraintLists.parallelStream().map(ipConstraintList -> IpConstraintListModel.createInsantce(ipConstraintList)).collect(Collectors.toList());
        ipConstraintListMapper.batchInsertForIpConstraintList(ipConstraintListModels);
    }

    /**
     * ??????ip???????????????ip??????
     * @param ipConstraintLists
     */
    public void modifyIpConstraintList(List<IpConstraintList> ipConstraintLists){
        List<IpConstraintListModel> ipConstraintListModels = ipConstraintLists.parallelStream().map(ipConstraintList -> IpConstraintListModel.createInsantce(ipConstraintList)).collect(Collectors.toList());
        ipConstraintListMapper.updateForIpConstraintList(ipConstraintListModels);
    }

    /**
     * ???????????????
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
     * ??????ipAddr?????????????????????ip
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
