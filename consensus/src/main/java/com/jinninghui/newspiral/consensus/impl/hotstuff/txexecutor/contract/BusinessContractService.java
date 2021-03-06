package com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor.contract;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.exception.NewSpiralErrorEnum;
import com.jinninghui.newspiral.common.entity.exception.NewspiralException;
import com.jinninghui.newspiral.common.entity.smartcontract.QuerySmartContractReq;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContract;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContractCallInstnace;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import com.jinninghui.newspiral.ledger.mgr.LedgerMgr;
import com.jinninghui.newspiral.security.contract.BusinessContractAuthChecker;
import com.jinninghui.newspiral.security.contract.ContractByteCodeSearcher;
import com.jinninghui.newspiral.security.contract.SandBoxCache;
import com.jinninghui.newspiral.security.contract.SandboxContractMgrImpl;
import com.jinninghui.newspiral.security.contract.SandboxException;
import com.jinninghui.newspiral.security.contract.SwitchableSecurityManager;
import com.jinninghui.newspiral.security.contract.sandbox.CallableSandbox;
import com.jinninghui.newspiral.security.contract.sandbox.RunnableSandbox;
import com.jinninghui.newspiral.security.contract.sandbox.SandboxThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;

import static com.jinninghui.newspiral.common.entity.exception.NewSpiralErrorEnum.ERROR_IN_BUSINESS_CONTRACT;
import static com.jinninghui.newspiral.common.entity.exception.NewSpiralErrorEnum.ERROR_RUN_BUSINESS_CONTRACT;

@Slf4j
@Component
public class BusinessContractService {


    private ThreadLocal<DefaultContractMonitor> monitors = new ThreadLocal<>();

    private final SmartContractClassLoader smartContractClassLoader = new SmartContractClassLoader();
    /**
     * ?????????
     */
    final static int concurrentNum = 60;

    private final ExecutorService executor = Executors.newFixedThreadPool(concurrentNum);

    @Autowired
    private SandboxContractMgrImpl sandboxContractMgr;

    @SofaReference
    private LedgerMgr ledgerMgr;

    @Autowired
    ContractByteCodeSearcher searcher;

    @Autowired
    private BusinessContractAuthChecker checker;

    private static final Semaphore semaphore = new Semaphore(concurrentNum, true);

