package com.jinninghui.newspiral.security.contract;

import com.jinninghui.newspiral.common.entity.smartcontract.SmartContract;
import com.jinninghui.newspiral.ledger.mgr.contract.BusinessContractBase;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class SandBoxCache implements SandBoxCacheMgr {

    private static final String pubKey = "public";

    private static String whiteList = getPropertiesKey("security.sandbox.whitelist");

    private static String blackList = getPropertiesKey("security.sandbox.blacklist");


    private static Properties properties;


    public static String getPropertiesKey(String key){
        if (properties==null) {
            properties = new Properties();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try {
                properties.load(loader.getResourceAsStream("application.properties"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return properties.getProperty(key, "");
    }



    //白名单缓存

    private static ConcurrentHashMap<String, WhiteListItem[]> whiteListCache = new ConcurrentHashMap() {{
        put(pubKey, Arrays.stream(whiteList.split(",")).map(whiteItem->new WhiteListItem(whiteItem,false)).toArray(WhiteListItem[]::new));
    }};


    private static ConcurrentHashMap<String, BlackListItem[]> blackListCache = new ConcurrentHashMap() {{
        put(pubKey, Arrays.stream(blackList.split(",")).map(BlackListItem::new).toArray(BlackListItem[]::new));
    }};

    //已加载合约类缓存

    private static ConcurrentHashMap<String, BusinessContractBase> contractClassCache = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, SmartContract> contractInfoCache = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, SmartContract> contractNameCache = new ConcurrentHashMap<>();

    public static void addWhiteListCache(String contractName) {
        whiteListCache.put(contractName, new WhiteListItem[]{
                new WhiteListItem(contractName, true)
        });
    }

    public static void addWhiteListCache(Collection<String> contractNames) {
        contractNames.forEach(SandBoxCache::addWhiteListCache);
    }

    public static WhiteListItem[] getWhiteListCache(String contractName) {
        WhiteListItem[] pubItems = whiteListCache.get(pubKey);
        SandBoxCache.addWhiteListCache(contractName);
        WhiteListItem[] contractItem = whiteListCache.get(contractName);
        return ArrayUtils.addAll(pubItems, contractItem);
    }

    public static BlackListItem[] getBlackListCache(String contractName) {
        BlackListItem[] pubItems = blackListCache.get(pubKey);
        return pubItems;
    }


    public static void addContractClassCache(String contractName, BusinessContractBase contractBase) {
        contractClassCache.put(contractName, contractBase);
    }

    public static void addContractInfoCache(String contractId, SmartContract info) {
        contractInfoCache.put(contractId, info);
        contractNameCache.put(info.getName(), info);
    }

    public static SmartContract getContractNameCache(String contractName) {
        return contractNameCache.get(contractName);
    }

    public static BusinessContractBase getContractClassCache(String contractName) {
        return contractClassCache.get(contractName);
    }

    public static SmartContract getContractInfoCache(String contractId) {
        return contractInfoCache.get(contractId);
    }


    public void removeBusinessContractCache(String contractId, String contractName) {
        contractClassCache.remove(contractName);
        whiteListCache.remove(contractName);
        contractNameCache.remove(contractName);
        contractInfoCache.remove(contractId);
    }
}
