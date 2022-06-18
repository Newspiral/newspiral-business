package com.jinninghui.newspiral.ledger.fabric;

import com.jinninghui.newspiral.ledger.mgr.contract.BusinessContractBase;
import com.jinninghui.newspiral.ledger.mgr.contract.BussinessContractConnector;

/**
 * @version V1.0
 * @Title: ChaincodeBase
 * @Package com.jinninghui.newspiral.ledger.mgr.impl.fabric
 * @Description:
 * @author: xuxm
 * @date: 2020/1/2 14:52
 */
public abstract class ChaincodeBaseBusiness extends BusinessContractBase implements Chaincode {
    public ChaincodeBaseBusiness(BussinessContractConnector connector) {
        super(connector);
    }

    @Override
    public abstract Response init(ChaincodeStub stub);

    @Override
    public abstract Response invoke(ChaincodeStub stub);
}
