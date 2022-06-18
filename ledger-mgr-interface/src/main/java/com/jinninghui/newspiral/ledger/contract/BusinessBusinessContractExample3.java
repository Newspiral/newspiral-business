package com.jinninghui.newspiral.ledger.contract;


import com.jinninghui.newspiral.ledger.mgr.contract.BusinessContractBase;
import com.jinninghui.newspiral.ledger.mgr.contract.BussinessContractConnector;

/**
 * @author lida
 * @date 2019/9/24 16:09
 */
public class BusinessBusinessContractExample3 extends BusinessContractBase {


    public BusinessBusinessContractExample3(BussinessContractConnector connector) {
        super(connector);
    }

    public String businessQuerySCTest3(String channelId)
    {
        System.out.println("--------call---------businessQuerySCTest--------");
        String value = channelId;
        System.out.println("业务合约中查key:"+channelId+",得到Value:"+value);
        return value;
    }

}
