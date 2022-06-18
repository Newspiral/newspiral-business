package com.jinninghui.newspiral.common.entity.member;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version V1.0
 * @Title: Role
 * @Package com.jinninghui.newspiral.common.entity.member
 * @Description:
 * @author: xuxm
 * @date: 2020/1/11 14:05
 */
@ApiModel(description = "角色信息")
@Data
@Slf4j
public class Role implements Serializable {
    /**
     * 主键
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
     * 成员加入该角色的控制策略
     */
    @ApiModelProperty(value = "成员加入该角色的控制策略")
    private MemberAddRoleStrategyEnum memberAddStrategy;

    /**
     * 成员移除该角色的控制策略
     */
    @ApiModelProperty(value = "成员移除该角色的控制策略")
    private MemberRemoveRoleStrategyEnum memberDelStrategy;
    /**
     * 角色类型（1：预定义；2：自定义）
     */
    @ApiModelProperty(value = "角色类型：1预定义，2自定义")
    private Integer roleFlag;
    /**
     * 通道Id
     */
    @ApiModelProperty(value = "通道ID")
    private String channelId;
    /**
     * 备注说明
     */
    @ApiModelProperty(value = "备注说明")
    private String remark;
    /**
     * 扩展属性
     */
    @ApiModelProperty(value = "扩展属性数据")
    private  Map<String,String> extendedData = new HashMap<String,String>();
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Long createTime;
    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private Long updateTime;

    /**
     * 状态，（1正常 2逻辑删除）
     */
    @ApiModelProperty(value = "状态,（1正常 2逻辑删除）")
    private Integer state;

    @ApiModelProperty(value = "权限组")
    private List<Auth> auths=new ArrayList<>();

    @Override
    public boolean equals(Object obj) {
        //如果不是一个类型则为false
        if (!(obj instanceof Role)) {
            return false;
        }
        Role newRole= (Role) obj;
        Role oldRole = (Role) this;
        //对属性进行判断
        //1.roleId
        if(newRole.getRoleId()!= null){
            if(!newRole.getRoleId().equals(oldRole.getRoleId())){
                return false;
            }
        }else if(oldRole.getRoleId()!=null){
            return false;
        }
        //2.name
        if(newRole.getName()!= null){
            if(!newRole.getName().equals(oldRole.getName())){
                return false;
            }
        }else if(oldRole.getName()!=null){
            return false;
        }
        //3shortName
        if(newRole.getShortName()!= null){
            if(!newRole.getShortName().equals(oldRole.getShortName())){
                return false;
            }
        }else if(oldRole.getShortName()!=null){
            return false;
        }
        //4 memberAddStrategy
        if(newRole.getMemberAddStrategy()!= null){
            if(!newRole.getMemberAddStrategy().equals(oldRole.getMemberAddStrategy())){
                return false;
            }
        }else if(oldRole.getMemberAddStrategy()!=null){
            return false;
        }
        //5 memberRemoveStrategy
        if(newRole.getMemberDelStrategy()!= null){
            if(!newRole.getMemberDelStrategy().equals(oldRole.getMemberDelStrategy())){
                return false;
            }
        }else if(oldRole.getMemberDelStrategy()!=null){
            return false;
        }
        //6 roleFlag
        if(newRole.getRoleFlag()!= null){
            if(!newRole.getRoleFlag().equals(oldRole.getRoleFlag())){
                return false;
            }
        }else if(oldRole.getRoleFlag()!=null){
            return false;
        }
        //7 channelId
        if(newRole.getChannelId()!= null){
            if(!newRole.getChannelId().equals(oldRole.getChannelId())){
                return false;
            }
        }else if(oldRole.getChannelId()!=null){
            return false;
        }
        //8 remark
        if(newRole.getRemark()!= null){
            if(!newRole.getRemark().equals(oldRole.getRemark())){
                return false;
            }
        }else if(oldRole.getRemark()!=null){
            return false;
        }
        //9 extendDate
        if(newRole.getExtendedData()!= null){
            if(!newRole.getExtendedData().equals(oldRole.getExtendedData())){
                return false;
            }
        }else if(oldRole.getExtendedData()!=null){
            return false;
        }
        //12 state
        if(newRole.getState()!= null){
            if(!newRole.getState().equals(oldRole.getState())){
                return false;
            }
        }else if(oldRole.getState()!=null){
            return false;
        }
        //13 auth
        //判断auth是否相等
        for (int i = 0; i <newRole.getAuths().size() ; i++) {
            Auth newAuth = new Auth();
            Auth oldAuth = new Auth();
            newAuth = newRole.getAuths().get(i).clone();
            oldAuth = this.getAuths().get(i).clone();
            //判断auth中属性是否相同
            // authCode
            if(newAuth.getAuthCode()!= null){
                if(!newAuth.getAuthCode().equals(oldAuth.getAuthCode())){
                    return false;
                }
            }else if(oldAuth.getAuthCode()!=null){
                return false;
            }

            // authName
            if(newAuth.getAuthName()!= null){
                if(!newAuth.getAuthName().equals(oldAuth.getAuthName())){
                    return false;
                }
            }else if(oldAuth.getAuthName()!=null){
                return false;
            }

            // authId
            if(newAuth.getAuthId()!= null){
                if(!newAuth.getAuthId().equals(oldAuth.getAuthId())){
                    return false;
                }
            }else if(oldAuth.getAuthId()!=null){
                return false;
            }

            // authDescription
            if(newAuth.getAuthDescription()!= null){
                if(!newAuth.getAuthDescription().equals(oldAuth.getAuthDescription())){
                    return false;
                }
            }else if(oldAuth.getAuthDescription()!=null){
                return false;
            }

        }


//        try {
//            if(!newRole.getRoleId().equals(this.getRoleId())
//                    ||!newRole.getName().equals(this.getName())
//                    ||!newRole.getShortName().equals(this.getShortName())
//                    ||newRole.getRoleFlag().intValue()!=this.getRoleFlag().intValue()
//                    ||!newRole.getChannelId().equals(this.getChannelId())
//                    ||newRole.getMemberAddStrategy()!=this.getMemberAddStrategy()
//                    ||newRole.getMemberDelStrategy()!=this.getMemberDelStrategy()
//                    ||!newRole.getExtendedData().equals(this.getExtendedData())
//                    || newRole.getState().intValue()!=this.getState().intValue()
//                    || !newRole.getRemark().equals(this.getRemark())
//                    || !newRole.getCreateTime().equals(this.getCreateTime())
//                    || !newRole.getUpdateTime().equals(this.getUpdateTime()))
//            {
//                return false;
//            }
//        }
//        catch (Exception e)
//        {
//            return false;
//        }
        return true;
    }


    public Role clone()
    {
        Role role=new Role();
        role.setRoleId(this.getRoleId());
        role.setChannelId(this.getChannelId());
        role.setRoleFlag(this.getRoleFlag());
        role.setShortName(this.getShortName());
        role.setName(this.getName());
        role.setRemark(this.getRemark());
        role.setState(this.getState());
        role.setMemberAddStrategy(this.getMemberAddStrategy());
        role.setExtendedData(this.getExtendedData());
        role.setMemberDelStrategy(this.getMemberDelStrategy());
        role.setState(this.state);
        List<Auth> auths=new ArrayList<>();
        if (this.auths != null){
            for (Auth auth:this.auths)
            {
                auths.add(auth.clone());
            }
        }
        role.setAuths(auths);
        role.setUpdateTime(this.getUpdateTime());
        role.setCreateTime(this.getCreateTime());
        return role;
    }

}