    public void invokeBusiness(ApplicationContext applicationContext, SDKTransaction sdkTransaction) throws InterruptedException {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            log.error("???????????????????????????{}???????????????????????????:", concurrentNum - semaphore.availablePermits(), e);
        }
        SmartContractCallInstnace scInstance = sdkTransaction.getSmartContractCallInstnace();
        String contractId = scInstance.getSmartContractId();
        SmartContract contractInfo = SandBoxCache.getContractInfoCache(contractId);
        if (contractInfo == null) {
            //????????????
            String[] arrs = contractId.split(",");
            String channelId = arrs[0];
            String version = arrs[1];
            String alisa = arrs[2];
            contractInfo = ledgerMgr.getActiveSmartContract(new QuerySmartContractReq(channelId, version, alisa));
            if (null == contractInfo) {
                throw new NewspiralException(ERROR_RUN_BUSINESS_CONTRACT);
            }
            SandBoxCache.addContractInfoCache(contractId, contractInfo);
        }
        String smartContractName = contractInfo.getName();
        Class<?>[] paramTypes = getArgTypes(scInstance.getMethodArgSigs());
        System.setSecurityManager(SwitchableSecurityManager.getInstance());
        DefaultContractMonitor monitor = new DefaultContractMonitor();
        monitors.set(monitor);
        sandboxContractMgr.init(TtlExecutors.getTtlExecutorService(executor), monitor);
        RunnableSandbox runnableSandbox = new RunnableSandbox(applicationContext, checker, sandboxContractMgr, smartContractName,
                scInstance.getMethodName(), scInstance.getMethodArgs(), paramTypes, searcher, monitor, sdkTransaction);
        Thread.currentThread().setContextClassLoader(smartContractClassLoader);
        SandboxThread sandboxThread = new SandboxThread(runnableSandbox, contractInfo.getAlisa());
        SandboxThread.SandboxThreadGroup group = sandboxThread.getSandboxThreadGroup();
        long tid = sandboxThread.getId();
        sandboxThread.start();
        try {
            do {
                Object waiter = new Object();
                synchronized (waiter) {
                    //??????10ms????????????????????????
                    waiter.wait(10);
                }
            } while (!runnableSandbox.isStarted);
            //??????????????????????????????3??????1???????????????????????????  660+10=670????????????2??????????????????
            boolean runOvered = group.join(0);
            if (!runOvered) {
                //???????????????????????????????????????????????????
                if (group.forceStop()) {
                    throw new SandboxException("?????????????????????????????????????????????");
                }else {
                    throw new SandboxException("???????????????????????????????????????");
                }
            }
        } finally {
            while (!group.isDestroyed()) {
                try {
                    group.destroy();
                } catch (IllegalThreadStateException ignored) {
                }
            }
            semaphore.release();
        }
        throwSandboxException(tid);
    }


    public Object invokeBusinessQuery(ApplicationContext applicationContext, SDKTransaction sdkTransaction) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            log.error("???????????????????????????{}???????????????????????????:", concurrentNum - semaphore.availablePermits(), e);
        }
        SmartContractCallInstnace scInstance = sdkTransaction.getSmartContractCallInstnace();
        String contractId = scInstance.getSmartContractId();
        SmartContract contractInfo = SandBoxCache.getContractInfoCache(contractId);
        if (contractInfo == null) {
            //????????????
            String[] arrs = contractId.split(",");
            String channelId = arrs[0];
            String version = arrs[1];
            String alisa = arrs[2];
            contractInfo = ledgerMgr.getActiveSmartContract(new QuerySmartContractReq(channelId, version, alisa));
            if (null == contractInfo) {
                throw new NewspiralException(ERROR_RUN_BUSINESS_CONTRACT);
            }
            SandBoxCache.addContractInfoCache(contractId, contractInfo);
        }
        String smartContractName = contractInfo.getName();
        Class<?>[] paramTypes = getArgTypes(scInstance.getMethodArgSigs());
        Object result = null;
        System.setSecurityManager(SwitchableSecurityManager.getInstance());
        DefaultContractMonitor monitor = new DefaultContractMonitor();
        monitors.set(monitor);
        sandboxContractMgr.init(TtlExecutors.getTtlExecutorService(executor),monitor);
        CallableSandbox callableSandbox = new CallableSandbox(applicationContext, checker, sandboxContractMgr, smartContractName,
                scInstance.getMethodName(), scInstance.getMethodArgs(), paramTypes, searcher, monitor, sdkTransaction);
        FutureTask<Object> futureTask = new FutureTask<>(callableSandbox);
        Thread.currentThread().setContextClassLoader(smartContractClassLoader);
        SandboxThread sandboxThread = new SandboxThread(futureTask, contractInfo.getAlisa());
        SandboxThread.SandboxThreadGroup group = sandboxThread.getSandboxThreadGroup();
        long tid = sandboxThread.getId();
        sandboxThread.start();
        try {
            do {
                Object waiter = new Object();
                synchronized (waiter) {
                    waiter.wait(10);
                }
            } while (!callableSandbox.isStarted);
            result = futureTask.get();
            //??????????????????????????????3??????1???????????????????????????  660+10=670????????????2??????????????????
            boolean runOvered = group.join(0);
            if (!runOvered) {
                //???????????????????????????????????????????????????
                group.forceStop();
                throw new SandboxException("??????????????????????????????????????????????????????????????????");
            }
        } catch (InterruptedException e) {
            log.error("??????????????????????????????,??????: {} ??????????????????????????????", smartContractName, e);
            throw new NewspiralException(NewSpiralErrorEnum.ERROR_STOP_BUSINESS_CONTRACT, e.getMessage());
        } catch (ExecutionException e) {
            log.error("????????????{}????????????", smartContractName, e);
            throw new NewspiralException(NewSpiralErrorEnum.ERROR_IN_BUSINESS_CONTRACT, e.getMessage());
        } finally {
            //???????????????
            while (!group.isDestroyed()) {
                try {
                    group.destroy();
                } catch (IllegalThreadStateException ignored) {
                }
            }
            semaphore.release();
        }
        throwSandboxException(tid);
        return result;
    }

    private Class<?>[] getArgTypes(String[] argSigs) {
        Class<?>[] paramTypes = new Class[argSigs.length];
        for (int i = 0; i < argSigs.length; i++) {
            try {
                paramTypes[i] = Class.forName(argSigs[i]);
            } catch (ClassNotFoundException e) {
                log.error("invokeBusinessQuery transfer argSigs occured error", e);
                throw new NewspiralException(ERROR_IN_BUSINESS_CONTRACT);
            }
        }
        return paramTypes;
    }

    private synchronized void throwSandboxException(Long sdPid) {
        Map<Long, Throwable> throwableMap = monitors.get().getThrowableMap();
        long nsPid = Thread.currentThread().getId();
        Throwable sandBoxException = throwableMap.get(sdPid);
        Throwable nsException = throwableMap.get(nsPid);
        if (sandBoxException != null) {
            throwableMap.clear();
            monitors.remove();
            throw new SandboxException(sandBoxException.getMessage(), sandBoxException);
        }else if (nsException != null) {
            throwableMap.clear();
            monitors.remove();
            throw new NewspiralException(ERROR_IN_BUSINESS_CONTRACT);
        }else {
            Collection<Throwable> values = throwableMap.values();
            if (!values.isEmpty()){
                Throwable throwable = values.stream().findFirst().get();
                throwableMap.clear();
                monitors.remove();
                throw new SandboxException(throwable.getMessage(), sandBoxException);
            }
        }
    }
}
