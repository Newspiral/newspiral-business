package com.jinninghui.newspiral.ledger.mgr.impl.mapper;

import com.jinninghui.newspiral.ledger.mgr.impl.domain.AuthModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AuthMapper {

    List<AuthModel> selectAll();

    String selectIdByMemberInfo(@Param("channelId") String channelId,@Param("publicKey") String publicKey);

    String selectIdByInterfaceInfo(@Param("mthFullName") String mthFullName);
    /**
     * 根据id查询权限名称
     */
    AuthModel selectAuthById(Integer id);
    /**
     * 查询到所有的权限数组
     */
    List<Integer> selectAuthIds();

    /**
     * 根据AuthCode查询AuthId
     */
    String selectAuthIdByAuthCode(String authCode);
}

