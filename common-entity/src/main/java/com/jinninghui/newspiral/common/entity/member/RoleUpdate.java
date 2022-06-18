package com.jinninghui.newspiral.common.entity.member;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ApiModel(description = "角色修改参数")
@Data
public class RoleUpdate {

    /**
     * 通道Id
     */
    @ApiModelProperty(value = "通道ID")
    private String channelId;
    /**
     * 角色id
     */
    @ApiModelProperty(value = "角色ID")
    private String roleId;
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
     * 备注说明
     */
    @ApiModelProperty(value = "备注说明")
    private String remark;
    /**
     * 权限组
     */
    @ApiModelProperty(value = "权限组")
    private List<Integer> authIds;
    /**
     * 扩展属性
     */
    @ApiModelProperty(value = "扩展属性数据")
    private Map<String,String> extendedData = new HashMap<String,String>();

    /**
     * 成员新增策略
     */
    @ApiModelProperty(value = "成员新增策略")
    private MemberAddRoleStrategyEnum memberAddStrategy;
    /**
     * 成员删除策略
     */
    @ApiModelProperty(value = "成员删除策略")
    private MemberRemoveRoleStrategyEnum memberDelStrategy;

    public RoleUpdate clone(){
        RoleUpdate roleUpdate = new RoleUpdate();
        roleUpdate.setChannelId(this.getChannelId());
        roleUpdate.setRoleId(this.getRoleId());
        roleUpdate.setName(this.getName());
        roleUpdate.setShortName(this.getShortName());
        roleUpdate.setRemark(this.getRemark());
        roleUpdate.setExtendedData(this.getExtendedData());
        List<Integer> authIds = new ArrayList<>();
        if(!CollectionUtils.isEmpty(this.getAuthIds())){
            for (Integer au:this.getAuthIds()) {
                authIds.add(au.intValue());
            }
        }
        roleUpdate.setAuthIds(authIds);
        roleUpdate.setMemberAddStrategy(this.getMemberAddStrategy());
        roleUpdate.setMemberDelStrategy(this.getMemberDelStrategy());
        return roleUpdate;
    }


    public Role toRole(){
        Role role = new Role();
        role.setRoleId(this.getRoleId());
        role.setChannelId(this.getChannelId());
        role.setName(this.getName());
        role.setShortName(this.getShortName());
        role.setRemark(this.getRemark());
        role.setExtendedData(this.getExtendedData());
        role.setAuths(transferAuthId(this.getAuthIds()));
        role.setMemberAddStrategy(this.getMemberAddStrategy());
        role.setMemberDelStrategy(this.getMemberDelStrategy());
        return role;
    }


    private List<Auth> transferAuthId(List<Integer> authIds){
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
