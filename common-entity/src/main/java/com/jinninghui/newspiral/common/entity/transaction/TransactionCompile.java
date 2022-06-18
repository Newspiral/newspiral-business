package com.jinninghui.newspiral.common.entity.transaction;

import com.jinninghui.newspiral.common.entity.smartcontract.SmartContract;
import lombok.Data;


/**
 * 缓存预编译的交易中的智能合约，当前仅限于内存级别维护
 */
@Data
public class TransactionCompile {
    /**
     * 为SmartContractInfo.toString()，因为对于一个智能合约中可以有多个SmartContractInfo，但是它们的内容一样，只有签名不一样。
     */
    private String txCompileID;
    /**
     * 对应的SDKTransaction，即SDK侧（即业务程序侧）生成的交易，里面附带了未执行编译的智能合约:
     * SmartContractCallInstnace中的Object[] methodArgs即为SmartContractInfo列表，其中每个SmartContractInfo都一样，只有签名信息不一样
     */
    private SDKTransaction sdkTX;
    /**
     * 已完成编译的智能合约，存储的是SDKTransaction中对应的智能合约通过编译之后的结构
     */
    private SmartContract smartContract;

    /**
     * 默认构造输入
     * @param _sdkTX
     * @param _smartContract
     */
    public  TransactionCompile(SDKTransaction _sdkTX, SmartContract _smartContract){
        this.setSdkTX(_sdkTX);
        this.setSmartContract(_smartContract);
    }

}
