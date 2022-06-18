package com.jinninghui.newspiral.ledger.mgr.impl.mapper;

import com.jinninghui.newspiral.ledger.mgr.impl.domain.PeerChannelModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.PeerChannelModelWithBLOBs;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PeerChannelModelMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PeerChannelModelWithBLOBs record);

    int insertSelective(PeerChannelModelWithBLOBs record);

    PeerChannelModelWithBLOBs selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PeerChannelModelWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(PeerChannelModelWithBLOBs record);

    int updateByPrimaryKey(PeerChannelModel record);

    List<PeerChannelModelWithBLOBs> selectAll();

    List<PeerChannelModelWithBLOBs> selectAllLatest();

    List<PeerChannelModelWithBLOBs> selectAllByChannelId(@Param("channelId") String channelId);

    int updateByPeerIdAndChannel(PeerChannelModelWithBLOBs record);

    /**
     *
     * @param channelId
     * @param peerId   取IdentityKey的value
     */
    int deleteByChannelIdAndPeerId(@Param("channelId") String channelId, @Param("peerId") String peerId);

    int updateUserPrivateKetByConditionKey(PeerChannelModel record);

    PeerChannelModel queryLatestChannelChangeHeight(@Param("channelId") String channelId);

    PeerChannelModelWithBLOBs selectByPeerIdAndChannel(@Param("channelId") String channelId, @Param("peerId") String peerId);
    int deleteByChannelId(@Param("channelId") String channelId);
    int selectCountByChannelId(@Param("channelId") String channelId, @Param("peerId") String peerId);

    PeerChannelModelWithBLOBs selectLatestRecordByActionType(@Param("peerId") String peerId, @Param("channelId") String channelId, @Param("actionType") String actionType);

    List<PeerChannelModelWithBLOBs> selectHistoryPeerChannelRelation(@Param("channelId") String channelId, @Param("peerId") String peerId);

}