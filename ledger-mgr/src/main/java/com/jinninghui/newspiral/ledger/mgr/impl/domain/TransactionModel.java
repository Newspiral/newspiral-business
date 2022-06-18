package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class TransactionModel {
    @Getter @Setter
    private Long id;

    @Getter @Setter
    private String version;

    @Getter @Setter
    private Long blockId;

    @Getter @Setter
    private String pooledTransVersion;

    @Getter @Setter
    private Date add2PoolTimestamp;

/*    @Getter @Setter
    private String transHash;*/

    @Getter @Setter
    private String sdkTransVersion;

    @Getter @Setter
    private Date clientTimestamp;

    @Getter @Setter
    private String clientTransId;

    @Getter @Setter
    private String channelId;

    @Getter @Setter
    private String smartContractId;

    @Getter @Setter
    private String smartContractMethodName;

    @Getter @Setter
    private String clientIdentityKey;

    @Getter @Setter
    private Date executeTimestamp;

    @Getter @Setter
    private Long executedMs;

    @Getter @Setter
    private String blockHash;

    @Getter @Setter
    private Integer indexInBlock;

    @Getter @Setter
    private Byte successed;

    @Getter @Setter
    private String errorMsg;

    @Getter @Setter
    private Date createTimestamp;

    @Getter @Setter
    private String transHashStr;


}
