package com.jinninghui.newspiral.gateway.vo.req;

import com.jinninghui.newspiral.common.entity.ConsensusAlgorithmEnum;
import com.jinninghui.newspiral.common.entity.chain.ChannelModifyStrategyEnum;
import com.jinninghui.newspiral.common.entity.chain.MemberAddChannelStrategyEnum;
import com.jinninghui.newspiral.common.entity.chain.MemberModifyChannelStrategyEnum;
import com.jinninghui.newspiral.common.entity.chain.MemberRemoveChannelStrategyEnum;
import com.jinninghui.newspiral.common.entity.chain.PeerAddChannelStrategyEnum;
import com.jinninghui.newspiral.common.entity.chain.PeerModifyChannelStrategyEnum;
import com.jinninghui.newspiral.common.entity.chain.PeerRemoveChannelStrategyEnum;
import com.jinninghui.newspiral.common.entity.chain.RoleAddStrategyEnum;
import com.jinninghui.newspiral.common.entity.chain.RoleDelStrategyEnum;
import com.jinninghui.newspiral.common.entity.chain.RoleModifyStrategyEnum;
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
@ApiModel("通道的基本参数")
@Data
public class ChannelBasicParamsReq {

    @ApiModelProperty("通道名称")
    @NotBlank
    private String name;

    /**
     * 本链的最大块大小，单位字节；区块构建时，如果交易总字节数达到此值，则生成一个新的区块，建议1M以上
     */
    @ApiModelProperty(" 本通道的最大块大小，最多包含500条交易")
    @NotNull
    private Long blockMaxSize=500000L;

    /**
     * 单位毫秒，如果blockMaxInterval时间范围内存在至少一笔交易，则生成一个区块
     */
    @ApiModelProperty("区块生成间隔，默认为2秒")
    @NotNull
    private Long blockMaxInterval=2000L;

    /**
     * 加入该链的最大节点数量
     */
    @ApiModelProperty("加入该通道的最大节点数，默认为100")
    @NotNull
    private Integer maxPeerCount=100;

    /**
     * 节点加入链的控制策略
     */
    @ApiModelProperty("节点加入通道的控制策略，默认为通道节点中2/3以上同意即可")
    private PeerAddChannelStrategyEnum peerAddStrategyEnum=PeerAddChannelStrategyEnum.ABSOLUTE_MAJORITY_AGREE;

    @ApiModelProperty("节点从通道移除的控制策略,默认为父成员同意")
    private PeerRemoveChannelStrategyEnum peerRemoveChannelStrategyEnum = PeerRemoveChannelStrategyEnum.PARENT_AGREE;

    @ApiModelProperty("通道中节点修改的控制策略,通道节点中2/3以上同意即可")
    private PeerModifyChannelStrategyEnum peerModifyChannelStrategyEnum = PeerModifyChannelStrategyEnum.ABSOLUTE_MAJORITY_AGREE;

    /**
     * 修改链的元数据的策略
     */
    @ApiModelProperty("修改通道元数据的策略,通道节点中2/3以上同意即可")
    private ChannelModifyStrategyEnum modifyStrategy=ChannelModifyStrategyEnum.ABSOLUTE_MAJORITY_AGREE;

    /**
     * 智能合约部署、升级控制策略
     */
    @ApiModelProperty("智能合约部署、升级控制策略,通道管理员同意即可")
    private SmartContractDeplyStrategyEnum smartContractDeplyStrategy=SmartContractDeplyStrategyEnum.MANAGER_AGREE;


    /**
     * 该链的共识算法，用于共识区块
     */
    @ApiModelProperty("该链的共识算法，用于共识区块,默认为NewSpiralHotStuff")
    private ConsensusAlgorithmEnum consensusAlgorithm=ConsensusAlgorithmEnum.NEWSPIRAL_HOT_STUFF;

    /**
     * 节点为该链创建的交易池的大小，单位字节//10M
     */
    @ApiModelProperty("节点为该链创建的交易池的大小，默认大小10M")
    @NotNull
    private Long txPoolSize=1024 * 1024 * 1024 * 10L;

    /**
     * 接收到请求的创建时间与本地时间差距大于此值的，直接在入口处拒绝该请求
     */
    @ApiModelProperty("接收到请求的创建时间与本地时间差距大于此值的，直接在入口处拒绝该请求,默认为3秒")
    @NotNull
    private Long allowTimeErrorSeconds=3L;

    /**
     * 使用的安全服务的key，用于指定使用何种加解密算法(OsccaSecurityServiceImpl)
     */
    @ApiModelProperty("使用的安全服务的key，用于指定使用何种加解密算法(OsccaSecurityServiceImpl)")
    @NotBlank
    private String securityServiceKey="OsccaSecurityServiceImpl";

    @ApiModelProperty("扩展参数(超时时间，数据版本等)")
    @Valid
    @NotEmpty
    private Map<String,String> extendsParams = new HashMap<String,String>(){
        {
            put("viewTimeoutMs", String.valueOf(2000L + 2000L));
            put("data_version", "Version1.0");
        }
    };

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
    @ApiModelProperty("交易池最大待打包交易数,达到该值后，将会拒绝新的交易进节点，默认为100000条")
    @NotNull
    private Long maxTransactionCount=100000L;

    /**
     * 成员加入链的控制策略
     */
    @ApiModelProperty("成员加入通道的控制策略,默认通道管理员同意即可")
    MemberAddChannelStrategyEnum memberAddChannelStrategyEnum=MemberAddChannelStrategyEnum.MANAGER_AGREE;

    /**
     * 成员修改链的控制策略
     */
    @ApiModelProperty("通道成员修改的控制策略,默认通道管理员同意即可")
    MemberModifyChannelStrategyEnum memberModifyChannelStrategyEnum=MemberModifyChannelStrategyEnum.MANAGER_AGREE;
    /**
     * 成员退出加入链的控制策略
     */
    @ApiModelProperty("成员退出加入链的控制策略,默认父成员同意即可")
    MemberRemoveChannelStrategyEnum memberRemoveChannelStrategyEnum=MemberRemoveChannelStrategyEnum.PARENT_AGREE;

    /**
     * 角色添加策略
     */
    @ApiModelProperty("角色添加策略,默认为通道管理员同意")
    RoleAddStrategyEnum roleAddStrategyEnum=RoleAddStrategyEnum.MANAGER_AGREE;


    /**
     * 角色修改策略
     */
    @ApiModelProperty("角色修改策略,默认为通道管理员同意")
    RoleModifyStrategyEnum roleModifyStrategyEnum=RoleModifyStrategyEnum.MANAGER_AGREE;


    /**
     * 角色删除策略
     */
    @ApiModelProperty("角色删除策略,默认为通道管理员同意")
    RoleDelStrategyEnum roleDelStrategyEnum=RoleDelStrategyEnum.MANAGER_AGREE;
}
