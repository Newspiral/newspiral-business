package com.jinninghui.newspiral.ledger.mgr;

import com.jinninghui.newspiral.common.entity.member.Auth;
import com.jinninghui.newspiral.common.entity.member.Member;
import com.jinninghui.newspiral.common.entity.member.QueryMemberListReq;
import com.jinninghui.newspiral.common.entity.member.QueryRoleListParams;
import com.jinninghui.newspiral.common.entity.member.QueryRoleParams;
import com.jinninghui.newspiral.common.entity.member.Role;

import java.util.List;

/**
 * @version V1.0
 * @Title: MemberLedgerMgr
 * @Package com.jinninghui.newspiral.ledger.mgr
 * @Description:
 * @author: xuxm
 * @date: 2020/2/3 18:47
 */
public interface MemberLedgerMgr {

    /**
     * 查询权限表中的所有权限id
     * @return
     */
    List<Integer> getAuthIds();

    /**
     * 查询权限id通过接口的方法名
     * @param methodName 方法名
     * @return authId，以逗号分隔
     */
    List<String> getAuthIdByInterfaceInfo(String methodName);

    /**
     * 查询权限id通过member里的channelId和公钥信息
     * @param channelId 通道id
     * @param publicKey 公钥
     * @return authId，以逗号分隔
     */
    List<String> getAuthIdByMemberInfo(String channelId, String publicKey);
    /**
     * 查询权限列表
     * @return List<Auth>
     */
    List<Auth> getAuthList();

    /**
     * 新增自定义角色
     * @param role
     */
    void insertCustomRole(Role role);

    /**
     * 修改自定义角色
     * @param role
     */
    void updateCustomRole(Role role);

    /**
     * 根据通道查询所有角色列表
     * @param channelId
     * @return
     */
    List<Role> getRoleList(String channelId);
//**********************************************start**********************************************************************
    /**
     * 根据通道查询所有角色
     * @param queryRoleListParams
     * @return
     * @author wuhuaijiang
     */
    public List<Role> getRoleList(QueryRoleListParams queryRoleListParams);

    /**
     * 根据通道查询角色信息
     * @param queryRoleParams
     * @return
     */
    public Role getRole(QueryRoleParams queryRoleParams);
//********************************************end***********************************************************************

    /**
     * 根据角色ID查询角色
     * @param roleId
     * @return
     */
    Role getRole(String roleId);

    List<Role> getRoleByRoleFlag(int flag);

    /**
     * 根据成员ID查询成员，或者成员的公钥和通道信息，包括成员的角色信息
     * @param form
     * @return
     */
    Member getMember(Member form);

    /**
     *  新增成员
     * @param member
     */
    void insertMember(Member member);

    /**
     * 修改成员
     * @param member
     */
    void updateMember(Member member);

    /**
     * 查询成员列表
     * @param form
     * @return
     */
    List<Member> getMemberList(QueryMemberListReq form);

    /**
     * 根据通道Id查询组织成员
     * @param channelId
     * @return
     */
    Member queryOrganziationMember(String channelId,String organizationId);

    /**
     *
     * @param channelId
     */
    void clearChannelCache(String channelId);

    /**
     * 查询授权成员
     * @param form
     * @return
     */
    Member getAuthorizedMember(Member form);

    /**
     * 获取所有成员
     * @return
     */
    List<Member> getAllMemberList();


    /**
     * 返回member（不包含角色列表）
     *
     * @param channelId 通道id
     * @param publicKey 公钥
     * @return member
     */
    Member getMemberByKey(String channelId,String publicKey);

    /**
     * 返回带roleList的member
     *
     * @param channelId 通道id
     * @param publicKey 公钥
     * @return member，带roleList
     */
    Member getMemberRoleByKey(String channelId,String publicKey);


    void delCustomRoleRelation(String roleId);

    /**
     * 根据输入参数查询该通道中是否存在以该名称命名的角色
     * @param channelId
     * @param name
     * @param shortName
     * @return
     */
    List<Role> getRoleByChannelIdandParams(String channelId, String name, String shortName);

    /**
     * 根据AuthCode查询AuthId
     */
    String getAuthIdByAuthCode(String authCode);
}
