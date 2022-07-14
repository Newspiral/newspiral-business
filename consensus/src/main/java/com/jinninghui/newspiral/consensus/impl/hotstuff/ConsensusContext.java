package com.jinninghui.newspiral.consensus.impl.hotstuff;

import com.alibaba.fastjson.JSON;
import com.jinninghui.newspiral.common.entity.ConsensusAlgorithmEnum;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.block.BlockHeader;
import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.chain.ChannelSummary;
import com.jinninghui.newspiral.common.entity.chain.Peer;
import com.jinninghui.newspiral.common.entity.common.logmodule.LogModuleCodes;
import com.jinninghui.newspiral.common.entity.consensus.BlockVoteMsg;
import com.jinninghui.newspiral.common.entity.consensus.ConsensusStageEnum;
import com.jinninghui.newspiral.common.entity.consensus.GenericMsg;
import com.jinninghui.newspiral.common.entity.consensus.GenericQC;
import com.jinninghui.newspiral.common.entity.consensus.HotStuffDataNode;
import com.jinninghui.newspiral.common.entity.consensus.NewViewMsg;
import com.jinninghui.newspiral.common.entity.consensus.View;
import com.jinninghui.newspiral.common.entity.exception.NewSpiralErrorEnum;
import com.jinninghui.newspiral.common.entity.exception.NewspiralException;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.ledger.mgr.LedgerMgr;
import com.jinninghui.newspiral.security.SecurityService;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * @author lida
 * @date 2019/7/18 18:38
 * 共识的上下文，与通道紧密相关，需要根据通道配置初始化，类似于一个与数据库相关的DOMAIN类，因此不需要通过Spring容器管理
 */
@Data
@Slf4j
public class ConsensusContext {
    //Set the genesis block timestamp to 2019-11-9 14:00:00
    public final Long GenesisBlockTimestamp = new Long("1573279200000");
    ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /**
     * 初始化为最大int值
     * 某个QC需要收集到的最小节点支持数，适用于GenericQC中的BlockVoteMsg，也适用于NewViewMsg
     */
    int qcMinNodeCnt;
    
    /**
      *上一次超时的视图号
      */
    Long latestTimeOutViewNo;
    
    /**
     * 本地节点上一次更新到的区块高度
     */
    Long latestChannelUpdateHeight = 0L;
    
    /**
     * 当前超时时间
     */
    Long currentTimeOut;

    /**
     * 共识所属的通道
     */
    Channel channel;

    /**
     * 通道中的各个节点的序号，从0开始，逐一增加，因此在某个节点退出通道（并非指宕机，而是指需要共识的退出），此处需要重新计算各个节点的序号
     */
    List<Peer> orderedPeerList;


    /**
     * 节点自身
     */
    Peer myself;
    
    /**
     * 当前视图的leader节点
     */
    Peer currLeader;

    /**
     * 当前view
     */
    View currView;

    /**
     * View number from genericMsg, only used in synchronization.
     */
    Long viewNoSync = new Long(0);
    /**
     * Confirm times about viewNoSync from successive leader.
     * Only used in synchronization.
     */
    Integer viewNoSyncConfirmTimes = new Integer(0);
    /**
     * current height of committed block in memory
     */
    long blockHeight;

    /**
     * the height of persisted block;
     * differs from *blockHeight* because of asynchronized insertion of block.
     */
    long persistedBlockHeight;

    long cachedBlockHeight;
    /**
     * 本节点所处的共识阶段
     */
    ConsensusStageEnum consensusStageEnum;



    public void setConsensusStageEnum(ConsensusStageEnum consensusStageEnum) {
        if (this.consensusStageEnum.equals(ConsensusStageEnum.NO_AVALIABLE_DB_ROUTE)){
            return;
        }
        this.consensusStageEnum = consensusStageEnum;
    }

    public void setNoAvailableConsensusStageToAnOther(ConsensusStageEnum consensusStageEnum) {
        this.consensusStageEnum = consensusStageEnum;
    }

    /**
     * 收到的本view的genericMsg
     * 如果本view中本节点是Leader，则存储的是本节点创建和广播的GenericMsg
     * 接收消息线程和主线程都会使用，但是不会有什么内部修改的动作，仅有赋值和取值动作，所以线程安全
     */
    GenericMsg genericMsgOfCurrView;


    /**
     * 同步线程与共识线程处理过程中，持续接收收到的缓存区块消息队列，最终清0
     */
    Map<Long, GenericMsg> genericMsgMap;

    Map<Long, GenericMsg> genericMsgMapBackup;

    ReadWriteLock genericMsgMapLock = new ReentrantReadWriteLock();

    //TODO 测试用修改成2 原来是20
    Integer maxCachedGenericMsg = 200;

    ExecutorService executorServiceForCachedGenericMsg = Executors.newSingleThreadExecutor();

    /**
     * 支持genericMsgOfCurrView中的Block的副本投票消息，只有本节点是Leader时，才有数据
     * 所有加入到这里的数据都是经过检查的合法投票
     * 接收消息线程和主线程都会使用，因此使用ConcurrentHashMap
     * key为BlockVoteMsg的业务键，即如果业务键相同，则BlockVoteMsg也相同
     */
    //Map<String, BlockVoteMsg> blockVoteMapOfCurrView;
    Map<String, Map<String, BlockVoteMsg>> blockVoteMap;
    /**
     * 本地收到的(GenericMsg)或自身生成的最大的合法genericQC，正常情况下是上一个view的Block的GenriceQC
     * 异常情况下则不是
     */
    GenericQC genericQC;

    GenericQC lockedQC;

    volatile GenericQC highestQC;

    /**
     * the hash of block which is voted by the peer, this block and the genericQC is of the same HotStuffDataNode.
     */
    volatile String hashPrePrepareBlock;

