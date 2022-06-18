package com.jinninghui.newspiral.common.entity.chain;

import com.jinninghui.newspiral.common.entity.block.Block;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lida
 * @date 2019/7/5 19:06
 * 区块链概要信息
 * 其实是某个通道的账本的概要信息
 */
@ToString
public class ChannelSummary {
    /**
     * 链的第一个块的前序Hash值
     */
    static final public String INIT_HASH = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";

    @Getter @Setter
    String channelId;
    /**
     * 区块高度
     */
    @Getter @Setter
    Long height;
    /**
     * 最新区块的Hash值，如果还没有区块，则该值为INIT_HASH
     */
    @Getter @Setter
    String hash = INIT_HASH;//默认设置为这个值
    /**
     * 是否与网络同步
     */
    @Getter @Setter
    boolean syncWithNetwork;

    @Getter @Setter
    List<Block> blockList;

    @ApiModelProperty(value = "扩展参数（共识算法相关）")
    @Valid
    @NotEmpty
    @Getter @Setter
    Map<String,String> extendsParams = new HashMap<String,String>();

    /**
     * 该通道的区块链是否已经包含了业务数据
     * @return
     */
    public boolean  hasBusinessData()
    {
        if(this.getHash().equals(ChannelSummary.INIT_HASH))
        {
            return false;
        }
        else
        {
            return true;
        }
    }



}
