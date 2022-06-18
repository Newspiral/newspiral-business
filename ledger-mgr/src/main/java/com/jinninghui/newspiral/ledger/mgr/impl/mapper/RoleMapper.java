package com.jinninghui.newspiral.ledger.mgr.impl.mapper;

import com.jinninghui.newspiral.ledger.mgr.impl.domain.RoleModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @version V1.0
 * @Title: RoleMapper
 * @Package com.jinninghui.newspiral.ledger.mgr.impl.mapper
 * @Description:
 * @author: xuxm
 * @date: 2020/1/11 10:05
 */
public interface RoleMapper {

    int deleteByPrimaryKey(String roleId);
    RoleModel selectByPrimaryKey(String roleId);
    List<RoleModel> selectByName(@Param("name") String name,@Param("shortName") String shortName,@Param("roleId") String roleId);
    int insert(RoleModel record);

    /**
     * 通过入参对角色参数进行修改
     */
    int updateByPrimaryKeySelective(RoleModel record);
    List<RoleModel> selectAll();
    List<RoleModel> selectByRoleFlag(Integer roleFlag);
    /**
     *根据通道Id查询所有的自定义角色
     * @param channelId
     * @return
     */
    List<RoleModel> selectByChannel(String channelId);


    /**
     * 通过channelId查询所有的系统预定义角色和自定义角色
     * @param channelId
     * @return
     */
    List<RoleModel> selectSystemRolesAndCustomRolesByChannelId(String channelId);


    List<RoleModel> selectByMemberId(Long memberId);

    /**
     * 根据通道Id和通道状态查询角色信息
     * @return
     */
    List<RoleModel> selectByChannelIdAndRoleState(@Param("channelId") String channelId, @Param("state") Integer state);

    /**
     * 查询预定义角色中，state为输入参数的角色列表
     */
    List<RoleModel> selectByRoleFalgAndState(@Param("roleFlag") Integer roleFlag,@Param("state") Integer state);

    /**
     * 根据通道Id以及角色名称信息进行查新
     */
    RoleModel selectByChannelIdAndRoleParams(@Param("channelId")String channelId,@Param("roleId") String roleId,@Param("name")String name,@Param("shortName")String shortName);

    /**
     * 通过channelId和state来查询角色列表信息
     * @param channelId
     * @param state
     */
    List<RoleModel> selectRoleListByChannelIdAndState(@Param("channelId") String channelId,@Param("state") List<Integer> state);

    /**
     * 根据输入参数查询该通道中是否存在以该名称命名的自定义角色
     * @param channelId
     * @param name
     * @param shortName
     * @return
     */
    List<RoleModel> selectRoleByChannelIdandParams(@Param("channelId") String channelId, @Param("name") String name, @Param("shortName") String shortName);
}
