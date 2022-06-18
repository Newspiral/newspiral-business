package com.jinninghui.newspiral.common.entity.state;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("引起时间状态变更的交易信息")
@Data
public class StateHistoryResp {

    /**
     * 通道ID
     */
    @ApiModelProperty("通道Id")
    private String channelId;

    /**
     * 交易hash
     */
    @ApiModelProperty("交易hash")
    private String transHashStr;

    /**
     * 交易发起方身份Id
     */
    @ApiModelProperty("交易发起方身份Id")
    private String clientIdentityKey;

    /**
     * 智能合约Id
     */
    @ApiModelProperty("智能合约Id")
    private String smartContractId;

    /**
     * 智能合约方法名
     */
    @ApiModelProperty("智能合约方法名")
    private String smartContractMethodName;

    /**
     * 交易所属区块高度
     */
    @ApiModelProperty("交易所属区块高度")
    private Long blockId;

    /**
     * 交易在区块中的索引
     */
    @ApiModelProperty("交易在区块中的索引")
    private Integer indexInBlock;

    /**
     * 交易所属区块hash
     */
    @ApiModelProperty("交易所属区块hash")
    private String blockHashStr;

    /**
     * 交易所属区块的共识时间：时间戳
     */
    @ApiModelProperty("易所属区块的共识时间：时间戳")
    private Long consensusTimestamp;

    /**
     * 是否成功
     */
    @ApiModelProperty("是否成功")
    private Byte successed;

    /**
     * 错误信息
     */
    @ApiModelProperty("错误信息")
    private String errorMsg;

    /**
     * 交易的客户端时间CLIENT_TIMESTAMP：时间戳
     */
    @ApiModelProperty("交易的客户端时间CLIENT_TIMESTAMP：时间戳")
    private Long clientTimestamp;
}
