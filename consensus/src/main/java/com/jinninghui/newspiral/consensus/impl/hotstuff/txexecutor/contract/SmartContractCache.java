package com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor.contract;

import com.jinninghui.newspiral.common.entity.common.logmodule.LogModuleCodes;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.ledger.mgr.contract.ContractBase;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author lida
 * @date 2019/9/19 11:15
 * 缓存所有的智能合约的类及方法，仿照SOAF-RPC的ReflectCache
 */
@Slf4j
public final class SmartContractCache {


    /*----------- Class Cache ------------*/
    /**
     * String-->Class 缓存
     */
    static final ConcurrentMap<String, Class> CLASS_CACHE = new ConcurrentHashMap<String, Class>();

    /**
     * Class-->SmartContractExecutor缓存
     */
    static final ConcurrentMap<Class, ContractBase> OBJECT_CACHE = new ConcurrentHashMap<>();

    /**
     *key(Name)--->SmartContract缓存
     */
    static final ConcurrentMap<String,  Class<?>> SMART_CONTRACT_CACHE = new ConcurrentHashMap<>();

    /**
     *key(channelId+version+alisa)--->name缓存
     */
    static final ConcurrentMap<String,  String> SMART_CONTRACT_NAME_CACHE = new ConcurrentHashMap<>();

    /**
     * Class-->String 缓存
     */
    static final ConcurrentMap<Class, String> TYPE_STR_CACHE = new ConcurrentHashMap<Class, String>();

    /**
     * 放入Class缓存
     *
     * @param typeStr 对象描述
     * @param clazz   类
     */
    public static void putClassCache(String typeStr, Class clazz) {
        log.info(ModuleClassification.TxM_SCC_+"SmartContractCache.putClassCache,智能合约对象缓存中添加一个类,typeStr={},class={}",typeStr,clazz);
        CLASS_CACHE.put(typeStr, clazz);
    }

    public static void removeClassCache(String typeStr) {
        CLASS_CACHE.remove(typeStr);
    }

    public synchronized static void putObjectCache(Class clazz, ContractBase executor)
    {
        log.info(ModuleClassification.TxM_SCC_+"SmartContractCache.putObjectCache,智能合约对象缓存中添加一个对象,class={},SmartContractExecutor={}",clazz,executor);
        boolean hasFound = false;
        for (Class clz : OBJECT_CACHE.keySet()) {
            if (clazz.getCanonicalName().equals(clz.getCanonicalName())) {
                hasFound = true;
            }
        }
        if (!hasFound) {
            OBJECT_CACHE.put(clazz,executor);
        }
    }

    public synchronized static void removeObjectCache(Class clazz)
    {
            OBJECT_CACHE.remove(clazz);
    }

    public synchronized static void putSmartContactCache(String id,  Class<?> smartContract)
    {
        log.info(ModuleClassification.TxM_SCC_+"SmartContractCache.putSmartContactCache,智能合约对象缓存,roleId={},smartContract={}",id,smartContract);
        boolean hasFound = false;
        for (String key : SMART_CONTRACT_CACHE.keySet()) {
            if (key.equals(id)) {
                hasFound = true;
            }
        }
        if (!hasFound) {
            SMART_CONTRACT_CACHE.put(id,smartContract);
        }
    }

    public synchronized static void removeSmartContactCache(String id)
    {
            SMART_CONTRACT_CACHE.remove(id);
    }

    public synchronized static void putSmartContactNameCache(String id,  String name)
    {
        SMART_CONTRACT_NAME_CACHE.put(id,name);
    }

    public synchronized static void removeSmartContactNameCache(String id)
    {
        SMART_CONTRACT_NAME_CACHE.remove(id);
    }

    public static String getSmartContactName(String id)
    {
        return SMART_CONTRACT_NAME_CACHE.get(id);
    }

    public static ContractBase getObjectCache(Class clazz)
    {

        for (Class clz : OBJECT_CACHE.keySet()) {
            if (clazz.getCanonicalName().equals(clz.getCanonicalName())) {
                return OBJECT_CACHE.get(clz);
            }
        }

        //log.error(ModuleClassification.TxM_SCC_+"根据"+clazz+"无法找到对应的缓存对象,智能合约执行会失败");
        return null;
    }

