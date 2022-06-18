package com.jinninghui.newspiral.common.entity.transaction;

import com.jinninghui.newspiral.common.entity.chain.ChannelModifyRecord;
import com.jinninghui.newspiral.common.entity.state.WorldState;
import com.jinninghui.newspiral.common.entity.state.WorldStateModifyRecord;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author lida
 * @date 2019/7/11 16:28
 * 交易执行引擎执行后的交易
 */
@ApiModel(description = "交易执行引擎执行后的交易")
@ToString
@Slf4j
public class ExecutedTransaction implements Serializable,Comparable<ExecutedTransaction> {

    /**
     * 数据结构版本号，这个是指ExecutedTransacion的版本号，与SDKTransaction版本号不一定完全一样
     */
    @ApiModelProperty(value = "数据结构版本号（ExecutedTransacion的版本号，与SDKTransaction版本号不一定完全一样）")
    @Getter @Setter
    String version;

    /**
     * 输入的交易
     */
    @ApiModelProperty(value = "输入的交易")
    @Getter @Setter
    SDKTransaction sdkTransaction;

    @ApiModelProperty(value = "是否顺序")
    @Getter @Setter
    boolean serial = false;

    /**
     * 交易执行引起的世界状态变更记录列表,且按修改时间顺序升序加入
     */
    @ApiModelProperty(value = "交易执行引起的世界状态变更记录列表,且按修改时间顺序升序加入")
    @Getter @Setter
    List<WorldStateModifyRecord> modifiedWorldStateList = new LinkedList<>();

    /**
     * 交易执行引起的世界状态变更记录列表,且按修改时间顺序升序加入
     * TODO：有点多余，因为可以从modifiedWorldStateList中获取到
     */
    @ApiModelProperty(value = "交易执行引起的世界状态变更记录列表,且按修改时间顺序升序加入",hidden = true)
    @Getter @Setter
    Set<String> modifiedWorldStateKeyList = new HashSet<>();

    // TODO:交易信息记录


    @ApiModelProperty(value = "系统智能合约引起的记录列表,按修改顺序升序加入")
    @Getter @Setter
    List<ChannelModifyRecord> modifiedChannelRecordList = new LinkedList<>();

    /**
     * 开始执行的时间戳
     */
    @ApiModelProperty(value = "开始执行的时间戳")
    @Getter @Setter
    Long executeTimestamp;

    /**
     * 执行耗时，单位毫秒
     */
    @ApiModelProperty(value = "执行耗时")
    @Getter @Setter
    Long executedMs;

    /**
     * 是否成功执行
     */
    @ApiModelProperty(value = "是否成功执行")
    @Getter @Setter
    String pass;

    /**
     * 错误原因
     */
    @ApiModelProperty(value = "错误原因")
    @Getter @Setter
    String errorMsg;

    /**
     * 所以加一个add方法
     * @param modifiedWorldStateCollection
     */
    public void addWorldStateModifiedRecords(Collection<WorldStateModifyRecord> modifiedWorldStateCollection) {
        this.modifiedWorldStateList.addAll(modifiedWorldStateCollection);
    }


    public void addChannelModifiedRecords(Collection<ChannelModifyRecord> channelModifyRecords) {
        this.modifiedChannelRecordList.addAll(channelModifyRecords);
    }


    public String getSDKTransactionHash() {
        return getSdkTransaction().getHash();
    }


    public ExecutedTransaction()
    {
        this.version = "V1.0";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ExecutedTransaction)) {
            return false;
        }
        ExecutedTransaction newTrans = (ExecutedTransaction) obj;
        if (!this.version.equals(newTrans.version)) {
            return false;
        }
        if (this.pass.equals(newTrans.pass) == false) {
            log.error("pass error");
            return false;
        }
        int idx = 0;
        for (WorldStateModifyRecord oldRecord : modifiedWorldStateList) {
            WorldState oldStateBeforeTrans = oldRecord.getOldState();
            WorldState oldStateAfterTrans = oldRecord.getNewState();

            if (newTrans.getModifiedWorldStateList() == null || newTrans.getModifiedWorldStateList().size() <= idx) {
                log.error("size error");
                return false;
            }

            WorldStateModifyRecord newRecord = newTrans.getModifiedWorldStateList().get(idx);

            WorldState newStateBeforeTrans = newRecord.getOldState();
            WorldState newStateAfterTrans = newRecord.getNewState();


            if (!equalWorldState(oldStateBeforeTrans,newStateBeforeTrans)
            || !equalWorldState(oldStateAfterTrans,newStateAfterTrans)) {
                log.error("state error");
                return false;
            }
            idx++;
        }

        List<ChannelModifyRecord> newChanges = newTrans.getModifiedChannelRecordList();
        for (int i = 0; i< this.modifiedChannelRecordList.size(); i++){
            ChannelModifyRecord oldRecord = this.modifiedChannelRecordList.get(i);
            if (newChanges.size() <= i) {
                log.error("size error");
                return false;
            }
            ChannelModifyRecord newRecord = newChanges.get(i);
            if (!newRecord.equals(oldRecord)){
                log.error("ChannelModifyRecord error");
                return false;
            }
        }
        return true;
    }

    public boolean isConflict(ExecutedTransaction tx) {
        Set<String> keyset = tx.getModifiedWorldStateKeyList();

        return this.modifiedWorldStateKeyList.parallelStream().anyMatch(
                key-> keyset.contains(key)
        );
/*        for (String key : this.modifiedWorldStateKeyList) {
            if (keyset.contains(key)) {
                return true;
            }
        }
        return false;*/
    }

    public boolean equalWorldState(WorldState oldWorldState, WorldState newWorldState) {
        if (oldWorldState == null && newWorldState == null) {
            return true;
        }
        if (oldWorldState != null && oldWorldState.equals(newWorldState)) {
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(ExecutedTransaction o) {
        int i = this.getSDKTransactionHash().hashCode() - o.getSDKTransactionHash().hashCode();
        if(i==0){
            return this.getSdkTransaction().getClientTimestamp() - o.getSdkTransaction().getClientTimestamp()>0?1:-1;
        }
        return i;
    }
}
