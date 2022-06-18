package com.jinninghui.newspiral.common.entity.chain;

import com.jinninghui.newspiral.common.entity.Enum.ActionTypeEnum;
import com.jinninghui.newspiral.common.entity.Enum.PeerActionTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PeerChannelOperationRecord implements Comparable<PeerChannelOperationRecord> {

    /**
     * actionType:枚举（IN，OUT，FROZEN，UNFROZEN）
     */
    @ApiModelProperty("actionType:枚举（IN，OUT，FROZEN，UNFROZEN）")
    private ActionTypeEnum actionTypeEnum;

    /**
     * 区块高度
     */
    @ApiModelProperty("区块高度")
    private Long blockHeight;

    /**
     * 操作时间，单位毫秒
     */
    @ApiModelProperty("操作时间，单位毫秒")
    private Long actionTime;

    //从大到小排序
    @Override
    public int compareTo(PeerChannelOperationRecord o) {
        return Long.compare(o.getBlockHeight(),this.getBlockHeight());
    }
}
