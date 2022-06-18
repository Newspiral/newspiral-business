package com.jinninghui.newspiral.ledger.mgr.impl.persist;

import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.common.logmodule.LogModuleCodes;
import com.jinninghui.newspiral.common.entity.common.persist.PersistActionInterface;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContract;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.ledger.mgr.impl.LedgerMgrImpl;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.SmartContractModelWithBLOBs;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.SmartContractModelMapper;
import com.jinninghui.newspiral.security.DataSecurityMgr;
import com.jinninghui.newspiral.security.contract.SandBoxCacheMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ContractAction implements PersistActionInterface<Channel> {

    @Autowired
    private SmartContractModelMapper contractMapper;

    @Autowired
    private LedgerMgrImpl ledgerMrg;

    @SofaReference
    private DataSecurityMgr dataSecurityMgr;

    @SofaReference
    private SandBoxCacheMgr sandBoxCacheMgr;

    @Override
    public void doAdd(Channel newChannel) {
        int updateTotal = 0;
        int insertTotal = 0;
        SmartContract sc = (SmartContract) newChannel.getChannelChange().getActionData();
        //插入之前先判断是否存在，
        SmartContractModelWithBLOBs smartContractModelWithBLOBs = SmartContractModelWithBLOBs.createInstance(sc);
        //先把存在的吊销掉
        contractMapper.destructionSmartCintract(sc.getName(),sc.getChannelId());
        sandBoxCacheMgr.removeBusinessContractCache(sc.getChannelId() + "," + sc.getVersion() + "," + sc.getAlisa(),sc.getName());
        String key = sc.getChannelId() + sc.getName() + sc.getVersion();
        if (null != contractMapper.selectByPrimaryKey(smartContractModelWithBLOBs)) {
            int updateRow = contractMapper.updateByPrimaryKeySelective(smartContractModelWithBLOBs);
            if (updateRow > 0) {
                updateTotal++;
                ledgerMrg.removeContractFromMap(key, sc);
            }
        } else {
            smartContractModelWithBLOBs.setScClassHash(dataSecurityMgr.getHash(smartContractModelWithBLOBs.getScClassFile()));
            int insertRow = contractMapper.insert(smartContractModelWithBLOBs);
            //for debug
            log.info(ModuleClassification.LedM_LMI_ + "insert smartcontract");
            if (insertRow > 0) {
                insertTotal++;
                //smartContractConcurrentHashMap.put(key, sc);
                //smartContractsConcurrentHashMap.remove(sc.getChannelId());
            }
        }
        log.info(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",insertSmartContractList 新增的智能合约:" + insertTotal + "条,修改的智能合约：" + updateTotal + "条");
    }

    @Override
    public void doRemove(Channel newChannel) {

    }

    @Override
    public void doModify(Channel newChannel) {

    }

    @Override
    public void doRest(Channel newChannel) {

    }

}
