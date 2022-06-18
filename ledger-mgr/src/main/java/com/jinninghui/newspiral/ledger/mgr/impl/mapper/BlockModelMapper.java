package com.jinninghui.newspiral.ledger.mgr.impl.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.BlockModel;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;

/**
 * //Block是不允许修改的和删除的
 */
public interface BlockModelMapper  {

    Long queryMaxBlockHightByChannelId(@Param("channelId") String channelId);

    int insertCachedBlock(@Param("record")BlockModel record);

    @DS("sharding")
    int insertCommittedBlock(@Param("record")BlockModel record);
    @DS("sharding")
    BlockModel selectByPrimaryKey(Long id);

    //void deleteByCacheBlock(@Param("tableName") String tableName);
    void deleteCachedBlock(@Param("channelId") String channelId, @Param("height") Long height);

    void deleteCachedBlockBehind(@Param("channelId") String channelId, @Param("height") Long height);
    @DS("sharding")
    void deleteCommittedBlock(@Param("channelId") String channelId, @Param("height") Long height);
    @DS("sharding")
    List<BlockModel> selectBlockByChannelId(@Param("channelId") String channelId,@Param("num") Integer num);
    @DS("sharding")
    BlockModel selectByHeight(@Param("channelId") String channelId, @Param("height") Long height);


    List<BlockModel> selectBlockCacheByHeight(@Param("channelId") String channelId, @Param("height") Long height);
    @DS("sharding")
    BlockModel selectByHash(@Param("channelId") String channelId, @Param("hash") String hash);
    @DS("sharding")
    Long selectHeightByHash(@Param("channelId") String channelId, @Param("hash") String hash);
    @DS("sharding")
    BlockModel selectMaxHeightBlock(@Param("channelId") String channelId);

    BlockModel selectCacheByHash(@Param("channelId") String channelId, @Param("hash") String hash);

    List<BlockModel> selectBlockCacheListByHeight(@Param("channelId") String channelId, @Param("height") Long height);

    void deleteBlockCacheListByHeight(@Param("channelId") String channelId, @Param("height") Long height);
    @DS("sharding")
    void deleteCommittedBlockByChannelId(@Param("channelId") String channelId);

    void deleteCachedBlockByChannelId(@Param("channelId") String channelId);

    /**
     * 根据通道Id和高度区间查询区块列表
     */
    @DS("sharding")
    List<BlockModel> selectBlockByChannelIdAndHeightRegion(@Param("channelId") String channelId, @Param("fromHeight") Long from, @Param("toHeight") Long to);

    void deleteCachedBlockByBlockHeight(@Param("channelId") String channelId,@Param("height") Long height);

    @DS("sharding")
    void deleteCommittedBlockByBlockHeight(@Param("channelId") String channelId,@Param("height") Long height);
    @DS("sharding")
    List<BlockModel> selectLatestBlockList(@Param("channelId") String channelId,@Param("count") Integer count);

    /**
     * 查询每个通道的最高快高度
     *
     * @return list
     */
    @DS("sharding")
    List<Long> queryMaxBlockId();

    @DS("sharding")
    HashMap<String,Long> queryChannelMaxBlockId();

    @DS("sharding")
    Long selectIfExistAfterFromBLock(@Param("channelId") String channelId, @Param("fromBlockId")Long fromBlockId);

    @DS("sharding")
    Long selectLatestBlockId(@Param("channelId") String channelId);
}
