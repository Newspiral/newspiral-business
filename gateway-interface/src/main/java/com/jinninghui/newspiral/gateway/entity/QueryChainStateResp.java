package com.jinninghui.newspiral.gateway.entity;

import com.jinninghui.newspiral.common.entity.consensus.GenericQC;
import com.jinninghui.newspiral.common.entity.consensus.HotStuffDataNode;
import lombok.Getter;
import lombok.Setter;

public class QueryChainStateResp {
    @Getter @Setter
    String channelId;
    @Getter @Setter
    Long height;
    @Getter @Setter
    Long currentView;
    @Getter @Setter
    HotStuffDataNode prePrepare;
    @Getter @Setter
    HotStuffDataNode prepare;
    @Getter @Setter
    HotStuffDataNode preCommit;
    @Getter @Setter
    GenericQC highestQC;
    public boolean betterThan(QueryChainStateResp resp) {
        if (height.longValue() > resp.getHeight().longValue()) {
            return true;
        } else if (preCommit.getJustify().newerThan(resp.getPreCommit().getJustify())) {
            return true;
        } else if (prepare.getJustify().newerThan(resp.getPrepare().getJustify())) {
            return true;
        } else if (prePrepare.getJustify().newerThan(resp.getPrePrepare().getJustify())) {
            return true;
        }
        return false;
    }
}
