package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContract;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SmartContractModelWithBLOBs extends SmartContractModel {
    private String scSourceCode;

    private String scClassFile;

    private String scInnerClassFiles;

    public String getScSourceCode() {
        return scSourceCode;
    }

    public void setScSourceCode(String scSourceCode) {
        this.scSourceCode = scSourceCode;
    }

    public String getScClassFile() {
        return scClassFile;
    }

    public void setScClassFile(String scClassFile) {
        this.scClassFile = scClassFile;
    }

    public String getScInnerClassFiles() {
        return scInnerClassFiles;
    }

    public void setScInnerClassFiles(String scInnerClassFiles) {
        this.scInnerClassFiles = scInnerClassFiles;
    }

    public SmartContract toSmartContract() {
        SmartContract sc = new SmartContract();
        sc.setChannelId(this.getScChannelId());
       // sc.setClassFileBytes(this.scClassFile==null?"".getBytes():this.getScClassFile().getBytes(StandardCharsets.UTF_8));
        sc.setClassFileBytes(this.scClassFile==null?null:Base64.getDecoder().decode(this.getScClassFile()));
        sc.setInnerClassFileList(this.scInnerClassFiles==null?null:getInner(this.getScInnerClassFiles()));
        sc.setClassFileHash(this.getScClassHash());
        sc.setName(this.getScName());
        sc.setId(this.getScChannelId()+"_"+this.getScName()+"_"+this.getScVersion());
        sc.setSourceContent(this.getScSourceCode());
        sc.setVersion(this.getScVersion());
        sc.setFlag(this.getFlag());
        if(!StringUtils.isEmpty(this.getExtendedData())) {
            sc.setExtendedData(JSON.parseObject(this.getExtendedData(), Map.class));
        }
/*        if(!StringUtils.isEmpty(this.getAuthorizedMember())) {
            sc.setAuthorizedMember(JSON.parseArray(this.getAuthorizedMember(), String.class));
        }
        if(!StringUtils.isEmpty(this.getAuthorizedRole())) {
            sc.setAuthorizedRole(JSON.parseArray(this.getAuthorizedRole(), String.class));
        }*/
        sc.setAlisa(this.getAlisa());
        sc.setSetupTimestamp(this.getSetupTimestamp());
        sc.setUpdateTime(this.getUpdateTime());
        return sc;
    }

    public static SmartContractModelWithBLOBs createInstance(SmartContract sc)
    {
        SmartContractModelWithBLOBs model = new SmartContractModelWithBLOBs();
        model.setScChannelId(sc.getChannelId());
        String str = "";
/*        try {
            str = new String(sc.getClassFileBytes(), "UTF-8");
        } catch (Exception ex) {

        }*/
        str=Base64.getEncoder().encodeToString(sc.getClassFileBytes());
        model.setScClassFile(str);
        model.setScInnerClassFiles(getInnerJsonStr(sc.getInnerClassFileList()));
        model.setScClassHash(sc.getClassFileHash());
        model.setScName(sc.getName());
        model.setScSourceCode(sc.getSourceContent());
        model.setScVersion(sc.getVersion());
        model.setSetupTimestamp(new Date());
        model.setUpdateTime(new Date());
        model.setFlag(sc.getFlag());
        if(!CollectionUtils.isEmpty(sc.getExtendedData())) {
            model.setExtendedData(JSON.toJSONString(sc.getExtendedData()));
        }
/*        if(!CollectionUtils.isEmpty(sc.getAuthorizedMember())) {
            model.setAuthorizedMember(JSON.toJSONString(sc.getAuthorizedMember()));
        }
        if(!CollectionUtils.isEmpty(sc.getAuthorizedRole())) {
            model.setAuthorizedRole(JSON.toJSONString(sc.getAuthorizedRole()));
        }*/
           model.setAlisa(sc.getAlisa());
        return model;
    }

    private  HashMap<String, byte[]> getInner(String scInnerClassFiles){
        HashMap<String, byte[]> map = new HashMap<String, byte[]>();
        HashMap<String, String> hashMap = JSONObject.parseObject(scInnerClassFiles, HashMap.class);
        for (Map.Entry<String,String> entry:hashMap.entrySet()){
            map.put(entry.getKey(),Base64.getDecoder().decode(entry.getValue()));
        }
        return map;
    }

    private  static String getInnerJsonStr(HashMap<String,byte[]> map){
        HashMap<String, String> strMap = new HashMap<>();
        for (Map.Entry<String,byte[]> entry:map.entrySet()){
            strMap.put(entry.getKey(),Base64.getEncoder().encodeToString(entry.getValue()));
        }
        return JSONObject.toJSONString(strMap);
    }
}