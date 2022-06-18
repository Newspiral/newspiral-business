package com.jinninghui.newspiral.ledger.mgr.impl.mapper;

import com.jinninghui.newspiral.ledger.mgr.impl.domain.MemberModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @version V1.0
 * @Title: MemberMapper
 * @Package com.jinninghui.newspiral.ledger.mgr.impl.mapper
 * @Description:
 * @author: xuxm
 * @date: 2020/1/11 10:48
 */
public interface MemberMapper {
    int deleteByPrimaryKey(String memberId);
    MemberModel selectByPrimaryKey(Long memberId);
    MemberModel selectByPublicKey(String channelId,String publicKey);
    int insert(MemberModel record);
    int updateByPrimaryKeySelective(MemberModel record);
    List<MemberModel> selectAll();
    List<MemberModel> selectByCondition(String channelId,Long id,String roleId);
    List<MemberModel> selectByChannelAndStatus(String channelId,String status);
    List<MemberModel> selectMembersOfRole(String channelId, String roleName);
    MemberModel selectOrganziationMember(String channelId,String issuerId );
    int deleteByChannelId(@Param("channelId")String channelId);
}
