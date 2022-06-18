package com.jinninghui.newspiral.common.entity.chain;

import com.jinninghui.newspiral.common.entity.ConsensusAlgorithmEnum;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContractDeplyStrategyEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lida
 * @date 2019/7/15 9:56
 * 一条链的基本参数
 */
@ApiModel(description = "通道基本参数")
@Data
public class ChannelBasicParams {

    @ApiModelProperty(value = "通道名称")
    @NotBlank
    private String name;

    /**
     * 本链的最大块大小，单位字节；区块构建时，如果交易总字节数达到此值，则生成一个新的区块，建议1M以上
     */
    @ApiModelProperty(value = "区块最大字节")
    @NotNull
    Long blockMaxSize;

    /**
     * 单位毫秒，如果blockMaxInterval时间范围内存在至少一笔交易，则生成一个区块
     */
    @ApiModelProperty(value = "区块生成最大时间间隔")
    @NotNull
    Long blockMaxInterval;

    /**
     * 加入该链的最大节点数量
     */
    @ApiModelProperty(value = "通道的最大节点数量")
    @NotNull
    Integer maxPeerCount;

    /**
     * 节点加入链的控制策略
     */
    @ApiModelProperty(value = "节点加入通道的控制策略")
    PeerAddChannelStrategyEnum peerAddStrategyEnum;

    /**
     * 修改链的元数据的策略
     */
    @ApiModelProperty(value = "修改通道元数据的策略")
    ChannelModifyStrategyEnum modifyStrategy;

    /**
     * 智能合约部署、升级控制策略
     */
    @ApiModelProperty(value = "智能合约部署、升级控制策略")
    SmartContractDeplyStrategyEnum smartContractDeplyStrategy;


    /**
     * 该链的共识算法，用于共识区块
     */
    @ApiModelProperty(value = "该通道的共识算法")
    ConsensusAlgorithmEnum consensusAlgorithm;

    /**
     * 节点为该链创建的交易池的大小，单位字节
     */
    @ApiModelProperty(value = "本节点该通道的交易池大小")
    @NotNull
    Long txPoolSize;

    /**
     * 接收到请求的创建时间与本地时间差距大于此值的，直接在入口处拒绝该请求
     */
    @ApiModelProperty(value = "最大请求时间")
    @NotNull
    Long allowTimeErrorSeconds;

    /**
     * 使用的安全服务的key，用于指定使用何种加解密算法(OsccaSecurityServiceImpl)
     */
    @ApiModelProperty(value = "安全服务key，用于指定加密算法")
    @NotBlank
    String securityServiceKey;

    @ApiModelProperty(value = "扩展参数（共识算法相关）")
    @Valid
    @NotEmpty
    Map<String,String> extendsParams = new HashMap<String,String>();

    /**
     * 获得一些扩展参数，这些扩展参数是与特定共识算法相关的
     * @param key
     * @return
     */
    public String getExtendParam(String key)
    {
        return extendsParams.get(key);
    }

    /**
     * 加入该链的最大交易数
     */
    @ApiModelProperty(value = "加入该链的最大交易数")
    @NotNull
    Long maxTransactionCount;

    /**
     * 创建时间戳
     */
    @ApiModelProperty(value = "创建时间戳")
    Long createTimestamp;

    /**
     * 组织ID
     */
    @ApiModelProperty(value = "创建者组织ID")
    private String organizationId;

    /**
     * 成员加入链的控制策略
     */
    @ApiModelProperty(value = "成员加入链的控制策略")
    MemberAddChannelStrategyEnum memberAddChannelStrategyEnum;

    @ApiModelProperty(value = "成员修改链的控制策略")
    MemberModifyChannelStrategyEnum memberModifyChannelStrategyEnum;

    /**
     * 成员退出加入链的控制策略
     */
    @ApiModelProperty(value = "成员退出加入链的控制策略")
    MemberRemoveChannelStrategyEnum memberRemoveChannelStrategyEnum;


    @ApiModelProperty(value = "角色添加策略")
    RoleAddStrategyEnum roleAddStrategyEnum;


    @ApiModelProperty(value = "角色修改策略")
    RoleModifyStrategyEnum roleModifyStrategyEnum;


    @ApiModelProperty(value = "角色删除策略")
    RoleDelStrategyEnum roleDelStrategyEnum;

}
