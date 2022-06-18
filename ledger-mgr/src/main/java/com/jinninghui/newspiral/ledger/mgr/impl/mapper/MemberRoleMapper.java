package com.jinninghui.newspiral.ledger.mgr.impl.mapper;

import com.jinninghui.newspiral.ledger.mgr.impl.domain.MemberRoleModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @version V1.0
 * @Title: MemberRoleMapper
 * @Package com.jinninghui.newspiral.ledger.mgr.impl.mapper
 * @Description:
 * @author: xuxm
 * @date: 2020/1/11 10:48
 */
public interface MemberRoleMapper {
    int deleteByPrimaryKey(Long id);
    int deleteByMemberId(Long memberId);
    MemberRoleModel selectByPrimaryKey(Long id);
    List<MemberRoleModel> selectByMemberId(String shortName);
    int insert(MemberRoleModel record);
    int updateByPrimaryKeySelective(MemberRoleModel record);
    List<MemberRoleModel> selectAll();
    int deleteByChannelId(@Param("channelId")String channelId);

    /**
     * 根据roleId进行删除角色成员关系
     * @param roleId
     */
    void delCustomRoleRelation(String roleId);
}
