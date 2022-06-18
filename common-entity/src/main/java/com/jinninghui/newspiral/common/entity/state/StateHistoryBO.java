package com.jinninghui.newspiral.common.entity.state;

import lombok.Data;

@Data
public class StateHistoryBO {

    private String channelId;

    private String stateKey;

    private Byte successed;

    private String startTime;

    private String endTime;

    private Long curPage;

    private Long start;

    private Integer pageSize;

}