    public static  Class<?> getSmartContactCache(String id)
    {

        for (String key : SMART_CONTRACT_CACHE.keySet()) {
            if (id.equals(key)) {
                return SMART_CONTRACT_CACHE.get(id);
            }
        }

        //log.error(ModuleClassification.TxM_SCC_+"根据"+memberId+"无法找到对应的缓存对象,智能合约执行会失败");
        return null;
    }


    /**
     * 得到Class缓存
     *
     * @param typeStr 对象描述
     * @return 类
     */
    public static Class getClassCache(String typeStr) {
        Class clazz = CLASS_CACHE.get(typeStr);
        if(clazz == null)
        {
            log.warn(ModuleClassification.TxM_SCC_+"SmartContractCache.getClassCache,根据输入的typeStr={},无法找到对应的类",typeStr);
        }
        return clazz;
    }


    /*----------- Method Cache NOT support overload ------------*/

    /**
     * 不支持重载的方法对象缓存 {service:{方法名:Method}}
     */
    static final ConcurrentMap<String, ConcurrentHashMap<String, Method>> NOT_OVERLOAD_METHOD_CACHE = new ConcurrentHashMap<String, ConcurrentHashMap<String, Method>>();

    /**
     * 不支持重载的方法对象参数签名缓存 {service:{方法名:对象参数签名}}
     */
    static final ConcurrentMap<String, ConcurrentHashMap<String, String[]>> NOT_OVERLOAD_METHOD_SIGS_CACHE = new ConcurrentHashMap<String, ConcurrentHashMap<String, String[]>>();

    /**
     * 往缓存里放入方法
     *
     * @param serviceName 服务名（非接口名）
     * @param method      方法
     */
    public static void putMethodCache(String serviceName, Method method) {
        ConcurrentHashMap<String, Method> cache = NOT_OVERLOAD_METHOD_CACHE.get(serviceName);
        if (cache == null) {
            cache = new ConcurrentHashMap<String, Method>();
            ConcurrentHashMap<String, Method> old = NOT_OVERLOAD_METHOD_CACHE.putIfAbsent(serviceName, cache);
            if (old != null) {
                cache = old;
            }
        }
        cache.putIfAbsent(method.getName(), method);
    }

    /**
     * 从缓存里获取方法
     *
     * @param serviceName 服务名（非接口名）
     * @param methodName  方法名
     * @return 方法
     */
    public static Method getMethodCache(String serviceName, String methodName) {
        ConcurrentHashMap<String, Method> methods = NOT_OVERLOAD_METHOD_CACHE.get(serviceName);
        return methods == null ? null : methods.get(methodName);
    }

    /**
     * 根据服务名使方法缓存失效
     *
     * @param serviceName 服务名（非接口名）
     */
    public static void invalidateMethodCache(String serviceName) {
        NOT_OVERLOAD_METHOD_CACHE.remove(serviceName);
    }

    /**
     * 往缓存里放入方法参数签名
     *
     * @param serviceName 服务名（非接口名）
     * @param methodName  方法名
     * @param argSigs     方法参数签名
     */
    public static void putMethodSigsCache(String serviceName, String methodName, String[] argSigs) {
        ConcurrentHashMap<String, String[]> cacheSigs = NOT_OVERLOAD_METHOD_SIGS_CACHE.get(serviceName);
        if (cacheSigs == null) {
            cacheSigs = new ConcurrentHashMap<String, String[]>();
            ConcurrentHashMap<String, String[]> old = NOT_OVERLOAD_METHOD_SIGS_CACHE
                    .putIfAbsent(serviceName, cacheSigs);
            if (old != null) {
                cacheSigs = old;
            }
        }
        cacheSigs.putIfAbsent(methodName, argSigs);
    }

    /**
     * 从缓存里获取方法参数签名
     *
     * @param serviceName 服务名（非接口名）
     * @param methodName  方法名
     * @return 方法参数签名
     */
    public static String[] getMethodSigsCache(String serviceName, String methodName) {
        ConcurrentHashMap<String, String[]> methods = NOT_OVERLOAD_METHOD_SIGS_CACHE.get(serviceName);
        return methods == null ? null : methods.get(methodName);
    }

