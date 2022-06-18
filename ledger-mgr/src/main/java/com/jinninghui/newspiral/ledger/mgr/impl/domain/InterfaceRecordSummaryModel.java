package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import com.jinninghui.newspiral.common.entity.record.InterfaceRecordSummary;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class InterfaceRecordSummaryModel {
    /**
     * 自增主键
     */
    @NotNull
    private Long id;

    /**
     * 记录的hash值，用来标识每条记录的唯一性，唯一索引
     */
    @NotNull
    private String recordHash;

    /**
     * 被调用的接口名称
     */
    @NotNull
    private String methodName;

    /**
     * 被调用的接口协议类型
     */
    @NotNull
    private String protocolName;

    /**
     * 调用所属通道ID
     */
    private String channelId;

    /**
     * 调用结果，1：Success:；0：Failed
     */
    private Integer successed;

    /**
     * 调用错误信息
     */
    private String errorMsg;

    /**
     * 调用合约别名
     */
    private String scAlisa;

    /**
     * 合约方法名
     */
    private String scMethodName;

    /**
     * 调用合约版本号
     */
    private String scVersion;

    /**
     * 调用总次数
     */
    @NotNull
    private Long totalCalls;

    /**
     * 调用总耗时，毫秒
     */
    @NotNull
    private Long totalCallTime;

    public static InterfaceRecordSummaryModel createInstance(InterfaceRecordSummary interfaceRecordSummary){
        InterfaceRecordSummaryModel interfaceRecordSummaryModel = new InterfaceRecordSummaryModel();
        interfaceRecordSummaryModel.setId(interfaceRecordSummary.getId());
        interfaceRecordSummaryModel.setRecordHash(interfaceRecordSummary.getRecordHash());
        interfaceRecordSummaryModel.setMethodName(interfaceRecordSummary.getMethodName());
        interfaceRecordSummaryModel.setProtocolName(interfaceRecordSummary.getProtocolName());
        interfaceRecordSummaryModel.setChannelId(interfaceRecordSummary.getChannelId());
        interfaceRecordSummaryModel.setSuccessed(interfaceRecordSummary.getSuccessed());
        interfaceRecordSummaryModel.setErrorMsg(interfaceRecordSummary.getErrorMsg());
        interfaceRecordSummaryModel.setScAlisa(interfaceRecordSummary.getScAlisa());
        interfaceRecordSummaryModel.setScMethodName(interfaceRecordSummary.getScMethodName());
        interfaceRecordSummaryModel.setScVersion(interfaceRecordSummary.getScVersion());
        interfaceRecordSummaryModel.setTotalCalls(interfaceRecordSummary.getTotalCalls());
        interfaceRecordSummaryModel.setTotalCallTime(interfaceRecordSummary.getTotalCallTime());
        return interfaceRecordSummaryModel;
    }

    public InterfaceRecordSummary toInterfaceRecordSummary(InterfaceRecordSummaryModel interfaceRecordSummaryModel){
        InterfaceRecordSummary interfaceRecordSummary = new InterfaceRecordSummary();
        interfaceRecordSummary.setId(interfaceRecordSummaryModel.getId());
        interfaceRecordSummary.setRecordHash(interfaceRecordSummaryModel.getRecordHash());
        interfaceRecordSummary.setMethodName(interfaceRecordSummaryModel.getMethodName());
        interfaceRecordSummary.setProtocolName(interfaceRecordSummaryModel.getProtocolName());
        interfaceRecordSummary.setChannelId(interfaceRecordSummaryModel.getChannelId());
        interfaceRecordSummary.setSuccessed(interfaceRecordSummaryModel.getSuccessed());
        interfaceRecordSummary.setErrorMsg(interfaceRecordSummaryModel.getErrorMsg());
        interfaceRecordSummary.setScAlisa(interfaceRecordSummaryModel.getScAlisa());
        interfaceRecordSummary.setScMethodName(interfaceRecordSummaryModel.getScMethodName());
        interfaceRecordSummary.setScVersion(interfaceRecordSummaryModel.getScVersion());
        interfaceRecordSummary.setTotalCalls(interfaceRecordSummaryModel.getTotalCalls());
        interfaceRecordSummary.setTotalCallTime(interfaceRecordSummaryModel.getTotalCallTime());
        return interfaceRecordSummary;
    }
}
