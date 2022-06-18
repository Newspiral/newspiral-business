package com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor;

import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.common.logmodule.LogModuleCodes;
import com.jinninghui.newspiral.common.entity.exception.NewSpiralErrorEnum;
import com.jinninghui.newspiral.common.entity.exception.NewspiralException;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContract;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContractCallInstnace;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.common.entity.transaction.ExecutedTransaction;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor.contract.BusinessContractService;
import com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor.contract.SmartContractCache;
import com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor.contract.SmartContractClassLoader;
import com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor.contract.SystemContractService;
import com.jinninghui.newspiral.ledger.mgr.LedgerMgr;
import com.jinninghui.newspiral.ledger.mgr.LedgerThreadLocalContext;
import com.jinninghui.newspiral.ledger.mgr.MemberLedgerMgr;
import com.jinninghui.newspiral.ledger.mgr.contract.BusinessContractBase;
import com.jinninghui.newspiral.ledger.mgr.SmartContractMgr;
import com.jinninghui.newspiral.ledger.mgr.StateAccessModeEnum;
import com.jinninghui.newspiral.security.contract.SandboxException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


/**
 * @author lida
 * @date 2019/7/22 16:47
 */
@Slf4j
@Component
public class TxExecutor implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @SofaReference
    private LedgerMgr ledgerMgr;
    @SofaReference
    private MemberLedgerMgr memberLedgerMgr;

    @SofaReference
    private SmartContractMgr smartContractMgr;

    @Autowired
    private TxExecutor txExecutor;

    @Autowired
    SystemContractService systemContractService;

    @Autowired
    BusinessContractService businessContractService;


    private static final String PASSED = "1";
    private static final String NO_PASSED = "0";

    //private SmartContractClassLoader smartContractClassLoader = new SmartContractClassLoader();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    void init() {
        log.info(ModuleClassification.TxM_TxE_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",init,start");
        try {
            List<Channel> channelList = this.ledgerMgr.readAllChannels();
            if (channelList.size() > 100) {
                String msg = "本节点加入了" + channelList.size() + "个Channel，这个是不允许的";
                log.error(ModuleClassification.TxM_TxE_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",init,error={}", msg);
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, msg);
            } /*else {
                for (Channel channel : channelList) {
                    log.info(ModuleClassification.TxM_TxE_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channel.getChannelId() + ",init,为通道channleId={}加载智能合约", channel.getChannelId());
                    this.loadSmartContractList(channel.getSmartContractList());
                }
            }*/
            //加载系统智能合约
            log.info(ModuleClassification.TxM_TxE_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",init,加载系统智能合约");
            cacheClassAndMethodAndObject(SystemSmartContract.class);
            //换成Spring控制
            SystemSmartContract systemSmartContract = applicationContext.getBean(SystemSmartContract.class);
            systemSmartContract.setLedgerMgr(this.ledgerMgr);
            systemSmartContract.setMemberLedgerMgr(this.memberLedgerMgr);
            SmartContractCache.putObjectCache(SystemSmartContract.class, systemSmartContract);

        } catch (Exception ex) {
            log.error(ModuleClassification.TxM_TxE_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",init,error={},退出系统", ex.getMessage(), ex);
            System.exit(1);
        }
    }

    private void cacheClassAndMethodAndObject(Class clazz) {
        SmartContractCache.putClassCache(clazz.getTypeName(), clazz);
        for (Method method : clazz.getMethods()) {
            SmartContractCache.putOverloadMethodCache(clazz.getTypeName(), method);
        }

    }

    private void removeClassAndMethodAndObject(Class clazz) {
        SmartContractCache.removeClassCache(clazz.getTypeName());
        SmartContractCache.removeMethodCache(clazz.getTypeName());

    }

    private void loadSmartContract(SmartContract sc) throws ClassNotFoundException {
        Class<?> existClass = SmartContractCache.getSmartContactCache(sc.getName());
        if (sc.getFlag().equals("3")) {
            if (existClass == null) {
                log.warn(ModuleClassification.TxM_TxE_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + "," + sc.getChannelId() + ",loadSmartContract,该智能合约实例已经被销毁,智能合约：{}", sc);
                return;
            }
            //内存中移除
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
            //Class<?> clazz = smartContractClassLoader.findClass(sc.getName());
            BusinessContractBase instance = (BusinessContractBase) applicationContext.getBean(existClass);
            instance.setSmartContractMgr(smartContractMgr);
            removeClassAndMethodAndObject(instance.getClass());
            SmartContractCache.removeObjectCache(instance.getClass());
            SmartContractCache.removeSmartContactCache(sc.getName());
            SmartContractCache.removeSmartContactNameCache(sc.getChannelId() + sc.getVersion() + sc.getAlisa());
            beanFactory.removeBeanDefinition(existClass.getName());

        } else {
            if (existClass != null) {
                log.warn(ModuleClassification.TxM_TxE_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + "," + sc.getChannelId() + ",loadSmartContract,该智能合约实例已经被加载,智能合约={}", sc);
                return;
            }
            Class<?> clazz = new SmartContractClassLoader().defineClass(sc);
            if (null == clazz || SmartContractCache.getObjectCache(clazz) != null) {
                log.warn(ModuleClassification.TxM_TxE_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + "," + sc.getChannelId() + ",loadSmartContract,该智能合约实例已经被加载或者该智能合约格式加载有问题，智能合约={}", sc);
                return;
            }
            //Object instance = clazz.getConstructor().newInstance();
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
            beanFactory.registerBeanDefinition(clazz.getName(), beanDefinitionBuilder.getBeanDefinition());
            BusinessContractBase instance = (BusinessContractBase) applicationContext.getBean(clazz);
            instance.setSmartContractMgr(smartContractMgr);
            cacheClassAndMethodAndObject(instance.getClass());
            SmartContractCache.putObjectCache(instance.getClass(), instance);
            SmartContractCache.putSmartContactCache(sc.getName(), clazz);
            SmartContractCache.putSmartContactNameCache(sc.getChannelId() + sc.getVersion() + sc.getAlisa(), sc.getName());
        }
    }


    /**
     * 加载新的业务智能合约列表
     *
     * @param smartContractList
     */
    public void loadSmartContractList(List<SmartContract> smartContractList) {
        if (CollectionUtils.isEmpty(smartContractList)) {
            log.warn(ModuleClassification.TxM_TxE_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",loadSmartContractList,没有加载到智能合约列表");
            return;
        }
        //排序，吊销的放在上面
        smartContractList = smartContractList.stream().sorted(Comparator.comparing(SmartContract::getFlag).reversed()).collect(Collectors.toList());
        for (SmartContract sc : smartContractList) {//TODO 这里防御性编程，可以再增加一些检查
            try {
                loadSmartContract(sc);
            } catch (Exception ex) {
            }
        }
    }

    /**
     * 执行一个交易trans，并将执行得到的ExecutedTranaction加入到executedTransactionList中
     *
     * @param
     * @param trans
     */
    protected ExecutedTransaction executeTrans(SDKTransaction trans) {
        if (LedgerThreadLocalContext.currChannelId.get() == null) {//当前实现下，其实放到Consensus模块初始化中更合适，
            // 但是考虑到后续的DAG并行交易执行，交易执行线程与共识线程并不共用，因此先写成这样。
            LedgerThreadLocalContext.currChannelId.set(trans.getChannelId());
        }

        long startTime = System.currentTimeMillis();
        ExecutedTransaction executedTx = null;
        try {
            //执行
            txExecutor.invoke(trans);
            //成功
            executedTx = buildExecutedTx(trans, startTime, "Success", PASSED);
            Set<String> excuteWorldStatekeyList = new HashSet<>(LedgerThreadLocalContext.getExecuteWorldStateKeyList().get());
            executedTx.setModifiedWorldStateKeyList(excuteWorldStatekeyList);
            executedTx.addWorldStateModifiedRecords(LedgerThreadLocalContext.getCurrTransModifiedStateMap().get().values());
            executedTx.addChannelModifiedRecords(LedgerThreadLocalContext.getCurrTransModifiedChannelsQueue().get());
            return executedTx;
        } catch (NewspiralException ex) {
            log.error(ModuleClassification.TxM_TxE_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + "," + trans.getChannelId() + ",executeTrans,执行智能合约失败,该交易的执行记录会同样加入到区块中,其状态为执行失败,transaction=" +
                    JSONObject.toJSON(trans) + "exception:", ex);
            executedTx = buildExecutedTx(trans, startTime, ex.getErrorMsg(), NO_PASSED);
            return executedTx;
        }catch (SandboxException ex){
            log.error(ModuleClassification.TxM_TxE_ + "TError" + "MODULE=" + LogModuleCodes.BUSINESS_SMART_CONTRACT_ACTION + "," + trans.getChannelId() + ",executeTrans,执行智能合约失败,该交易的执行记录会同样加入到区块中,其状态为执行失败,transaction=" +
                    JSONObject.toJSON(trans) + "exception:", ex);
            //log.error("sandbox occured error, reason is Contract code threw exception:",ex);
            executedTx = buildExecutedTx(trans, startTime, ex.getMessage(), NO_PASSED);
            return executedTx;
        } catch (Exception ex) {
            log.error("MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + "," + trans.getChannelId() + ",TxExecutor.executeTrans,执行智能合约失败,该交易的执行记录会同样加入到区块中,其状态为执行失败,transaction:" +
                    JSONObject.toJSON(trans) + "\n Exception:", ex);
            executedTx = buildExecutedTx(trans, startTime, "智能合约执行时错误", NO_PASSED);
            return executedTx;
        } finally {
            //执行完毕后要清理
            LedgerThreadLocalContext.getExecuteWorldStateKeyList().get().clear();
            LedgerThreadLocalContext.getCurrTransModifiedStateMap().get().clear();
            //LedgerThreadLocalContext.currTransModifiedChannel.set(null);
            LedgerThreadLocalContext.currTransModifiedChannels.get().clear();
        }
    }

    /**
     * 用于不同的执行结果构建对应的ExecutedTransaction
     *
     * @param trans     交易
     * @param startTime 开始时间
     * @param msg       执行结果描述
     * @param pass      是否通过
     * @return ExecutedTransaction
     */
    public ExecutedTransaction buildExecutedTx(SDKTransaction trans, long startTime, String msg, String pass) {
        ExecutedTransaction executedTransaction = new ExecutedTransaction();
        executedTransaction.setExecutedMs(System.currentTimeMillis() - startTime);
        executedTransaction.setExecuteTimestamp(startTime);
        executedTransaction.setSdkTransaction(trans);
        executedTransaction.setErrorMsg(msg);
        executedTransaction.setPass(pass);
        return executedTransaction;
    }

    protected Object invokeQuery(SDKTransaction sdkTransaction) throws InvocationTargetException, IllegalAccessException {
        SmartContractCallInstnace scInstance = sdkTransaction.getSmartContractCallInstnace();
        if (scInstance.getSmartContractId().contains(",")) {
            //业务合约
            return businessContractService.invokeBusinessQuery(applicationContext, sdkTransaction);
        } else {
            //系统合约
            return systemContractService.invokeSystemQuery(sdkTransaction);
        }
    }

    protected void invoke(SDKTransaction sdkTransaction) throws InvocationTargetException, IllegalAccessException, InterruptedException, ExecutionException {
        SmartContractCallInstnace scInstance = sdkTransaction.getSmartContractCallInstnace();
        if (scInstance.getSmartContractId().contains(",")) {
            //业务合约
            businessContractService.invokeBusiness(applicationContext, sdkTransaction);
        } else {
            //系统合约
            systemContractService.invokeSystem(sdkTransaction);
        }
    }

    /**
     * 执行查询类智能合约交易调用
     *
     * @param trans
     * @return
     */
    public Object executeQueryTransaction(SDKTransaction trans) {
        log.info(ModuleClassification.TxM_TxE_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + "," + trans.getChannelId() + ",executeQueryTransaction,start");
        try {
            LedgerThreadLocalContext.stateAccessMode.set(StateAccessModeEnum.QUERY_TRANS_EXXECUTE);
            LedgerThreadLocalContext.currChannelId.set(trans.getChannelId());
            return txExecutor.invokeQuery(trans);
        } catch (NewspiralException ex) {
            log.error(ModuleClassification.TxM_TxE_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + "," + trans.getChannelId() + ",executeQueryTransaction,error={}", ex.getErrorCode(), ex);
            throw ex;
        }catch (SandboxException ex){
            throw new NewspiralException(NewSpiralErrorEnum.PROCESS_ERROR, ex);
        } catch (Exception ex) {
            log.error(ModuleClassification.TxM_TxE_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + "," + trans.getChannelId() + ",executeQueryTransaction,error={}", ex.getMessage(), ex);
            throw new NewspiralException(NewSpiralErrorEnum.PROCESS_ERROR, ex.getMessage());

        } finally {
            LedgerThreadLocalContext.stateAccessMode.set(StateAccessModeEnum.UNDEFINED);
            LedgerThreadLocalContext.currChannelId.set(null);
            log.info(ModuleClassification.TxM_TxE_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + "," + trans.getChannelId() + ",executeQueryTransaction,end");
        }
    }

    /*public void updateSmartContract(List<SmartContract> smartContracts) {
        //排序，吊销的放在上面
        smartContracts = smartContracts.stream().sorted(Comparator.comparing(SmartContract::getFlag).reversed()).collect(Collectors.toList());
        for (SmartContract smartContract : smartContracts) {
            try {
                loadSmartContract(smartContract);
            } catch (Exception ex) {
                log.error(ModuleClassification.TxM_TxE_ + "TError" + "," + smartContract.getChannelId() + "updateSmartContract,{} error={}", NewSpiralErrorEnum.SMART_CONTRACT_LOAD_ERROR, ex);
                //throw new NewspiralException(NewSpiralErrorEnum.SMART_CONTRACT_LOAD_ERROR);
            }
        }
    }*/


}
