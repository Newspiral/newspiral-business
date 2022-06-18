package com.jinninghui.newspiral.gateway.entity;

import com.jinninghui.newspiral.common.entity.consensus.GenericQC;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
@ToString
public class QueryHistoryQCResp {
    @Getter @Setter
    List<GenericQC> listQC;
}