    /**
     * 根据服务名使方法缓存失效
     *
     * @param serviceName 服务名（非接口名）
     */
    public static void invalidateMethodSigsCache(String serviceName) {
        NOT_OVERLOAD_METHOD_SIGS_CACHE.remove(serviceName);
    }

    /*----------- Method Cache support overload ------------*/

    /**
     * 方法对象缓存 {service:{方法名#(参数列表):Method}} <br>
     * 用于缓存参数列表，不是按接口，是按ServiceUniqueName
     */
    final static ConcurrentMap<String, ConcurrentHashMap<String, Method>> OVERLOAD_METHOD_CACHE = new ConcurrentHashMap<String, ConcurrentHashMap<String, Method>>();

    /**
     * 往缓存里放入方法
     *
     * @param serviceName 服务名（非接口名）
     * @param method      方法
     */
    public static void putOverloadMethodCache(String serviceName, Method method) {
        log.info(ModuleClassification.TxM_SCC_+"MODULE="+ LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION+",SmartContractCache.putOverloadMethodCache,智能合约对象缓存中添加一个Method,seriveName:"+serviceName+",method:"+method);
        ConcurrentHashMap<String, Method> cache = OVERLOAD_METHOD_CACHE.get(serviceName);
        if (cache == null) {
            cache = new ConcurrentHashMap<String, Method>();
            ConcurrentHashMap<String, Method> old = OVERLOAD_METHOD_CACHE.putIfAbsent(serviceName, cache);
            if (old != null) {
                cache = old;
            }
        }
        StringBuilder mSigs = new StringBuilder(128);
        mSigs.append(method.getName());
        for (Class<?> paramType : method.getParameterTypes()) {
            mSigs.append(paramType.getName());
        }
        log.info(ModuleClassification.TxM_SCC_+"MODULE="+ LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION+",SmartContractCache.putOverloadMethodCache,以"+mSigs.toString()+"为key将method:"+method+"放入OVERLOAD_METHOD_CACHE");
        cache.putIfAbsent(mSigs.toString(), method);
    }

    public static void removeMethodCache(String serviceName) {

       OVERLOAD_METHOD_CACHE.remove(serviceName);
    }

    /**
     * 从缓存里获取方法
     *
     * @param serviceName 服务名（非接口名）
     * @param methodName  方法名
     * @param methodSigs  方法描述
     * @return 方法
     */
    public static Method getOverloadMethodCache(String serviceName, String methodName, String[] methodSigs) {
        ConcurrentHashMap<String, Method> methods = OVERLOAD_METHOD_CACHE.get(serviceName);
        if (methods == null) {
            return null;
        }
        StringBuilder mSigs = new StringBuilder(128);
        mSigs.append(methodName);
        for (String methodSign : methodSigs) {
            mSigs.append(methodSign);
        }
        Method matchMethod = methods.get(mSigs.toString());
        if(matchMethod == null) {
            log.info(ModuleClassification.TxM_SCC_+"MODULE="+ LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION+",SmartContractCache.getOverloadMethodCache,智能合约对象缓存不存在对应的Method,serviceName:" + serviceName + ",methodName:" + methodName+",methodSigs:"+
                    methodSigs+",以"+mSigs+"为key在OVERLOAD_METHOD_CACHE中查找");
        }
        return matchMethod;
    }

    /**
     * 取消缓存服务的公共方法
     *
     * @param serviceName 服务名（非接口名）
     */
    public static void invalidateOverloadMethodCache(String serviceName) {
        OVERLOAD_METHOD_CACHE.remove(serviceName);
    }

    /*----------- Cache Management ------------*/

    /**
     * 清理方法
     */
    static void clearAll() {
        CLASS_CACHE.clear();
        TYPE_STR_CACHE.clear();
        NOT_OVERLOAD_METHOD_CACHE.clear();
        NOT_OVERLOAD_METHOD_SIGS_CACHE.clear();
        OVERLOAD_METHOD_CACHE.clear();


    }
}
