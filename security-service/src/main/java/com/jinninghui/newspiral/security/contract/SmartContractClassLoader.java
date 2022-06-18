package com.jinninghui.newspiral.security.contract;

import com.jinninghui.newspiral.common.entity.common.logmodule.LogModuleCodes;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContract;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lida
 * @date 2019/9/24 16:15
 */
@Slf4j
public class SmartContractClassLoader extends ClassLoader {

    public SmartContractClassLoader() {
        super(SmartContractClassLoader.class.getClassLoader());

    }

    public Class<?> defineClass(SmartContract sc) throws ClassNotFoundException {
        try {
            byte[] classBytes = sc.getClassFileBytes();
            return defineClass(null, classBytes, 0, classBytes.length);
        } catch (Exception ex) {
            log.error(ModuleClassification.TxM_SCCL_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",类加载出现异常,", ex);
            //throw new ClassNotFoundException();
            return null;
        } catch (Throwable t) {
            log.error(ModuleClassification.TxM_SCCL_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",类加载出现异常,可能是有重复的了", t);
            //throw new ClassNotFoundException();
            return null;
        }

    }

    public Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return Class.forName(name);
        } catch (Throwable t) {
            log.error(ModuleClassification.TxM_SCCL_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_SMART_CONTRACT_ACTION + ",类查询出现异常", t);
            //throw new ClassNotFoundException();
            return null;
        }

    }

}
