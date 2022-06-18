package com.jinninghui.newspiral.ledger.mgr.contract;

/**
 * @author lida
 * @date 2019/9/21 11:18
 * 所有的智能合约都需要继承这个虚类，以获得使用账本的能力
 */
public abstract class BusinessContractBase extends ContractBase implements BussinessContractCaller, BussinessContractConnector {

    protected BussinessContractConnector connector;

    public BusinessContractBase(BussinessContractConnector connector) {
        this.connector = connector;
    }

}
