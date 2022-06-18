package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.jinninghui.newspiral.common.entity.member.Auth;
import com.jinninghui.newspiral.common.entity.member.MemberAddRoleStrategyEnum;
import com.jinninghui.newspiral.common.entity.member.MemberRemoveRoleStrategyEnum;
import com.jinninghui.newspiral.common.entity.member.Role;
import lombok.Data;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version V1.0
 * @Title: RoleModel
 * @Package com.jinninghui.newspiral.ledger.mgr.impl.domain
 * @Description:
 * @author: xuxm
 * @date: 2020/1/10 11:11
 */
@Data
public class RoleModel {
    /**
     * 主键，数据库主键
     */
    private Long id;

    /**
     * 角色ID，全局唯一
     */
    private String roleId;

    /**
     * 中文名称
     */
    private String name;

    /**
     * 英文名称
     */
    private String shortName;

    /**
     * 成员新增策略
     */
    private String memberAddStrategy;
    /**
     * 成员移除策略
     */
    private String memberDelStrategy;
    /**
     * 角色类型（1：预定义；2：自定义）
     */
    private Integer roleFlag;
    /**
     * 通道Id
     */
    private String channelId;
    /**
     * 扩展属性
     */
    private String extendedData;
    /**
     * 备注说明
     */
    private String remark;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 权限列表
     */
    private String authId;

    /**
     * 状态，（1正常 2业务删除）
     */
    private Integer state;


    /**
     *
     * @return
     */
    public Role toRole()
    {
        Gson gson = new Gson();
        Map<String,String> map = new HashMap<>();
        Role role=new Role();
        role.setRoleId(this.getRoleId());
        role.setName(this.getName());
        role.setShortName(this.getShortName());
        role.setChannelId(this.getChannelId());
        role.setRoleFlag(this.getRoleFlag());
        if(!StringUtils.isEmpty(this.getExtendedData())) {
            role.setExtendedData(gson.fromJson(this.getExtendedData(),map.getClass()));
        }
        role.setRemark(this.getRemark());
        role.setMemberAddStrategy(JSON.parseObject(this.memberAddStrategy, MemberAddRoleStrategyEnum.class));
        role.setMemberDelStrategy(JSON.parseObject(this.memberDelStrategy, MemberRemoveRoleStrategyEnum.class));
        role.setCreateTime(this.getCreateTime()!=null?this.getCreateTime().getTime()/1000:null);
        role.setUpdateTime(this.getUpdateTime()!=null?this.getUpdateTime().getTime()/1000:null);
        role.setState(this.getState());
        return role;
    }

    /**
     *
     * @param role
     * @return
     */
    public static RoleModel createInstance(Role role)
    {
        RoleModel roleModel=new RoleModel();
        roleModel.setRoleId(role.getRoleId());
        roleModel.setName(role.getName());
        roleModel.setShortName(role.getShortName());
        roleModel.setRoleFlag(role.getRoleFlag());
        roleModel.setChannelId(role.getChannelId());
        roleModel.setState(role.getState());
        if(!CollectionUtils.isEmpty(role.getExtendedData())) {
            roleModel.setExtendedData(JSON.toJSONString(role.getExtendedData()));
        }
        roleModel.setRemark(role.getRemark());

        roleModel.setCreateTime(role.getCreateTime()!=null?new Date(role.getCreateTime()):null);
        roleModel.setUpdateTime(role.getUpdateTime()!=null?new Date(role.getUpdateTime()):null);
        //成员添加策略以及成员删除策略是否为空
        if(StringUtils.isEmpty(role.getMemberAddStrategy())){
            roleModel.setMemberAddStrategy(JSON.toJSONString(MemberAddRoleStrategyEnum.MAJORITY_AGREE));
        }else {
            roleModel.setMemberAddStrategy(JSON.toJSONString(role.getMemberAddStrategy()));
        }
        if(StringUtils.isEmpty(role.getMemberDelStrategy())){
            roleModel.setMemberDelStrategy(JSON.toJSONString(MemberRemoveRoleStrategyEnum.PARENT_AGREE));
        }else {
            roleModel.setMemberDelStrategy(JSON.toJSONString(role.getMemberDelStrategy()));
        }
        //权限组
        if(null != role.getAuths() && role.getAuths().size()!=0){
            roleModel.setAuthId(transferToAuthIds(role.getAuths()));
        }else {
            roleModel.setAuthId(null);
        }
        return roleModel;
    }

    private static String  transferToAuthIds(List<Auth> auths){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <auths.size() ; i++) {
            sb.append(auths.get(i).getAuthId());
            if(i!=auths.size()-1){
                sb.append(",");
            }
        }
        return sb.toString();
    }


}
