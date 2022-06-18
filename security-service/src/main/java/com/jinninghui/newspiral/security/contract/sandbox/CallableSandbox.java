package com.jinninghui.newspiral.security.contract.sandbox;

import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import com.jinninghui.newspiral.ledger.mgr.SmartContractMgr;
import com.jinninghui.newspiral.ledger.mgr.contract.BusinessContractBase;
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
import java.util.concurrent.Callable;

public class CallableSandbox extends AbstractSandbox implements Callable<Object> {

    public CallableSandbox(ApplicationContext applicationContext, BusinessContractAuthChecker checker, SmartContractMgr smartContractMgr, String contractClassName, String methodName, Object[] args, Class<?>[] paramType, ContractByteSource codeSource, BussinessContractCallerMonitor monitor, SDKTransaction sdkTransaction) {
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
    public Object call() throws Exception {
        Object result = null;
        //下面是合约执行
        try {
            BusinessContractBase contract = initContract();
            if (null == contract) {
                monitor.uncaughtException(Thread.currentThread(), new SandboxException("contract is not exist or contract is illegal"));
                return result;
            }
            SwitchableSecurityManager.isRestricted.set(Boolean.FALSE);
            Method method = contract.getClass().getDeclaredMethod(methodName, paramType);
            if (checker.checkAuth(method, sdkTransaction)) {
                //鉴权
                SwitchableSecurityManager.isRestricted.set(Boolean.TRUE);
                result = method.invoke(contract, args);
            }
        } catch (InvocationTargetException e) {
            monitor.uncaughtException(Thread.currentThread(), e.getTargetException());
        } catch (Exception e) {
            monitor.uncaughtException(Thread.currentThread(), e);
        } finally {
            LoopCounter.clear();
            MemoryUseCounter.clear();
        }
        return result;
    }

}
