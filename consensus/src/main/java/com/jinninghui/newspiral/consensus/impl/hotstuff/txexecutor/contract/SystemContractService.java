package com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor.contract;


import com.jinninghui.newspiral.common.entity.common.logmodule.LogModuleCodes;
import com.jinninghui.newspiral.common.entity.exception.NewSpiralErrorEnum;
import com.jinninghui.newspiral.common.entity.exception.NewspiralException;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContractCallInstnace;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
@Component
public class SystemContractService {

    public void invokeSystem(SDKTransaction sdkTransaction) {
        SmartContractCallInstnace scInstance = sdkTransaction.getSmartContractCallInstnace();
        String smartContractName = scInstance.getSmartContractId();
        Method method = SmartContractCache.getOverloadMethodCache(smartContractName, scInstance.getMethodName(), scInstance.getMethodArgSigs());
        if (method == null) {
            log.warn("MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + "," + sdkTransaction.getChannelId() + ",TxExecutor.invoke,无法为交易获得对应的智能合约方法,sdkTransaction={}", sdkTransaction.toString());
            throw new NewspiralException(NewSpiralErrorEnum.SMART_CONTRACT_NOT_EXIST);
        }
        try {
            Object object = SmartContractCache.getObjectCache(SmartContractCache.getClassCache(smartContractName));
            log.debug(ModuleClassification.TxM_TxE_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + "," + sdkTransaction.getChannelId() + ",invoke,method={},object={},args={}", method, object, scInstance.getMethodArgs());
            method.invoke(object, scInstance.getMethodArgs());
            log.debug(ModuleClassification.TxM_TxE_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + "," + sdkTransaction.getChannelId() + ",invoke,end");

        } catch (NewspiralException ex) {
            log.error(ModuleClassification.TxM_TxE_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + "," + sdkTransaction.getChannelId() + ",invoke,为交易动态invoke智能合约方法失败,error={}", ex.getErrorCode(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error(ModuleClassification.TxM_TxE_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + "," + sdkTransaction.getChannelId() + ",invoke,为交易动态invoke智能合约方法失败,error={}", ex.toString(), ex);
            Throwable cause = ex.getCause();
            if (cause instanceof NewspiralException) {
                throw (NewspiralException) cause;
            } else {
                throw new NewspiralException(NewSpiralErrorEnum.PROCESS_ERROR, ex.getCause().toString());
            }
        }
    }

    public Object invokeSystemQuery(SDKTransaction sdkTransaction) throws InvocationTargetException, IllegalAccessException {
        log.info(ModuleClassification.TxM_TxE_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + "," + sdkTransaction.getChannelId() + ",invokeQuery,start");
        Object returnObject = null;
        SmartContractCallInstnace scInstance = sdkTransaction.getSmartContractCallInstnace();
        String smartContractName = scInstance.getSmartContractId();
        Method method = SmartContractCache.getOverloadMethodCache(smartContractName, scInstance.getMethodName(), scInstance.getMethodArgSigs());
        if (method == null) {
            log.warn(ModuleClassification.TxM_TxE_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + "," + sdkTransaction.getChannelId() + ",invokeQuery,无法为Query交易获得对应的智能合约方法,sdkTransaction={}" + sdkTransaction.toString());
            throw new NewspiralException(NewSpiralErrorEnum.SMART_CONTRACT_NOT_EXIST);
        }
        Object object = SmartContractCache.getObjectCache(SmartContractCache.getClassCache(smartContractName));
        //transferMethodArgs(scInstance);
        log.info(ModuleClassification.TxM_TxE_ + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + "," + sdkTransaction.getChannelId() + ",invokeQuery,method={},object={},args={}", method, object, scInstance.getMethodArgs());
        try {
            returnObject = method.invoke(object, scInstance.getMethodArgs());
        } catch (Exception ex) {
            log.error(ModuleClassification.TxM_TxE_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + "," + sdkTransaction.getChannelId() + ",invokeQuery,为Query交易获得对应的智能合约方法失败,error={}", ex.getMessage(), ex);
            Throwable cause = ex.getCause();
            if (cause instanceof NewspiralException) {
                return ((NewspiralException) cause).getResponseErrorMsg();
            } else {
                throw new NewspiralException(NewSpiralErrorEnum.PROCESS_ERROR, ex.getCause().toString());
            }
        }
        return returnObject;
    }

}
