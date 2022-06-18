package com.jinninghui.newspiral.ledger.mgr.impl.domain;



import lombok.Data;

import java.util.Date;

@Data
public class PeerChannelModel {

    private Integer id;
    //对应identityKey中的value
    private String peerId;

    private String channelId;

    private Date joinTimestamp;

    private String extendedData;

    private String userPrivateKey;

    private Long inBlockHeight;

    private Long outBlockHeight;

    private String actionType;

    private Date updateTimestamp;
}