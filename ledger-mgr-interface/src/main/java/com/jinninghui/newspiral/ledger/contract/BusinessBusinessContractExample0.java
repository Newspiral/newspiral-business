package com.jinninghui.newspiral.ledger.contract;

import com.jinninghui.newspiral.ledger.mgr.contract.BusinessContractBase;
import com.jinninghui.newspiral.ledger.mgr.contract.BussinessContractConnector;

/**
 * @version V1.0
 * @Title: BusinessSmartContractExample
 * @Package com.jinninghui.newspiral.ledger.mgr
 * @Description:
 * @author: xuxm
 * @date: 2020/2/7 21:17
 */
public class BusinessBusinessContractExample0 extends BusinessContractBase {

    public BusinessBusinessContractExample0(BussinessContractConnector connector) {
        super(connector);
    }

    public String businessQuerySC(String stateKey)
    {
        System.out.println("--------call---------businessQuerySC--------");
        byte[] value = smartContractMgr.getState(stateKey);
        System.out.println("业务合约中查询状态:"+stateKey+",得到 WorldState:"+value.toString());
        return value.toString();
    }

    public void businessWriteSC(String stateKey, String stateValue)
    {
        System.out.println("--------call---------businessWriteSC--------");
        smartContractMgr.putState(stateKey,stateValue.getBytes());
    }

}
