package com.jinninghui.newspiral.common.entity.state;

import lombok.Data;

import java.util.Date;

@Data
public class StateHistoryModel {

    private Long id;

    private String stateKey;

    private String channelId;

    private String transactionId;

    private String transHashStr;

    private String clientIdentityKey;

    private String smartContractId;

    private String smartContractMethodName;

    private Long blockId;

    private Integer indexInBlock;

    private String blockHashStr;

    private Date consensusTimestamp;

    private Byte successed;

    private String errorMsg;

    private Date clientTimestamp;

    private Long insertVersion;

    public StateHistoryResp transferToStateHistoryResp(StateHistoryModel stateHistoryModel){
        StateHistoryResp stateHistoryResp = new StateHistoryResp();
        stateHistoryResp.setChannelId(stateHistoryModel.getChannelId());
        stateHistoryResp.setTransHashStr(stateHistoryModel.getTransHashStr());
        stateHistoryResp.setClientIdentityKey(stateHistoryModel.getClientIdentityKey());
        stateHistoryResp.setSmartContractId(stateHistoryModel.getSmartContractId());
        stateHistoryResp.setSmartContractMethodName(stateHistoryModel.getSmartContractMethodName());
        stateHistoryResp.setBlockId(stateHistoryModel.getBlockId());
        stateHistoryResp.setIndexInBlock(stateHistoryModel.getIndexInBlock());
        stateHistoryResp.setBlockHashStr(stateHistoryModel.getBlockHashStr());
        stateHistoryResp.setSuccessed(stateHistoryModel.getSuccessed());
        stateHistoryResp.setErrorMsg(stateHistoryModel.getErrorMsg());
        stateHistoryResp.setConsensusTimestamp(stateHistoryModel.getConsensusTimestamp().getTime());
        stateHistoryResp.setClientTimestamp(stateHistoryModel.getClientTimestamp().getTime());
        return stateHistoryResp;
    }
}
