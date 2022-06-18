package com.jinninghui.newspiral.ledger.mgr.impl.mapper;

import com.jinninghui.newspiral.ledger.mgr.impl.domain.PeerCertificateModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @version V1.0
 * @Title: PeerCertificateMapper
 * @Package com.jinninghui.newspiral.ledger.mgr.impl.mapper
 * @Description:
 * @author: xuxm
 * @date: 2020/4/8 14:47
 */
public interface PeerCertificateModelMapper {

    int deleteByPrimaryKey(@Param("id") String id);

    void deleteByPeerId(@Param("peerId") String peerId, @Param("channelId") String channelId);

    void deleteBycertificateHash(PeerCertificateModel record);

    int insert(PeerCertificateModel record);

    int insertSelective(PeerCertificateModel record);

    PeerCertificateModel selectByPrimaryKey(@Param("id") String id);

    int updateByPrimaryKeySelective(PeerCertificateModel record);

    int updateByPrimaryKey(PeerCertificateModel record);

    List<PeerCertificateModel> selectAll();

    /**
     * 查询所有同个通道中的节点
     *
     * @param channelId
     * @return
     */
    List<PeerCertificateModel> selectAllByChannelID(@Param("channelId") String channelId);

    List<PeerCertificateModel> listByPeerId(@Param("peerId") String peerId, @Param("channelId") String channelId);


    int updatePeerCertificateFlagByPrimaryKey(PeerCertificateModel record);

    int updatePeerCertificateFlagByPeerHash(PeerCertificateModel record);

    int revokePeerCertificate(PeerCertificateModel record);

    int deleteByChannelId(@Param("channelId") String channelId);

    PeerCertificateModel queryLatestChannelChangeHeight(@Param("channelId") String channelId);

    PeerCertificateModel selectByPeerIdAndChannelId(@Param("peerId") String peerId, @Param("channelId") String channelId);
}
