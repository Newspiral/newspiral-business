package com.jinninghui.newspiral.ledger.contract;


import com.jinninghui.newspiral.ledger.mgr.contract.BusinessContractBase;
import com.jinninghui.newspiral.ledger.mgr.contract.BussinessContractConnector;

/**
 * @author lida
 * @date 2019/9/24 16:09
 */
public class BusinessBusinessContractExample4 extends BusinessContractBase {


    public BusinessBusinessContractExample4(BussinessContractConnector connector) {
        super(connector);
    }

    public String businessQuerySC(String stateKey)
    {
        String valueStr="";
        System.out.println("--------call---------businessQuerySC--------");
        byte[] value = smartContractMgr.getState(stateKey);
        try {
            valueStr=new String(value, "UTF-8");
        } catch (Exception e) {
            System.out.println("世界状态value转化错误");
        }
        System.out.println("业务合约中查询状态:"+stateKey+",得到WorldState:"+valueStr);
        return valueStr;
    }

    public void businessWriteSC(String stateKey, String stateValue)
    {
        System.out.println("--------call---------businessWriteSC--------");
        smartContractMgr.putState(stateKey,stateValue.getBytes());

    }

}