    /**
     * 接收消息线程和主线程都会使用，因此使用ConcurrentHashMap
     * 第一层key使用viewNo，第二层key使用NewViewMsg的业务键
     */
    Map<Long, Map<String, NewViewMsg>> newViewMsgMap;

    /**
     * key为HotStuffDataNode的block的Hash的16进制字符串表示
     * byte[]不能直接作为Map的key
     * 本地缓存的区块
     */
    Map<String, HotStuffDataNode> localDataNodeMap;

    /**
     * 用来存储某个区块对应的QC，目前主要用于同步过程中，某个区块没有dataNode结构，而又需要将该区块持久化时使用。
     * todo:将区块的持久化从现在的和dataNode绑定到和blockMap以及genericQCMap绑定。
     */
    Map<String, GenericQC> genericQCMap = new ConcurrentHashMap<>();

    @Getter
    Map<String, Block> cachedBlockMap = new ConcurrentHashMap<>();

    @Getter
    Map<String, Block> committedBlockMap = new ConcurrentHashMap<>();

    private LedgerMgr ledgerMgr;



    //for test

    long collectVoteTime = -1;

    long collectNewViewTime = -1;

    long canCreateTime = -1;

    long totalViewTime = -1;



    /**
     * 根据channel信息初始化
     *
     * @param channel
     * @param securityService
     * @param ledgerMgr
     */
    public void init(Channel channel, SecurityService securityService, LedgerMgr ledgerMgr) {
        long sizeTotal = channel.getMemberPeerList().parallelStream().filter(peer -> peer.isState()).count();
        this.qcMinNodeCnt = calcQCMinSupport((int) sizeTotal);
        this.ledgerMgr = ledgerMgr;
        this.channel = channel;

        this.initPeerProporties(channel, securityService);
        //链是不会不存在的，因为流程会保证创建链时一定是先创建通道然后再启动对应的共识模块
        ChannelSummary channelSummary = ledgerMgr.queryChannelSummary(channel.getChannelId());
        channelSummary.setExtendsParams(channel.getExtendsParams());
        channelSummary.setBlockList(channel.getBlockList());

        if (channelSummary.hasBusinessData() == false) {
            log.info(ModuleClassification.ConM_CC_ +"MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION +","+channel.getChannelId()+ ",初始化ConsensusContext:channel尚无任何业务数据");
            initForOnePeerWithoutBusinessDataChannel(securityService, channelSummary);
        } else {
            log.info(ModuleClassification.ConM_CC_ +"MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION +","+channel.getChannelId()+ ",初始化ConsensusContext:Channel已经产生了一部分业务数据");
            if (!initForRestartConsensusPeerWithChannel(channelSummary, securityService)) {
                log.error(ModuleClassification.ConM_CC_ +"TError"+"MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION +","+channel.getChannelId()+ ",Fatal error, the data is broken and unable to restore");
                System.exit(1);
            }
        }
        //BatchWriteGenericMsgTask batchWriteGenericMsgTask = new BatchWriteGenericMsgTask();
        //batchWriteGenericMsgTask.setConsensusContext(this);
        //executorServiceForCachedGenericMsg.execute(batchWriteGenericMsgTask);
        log.info("MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+","+channel.getChannelId()+",初始化完毕");
        //log.info("MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",初始化完毕:" + this.toString());

    }

    /**
     * TODO：获取节点列表，获取view等初始化动作，此时不参与共识投票
     * * 考虑到同步时间较长，qcMinNodeCnt，orderedPeerList等待key数据同步完成再行确定
     * * currLeader，currView现在可以获取，注意leader是不停变化的。
     * * 此状态下不会设置为leader节点，只会作为replica从leader获取区块。
     * * ConsensusStageEnum为等待数据同步，同步自检完成设置为共识节点
     * * 此时只接受共识消息
     */
    private void initForNewConsensusPeerWithChannel() {
        // 设置字段为初次同步
        this.consensusStageEnum = ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL;
    }


    /**
     * TODO：获取节点列表，获取view等初始化动作，此时不参与共识投票
     * * 大部分情况类似于初始化节点
     */
    private boolean initForRestartConsensusPeerWithChannel(ChannelSummary channelSummary, SecurityService securityService) {
        initBasicParams();
        Block latestCommittedBlock = ledgerMgr.queryBlock(this.channel.getChannelId(), channelSummary.getHeight());
        List<Block> preparedBlockList = ledgerMgr.queryCacheBlock(this.channel.getChannelId(), channelSummary.getHeight() + 2);
        while (preparedBlockList.isEmpty()) {
            log.info(ModuleClassification.ConM_CC_ +"TError"+"MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION +","+channel.getChannelId()+ ",Prepared block lost");
            fixBlockData(latestCommittedBlock);
            channelSummary = ledgerMgr.queryChannelSummary(channel.getChannelId());
            latestCommittedBlock = ledgerMgr.queryBlock(this.channel.getChannelId(), channelSummary.getHeight());
            preparedBlockList = ledgerMgr.queryCacheBlock(this.channel.getChannelId(), channelSummary.getHeight() + 2);
        }
        Block preparedBlock = preparedBlockList.get(0);
        Block lockedBlock = ledgerMgr.queryCacheBlock(this.channel.getChannelId(), preparedBlock.getBlockHeader().getPrevBlockHash());
        if (lockedBlock == null || !latestCommittedBlock.getHash().equals(lockedBlock.getBlockHeader().getPrevBlockHash())) {
            log.error(ModuleClassification.ConM_CC_ +"TError"+"MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION +","+channel.getChannelId()+ ",Locked Block is conflict with latest committed block");
            return false;
        }
        Block dummyBlock = createGenesisBlock(preparedBlock.getHash(), preparedBlock.getBlockHeader().getHeight(), securityService);
        this.genericQC = JSON.parseObject(preparedBlock.getBlockHeader().getWitness(), GenericQC.class);
        this.lockedQC = JSON.parseObject(lockedBlock.getBlockHeader().getWitness(), GenericQC.class);
        this.highestQC = this.genericQC;
        //this.hashPrePrepareBlock = Block.bytesToHexString(dummyBlock.getHash());
        this.hashPrePrepareBlock = genericQC.getBlockHash();
        addNode(createHotStuffNode(dummyBlock, genericQC));
        addNode(createHotStuffNode(preparedBlock, lockedQC));
        addNode(createHotStuffNode(lockedBlock, JSON.parseObject(latestCommittedBlock.getBlockHeader().getWitness(), GenericQC.class)));
        this.currView = View.createView(genericQC.getBlockViewNo() + 2, this.currentTimeOut);
        log.info(ModuleClassification.ConM_CC_ +"MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION +","+channel.getChannelId()+ ",viewNO初始值为" + this.currView);
        this.blockHeight = latestCommittedBlock.getBlockHeader().getHeight();
        this.persistedBlockHeight = this.blockHeight;
        this.currLeader = calcLeader(this.currView.getNo());
        return true;
    }

    private void initBasicParams() {
        this.latestTimeOutViewNo = new Long(0);
        this.currentTimeOut = Long.parseLong(channel.getExtendParam(ConsensusExtendedParamEnum.VIEW_TIMEOUT_MS.getCode()));
        this.localDataNodeMap = new ConcurrentHashMap<>();
        this.consensusStageEnum = ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL;//默认设置为没有与通道完成数据同步
        this.genericMsgOfCurrView = null;//可以设置为空，等待自己作为Leader设置或者作为Replica接收到GenericMsg后设置
        //this.blockVoteMapOfCurrView = new ConcurrentHashMap<>();//初始化为空
        this.blockVoteMap = new ConcurrentHashMap<>();
        this.newViewMsgMap = new ConcurrentHashMap<>();
        this.genericMsgMap = new ConcurrentHashMap<>();
        this.genericMsgMapBackup = new ConcurrentHashMap<>();
    }

    private void fixBlockData(Block lastCommittedBlock) {
        log.info(ModuleClassification.ConM_CC_ +"MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION +","+channel.getChannelId()+ ",Try to fix local data");
        if (lastCommittedBlock.getBlockHeader().getHeight() == 1) {
            return;
        }
        if (lastCommittedBlock.getBlockHeader().getHeight() == 2) {
            ledgerMgr.deleteCommittedBlock(channel.getChannelId(), new Long(2));
        } else {
            ledgerMgr.insertCacheBlock(channel.getChannelId(), lastCommittedBlock);
            ledgerMgr.deleteCommittedBlock(channel.getChannelId(), lastCommittedBlock.getBlockHeader().getHeight());
            Block prevBlock = ledgerMgr.queryBlock(channel.getChannelId(), lastCommittedBlock.getBlockHeader().getHeight() - 1);
            ledgerMgr.insertCacheBlock(channel.getChannelId(), prevBlock);
            ledgerMgr.deleteCommittedBlock(channel.getChannelId(), prevBlock.getBlockHeader().getHeight());
        }
        return;
    }


    void initForOnePeerWithoutBusinessDataChannel(SecurityService securityService, ChannelSummary channelSummary) {
        initBasicParams();
        initGenesisBlocks(securityService, channelSummary);
        this.currView = View.createDefaultView(this.currentTimeOut);
        this.blockHeight = 0;
        this.persistedBlockHeight = 0;
        this.currLeader = calcLeader(this.currView.getNo());
    }

    private void initGenesisBlocks(SecurityService securityService, ChannelSummary channelSummary) {
        Block genesisBlock0;
        Block genesisBlock1;
        Block genesisBlock2;
        Block dummyBlock;
/*        if (CollectionUtils.isEmpty(channelSummary.getBlockList())) {
            genesisBlock0 = createGenesisBlock(channelSummary.INIT_HASH, 0, securityService);
            genesisBlock1 = createGenesisBlock(genesisBlock0.getHash(), 1, securityService);
            genesisBlock2 = createGenesisBlock(genesisBlock1.getHash(), 2, securityService);
            dummyBlock = createGenesisBlock(genesisBlock2.getHash(), 3, securityService);
        } else if(channelSummary.getExtendsParams()!=null
            && "Fabric1.0".equals(channelSummary.getExtendsParams().get("data_version"))
            && channelSummary.getBlockList().size() == 4)
        {
                List<Block> blockList = channelSummary.getBlockList();
                genesisBlock0 = blockList.get(0);
                genesisBlock1 = blockList.get(1);
                genesisBlock2 = blockList.get(2);
                dummyBlock = blockList.get(3);
        } else {
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_BASIC_CONFIG, "channel extendedParam config invalid.");
        }*/

        if(channelSummary.getExtendsParams()!=null
                && "Fabric1.0".equals(channelSummary.getExtendsParams().get("data_version"))
                && channelSummary.getBlockList()!=null&& channelSummary.getBlockList().size() == 4)
        {
            List<Block> blockList = channelSummary.getBlockList();
            genesisBlock0 = blockList.get(0);
            genesisBlock1 = blockList.get(1);
            genesisBlock2 = blockList.get(2);
            dummyBlock = blockList.get(3);
        }
        else
        {
            if(!CollectionUtils.isEmpty(channelSummary.getBlockList()))
            {
                log.info("channelSummary.BlockList={}",channelSummary.getBlockList().size());
            }
            genesisBlock0 = createGenesisBlock(channelSummary.INIT_HASH, 0, securityService);
            genesisBlock1 = createGenesisBlock(genesisBlock0.getHash(), 1, securityService);
            genesisBlock2 = createGenesisBlock(genesisBlock1.getHash(), 2, securityService);
            dummyBlock = createGenesisBlock(genesisBlock2.getHash(), 3, securityService);
        }
        ledgerMgr.persistBlock(genesisBlock0);
        ledgerMgr.insertCacheBlock(this.getChannel().getChannelId(), genesisBlock1);
        ledgerMgr.insertCacheBlock(this.getChannel().getChannelId(), genesisBlock2);
        this.genericQC = JSON.parseObject(genesisBlock2.getBlockHeader().getWitness(), GenericQC.class);
        this.lockedQC = JSON.parseObject(genesisBlock1.getBlockHeader().getWitness(), GenericQC.class);
        this.highestQC = genericQC;
        //for package transaction.
        //this.hashPrePrepareBlock = Block.bytesToHexString(dummyBlock.getHash());
        //this.hashPrePrepareBlock = genericQC.getBlockHash();
        this.hashPrePrepareBlock = dummyBlock.getHash();
        addNode(createHotStuffNode(genesisBlock1, JSON.parseObject(genesisBlock0.getBlockHeader().getWitness(), GenericQC.class)));
        addNode(createHotStuffNode(genesisBlock2, lockedQC));
        addNode(createHotStuffNode(dummyBlock, genericQC));
    }

    private Block createGenesisBlock(String prevBlockHash, long blockHeight, SecurityService securityService) {
        Block block = new Block();
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setChannelId(this.channel.getChannelId());
        blockHeader.setConsensusAlgorithm(ConsensusAlgorithmEnum.NEWSPIRAL_HOT_STUFF);
        blockHeader.setHeight(blockHeight);
        blockHeader.setPrevBlockHash(prevBlockHash);
        blockHeader.setTimestamp(this.GenesisBlockTimestamp);
        blockHeader.setVersion(BlockHeader.VERSION_1_0);
        blockHeader.setConsensusTimestamp(this.GenesisBlockTimestamp);
        blockHeader.setPersistenceTimestamp(this.GenesisBlockTimestamp);
        block.setBlockHeader(blockHeader);
        block.addTransactionList(new ArrayList<>());
        block.setHash(SignForConsensus.hashBlockHeader(blockHeader, securityService));
        securityService.signByGMCertificate(block, channel.getChannelId());
        //securityService.sm9Sign(block, this.channel.getChannelId());
        blockHeader.setWitness(createWitnessForGenesisBlock(block, securityService));
        //log.info(ModuleClassification.ConM_CC+"Build the genesis block of height {}", blockHeight);
        return block;
    }

    private String createWitnessForGenesisBlock(Block block, SecurityService securityService) {
        GenericQC qc = new GenericQC();
        qc.setBlockHash(block.getHash());
        qc.setBlockCreateTimestamp(block.getBlockHeader().getTimestamp());
        qc.setBlockViewNo(block.getBlockHeader().getHeight());
        qc.setPrevBlockHash(block.getBlockHeader().getPrevBlockHash());
        qc.setHeight(block.getBlockHeader().getHeight());
        //genesis block is null and determined, so doesn't need vote.
        LinkedHashMap<String, BlockVoteMsg> voteMap = new LinkedHashMap<>();
        qc.setVoteMap(voteMap);
        return JSON.toJSONString(qc);
    }

    private HotStuffDataNode createHotStuffNode(Block block, GenericQC qc) {
        HotStuffDataNode hotStuffDataNode = new HotStuffDataNode();
        hotStuffDataNode.setParentNodeHashStr(block.getBlockHeader().getPrevBlockHash());
        hotStuffDataNode.setJustify(qc);
        hotStuffDataNode.setBlock(block);
        return hotStuffDataNode;
    }


    /**
     * 根据Channel初始化Peer相关的属性
     *
     * @param channel
     * @param securityService
     */
    void initPeerProporties(Channel channel, SecurityService securityService) {
        //初始化orderedPeerList，需要根据extendedData指定的节点序号升序排列
        this.orderedPeerList = this.calcOrderPeerList(channel.getMemberPeerList());
        log.info(ModuleClassification.ConM_CC_ +","+channel.getChannelId()+"初始化orderedPeerList成功:" + this.orderedPeerList.toString());

        //初始化myself，逻辑清晰起见，不复用上面的循环
        for (Peer peer : channel.getMemberPeerList()) {
            if (peer.getIsLocalPeer()) {
                this.myself = peer;
                break;
            }
        }
        if (this.myself == null) {
            log.error(ModuleClassification.ConM_CC_ +"TError"+"MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION +","+channel.getChannelId()+ ",Channel中存储的节点没有本节点.");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "Channel中存储的节点没有本节点，两种可能:(1)本节点加入该Channel后，节点证书变更了，但是未正常的执行证书变更操作；" +
                    ",(2)该Channel本身就没有本节点");
        }
        /*if (this.myself == null) {
            List<Peer> peerList = ledgerMgr.queryPeersOfChannel(channel.getChannelId());
            for (Peer peer : peerList) {
                if (peer.isLocalPeer()) {
                    this.myself = peer;
                    break;
                }
            }
            if (this.myself == null) {
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "Channel中存储的节点没有本节点，两种可能:(1)本节点加入该Channel后，节点证书变更了，但是未正常的执行证书变更操作；" +
                    ",(2)该Channel本身就没有本节点");
            }
        }*/

        //初始化时，一定没有完成数据同步，所以Leader无法确定
        this.currLeader = null;

    }

    //对MemberPeerList中的全部peer进行一个判重，然后按照加入顺序进行排序
    public List<Peer> calcOrderPeerList(List<Peer> memberPeerList) {
        Set<Long> peerNoSet = new HashSet<>();
        HashMap<Long, Peer> no2PeerMap = new HashMap<>();
        for (Peer peer : memberPeerList) {
            try {
                Long peerNo = Long.parseLong(peer.getPeerChannelRelation().getExtendedData());
                peerNoSet.add(peerNo);
                no2PeerMap.put(peerNo, peer);
            } catch (NumberFormatException ex) {
                log.error(ModuleClassification.ConM_CC_ +"TError"+"MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION +","+channel.getChannelId()+ ",Channel中存储的节点序号存在非法值.",ex);
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "Channel中存储的节点序号存在非法值，必须为整数字符串,实际为:" + peer.getPeerChannelRelation().getExtendedData());
            }
        }
        if (peerNoSet.size() != memberPeerList.size()) {
            log.error(ModuleClassification.ConM_CC_ +"TError"+"MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION +","+channel.getChannelId()+ ",Channel中存储的节点序号存在重复值.");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "Channel中存储的节点序号存在重复值，不允许重复值");
        }
        Long[] peerNoArray = peerNoSet.toArray(new Long[]{});
        Arrays.sort(peerNoArray);
        List<Peer> peerList = new ArrayList<>();
        for (Long no : peerNoArray) {
            peerList.add(no2PeerMap.get(no));
        }
        return peerList;
    }

    /**
     * 计算BFT下一个QC需要的支持节点数量
     * 假设n=3f+1，则返回n-f，其中f是保证3f+1<=n的最大整数
     *
     * @param channelNodeSize
     * @return
     */
    public int calcQCMinSupport(int channelNodeSize) {
        if (channelNodeSize <= 0) {
            log.error(ModuleClassification.ConM_CC_ +"TError"+"MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION +","+channel.getChannelId()+ ",Channel中的节点数量需要大于等于0.");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "Channel中的节点数量需要大于等于0");
        }
        //return 1;
        if (channelNodeSize == 1) {
            return 1;
        } else {
            int f = (channelNodeSize - 1) / 3;//向下取整
            return channelNodeSize - f;
        }
    }

    public String toSimpleStr() {//不用使用StringBuilder，JVM会自己优化
        String string = "Context information at View " + this.getCurrView().getNo() + "of height " + this.getBlockHeight()
                + " PrePrepare " + this.getHashPrePrepareBlock()
                + " prepare" + this.getGenericQC().getBlockHash()
                + " locked " + this.lockedQC.getBlockHash();
        return string;
    }

    /**
     * 在进入共识状态之前，需要重新计算一下当前的QCMin以及orderPeerList
     */
    public void tryUpdateConsensusBasis(Long blockHeight) {
        List<Peer> peerList = this.channel.getValidMemberPeerList(blockHeight);
        //long sizeTotal = peerList.parallelStream().filter(peer -> peer.isState()).count();
        long sizeTotal=peerList.size();
        this.qcMinNodeCnt = calcQCMinSupport((int) sizeTotal);
        this.orderedPeerList = calcOrderPeerList(peerList);
    }


    /**
     * viewNo加一，清除与viewNo相关的一些数据
     */
    public void localEnterNewView() {
        currView.enterNewView(this.getCurrentTimeOut());
        currLeader = calcLeader(this.currView.getNo());
        if (myself.equals(currLeader)) {
            consensusStageEnum = ConsensusStageEnum.LEADER_WAIT_NEWVIEW;
        } else {
            consensusStageEnum = ConsensusStageEnum.REPLICA_WAIT_BLOCK;
        }
        if (genericMsgOfCurrView != null) {
            blockVoteMap.remove(genericMsgOfCurrView.getHotStuffDataNode().getBlock().getHash());
        }
        genericMsgOfCurrView = null;
        //blockVoteMapOfCurrView.clear();
        //genericQC不用修改

        //清除newView消息要小心一点
        //因为各个节点异步处理，如果本节点是下一个Leader，则可能在本地节点进入下一个View前，就收到了其他节点的NewView消息
        for (Long viewNo : newViewMsgMap.keySet()) {
            if (viewNo < currView.getNo() - 2) {//注意这里是小于当前ViewNo-1，newViewMap的key是NewViewMsg的viewNo，其含义是发送者发送时刻的viewNo; 例如发送者从viewNo=3进入到viewNo=4时，
                //其发送的NewViewMsg的viewNo会是3
                newViewMsgMap.remove(viewNo);
            }
        }
    }

    /**
     * 计算viewNo指定view的Leader
     *
     * @param viewNo
     * @return
     */
    public Peer calcLeader(Long viewNo) {
        //long index = viewNo % getOrderedPeerList().size();
        //List<Peer> list=getOrderedPeerList().parallelStream().filter(peer -> peer.isState()).collect(Collectors.toList());
        List<Peer> list = new ArrayList<>();
        for (Peer peer: getOrderedPeerList()) {
            if (peer.isState()) {
                list.add(peer);
            }
        }
        long sizeTotal = list.size();
        long index = viewNo % (int) sizeTotal;
        //long型的view取模int型的size，必然在int型的范围内，所以下面可以强转
/*        if(sizeTotal<=0)
        {
            //
            return getOrderedPeerList().get((int) index);
        }
        else {*/
            return list.get((int) index);
        //}
        //return getOrderedPeerList().get((int) index);
        //return getOrderedPeerList().get(0);
    }

    public HotStuffDataNode findNodeByBlockHash(String blockHashStr) {
        return localDataNodeMap.get(blockHashStr);
    }

    /**
     * 添加一个新的Node到本地缓存Node的Map中
     *
     * @param hotStuffDataNode
     */
    public void addNode(HotStuffDataNode hotStuffDataNode) {
        log.info(ModuleClassification.ConM_CC_.toString() + channel.getChannelId() +
                " add datanode:" + hotStuffDataNode.getBlock().getHash());
        localDataNodeMap.put(hotStuffDataNode.getBlock().getHash(), hotStuffDataNode);
        addGenericQC(hotStuffDataNode.getJustify());
    }


    /**
     * 区块持久化之后，将内存中共识上下文里没必要的数据删除
     * @param persistedBlock
     */
    public void cleanUnusedDataAfterPersist(Block persistedBlock) {
        Iterator<Map.Entry<String, Block>> iterator = cachedBlockMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Block> entry = iterator.next();
            if (entry.getValue().getBlockHeader().getHeight() <= persistedBlock.getBlockHeader().getHeight()) {
                genericQCMap.remove(entry.getValue().getHash());
                log.info(ModuleClassification.ConM_CC_.toString() + channel.getChannelId() +
                        " remove datanode after persist:" + entry.getValue().getHash());
                localDataNodeMap.remove(entry.getValue().getHash());
                committedBlockMap.remove(entry.getValue().getHash());
                iterator.remove();
            }
        }
    }

    /**
     * 发生超时之后，将内存中共识上下文里没有必要的数据删除
     */
    public void cleanUnusedDataAfterTimeOut() {
        Iterator<Map.Entry<String, HotStuffDataNode>> iterator = localDataNodeMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, HotStuffDataNode> entry = iterator.next();
            if (null == genericQCMap.get(entry.getKey()) && false == hashPrePrepareBlock.equals(entry.getKey())) {
                cachedBlockMap.remove(entry.getKey());
                log.info(ModuleClassification.ConM_CC_.toString() + channel.getChannelId() +
                        " remove datanode after timeout:" + entry.getKey());
                iterator.remove();
            }
        }
    }

    public void addGenericQC(GenericQC genericQC) {
        if (genericQC != null) {
            genericQCMap.put(genericQC.getBlockHash(), genericQC);
        }
    }

    /**
     * TODO 子荣需要检查一下
     *
     * @param hotStuffDataNode
     * @return
     */
    public boolean isSafeNode(HotStuffDataNode hotStuffDataNode) {
        if (lockedQC != null) {
            // Safe node should extend from lockedQC.node, which has preCommitQC.
            // There are two cases:
            // 1.Safe node extends from the genericQC.node that extends from lockedQC.node.
            // 2.Safe node extends from the lockedQC.node directly.
            if (hotStuffDataNode.getParentNodeHashStr().equals(genericQC.getBlockHash())) {
                log.info(ModuleClassification.ConM_CC_ +","+channel.getChannelId()+",Data node extends from prepared node");
                return true;
            }
            if (hotStuffDataNode.getParentNodeHashStr().equals(lockedQC.getBlockHash())) {//新节点extends from lockQC.node
                log.info(ModuleClassification.ConM_CC_ +","+channel.getChannelId()+",Data node extends from locked node");
                return true;
            }
            // For liveness,
            if (hotStuffDataNode.getJustify().getBlockViewNo() > lockedQC.getBlockViewNo()) {//新节点的jusitry的view大于lockedQC的view
                log.info(ModuleClassification.ConM_CC_ +","+channel.getChannelId()+",Data node satisfies liveness rule");
                if (false == checkIfPrevBlockExist(hotStuffDataNode.getJustify().getBlockHash())) {
                    log.info(ModuleClassification.ConM_CC_ +","+channel.getChannelId()+",Miss block, need synchronization");
                    this.consensusStageEnum = ConsensusStageEnum.WAIT_SYNC_WITH_CHANNEL;
                    return false;
                }
                return true;
            }
        } else {//lockQC为空，其含义为本地尚未有曾经执行过的Block
            log.info(ModuleClassification.ConM_CC_ +"MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION +","+channel.getChannelId()+ ",节点本地的lockedQC为空，只要接收到的HotSutffDataNode的viewNo大于等于" + View.INIT_LOCKED_QC_VIEW_NO + "就合法,hotStuffDataNode.getJustify().getBlockViewNo():"
                    + hotStuffDataNode.getJustify().getBlockViewNo());
            if (hotStuffDataNode.getJustify().getBlockViewNo() >= View.INIT_LOCKED_QC_VIEW_NO) {//新节点的jusitry的view大于lockedQC的view
                return true;
            }
        }
        return false;
    }

    private boolean checkIfPrevBlockExist(String hashOfPrev) {
        Block block = this.getBlock(hashOfPrev);
        long height = 0;
        if (null == block) {
            return false;
        } else {
            height = block.getBlockHeader().getHeight().longValue();
        }
        Long localHeight = this.getBlockHeight();
        while (height > localHeight) {
            block = this.getBlock(block.getPrevBlockHash());
            if (null == block) {
                return false;
            }
            height--;
        }
        return true;
    }


    /**
     * 检查当前Genric区块是否需要回退，回退只有一种可能性
     * 上次的leader区块没有经过n-f区块同意，或者上次leader区块宕机
     * 重新选举了新leader区块进行处理
     * 此种情况下当前区块需要回退，回退则只是回退缓存而已
     * 而且只是genric区块的缓存
     */
    public boolean needRollbackWhileSafe(HotStuffDataNode receiveNode) {

        // 判断方式为获取的区块的前序hash同genric区块的前序hash一致，
        // 并且获取的区块hash与genric区块的hash不一致
        if (!receiveNode.getBlock().getHash().equals(genericQC.getBlockHash())
                && receiveNode.getParentNodeHashStr().equals(lockedQC.getBlockHash())
                && receiveNode.getJustify().getBlockViewNo() > lockedQC.getBlockViewNo()) {
            return true;
        }
        if (receiveNode.getBlock().getBlockHeader().getPrevBlockHash() == receiveNode.getJustify().getBlockHash()
                && receiveNode.getParentNodeHashStr().equals(genericQC.getBlockHash())
                && receiveNode.getJustify().getBlockViewNo() > lockedQC.getBlockViewNo()) {
            return true;
        }

        return false;
    }

    /**
     * 回退共识上下文环境
     *
     * @param receiveNode
     */
    public void rollbackConsensusContext(HotStuffDataNode receiveNode) {
        this.genericQC = receiveNode.getJustify();
    }

    /**
     * 通道中的节点数，简化外层调用而已
     */
    int getPeerCount() {
        //long sizeTotal=this.channel.getMemberPeerList().parallelStream().filter(peer -> peer.isState()).count();
        //return (int)sizeTotal;
        return this.channel.getMemberPeerList().size();
    }


    /**
     * 根据区块的Hash获得对应的Block
     *
     * @param blockHash
     * @return
     */
    public Block getBlock(String blockHash) {
        HotStuffDataNode hotStuffDataNode = this.localDataNodeMap.get(blockHash);
        if (hotStuffDataNode != null) {
            log.debug("getblock in hotstuffDataNode,blockhash:{},txList:{}",hotStuffDataNode.getBlock().getHash(),hotStuffDataNode.getBlock().getTransactionList());
            return hotStuffDataNode.getBlock();
        }
        Block block = cachedBlockMap.get(blockHash);
        if (null != block) {
            log.debug("getblock in cachedBlockMap,blockhash:{},txList:{}",block.getHash(),block.getTransactionList());
            return block;
        }
        block = committedBlockMap.get(blockHash);
        if (null != block) {
            log.debug("getblock in committedBlockMap,blockhash:{},txList:{}",block.getHash(),block.getTransactionList());
            return block;
        }
        block = ledgerMgr.queryCacheBlock(this.getChannel().getChannelId(), blockHash);
        if (null != block) {
            log.debug("getblock in DB cached block,blockhash:{},txList:{}",block.getHash(),block.getTransactionList());
            return block;
        }
        block = ledgerMgr.queryBlock(this.getChannel().getChannelId(), blockHash);
        if(null!=block) {
            log.debug("getblock in DB committed block,blockhash:{},txList:{}", block.getHash(), block.getTransactionList());
        }
        return block;
    }

    public Block getCommittedBlock(String hash) {
        Block block = committedBlockMap.get(hash);
        if (null == block) {
            return ledgerMgr.queryBlock(getChannel().getChannelId(), hash);
        }
        return block;
    }

    //------------------------------------------------------
    private class BatchWriteGenericMsgTask implements Runnable {
        private ConsensusContext consensusContext;

        public void setConsensusContext(ConsensusContext consensusContext) {
            this.consensusContext = consensusContext;
        }

        public void run() {
            while (true) {
                if (consensusContext.getGenericMsgMap().size() >= consensusContext.getMaxCachedGenericMsg()) {
                    log.info(ModuleClassification.ConM_CC_ +"MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION +","+channel.getChannelId()+ ",persist cached genericMsgs");
                    this.consensusContext.persistCachedGenericMsg();
                    this.deleteStaleGenericMsg();
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        log.error(ModuleClassification.ConM_CC_ +"TError"+"MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION +","+channel.getChannelId()+ ",Interrupted in the thread of persisting cached genericMsgs",ex);
                    }

                }
            }
        }

        private void deleteStaleGenericMsg() {
            //TODO:delete genericMsg with viewNo < currentLocalviewNo
            log.info(ModuleClassification.ConM_CC_ +"MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION +","+channel.getChannelId()+ ",delete genericMsg, view = " + (currView.getNo() - 1));
            ledgerMgr.deleteGenericMsg(channel.getChannelId(), currView.getNo() - 1);
        }
    }

    public void acceptGenericMsg(GenericMsg genericMsg) {
        /*this.genericMsgMapLock.writeLock().lock();
        if (this.getGenericMsgMap().size() >= maxCachedGenericMsg) {
            this.getGenericMsgMapBackup().put(genericMsg.getViewNo(), genericMsg);
        } else {
            this.getGenericMsgMap().put(genericMsg.getViewNo(), genericMsg);
        }
        this.genericMsgMapLock.writeLock().unlock();*/
        this.getGenericMsgMap().put(genericMsg.getViewNo(), genericMsg);
    }

    private void persistCachedGenericMsg() {
        Map<Long, GenericMsg> genericMsgMap = this.getGenericMsgMap();
        try {
            for (Map.Entry<Long, GenericMsg> entry :
                    genericMsgMap.entrySet()) {
                log.info(ModuleClassification.ConM_CC_ +"MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION +","+channel.getChannelId()+ ",insert genericMsg, view = " + entry.getValue().getViewNo());
                ledgerMgr.insertGenericMsg(channel.getChannelId(), entry.getValue());
            }
        } catch (Exception ex) {
            log.error(ModuleClassification.ConM_CC_ +"TError"+"MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION +","+channel.getChannelId()+ ",Exception occurs in insertion of genericMsg" + ex.getMessage(),ex);
        }

        this.genericMsgMapLock.writeLock().lock();
        this.setGenericMsgMap(this.getGenericMsgMapBackup());
        this.setGenericMsgMapBackup(new ConcurrentHashMap<>());
        genericMsgMap.clear();
        this.genericMsgMapLock.writeLock().unlock();
    }

    public Map<Long, GenericMsg> getCachedGenericMsgs() {
        /*if (this.getGenericMsgMap().size() >= maxCachedGenericMsg && this.getGenericMsgMapBackup().size() != 0) {
            return this.getGenericMsgMapBackup();
        } else {
            return this.getGenericMsgMap();
        }*/
        return this.getGenericMsgMap();
    }

    public void cleanCachedGenericMsgs(Long viewNo) {
        this.getGenericMsgMap().remove(viewNo);
        if (this.getGenericMsgMap().size() >= this.getMaxCachedGenericMsg()) {
            //log.info("clean cached genericMsgs, size is {}", this.getGenericMsgMap().size());
            Iterator<Map.Entry<Long, GenericMsg>> iterator = this.getGenericMsgMap().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, GenericMsg> entry = iterator.next();
                if (entry.getKey() < viewNo) {
                    iterator.remove();
                }
            }
            //log.info("after clean cached genericMsgs, size is {}", this.getGenericMsgMap().size());

        }
    }

    public Map<Long, GenericMsg> getPersistedCachedGenericMsg(Long viewNo) {
        Map<Long, GenericMsg> genericMsgHashMap = ledgerMgr.selectGenericMsg(channel.getChannelId(), viewNo);
        return genericMsgHashMap;
    }

    public void adjustBackViewTimeOut() {
        Long defaultTimeOut = Long.parseLong(channel.getExtendParam(ConsensusExtendedParamEnum.VIEW_TIMEOUT_MS.getCode()));
        this.currentTimeOut = (this.currentTimeOut / 2 >= defaultTimeOut) ? this.currentTimeOut / 2 : defaultTimeOut;
        //this.currentTimeOut = defaultTimeOut;
        log.info("MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",adjust timeout to "+ this.currentTimeOut + "ms");
    }

    public int getQCMinCntOfHeight(Long height) {
        /*if (height > this.channel.getLatestChannelChangeHeight() + 3) {
            return this.qcMinNodeCnt;
        } else {
            List<Peer> peerList = this.channel.getValidMemberPeerList(height);
            return calcQCMinSupport(peerList.size());
        }*/
        List<Peer> peerList = this.channel.getValidMemberPeerList(height);
        //peerList = peerList.parallelStream().filter(peer -> peer.isState()).collect(Collectors.toList());
        long sizeTotal = peerList.size();
/*        for (Peer peer : peerList) {
            if (peer.getPeerCert().get(0).getFlag().equals("0")) {
                Long maxBlockHeight = peer.getPeerCert().get(0).getBlockHeight();
                if (height < maxBlockHeight + 3) {
                    sizeTotal--;
                }
            }
        }*/
        if(sizeTotal<=0) sizeTotal=1l;
        return calcQCMinSupport((int) sizeTotal);
    }

    public void readLock(Long locktime) {
        //log.info(ModuleClassification.ConM_CC_.toString() + " *****test lock read get at " + locktime.toString() + "******");
        this.readWriteLock.readLock().lock();
        //log.info(ModuleClassification.ConM_CC_.toString() + " *****test lock read get at " + locktime.toString() + "******");
    }

    public void readUnlock(Long locktime) {
        //log.info(ModuleClassification.ConM_CC_.toString() + " *****test unlock read at " + locktime.toString() + "******");
        this.readWriteLock.readLock().unlock();
    }

    public void writeLock(Long locktime) {
        //log.info(ModuleClassification.ConM_CC_.toString() + " *****test lock write get at " + locktime.toString() + "******");
        this.readWriteLock.writeLock().lock();
        //log.info(ModuleClassification.ConM_CC_.toString() + " *****test lock write get ok of " + locktime.toString() + "******");
    }

    public void writeUnlock(Long locktime) {
        //log.info(ModuleClassification.ConM_CC_.toString() + " *****test unlock write at " + locktime.toString() + "******");
        this.readWriteLock.writeLock().unlock();
    }


    public ConsensusContext clone2() {
        ConsensusContext consensusContext = new ConsensusContext();
/*        consensusContext.setConsensusStageEnum(this.getConsensusStageEnum());
        consensusContext.setBlockHeight(this.getBlockHeight());
        consensusContext.setBlockVoteMap(this.blockVoteMap);
        consensusContext.setChannel(this.channel);
        consensusContext.setCurrentTimeOut(this.getCurrentTimeOut());
        consensusContext.setCurrView(this.getCurrView());
        consensusContext.setMyself(this.myself);
        consensusContext.setGenericMsgMap(this.getGenericMsgMap());
        consensusContext.setGenericQC(this.genericQC);
        consensusContext.setLockedQC(this.lockedQC);
        consensusContext.setHighestQC(this.highestQC);
        consensusContext.setOrderedPeerList(this.getOrderedPeerList());*/
        BeanUtils.copyProperties(this, consensusContext);
        return consensusContext;
    }

    public int getQcMinNodeCnt() {
        //long sizeTotal = this.channel.getMemberPeerList().parallelStream().filter(peer -> peer.isState()).count();
      List<Peer> peerList = this.getOrderedPeerList().parallelStream().filter(peer -> peer.isState()).collect(Collectors.toList());
        long sizeTotal = peerList.size();
/*        for (Peer peer : peerList) {
            if (peer.getPeerCert().get(0).getFlag().equals("0")) {
                Long maxBlockHeight = peer.getPeerCert().get(0).getBlockHeight();
                if (this.getHighestQC().getHeight() <= maxBlockHeight + 3) {
                    sizeTotal--;
                }
            }
        }
        if(sizeTotal<=0) sizeTotal=1l;*/
        return calcQCMinSupport((int) sizeTotal);
        //return this.qcMinNodeCnt;
    }

    public void putCachedBlock(Block block) {
        cachedBlockMap.put(block.getHash(), block);
        if (block.getBlockHeader().getHeight().longValue() > cachedBlockHeight) {
            cachedBlockHeight = block.getBlockHeader().getHeight();
        }
    }

    public void putCommittedBlock(Block block) {
        if (block.getBlockHeader().getHeight().longValue() > blockHeight) {
            blockHeight = block.getBlockHeader().getHeight();
            committedBlockMap.put(block.getHash(), block);
        }
    }

    public String getConsensusInfo() {
        return "{\"consensusStage\":\"" + getConsensusStageEnum().toString() + "\"," +
                "\"channelId\":\"" + getChannel().getChannelId() + "\"," +
                "\"view\":\"" + getCurrView().getNo() + "\"," +
                "\"highestQC\":\"" + getHighestQC().getBlockHash() + "\"," +
                "\"genericQC\":\"" + getGenericQC().getBlockHash() + "\"," +
                "\"lockedQC\":\"" + getLockedQC().getBlockHash() + "\"," +
                "\"prePrepareBlock\":\"" + getHashPrePrepareBlock() + "\"}";
    }

}
