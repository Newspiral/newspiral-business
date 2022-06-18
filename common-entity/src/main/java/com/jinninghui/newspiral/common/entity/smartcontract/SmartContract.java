package com.jinninghui.newspiral.common.entity.smartcontract;

import com.alibaba.fastjson.JSON;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lida
 * @date 2019/7/11 17:31
 * 智能合约
 */
@ApiModel(description = "智能合约")
@Data
public class SmartContract implements Serializable {

    //TODO 智能合约放在这个package下其实并不合理，跟hotstuff这个算法的包耦合在一起了
    public static final String SMART_CONTRACT_PACKAGE = "com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor";
    /**
     * 所属的通道ID
     */

    //@NotBlank
    @ApiModelProperty(value = "通道ID")
    private String channelId;
    /**
     * 智能合约的class文件的字节数组
     */
    @ApiModelProperty(value = "智能合约的class文件的字节数组")
    private byte[] classFileBytes;


    @ApiModelProperty(value = "智能合约的class文件的字节数组")
    private HashMap<String, byte[]> innerClassFileList;
    /**
     * 智能合约的源码内容
     */
    @ApiModelProperty(value = "智能合约的源码内容")
    private String sourceContent;
    /**
     * class文件的哈希值
     */
    @ApiModelProperty(value = "class文件的哈希值")
    private String classFileHash;
    /**
     * 编码
     */
    @ApiModelProperty(value = "智能合约名称")
    //@Length(min = 1, max = 128, message = "智能合约名称必须在1-128长度之间")
    private String name;
    /**
     * 版本号
     */
    @ApiModelProperty(value = "智能合约版本号")
    //@Length(min = 1, max = 45, message = "智能合约版本号必须在1-45长度之间")
    private String version;
    /**
     * ID值，取值为“Code_Version"
     */
    @ApiModelProperty(value = "智能合约ID")
    private String id;

    /**
     * 状态，1正常；2冻结；3销毁
     */
    @ApiModelProperty(value = "智能合约状态,1正常；2冻结；3销毁")
    private String flag;

    /**
     * 操作状态，安装，升级
     */
    @ApiModelProperty(value = "智能合约操作状态，安装，升级")
    private SmartContractOperationTypeEnum state;

    /**
     * 扩展属性
     */
    @ApiModelProperty(value = "扩展属性")
    private Map<String, String> extendedData = new HashMap<String, String>();


    /**
     * 别名
     */
    @ApiModelProperty(value = "智能合约别名")
    private String alisa;

    @ApiModelProperty(value = "创建时间戳")
    private Date setupTimestamp;

    @ApiModelProperty(value = "更新时间戳")
    private Date updateTime;

    /**
     * id相同且channelId相同就相同，不管内容是否一致
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof SmartContract) {
            SmartContract sc = (SmartContract) o;
            if (o == this) {
                return true;
                // 检查ID是否一致即可
            } else if (sc.getChannelId().equals(this.getChannelId()) && sc.getId().equals(this.getId())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    public String toSimpleString() {
        return "SmartContract:(channelID=" + channelId + ",id=" + id + ")";
    }

    public SmartContract Clone() {
        SmartContract smartContract = new SmartContract();
        smartContract.setId(this.getId());
        smartContract.setName(this.getName());
        smartContract.setFlag(this.getFlag());
        smartContract.setState(this.getState());
        smartContract.setInnerClassFileList(this.getInnerClassFileList());
        smartContract.setChannelId(this.getChannelId());
        smartContract.setClassFileBytes(this.getClassFileBytes());
        smartContract.setClassFileHash(this.getClassFileHash());
        smartContract.setSourceContent(this.getSourceContent());
        smartContract.setVersion(this.getVersion());
        if (!CollectionUtils.isEmpty(this.extendedData)) {
            String extendedDataStr = JSON.toJSONString(this.extendedData);
            smartContract.setExtendedData(JSON.parseObject(extendedDataStr, Map.class));
        }
        smartContract.setAlisa(this.getAlisa());
        smartContract.setSetupTimestamp(this.setupTimestamp);
        smartContract.setUpdateTime(this.updateTime);
        return smartContract;
    }

    public static List<SmartContract> createInstanceList(SmartContractsAuthorized smartContractsAuthorized) {
        List<SmartContract> smartContracts = new ArrayList<>();
        for (SmartContractShort smartContractShort : smartContractsAuthorized.getSmartContractShorts()) {
            SmartContract smartContract = new SmartContract();
            smartContract.setChannelId(smartContractsAuthorized.getChannelId());
            smartContract.setAlisa(smartContractShort.getAlisa());
            smartContract.setVersion(smartContractShort.getVersion());
            smartContracts.add(smartContract);
        }
        return smartContracts;
    }

    /**
     * 仅使用info中有的属性设置smartcontract对应属性，其他属性使用smartContract的默认值
     *
     * @param info
     * @return
     */
    public static SmartContract createInstance(SmartContractInfo info) {
        SmartContract smartContract = new SmartContract();
        smartContract.setAlisa(info.getAlisa());
        smartContract.setChannelId(info.getChannelId());
        smartContract.setExtendedData(info.getExtendedData());
        smartContract.setFlag(SmartContractStateEnum.SMARTCONTRACT_VALID.getCode());
        smartContract.setSourceContent(info.getSourceContent());
        smartContract.setVersion(info.getVersion());
        smartContract.setState(info.getOperationType());
        return smartContract;
    }


}
