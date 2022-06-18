package com.jinninghui.newspiral.common.entity.smartcontract;

import lombok.Data;

@Data
public class SmartContractModelKeyReq {
    private String scName;

    private String scVersion;

    private String scChannelId;

    /**
     * 别名
     */
    private String alisa;

}