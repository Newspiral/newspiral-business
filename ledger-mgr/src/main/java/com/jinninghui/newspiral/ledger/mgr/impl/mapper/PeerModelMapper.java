package com.jinninghui.newspiral.ledger.mgr.impl.mapper;

import com.jinninghui.newspiral.ledger.mgr.impl.domain.PeerModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PeerModelMapper {
    int deleteByPrimaryKey(@Param("peerIdValue") String peerIdValue);

    int insert(PeerModel record);

    int insertSelective(PeerModel record);

    PeerModel selectByPrimaryKey(@Param("peerIdValue") String peerIdValue);

    int updateByPrimaryKeySelective(PeerModel record);

    int updateByPrimaryKey(PeerModel record);

    List<PeerModel> selectAll();

    /**
     * 查找所有同个组织下的peer
     *
     * @param organizationIdValue
     * @return
     */
    List<PeerModel> selectAllByIssuerID(@Param("organizationIdValue") String organizationIdValue);

    List<PeerModel> selectValidPeers(@Param("channelId") String channelId, @Param("height") Long height);

    List<PeerModel> selectLocalPeer();

    int updatePeerCertificateByPrimaryKey(PeerModel record);

    List<PeerModel> listPeerCertificateBlackList();

    List<String> selecetServiceUrls();

}
