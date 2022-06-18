package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import lombok.Data;

@Data
public class SmartContractModelKey {
    private String scName;

    private String scVersion;

    private String scChannelId;

    /**
     * 别名
     */
    private String alisa;

}