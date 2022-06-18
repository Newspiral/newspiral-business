package com.jinninghui.newspiral.security.contract.sandbox;

import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import com.jinninghui.newspiral.ledger.mgr.SmartContractMgr;
import com.jinninghui.newspiral.ledger.mgr.contract.BusinessContractBase;
import com.jinninghui.newspiral.ledger.mgr.contract.BussinessContractConnector;
import com.jinninghui.newspiral.security.contract.BlackListItem;
import com.jinninghui.newspiral.security.contract.BusinessContractAuthChecker;
import com.jinninghui.newspiral.security.contract.ContractByteSource;
import com.jinninghui.newspiral.security.contract.BussinessContractCallerMonitor;
import com.jinninghui.newspiral.security.contract.SandBoxCache;
import com.jinninghui.newspiral.security.contract.SandboxClassLoader;
import com.jinninghui.newspiral.security.contract.SandboxException;
import com.jinninghui.newspiral.security.contract.SwitchableSecurityManager;
import com.jinninghui.newspiral.security.contract.WhiteListItem;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.stream.Collectors;

public abstract class AbstractSandbox {

    protected String contractName;
    protected ContractByteSource codeSource;
    protected BussinessContractCallerMonitor monitor = null;
    protected Object[] args;
    protected String methodName;
    protected Class<?>[] paramType;
    protected ApplicationContext applicationContext;
    protected SmartContractMgr smartContractMgr;
    protected SDKTransaction sdkTransaction;
    protected BusinessContractAuthChecker checker;



    /**
     * 是否已经启动的标志
     */
    public boolean isStarted = false;

    protected BusinessContractBase initContract() {
        if (isStarted) {
            throw new SecurityException("this SandBox is running");
        }
        isStarted = true;
        if (monitor == null) {
            throw new NullPointerException("No monitor");
        }
        //先去掉限制
        SwitchableSecurityManager.isRestricted.set(Boolean.FALSE);
        //设置沙箱异常处理
        Thread.setDefaultUncaughtExceptionHandler(monitor);
        // 沙箱白名单缓存
        WhiteListItem[] wItems = SandBoxCache.getWhiteListCache(contractName);
        BlackListItem[] blacks = SandBoxCache.getBlackListCache(contractName);
        //去数据库查询，获取到内部类
        HashMap<String, byte[]> innerMap = SandBoxCache.getContractNameCache(contractName).getInnerClassFileList();
        //HashMap<String, byte[]> innerMap = helper.addInnerClasses(contractClassName);
        //内部类不放入缓存，如果放入缓存，那么在合约吊销时，也要同时清除内部类的缓存，太麻烦。直接在启动时放入。后期要想更好的方式存放
        WhiteListItem[] inners = innerMap.keySet().stream().map(name -> new WhiteListItem(name, true))
                .collect(Collectors.toList()).toArray(new WhiteListItem[innerMap.size()]);
        //将内部类也加入到白名单
        WhiteListItem[] whites = ArrayUtils.addAll(wItems, inners);
        // 沙箱ClassLoader
        SandboxClassLoader sandboxClassLoader = new SandboxClassLoader(codeSource, whites, blacks, innerMap);
        BusinessContractBase contract = SandBoxCache.getContractClassCache(contractName);
        if (contract == null) {
            //如果缓存中没有，就加载
            try {
                Class<?> contractClass = sandboxClassLoader.loadClass(contractName);
                Constructor<?> contractConstrustor = contractClass.getConstructor(BussinessContractConnector.class);
                //初始化智能合约，调用构造函数，要启动securityManager
                SwitchableSecurityManager.isRestricted.set(Boolean.TRUE);
                contract = (BusinessContractBase) contractConstrustor.newInstance(monitor);
                contract.setSmartContractMgr(smartContractMgr);
                SwitchableSecurityManager.isRestricted.set(Boolean.FALSE);
                //加载后放入缓存
                SandBoxCache.addContractClassCache(contractName, contract);
            } catch (InvocationTargetException e) {
                monitor.uncaughtException(Thread.currentThread(), e.getTargetException());
            } catch (SandboxException e){
                monitor.uncaughtException(Thread.currentThread(), e.getCause());
            }catch (Exception e) {
                monitor.uncaughtException(Thread.currentThread(), e);
            }
        }
        return contract;
    }


}
