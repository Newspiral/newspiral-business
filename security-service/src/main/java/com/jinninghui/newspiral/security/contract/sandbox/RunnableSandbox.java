package com.jinninghui.newspiral.security.contract.sandbox;


import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import com.jinninghui.newspiral.ledger.mgr.contract.BusinessContractBase;
import com.jinninghui.newspiral.ledger.mgr.SmartContractMgr;

import com.jinninghui.newspiral.security.asm.LoopCounter;
import com.jinninghui.newspiral.security.asm.MemoryUseCounter;
import com.jinninghui.newspiral.security.contract.BusinessContractAuthChecker;
import com.jinninghui.newspiral.security.contract.ContractByteSource;
import com.jinninghui.newspiral.security.contract.BussinessContractCallerMonitor;

import com.jinninghui.newspiral.security.contract.SandboxException;
import com.jinninghui.newspiral.security.contract.SwitchableSecurityManager;

import org.springframework.context.ApplicationContext;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class RunnableSandbox extends AbstractSandbox implements Runnable {

    public RunnableSandbox(ApplicationContext applicationContext, BusinessContractAuthChecker checker, SmartContractMgr smartContractMgr,
                           String contractClassName, String methodName, Object[] args, Class<?>[] paramType, ContractByteSource codeSource, BussinessContractCallerMonitor monitor, SDKTransaction sdkTransaction) {
        this.contractName = contractClassName;
        this.codeSource = codeSource;
        this.monitor = monitor;
        this.args = args;
        this.applicationContext = applicationContext;
        this.methodName = methodName;
        this.paramType = paramType;
        this.smartContractMgr = smartContractMgr;
        this.sdkTransaction = sdkTransaction;
        this.checker = checker;
    }


    @Override
    public void run() {
        try {
            BusinessContractBase contract = initContract();
            if (null == contract) {
                monitor.uncaughtException(Thread.currentThread(), new SandboxException("contract is not exist or contract is illegal"));
                return;
            }
            //下面是合约执行
            SwitchableSecurityManager.isRestricted.set(Boolean.FALSE);
            Method method = contract.getClass().getDeclaredMethod(methodName, paramType);
            if (checker.checkAuth(method, sdkTransaction)) {
                //鉴权
                SwitchableSecurityManager.isRestricted.set(Boolean.TRUE);
                method.invoke(contract, args);
            }
        } catch (InvocationTargetException e) {
            monitor.uncaughtException(Thread.currentThread(), e.getTargetException());
        }  catch (Exception e) {
            monitor.uncaughtException(Thread.currentThread(), e);
        } finally {
            LoopCounter.clear();
            MemoryUseCounter.clear();
        }
    }


}
