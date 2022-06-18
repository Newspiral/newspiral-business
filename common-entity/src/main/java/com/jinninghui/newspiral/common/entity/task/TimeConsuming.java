package com.jinninghui.newspiral.common.entity.task;

import lombok.Data;

@Data
public class TimeConsuming {
    private Long id;

    private Long viewNumber;

    private Long phaseOne;

    private Long phaseTwo;

    private Long phaseThree;

    private Long phaseFour;

    private Long timeout;

    private Long receiveBlock;

}
