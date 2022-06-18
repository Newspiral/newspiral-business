package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import java.util.Date;


public class TransactionModelExpandStateHistory extends TransactionModelWithBLOBs {

    private Date consensusTimeStamp;

    public Date getConsensusTimeStamp() {
        return consensusTimeStamp;
    }

    public void setConsensusTimeStamp(Date consensusTimeStamp) {
        this.consensusTimeStamp = consensusTimeStamp;
    }
}
