package com.jinninghui.newspiral.common.entity.state;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.binary.Hex;

import java.util.Arrays;

/**
 * @author lida
 * @date 2019/7/11 16:31
 * 世界状态
 */
@ApiModel(description = "世界状态")
public class WorldState {
    @ApiModelProperty(value = "世界状态key")
    @Getter @Setter
    String key;

    @ApiModelProperty(value = "世界状态value")
    @Getter @Setter
    byte[] value;

    @ApiModelProperty(value="最新更新的区块hash")
    @Getter @Setter
    private String latestBlockHash;

    @ApiModelProperty(value="最新更新的区块高度")
    @Getter @Setter
    private Long latestBlockHeight;

    @ApiModelProperty(value="最新更新的交易hash")
    @Getter @Setter
    private String latestTransHash;


    @ApiModelProperty(value = "区块最新的更新时间")
    @Getter @Setter
    private Long updateTime;

    public static WorldState createInstance(String key, byte[] value) {
        WorldState state = new WorldState();
        state.setValue(value);
        state.setKey(key);
        return state;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"key\":\"")
                .append(key).append('\"');
        sb.append(",\"value\":")
                .append(Hex.encodeHexString(value,false));
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof  WorldState)) {
            return false;
        }

        WorldState otherState = (WorldState) obj;

        if (key.equals(otherState.getKey())
        && Arrays.equals(value, otherState.getValue())) {
            return true;
        }
        return false;
    }
}
