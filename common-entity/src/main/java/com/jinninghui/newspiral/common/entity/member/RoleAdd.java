package com.jinninghui.newspiral.common.entity.member;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ApiModel(description = "角色新增参数")
@Data
public class RoleAdd {

    /**
     * 通道Id
     */
    @ApiModelProperty(value = "通道ID")
    private String channelId;
    /**
     * 名称
     */
    @ApiModelProperty(value = "角色名称")
    private String name;
    /**
     * 英文名称
     */
    @ApiModelProperty(value = "角色英文名称")
    private String shortName;
    /**
     * 权限组
     */
    @ApiModelProperty(value = "权限组")
    private List<Integer> authIds;
    /**
     * 备注说明
     */
    @ApiModelProperty(value = "备注说明")
    private String remark;
    /**
     * 扩展属性
     */
    @ApiModelProperty(value = "扩展属性数据")
    private Map<String,String> extendedData = new HashMap<String,String>();
    /**
     * 成员加入该角色的控制策略
     */
    @ApiModelProperty(value = "成员加入该角色的控制策略")
    private MemberAddRoleStrategyEnum memberAddStrategy;

    /**
     * 成员删除该角色的控制策略
     */
    @ApiModelProperty(value = "成员删除该角色的控制策略")
    private MemberRemoveRoleStrategyEnum memberDelStrategy;

    public RoleAdd clone()
    {
        RoleAdd roleAdd=new RoleAdd();
        roleAdd.setChannelId(this.getChannelId());
        roleAdd.setName(this.getName());
        roleAdd.setShortName(this.getShortName());
        roleAdd.setRemark(this.getRemark());
        roleAdd.setExtendedData(this.getExtendedData());
        roleAdd.setMemberAddStrategy(this.getMemberAddStrategy());
        roleAdd.setMemberDelStrategy(this.getMemberDelStrategy());
        List<Integer> authIds = new ArrayList<>();
        if(!CollectionUtils.isEmpty(this.getAuthIds())){
            for (Integer au:this.getAuthIds()) {
                authIds.add(au.intValue());
            }
        }
        roleAdd.setAuthIds(authIds);
        return roleAdd;
    }

    public Role toRole(){
        Role role = new Role();
        role.setChannelId(this.getChannelId());
        role.setName(this.getName());
        role.setShortName(this.getShortName());
        role.setRemark(this.getRemark());
        role.setExtendedData(this.getExtendedData());
        role.setAuths(transferAuthId(this.getAuthIds()));
        if(StringUtils.isEmpty(this.getMemberAddStrategy())){
            role.setMemberAddStrategy(MemberAddRoleStrategyEnum.MANAGER_AGREE);
        }else {
            role.setMemberAddStrategy(this.getMemberAddStrategy());
        }
        if(StringUtils.isEmpty(this.getMemberDelStrategy())){
            role.setMemberDelStrategy(MemberRemoveRoleStrategyEnum.PARENT_AGREE);
        }else {
            role.setMemberDelStrategy(this.getMemberDelStrategy());
        }
        //新增角色的时候，角色类型肯定为自定义类型
        role.setRoleFlag(2);
        role.setState(RoleStateEnum.NORMOR.getCode());
        role.setCreateTime(new Date().getTime());
        //新增的时候不需要update时间
        //role.setUpdateTime(new Date());
        return role;
    }

    private List<Auth> transferAuthId(List<Integer> authIds){
        if(CollectionUtils.isEmpty(authIds)){
            return new ArrayList<Auth>();
        }
        Set<Integer> authIdset = authIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        List<Auth> auth = new ArrayList<>();
        try {
            for(Integer authId: authIdset) {
                Auth au = new Auth();
                au.setAuthId(authId);
                auth.add(au);
            }
        } catch (Exception e) {
            return null;
        }
        return auth;
    }

}
