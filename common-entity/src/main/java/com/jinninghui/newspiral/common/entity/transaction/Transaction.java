package com.jinninghui.newspiral.common.entity.transaction;

import com.jinninghui.newspiral.common.entity.identity.IdentityKey;
import lombok.Data;

import java.util.Date;

/**
 * @version V1.0
 * @Title: Transaction
 * @Package com.jinninghui.newspiral.common.entity.transaction
 * @Description:
 * @author: xuxm
 * @date: 2019/12/9 14:52
 */
@Data
public class Transaction {

    /**
     * 主键
     */
    private Long id;

    /**
     * 所属区块高度
     */
    private Long blockId;

    /**
     * 版本
     */
    private String version;
    /**
     * 交易池版本
     */
    private String pooledTransVersion;
    /**
     * 加入交易池时间
     */
    private Date add2PoolTimestamp;
    /**
     * 交易hash
     */
    private String transHashStr;
    /**
     * sdk版本
     */
    private String sdkTransVersion;
    /**
     * 客户端时间
     */
    private Date clientTimestamp;
    /**
     * 客户端交易编号
     */
    private String clientTransId;
    /**
     * 通道ID
     */
    private String channelId;
    /**
     * 智能合约ID，含版本号
     */
    private String smartContractId;
    /**
     * 所调用的方法名
     */
    private String smartContractMethodName;
    /**
     * 调用者身份
     */
    private IdentityKey clientIdentityKey;
    /**
     * 执行时间
     */
    private Date executeTimestamp;
    /**
     * 执行耗时
     */
    private Long executedMs;
    /**
     * 区块hash
     */
    private String blockHashStr;
    /**
     * 区块Index
     */
    private Integer indexInBlock;
    /**
     * 是否成功 1成功 0失败
     */
    private boolean successed;
    /**
     * 执行消息
     */
    private String errorMsg;
    /**
     * 输入的交易,是SdkTransation
     */
    private String pooledTrans;
    /**
     * 交易执行引起的世界状态变更记录列表,且按修改时间顺序升序加入
     */
    private String modifiedWorldStateList;
    /**
     * 系统智能合约执行引起的的通道变更记录
     */
    private String modifiedChannelRecordList;

    /**
     * 创建交易时间
     */
    private Date createTimestamp;
}
