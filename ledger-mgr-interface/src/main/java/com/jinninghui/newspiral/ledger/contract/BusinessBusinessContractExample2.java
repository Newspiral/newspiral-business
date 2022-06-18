package com.jinninghui.newspiral.ledger.contract;


import com.jinninghui.newspiral.ledger.mgr.contract.BusinessContractBase;
import com.jinninghui.newspiral.ledger.mgr.contract.BussinessContractConnector;

/**
 * @author lida
 * @date 2019/9/24 16:09
 */
public class BusinessBusinessContractExample2 extends BusinessContractBase {


    public BusinessBusinessContractExample2(BussinessContractConnector connector) {
        super(connector);
    }

    public String businessQuerySCTest(String channelId, Integer num)
    {
        System.out.println("--------call---------businessQuerySCTest--------");
        String value = smartContractMgr.getBlockTest(channelId,num);
        System.out.println("业务合约中查key:"+channelId+",得到Value:"+value);
        return value;
    }
}
