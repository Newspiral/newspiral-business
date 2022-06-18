package com.jinninghui.newspiral.common.entity.transaction;

import com.alibaba.fastjson.JSON;
import com.jinninghui.newspiral.common.entity.state.WorldStateModifyRecord;
import com.jinninghui.newspiral.common.entity.state.WorldStateModifyRecordResp;
import com.jinninghui.newspiral.common.entity.state.WorldStateResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @version V1.0
 * @Title: TransactionResp
 * @Package com.jinninghui.newspiral.gateway.vo.resp
 * @Description:
 * @author: xuxm
 * @date: 2019/12/7 16:17
 * @modify whj
 * @date 2020/10/26
 */

@ApiModel(description = "交易返回信息")
@Data
public class TransactionResp {

    /**
     * 交易池交易
     */
    @ApiModelProperty(value = "交易池交易")
    private SDKTransaction pooledTransaction;

    /**
     * 交易状态
     */
    @ApiModelProperty(value = "交易状态")
    private TxStateEnum transactionState;

    /**
     *如果上述交易状态为已共识，则有以下字段
     */

    /**
     * 数据结构版本号，这个是指ExecutedTransacion的版本号，与SDKTransaction版本号不一定完全一样
     */
    @ApiModelProperty(value = "数据结构版本号（ExecutedTransacion的版本号）")
    private String version;

    /**
     * 交易执行引起的世界状态变更记录列表,且按修改时间顺序升序加入
     */
    @ApiModelProperty(value = "交易执行引起的世界状态变更记录列表")
    private  String modifiedWorldStateList;

    /**
     * 系统智能合约执行引起的的通道变更记录
     */
    @ApiModelProperty(value = "系统智能合约执行引起的的通道变更记录")
    private  String modifiedChannelRecord;

    /**
     * 开始执行的时间戳
     */
    @ApiModelProperty(value = "开始执行的时间戳")
    private Long executeTimestamp;

    /**
     * 执行耗时，单位毫秒
     */
    @ApiModelProperty(value = "执行耗时，毫秒")
    private  Long executedMs;

    /**
     * 是否成功执行
     */
    @ApiModelProperty(value = "是否成功执行")
    private boolean successed;

    /**
     * 错误原因
     */
    @ApiModelProperty(value = "错误原因")
    private String errorMsg;

    /**
     * 所属区块hash
     */
    @ApiModelProperty(value = "所属区块hash")
    private String blockHashStr;

    /**
     * 交易在区块中的序号：从1开始
     */
    @ApiModelProperty(value = "区块在交易中的序号，从1开始")
    private Integer indexInBlock;



    @ApiModelProperty(value = "通道id")
    private String channelId;


    @ApiModelProperty(value = "通道名称")
    private String channelName;

    /**
     * 创建交易时间
     */
    @ApiModelProperty(value = "创建交易的时间")
    private Long createTimestamp;

    /**
     * 交易的附加信息
     */
    @ApiModelProperty(value = "交易附加信息")
    private TransactionAttached transactionAttached;







   //------------------------------------------------------以下属性废弃--------------------------------------
//    /**
//     * 客户端时间
//     */
//    @ApiModelProperty(value = "客户端时间戳")
//    private Long clientTimestamp;
//    /**
//     * 客户端交易编号
//     */
//    @ApiModelProperty(value = "客户端交易编号")
//    private String clientTransId;
//
//    /**
//     * 智能合约ID，含版本号
//     */
//    @ApiModelProperty(value = "智能合约ID，含版本号")
//    private String smartContractId;
//    /**
//     * 所调用的方法名
//     */
//    @ApiModelProperty(value = "智能合约所调用的方法名")
//    private String smartContractMethodName;
//    /**
//     * 调用者身份
//     */
//    @ApiModelProperty(value = "调用者身份")
//    private IdentityKey clientIdentityKey;
//    /**
//     * 加入交易池时间
//     */
//    @ApiModelProperty(value = "加入交易池时间")
//    private Long add2PoolTimestamp;
//    /**
//     * 交易hash
//     */
//    @ApiModelProperty(value = "交易hash")
//    private String transHashStr;
//    /**
//     * 所属通道ID
//     */
//    @ApiModelProperty(value = "通道ID")
//    private String channelId;
//
//

//
//

    /**
     * 交易实体转化
     *
     * @param transaction
     * @return
     */
    public static TransactionResp transferTransactionResp(Transaction transaction) {

        TransactionResp transactionResp = new TransactionResp();
        transactionResp.setPooledTransaction(JSON.parseObject(transaction.getPooledTrans(),SDKTransaction.class));
        transactionResp.setTransactionState(TxStateEnum.TX_ACCOMPLISH_CONSENSUS);
        transactionResp.setVersion(transaction.getVersion());
        transactionResp.setExecuteTimestamp(transaction.getExecuteTimestamp().getTime());
        transactionResp.setExecutedMs(transaction.getExecutedMs());
        transactionResp.setErrorMsg(transaction.getErrorMsg());
        transactionResp.setBlockHashStr(transaction.getBlockHashStr());
        transactionResp.setIndexInBlock(transaction.getIndexInBlock());
        transactionResp.setSuccessed(transaction.isSuccessed());
        transactionResp.setIndexInBlock(transaction.getIndexInBlock());
        transactionResp.setModifiedChannelRecord(transaction.getModifiedChannelRecordList());
        //transactionResp.setModifiedWorldStateList(transaction.getModifiedWorldStateList());
        List<WorldStateModifyRecord> modifiedWorldStateList = JSON.parseArray(transaction.getModifiedWorldStateList(), WorldStateModifyRecord.class);
        List<WorldStateModifyRecordResp> worldStateModifyRecordResps = new ArrayList<>();
        if (!CollectionUtils.isEmpty(modifiedWorldStateList)) {
            for (WorldStateModifyRecord worldStateModifyRecord : modifiedWorldStateList) {
                WorldStateModifyRecordResp worldStateModifyRecordResp = new WorldStateModifyRecordResp();
                if (null != worldStateModifyRecord.getNewState()) {
                    worldStateModifyRecordResp.setNewState(WorldStateResp.transferWorldState(worldStateModifyRecord.getNewState()));
                }
                if (null != worldStateModifyRecord.getOldState()) {
                    worldStateModifyRecordResp.setOldState(WorldStateResp.transferWorldState(worldStateModifyRecord.getOldState()));
                }
                worldStateModifyRecordResp.setLatestUpdateViewNo(worldStateModifyRecord.getLatestUpdateViewNo());
                worldStateModifyRecordResps.add(worldStateModifyRecordResp);
            }
            transactionResp.setModifiedWorldStateList(JSON.toJSONString(worldStateModifyRecordResps));
        } else {
            transactionResp.setModifiedWorldStateList(transaction.getModifiedWorldStateList());
        }
        transactionResp.setCreateTimestamp(transaction.getCreateTimestamp().getTime());
//        transactionResp.setAdd2PoolTimestamp(transaction.getAdd2PoolTimestamp().getTime());
//        transactionResp.setClientIdentityKey(transaction.getClientIdentityKey());
//        transactionResp.setClientTimestamp(transaction.getClientTimestamp().getTime());
//        transactionResp.setClientTransId(transaction.getClientTransId());
//        transactionResp.setSmartContractId(transaction.getSmartContractId());
//        transactionResp.setSmartContractMethodName(transaction.getSmartContractMethodName());
//        transactionResp.setTransHashStr(transaction.getTransHashStr());
//        transactionResp.setChannelId(transaction.getChannelId());
        return transactionResp;
    }

}
