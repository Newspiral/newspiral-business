package com.jinninghui.newspiral.common.entity.member;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version V1.0
 * @Title: Member
 * @Package com.jinninghui.newspiral.common.entity.member
 * @Description:
 * @author: xuxm
 * @date: 2020/1/11 13:56
 */
@Data
public class Member implements Serializable {
    /**
     * 主键
     */
    @ApiModelProperty(value = "成员ID")
    private Long id;
    /**
     * 数字证书的公钥
     */
    @ApiModelProperty(value = "数字证书公钥")
    private String publicKey;
    /**
     * 名称
     */
    @ApiModelProperty(value = "成员名称")
    private String name;
    /**
     * 签发者ID
     */
    @ApiModelProperty(value = "签发者ID")
    private String issuerId;
    /**
     * 扩展属性
     */
    @ApiModelProperty(value = "扩展属性数据")
    private Map<String, String> extendedData = new HashMap<String, String>();
    /**
     * 通道编号
     */
    @ApiModelProperty(value = "通道ID")
    private String channelId;
    /**
     * 证书
     */
    @ApiModelProperty(value = "证书")
    private String certificateCerFile;

    /**
     * 状态0正常，1证书过期，2业务冻结，3业务删除
     */
    @ApiModelProperty(value = "证书状态：0正常，1证书过期，2业务冻结，3业务删除")
    private Integer status;

    /**
     * 签名算法
     */
    @ApiModelProperty(value = "签名算法")
    private String signAlgorithm;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Long createTime;
    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间")
    private Long updateTime;
    /**
     * 角色组
     */
    @ApiModelProperty(value = "角色组")
    private List<Role> roles = new ArrayList<>();

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof Member)) {
            return false;
        }

        Member newMember = (Member) obj;
        if (!newMember.getName().equals(this.getName())
                || !newMember.getPublicKey().equals(this.getPublicKey())
                || !newMember.getIssuerId().equals(this.getIssuerId())
                || !newMember.getChannelId().equals(this.getChannelId())
                || newMember.getStatus().intValue() != this.getStatus().intValue()) {
            return false;
        }

        if (!StringUtils.isEmpty(newMember.getExtendedData()) && !StringUtils.isEmpty(this.getExtendedData())) {
            if (!newMember.getExtendedData().equals(this.getExtendedData())) {
                return false;
            }
        } else if (StringUtils.isEmpty(newMember.getExtendedData()) && StringUtils.isEmpty(this.getExtendedData())) {

        } else {
            return false;
        }

        if (!StringUtils.isEmpty(newMember.getSignAlgorithm()) && !StringUtils.isEmpty(this.getSignAlgorithm())) {
            if (!newMember.getSignAlgorithm().equals(this.getSignAlgorithm())) {
                return false;
            }
        } else if (StringUtils.isEmpty(newMember.getSignAlgorithm()) && StringUtils.isEmpty(this.getSignAlgorithm())) {

        } else {
            return false;
        }

        if (newMember.getRoles().size() != this.getRoles().size()) {
            return false;
        }

        //role进行比较
        //旧的先存起来
        Map<String, Role> oldRoleMap = new HashMap<>();
        if (this.getRoles() != null) {
            for (Role oldR : this.getRoles()) {
                oldRoleMap.put(oldR.getRoleId(), oldR);
            }
        }
        //新的加入
        if (newMember.getRoles() != null) {
            for (Role role : newMember.getRoles()) {
                Role oldRole = oldRoleMap.get(role.getRoleId());
                if (oldRole == null) {
                    return false;
                } else {
                    if (!oldRole.equals(role)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public Member clone() {
        Member member = new Member();
        member.setId(this.getId());
        member.setName(this.getName());
        member.setIssuerId(this.getIssuerId());
        member.setPublicKey(this.getPublicKey());
        member.setChannelId(this.getChannelId());
        member.setCertificateCerFile(this.getCertificateCerFile());
        member.setExtendedData(this.getExtendedData());
        List<Role> roles = new ArrayList<>();
        for (Role role : this.getRoles()) {
            roles.add(role.clone());
        }
        member.setRoles(roles);
        member.setStatus(this.getStatus());
        member.setSignAlgorithm(this.getSignAlgorithm());
        member.setCreateTime(this.createTime);
        member.setUpdateTime(this.updateTime);
        return member;
    }

    public static Member createInstance(MemberInfo info) {
        Member member = new Member();
        member.setCertificateCerFile(info.getCertificateCerFile());
        List<Role> roleList = new ArrayList<>();
        for (String roleId : info.getRoleIdList()) {
            Role role = new Role();
            role.setRoleId(roleId);
            roleList.add(role);
        }
        member.setRoles(roleList);
        member.setExtendedData(info.getExtendedData());
        return member;
    }

    public static Member createInstance(MemberUpdateInfo info) {
        Member member = Member.createInstance(info.getMemberInfo());
        member.setStatus(info.getStateEnum()==null?null:info.getStateEnum().getCode());
        return member;
    }

    public static Member createInstance(MemberDeleteInfo info) {
        Member member = new Member();
        member.setPublicKey(info.getPublicKey());
        member.setChannelId(info.getChannelId());
        return member;
    }
}
