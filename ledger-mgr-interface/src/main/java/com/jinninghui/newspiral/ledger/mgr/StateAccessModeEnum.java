package com.jinninghui.newspiral.ledger.mgr;

import com.alibaba.fastjson.JSON;

/**
 * @author lida
 * @date 2019/9/26 10:46
 */
public enum StateAccessModeEnum {

    TRANS_CREATE_FOR_STEPQC_VERIFY("TRANS_CREATE_FOR_STEPQC_VERIFY","交易执行引擎执行智能合约校验串行时候使用"),
    TRANS_CREATE_FOR_FIXQC_VERIFY("TRANS_CREATE_FOR_FIXQC_VERIFY","交易执行引擎执行智能合约校验并行时候使用"),
    PERSIST_STATE("PERSIST_STATE","存储已共识区块时使用，会修改持久化世界状态的值"),
    QUERY_TRANS_EXXECUTE("QUERY_TRANS_EXXECUTE","交易管理模块执行Query交易时调用"),
    UNDEFINED("UNDEFINED","未定义状态，账本模块将拒绝任何操作");
    public final String code;
    public final String message;
    StateAccessModeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
    public String getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
