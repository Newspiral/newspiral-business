package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import lombok.Data;

import java.util.Date;

@Data
public class SmartContractModel extends SmartContractModelKey {
    private String scClassHash;

    private Date setupTimestamp;

    private String flag;

    private Date updateTime;

    /**
     * 扩展属性
     */
    private String extendedData;



}