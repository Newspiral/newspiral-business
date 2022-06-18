package com.jinninghui.newspiral.common.entity.transaction;

/**
 * @author whj
 * @date 2020/10/26
 */
public enum TxStateEnum {
    TX_ACCOMPLISH_CONSENSUS(1,"已共识"),
    TX_WAIT_FOR_PACKAGE(2,"待打包"),
    TX_HAVING_CONSENSUS(3,"共识中"),
    TX_POOLED(4,"交易已入池"),
    TX_WAIT_PRE_EXEC(5,"交易等待预执行"),
    TX_IN_BLOCK(6,"交易已持久化在block中");

    private final Integer code;
    private final String message;

    TxStateEnum(Integer code,String message){
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage(){return message;}

}
