package com.jinninghui.newspiral.ledger.mgr;

import com.jinninghui.newspiral.common.entity.smartcontract.SmartContract;
import com.jinninghui.newspiral.common.entity.state.WorldStateResp;
import com.jinninghui.newspiral.common.entity.transaction.TransactionResp;

import java.util.List;

/**
 * @author lida
 * @date 2019/7/5 18:45
 * 用于执行业务合约的
 */
public interface SmartContractMgr {

/******************** StateStorage 的接口开始**************************/
    byte[] getState(String key);

    String getStrState(String key);

    void insertState(String key, byte[] value);

    void insertStrState(String key, String value);

    void updateState(String key, byte[] newValue);


    void updateStrState(String key, String value);



    /**
     * 设置key指定的状态的value为输入的value，如果不存在则新增，如果存在则更新
     * @param key
     * @param value
     */
    void putState(String key, byte[] value);


    void putStrState(String key, String value);

    boolean existKey(String key);

    /**
     * 删除成功，返回true，否则返回false
     * @param key
     * @return
     */
    boolean deleteKey(String key);
/******************** StateStorage 的接口结束**************************/

    /**
     *
     * @param channelId
     * @return
     */
    String getBlockTest(String channelId,Integer num);


    SmartContract getSmartContact(String scName, String scVersion, String scChannelId);

    /**
     * 历史交易查询
     */
    List<TransactionResp> queryTxHistory(String key);


    /**
     * 时间段账本
     * @param
     * @return
     */
    List<WorldStateResp> queryStatesByTimeRegion(Long startTime, Long endTime);



    List<TransactionResp> queryStatesHistory(Long startTime, Long endTime,String key);


}
