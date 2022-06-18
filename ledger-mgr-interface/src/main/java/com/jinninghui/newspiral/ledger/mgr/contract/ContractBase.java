package com.jinninghui.newspiral.ledger.mgr.contract;

import com.jinninghui.newspiral.ledger.mgr.SmartContractMgr;

public abstract class ContractBase {

    protected SmartContractMgr smartContractMgr;

    public SmartContractMgr getSmartContractMgr() {
        return smartContractMgr;
    }

    public void setSmartContractMgr(SmartContractMgr smartContractMgr) {
        this.smartContractMgr = smartContractMgr;
    }
}
