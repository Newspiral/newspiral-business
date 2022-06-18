package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import lombok.Data;

@Data
public class TimeConsumingModel {
    private Long id;

    private Long viewNumber;

    private Long phaseOne;

    private Long phaseTwo;

    private Long phaseThree;

    private Long phaseFour;

    private Long timeout;

    private Long receiveBlock;

}
