package com.jinninghui.newspiral.ledger.mgr;



/**
 * @author lida
 * @date 2019/10/5 7:53
 * 状态存储接口
 * 该接口会注入到所有的智能合约对象中
 * 使用StateStorage是为了限制业务智能合约所能使用到的能力范围，避免越界使用LedgerMgr的接口
 */
public interface StateStorage {

    byte[] getState(String key);

    void insertState(String key, byte[] value);

    void updateState(String key, byte[] newValue);

    /**
     * 设置key指定的状态的value为输入的value，如果不存在则新增，如果存在则更新
     * @param key
     * @param value
     */
    void putState(String key, byte[] value);

    boolean existKey(String key);

    /**
     * 删除成功，返回true，否则返回false
     * @param key
     * @return
     */
    boolean deleteKey(String key);
}
