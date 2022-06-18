package com.jinninghui.newspiral.ledger.mgr.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.Enum.PeerActionTypeEnum;
import com.jinninghui.newspiral.common.entity.Enum.VersionEnum;
import com.jinninghui.newspiral.common.entity.VerifiableData;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.block.BlockHeader;
import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.chain.ChannelChange;
import com.jinninghui.newspiral.common.entity.chain.ChannelModifyRecord;
import com.jinninghui.newspiral.common.entity.chain.ChannelSummary;
import com.jinninghui.newspiral.common.entity.chain.Peer;
import com.jinninghui.newspiral.common.entity.chain.PeerCert;
import com.jinninghui.newspiral.common.entity.chain.PeerCertificate;
import com.jinninghui.newspiral.common.entity.chain.PeerCertificateCipher;
import com.jinninghui.newspiral.common.entity.chain.PeerChannelRelation;
import com.jinninghui.newspiral.common.entity.chain.PeerOrganization;
import com.jinninghui.newspiral.common.entity.chain.PeerServiceUrl;
import com.jinninghui.newspiral.common.entity.chain.PeerServiceUrls;
import com.jinninghui.newspiral.common.entity.common.base.BizVO;
import com.jinninghui.newspiral.common.entity.common.logmodule.LogModuleCodes;
import com.jinninghui.newspiral.common.entity.common.utils.CloneUtils;
import com.jinninghui.newspiral.common.entity.consensus.GenericMsg;
import com.jinninghui.newspiral.common.entity.exception.NewSpiralErrorEnum;
import com.jinninghui.newspiral.common.entity.exception.NewspiralException;
import com.jinninghui.newspiral.common.entity.identity.Identity;
import com.jinninghui.newspiral.common.entity.identity.IdentityKey;
import com.jinninghui.newspiral.common.entity.member.Member;
import com.jinninghui.newspiral.common.entity.member.QueryMemberListReq;
import com.jinninghui.newspiral.common.entity.member.Role;
import com.jinninghui.newspiral.common.entity.record.InterfaceRecord;
import com.jinninghui.newspiral.common.entity.record.InterfaceRecordBO;
import com.jinninghui.newspiral.common.entity.record.InterfaceRecordSummary;
import com.jinninghui.newspiral.common.entity.record.PageInfo;
import com.jinninghui.newspiral.common.entity.smartcontract.QuerySmartContractListReq;
import com.jinninghui.newspiral.common.entity.smartcontract.QuerySmartContractReq;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContract;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContractModelKeyReq;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.common.entity.state.StateHistory;
import com.jinninghui.newspiral.common.entity.state.StateHistoryBO;
import com.jinninghui.newspiral.common.entity.state.StateHistoryModel;
import com.jinninghui.newspiral.common.entity.state.StateHistoryResp;
import com.jinninghui.newspiral.common.entity.state.WorldState;
import com.jinninghui.newspiral.common.entity.state.WorldStateModifyRecord;
import com.jinninghui.newspiral.common.entity.state.WorldStateResp;
import com.jinninghui.newspiral.common.entity.task.TansactionShortResp;
import com.jinninghui.newspiral.common.entity.task.Task;
import com.jinninghui.newspiral.common.entity.transaction.ExecutedTransaction;
import com.jinninghui.newspiral.common.entity.transaction.Transaction;
import com.jinninghui.newspiral.common.entity.transaction.TransactionAttached;
import com.jinninghui.newspiral.common.entity.transaction.TransactionCompile;
import com.jinninghui.newspiral.common.entity.transaction.TransactionResp;
import com.jinninghui.newspiral.common.entity.util.MerkleUtil;
import com.jinninghui.newspiral.common.entity.version.VersionResp;
import com.jinninghui.newspiral.consensus.hotstuff.ConsensusMsgProcessor;
import com.jinninghui.newspiral.ledger.mgr.BlockChangesSnapshots;
import com.jinninghui.newspiral.ledger.mgr.LedgerMgr;
import com.jinninghui.newspiral.ledger.mgr.LedgerThreadLocalContext;
import com.jinninghui.newspiral.ledger.mgr.MemberLedgerMgr;
import com.jinninghui.newspiral.ledger.mgr.StateAccessModeEnum;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.BlockModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.ChannelModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.IdentityModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.InterfaceRecordModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.InterfaceRecordSummaryModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.MemberModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.MemberRoleModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.MessageModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.PeerCertificateModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.PeerChannelModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.PeerChannelModelWithBLOBs;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.PeerModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.PersistTaskModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.RoleModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.SmartContractModelKey;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.SmartContractModelWithBLOBs;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.StateModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.SystemVersionModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.TransactionModel;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.TransactionModelExpandStateHistory;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.TransactionModelWithBLOBs;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.BlockModelMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.ChannelModelMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.IdentityModelMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.InterfaceRecordMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.InterfaceRecordSummaryMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.MemberMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.MemberRoleMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.MessageModelMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.PeerCertificateModelMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.PeerChannelModelMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.PeerModelMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.PersistTaskModelMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.RoleMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.SmartContractModelMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.StateHistoryModelMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.StateModelMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.SystemVersionMapper;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.TransactionModelMapper;
import com.jinninghui.newspiral.security.DataSecurityMgr;
import com.jinninghui.newspiral.security.SecurityService;
import com.jinninghui.newspiral.security.SecurityServiceMgr;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.jinninghui.newspiral.ledger.mgr.impl.domain.IdentityModel.transforIdentityKeyBrief;

/**
 * @author lida
 * @date 2019/7/5 18:46
 */
@Slf4j
@Service
public class LedgerMgrImpl implements LedgerMgr {


    @SofaReference
    private ConsensusMsgProcessor consensusMsgProcessor;
    @SofaReference
    private SecurityServiceMgr securityServiceMgr;
    @SofaReference
    private DataSecurityMgr dataSecurityMgr;
    @Autowired
    private PersistActionHandlerImpl persistActionHandler;

    @Autowired
    private BlockModelMapper blockModelMapper;
    @Autowired
    private ChannelModelMapper channelModelMapper;
    @Autowired
    private TransactionModelMapper transactionModelMapper;
    @Autowired
    private StateModelMapper stateModelMapper;
    @Autowired
    private IdentityModelMapper identityModelMapper;
    @Autowired
    private PeerModelMapper peerModelMapper;
    @Autowired
    private PeerChannelModelMapper peerChannelModelMapper;
    @Autowired
    private SmartContractModelMapper smartContractModelMapper;
    @Autowired
    private PersistTaskModelMapper persistTaskModelMapper;
    @Autowired
    private MessageModelMapper messageModelMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private MemberRoleMapper memberRoleMapper;
    @Autowired
    private MemberLedgerMgr memberLedgerMgr;
    @Autowired
    private PeerCertificateModelMapper peerCertificateModelMapper;
    @Autowired
    private InterfaceRecordMapper interfaceRecordMapper;
    @Autowired
    private InterfaceRecordSummaryMapper interfaceRecordSummaryMapper;
    @Autowired
    private StateHistoryModelMapper stateHistoryModelMapper;
    @Autowired
    private SystemVersionMapper systemVersionMapper;

    /**
     * 对应交易中的已编译的智能合约，其中SDKTransaction中包含了未编译前的智能合约，TransactionCompile为对应的编译之后的智能合约
     * key：SmartContractInfo.toString，value：SDKTransaction & TransactionCompile
     */
    private Map<String, TransactionCompile> mapTXCompile = new HashMap<>();

    private final static String ledgerModuleKey = "ledgerM";
    /**
     * 智能合约缓存 key为 channelId+name+version
     */
    private ConcurrentHashMap<String, SmartContract> smartContractConcurrentHashMap = new ConcurrentHashMap<>();

    /**
     * 通道缓存,目前只是通道信息 key为channelId
     */
    private ConcurrentHashMap<String, Channel> channelModelConcurrentHashMap = new ConcurrentHashMap<>();


    /**
     * 身份缓存，key为主键
     */
    private ConcurrentHashMap<String, Identity> identityConcurrentHashMap = new ConcurrentHashMap<>();

    /**
     * 通道节点缓存，key为channelId
     */
    private ConcurrentHashMap<String, List<Peer>> peersConcurrentHashMap = new ConcurrentHashMap<>();


    /**
     * 节点缓存 key为peerId
     */
    private ConcurrentHashMap<String, Peer> peerConcurrentHashMap = new ConcurrentHashMap<>();


    private ConcurrentHashMap<String, TansactionShortResp> txShortMap = new ConcurrentHashMap<>();

    /**
     * 用于缓存交易hash key-过期时间 value-交易hashList
     */
    private final ConcurrentHashMap<Long, Set<String>> txHashCache = new ConcurrentHashMap<>();

    /**
     * 5分钟
     */
    private static final Long exprireTime = 300000L;


    private ExecutorService executorService = Executors.newFixedThreadPool(32);

    private ExecutorService executorServiceForDeleteCache = Executors.newSingleThreadExecutor();


    /**
     * 通道智能合约缓存，key为channelId
     */
    // private ConcurrentHashMap<String, List<SmartContract>> smartContractsConcurrentHashMap = new ConcurrentHashMap<>();


    /**
     * key为peerId,  证书文件hash,证书文件状态
     */
    // private ConcurrentHashMap<String, HashMap<String, String>> peerCertificateConcurrentHashMap = new ConcurrentHashMap<>();
    @Override
    public boolean hasTrans(String transHash, String channelId) {
        TransactionModelWithBLOBs model = transactionModelMapper.selectByTxHash(channelId, transHash);
        if (model != null) {
            return true;
        }
        return false;
    }


    @Override
    public ChannelSummary queryChannelSummary(String channelId) {
        ChannelSummary summary = new ChannelSummary();
        try {
            List<BlockModel> models = blockModelMapper.selectBlockByChannelId(channelId, 1);
            if (!CollectionUtils.isEmpty(models)) {
                BlockModel result = models.get(0);
                summary.setHeight(result.getBlockId());
                summary.setHash(result.getHash());
                summary.setChannelId(channelId);
                long latestBlockTime = result.getPackTimestamp().getTime();
                long now = new Date().getTime();
                ChannelModel channelResult = channelModelMapper.selectByPrimaryKey(channelId);
                long maxBlockInterval = channelResult.getBlockMaxInterval();
                if (Math.abs(latestBlockTime - now) < 2 * maxBlockInterval) {
                    summary.setSyncWithNetwork(true);
                } else {
                    summary.setSyncWithNetwork(false);
                }
            }
        } catch (Exception ex) {
            //其实至少有两种情况，第一是根本没有这个对应的Block表，第二是该表中没有数据
            log.warn(ModuleClassification.LedM_LMI_ + "TError" + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",读取ChannelId=" + channelId + "对应的Block表异常，一般情况下是没有该表，构造默认的ChainSummary返回，ex:" + ex.toString(), ex);
        }
        return summary;
    }

    @Override
    public String getChannelConfigValue(String key, String channelId) {
        ChannelModel record = channelModelMapper.selectByPrimaryKey(channelId);
        Map map = JSONObject.parseObject(JSONObject.toJSONString(record), Map.class);
        return map.get(key).toString();
    }


    @Override
    public Long getChannelConfigLongValue(String key, String channelId) {
        ChannelModel record = channelModelMapper.selectByPrimaryKey(channelId);
        String name = "get" + key;
        try {
            Method method = record.getClass().getMethod(name);
            return (long) method.invoke(record);
        } catch (Exception e) {
            throw new NewspiralException(NewSpiralErrorEnum.CHANNEL_NOT_EXIST);
        }
    }

    /**
     * 插入缓存区块，只做为状态证据，不做为世界状态修改
     *
     * @param channelId
     * @param block
     * @return
     */
    @Transactional
    @Override
    public void insertCacheBlock(String channelId, Block block) {
        log.info(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",开始缓存Block" + block.getHash() + ",该缓存Block包含" + block.getTransactionList().size() + "个交易");
        BlockModel blockModel = BlockModel.createInstance(block);
        BlockModel form = blockModelMapper.selectCacheByHash(channelId, blockModel.getHash());
        if (null != form) {
            return;
        }
        blockModelMapper.insertCachedBlock(blockModel);
        //log.info("insertCachedBlock table time is {}", System.currentTimeMillis() - curr);

        //批量插入来提升性能,每5000条插入一次
        int transSize = block.getTransactionList().size();
        //交易列表
        List<TransactionModelWithBLOBs> transList = new ArrayList<>();
        for (int i = 0; i < transSize; i++) {
            ExecutedTransaction executedTransaction = block.getTransactionList().get(i);
            //indexInBlock从1开始
            transList.add(TransactionModelWithBLOBs.createInstance(executedTransaction, block, i + 1));
            if (i % 100 == 0) {
                transactionModelMapper.batchInsertCache(transList);
                transList.clear();
            }
        }
        //不是5000倍数最后再提交一次
        if (!CollectionUtils.isEmpty(transList)) {
            transactionModelMapper.batchInsertCache(transList);
        }
    }

    /**
     * 根据区块高度查询某个缓存区块
     *
     * @param channelId
     * @param blockHeight
     * @return
     */
    @Override
    public List<Block> queryCacheBlock(String channelId, Long blockHeight) {
        List<BlockModel> blockRecordList = blockModelMapper.selectBlockCacheByHeight(channelId, blockHeight);
        if (blockRecordList == null) {
            return null;
        }

        // 这种情况可能性较小
        LinkedList<Block> linkedList = new LinkedList<>();
        for (BlockModel block : blockRecordList) {
            linkedList.add(queryCacheBlock(channelId, block.getHash()));
        }

        return linkedList;

    }

    /**
     * 根据区块Hash值查询某个缓存区块
     *
     * @param channelId
     * @param blockHashStr
     * @return
     */
    @Override
    public Block queryCacheBlock(String channelId, String blockHashStr) {
        BlockHeader blockHeader = this.queryBlockCacheHeader(channelId, blockHashStr);
        if (null == blockHeader) {
            return null;
        }
        Block block = new Block();
        block.setBlockHeader(blockHeader);
        List<TransactionModelWithBLOBs> txRecord = transactionModelMapper.selectCacheByBlockHash(channelId, blockHashStr);
        List<ExecutedTransaction> txList = new LinkedList<>();
        for (TransactionModelWithBLOBs tx : txRecord) {
            txList.add(tx.toExecutionTransaction());
        }
        block.addTransactionList(txList);
        return block;
    }

    @Override
    public void deleteCommittedBlock(String channelId, Long height) {
        blockModelMapper.deleteCommittedBlock(channelId, height);
    }

    @Override
    public void deleteCachedBlock(String channelId, Long height) {
        blockModelMapper.deleteCachedBlock(channelId, height);
    }

    @Override
    public void deleteCachedBlockBehind(String channelId, Long height) {
        blockModelMapper.deleteCachedBlockBehind(channelId, height);
    }


    /**
     * 根据通道Id和区块高度查询区块
     *
     * @param channelId
     * @param blockHeight
     * @return
     */
    @Override
    public Block queryBlock(String channelId, Long blockHeight) {
        BlockModel blockRecord = blockModelMapper.selectByHeight(channelId, blockHeight);
        if (blockRecord == null) {
            return null;
        }
        Block block = new Block();
        block.setBlockHeader(blockRecord.toBlockHeader());
        List<TransactionModelWithBLOBs> txRecord = transactionModelMapper.selectByBlockHash(channelId, blockRecord.getHash());
        List<ExecutedTransaction> txList = new LinkedList<>();
        for (TransactionModelWithBLOBs tx : txRecord) {
            txList.add(tx.toExecutionTransaction());
        }
        block.addTransactionList(txList);
        return block;
    }

    @Override
    public Block queryBlock(String chainId, String blockHash) {
        Block block = new Block();
        BlockHeader blockHeader = this.queryBlockHeader(chainId, blockHash);
        if (null == blockHeader) return null;
        block.setBlockHeader(blockHeader);
        List<TransactionModelWithBLOBs> txRecord = transactionModelMapper.selectByBlockHash(chainId, blockHash);
        List<ExecutedTransaction> txList = new LinkedList<>();
        for (TransactionModelWithBLOBs tx : txRecord) {
            txList.add(tx.toExecutionTransaction());
        }
        block.addTransactionList(txList);
        return block;
    }

    @Override
    public List<Block> queryBlock(String channelId, Integer num) {
        List<BlockModel> models = blockModelMapper.selectBlockByChannelId(channelId, num <= 0 ? 1 : num);
        ArrayList<Block> blockList = new ArrayList<>();
        for (BlockModel model : models) {
            Block block = new Block();
            block.setBlockHeader(model.toBlockHeader());
            List<TransactionModelWithBLOBs> txRecord = transactionModelMapper.selectByBlockHash(channelId, model.getHash());
            List<ExecutedTransaction> txList = new LinkedList<>();
            for (TransactionModelWithBLOBs tx : txRecord) {
                txList.add(tx.toExecutionTransaction());
            }
            block.addTransactionList(txList);
            blockList.add(block);
        }
        return blockList;
    }

    @Override
    public BlockHeader queryBlockHeader(String channelId, String blockHash) {
        BlockModel blockRecord = blockModelMapper.selectByHash(channelId, blockHash);
        if (blockRecord == null) {
            return null;
        } else {
            return blockRecord.toBlockHeader();
        }
    }


    private BlockHeader queryBlockCacheHeader(String channelId, String blockHash) {
        BlockModel blockRecord = blockModelMapper.selectCacheByHash(channelId, blockHash);
        if (blockRecord == null) {
            return null;
        } else {
            return blockRecord.toBlockHeader();
        }
    }

    @Override
    public WorldState queryWorldState(String channelId, String key, boolean flag) {
        StateModel stateRecord = stateModelMapper.selectByPrimaryKey(channelId, key, flag);
        if (null == stateRecord) return null;
        return stateRecord.toWorldState();
    }

    private String getTransactionCacheTableName(String channelId) {
        return "TRANSACTION_CACHE_" + channelId;
    }


    @Override
    public Transaction queryTransaction(String clientIdentitykey, String clientTxId, String channelId) {
        //String channelId = LedgerThreadLocalContext.currChannelId.get();
        //log.info(ModuleClassification.LedM_LMI+"queryTransaction输出LedgerThreadLocalContext:" + JSON.toJSONString(LedgerThreadLocalContext.currChannelId));
        TransactionModelWithBLOBs txRecord = transactionModelMapper.selectByClientTxId(clientIdentitykey, clientTxId, channelId);
        if (null == txRecord) return null;
        return txRecord.toTransaction();
    }

    @Override
    public List<Transaction> queryTxHistory(String channelId, String key) {
        List<TransactionModelWithBLOBs> transactionModelWithBLOBs = transactionModelMapper.selectAllByKey(channelId, key);
        List<Transaction> result = new ArrayList<>();
        for (TransactionModelWithBLOBs tx : transactionModelWithBLOBs) {
            result.add(tx.toTransaction());
        }
        return result;
    }

    @Override
    public Transaction queryTransaction(String txHash, String channelId) {
        //String channelId = LedgerThreadLocalContext.currChannelId.get();
        TransactionModelWithBLOBs txRecord = transactionModelMapper.selectByTxHash(channelId, txHash);
        if (null == txRecord) return null;
        return txRecord.toTransaction();
    }

    @Override
    public TransactionResp queryTransactionResp(String txHash, String channelId) {
        //String channelId = LedgerThreadLocalContext.currChannelId.get();
        TransactionModelWithBLOBs txRecord = transactionModelMapper.selectByTxHash(channelId, txHash);
        if (null == txRecord) return null;
        Transaction transaction = txRecord.toTransaction();
        TransactionResp transactionResp = TransactionResp.transferTransactionResp(transaction);
        TransactionAttached transactionAttached = new TransactionAttached();
        Long blockHeight = blockModelMapper.selectHeightByHash(txRecord.getChannelId(), txRecord.getBlockHash());
        transactionAttached.setBlockHeight(blockHeight);
        //transactionAttached.setBlockSize(new Long(block.toString().getBytes().length));
        //transactionAttached.setBuilderPeerId(block.getBlockHeader().getPackagerAndSign().getIdentityKey().getValue());
        Channel channel = getChannel(txRecord.getChannelId());
        transactionResp.setChannelId(channel.getChannelId());
        transactionResp.setChannelName(channel.getName());
        transactionResp.setTransactionAttached(transactionAttached);
        return transactionResp;
    }

    @Override
    public Transaction queryTransaction(String blockHash, Integer txIndex, String channelId) {
        //String channelId = LedgerThreadLocalContext.currChannelId.get();
        TransactionModelWithBLOBs txRecord = transactionModelMapper.selectByBlockHashAndIndex(channelId, blockHash, txIndex);
        if (null == txRecord) return null;
        return txRecord.toTransaction();
    }

    @Override
    public List<Transaction> queryTransactionList(String blockHash, String channelId) {
        List<Transaction> transactions = new ArrayList<>();
        List<TransactionModelWithBLOBs> transactionModelWithBLOBs = transactionModelMapper.selectByBlockHash(channelId, blockHash);
        for (TransactionModelWithBLOBs modelWithBLOBs : transactionModelWithBLOBs) {
            Transaction transaction = modelWithBLOBs.toTransaction();
            transactions.add(transaction);
        }
        return transactions;
    }

    /**
     * 根据区块hash和交易索引查询交易列表
     *
     * @param blockHash
     * @param channelId
     * @param txIndexFrom
     * @param txIndexTo
     * @return
     */
    public List<Transaction> queryTransactionByBlockHashAndTxRegion(String blockHash, String channelId, Integer txIndexFrom, Integer txIndexTo) {
        List<Transaction> transactions = new ArrayList<>();
        //查询区块总交易数
        List<TransactionModelWithBLOBs> allTransOfBlock = transactionModelMapper.selectByBlockHash(channelId, blockHash);
        if (CollectionUtils.isEmpty(allTransOfBlock)) {
            return transactions;
        }
        //对交易Index进行判断
        if (txIndexFrom == null || txIndexTo == null) {
            txIndexFrom = txIndexFrom == null ? 1 : txIndexFrom;
            txIndexTo = txIndexTo == null ? allTransOfBlock.size() : txIndexTo;
        } else {
            txIndexFrom = txIndexFrom > allTransOfBlock.size() ? allTransOfBlock.size() : txIndexFrom;
            txIndexTo = txIndexTo > allTransOfBlock.size() ? allTransOfBlock.size() : txIndexTo;
        }

        if (txIndexFrom <= 0 || txIndexTo <= 0 || txIndexFrom > txIndexTo) {
            log.error(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "交易索引错误");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM);
        }
        List<TransactionModelWithBLOBs> transactionModelWithBLOBs = transactionModelMapper.selectByBlockHashAndIndexRegion(channelId, blockHash, txIndexFrom, txIndexTo);
        for (TransactionModelWithBLOBs modelWithBLOBs : transactionModelWithBLOBs) {
            Transaction transaction = modelWithBLOBs.toTransaction();
            transactions.add(transaction);
        }
        return transactions;
    }

    /**
     * 根据客户端交易Id来查询交易
     *
     * @param channelId
     * @param clientId
     * @param clientIdentitykey
     * @return
     * @author whj
     */
    @Override
    public Transaction getTransByClientId(String channelId, String clientId, String clientIdentitykey) {
        Transaction transaction = null;
        try {
            TransactionModelWithBLOBs transactionModelWithBLOBs = transactionModelMapper.selectTransByClientId(channelId, clientId, clientIdentitykey);
            transaction = transactionModelWithBLOBs.toTransaction();
        } catch (Exception e) {
            return null;
        }
        return transaction;
    }


    @Override
    public void cacheTxShort(Block block) {
        //清理已经过期的交易hash
        Long currTime = System.currentTimeMillis();
        for (Map.Entry<Long, Set<String>> entry : txHashCache.entrySet()) {
            //缓存中的过期时间
            long cacheMillis = entry.getKey();
            //缓存中的交易hash
            Set<String> txHashs = entry.getValue();
            if (cacheMillis + exprireTime < currTime) {
                //过期时间<当前时间，说明已过期，则移除已经缓存中过期的数据
                for (String hash : txHashs) {
                    txShortMap.remove(hash);
                }
                txHashCache.remove(cacheMillis);
                continue;
            }
        }

        Set<String> txHashList = new HashSet<>();
        for (ExecutedTransaction transaction : block.getTransactionList()) {
            TansactionShortResp resp = new TansactionShortResp();
            resp.setTimeStamp(System.currentTimeMillis());
            resp.setTransHash(transaction.getSDKTransactionHash());
            txShortMap.put(transaction.getSDKTransactionHash(), resp);
            txHashList.add(transaction.getSDKTransactionHash());
        }
        txHashCache.put(System.currentTimeMillis(), txHashList);

    }

    @Override
    public boolean ifTxExist(String hash) {
        return txShortMap.get(hash) != null;
    }

    @Override
    public Transaction getTransBlockHash(String channelId, String transHash, String peerId, String clientTxId) {
        TransactionModelWithBLOBs txRecord = null;
        if (!StringUtils.isEmpty(transHash)) {
            txRecord = transactionModelMapper.selectByTxHash(channelId, transHash);
        }
        if (!StringUtils.isEmpty(peerId) && !StringUtils.isEmpty(clientTxId)) {
            txRecord = transactionModelMapper.selectByClientTxId(peerId, clientTxId, channelId);
        }
        if (null == txRecord) return null;
        return txRecord.toTransaction();

    }


    @Override
    public Transaction selectByClientTransId(String channelId, String clientTransId) {
        if (StringUtils.isEmpty(channelId) || StringUtils.isEmpty(clientTransId)) {
            return null;
        }
        TransactionModelWithBLOBs txRecord = transactionModelMapper.selectByClientTransId(clientTransId, channelId);
        if (null == txRecord) return null;
        return txRecord.toTransaction();

    }

    @Override
    public void persistBlock(Block block) {
        persistActionHandler
                .init(block) //初始化
                .persistCommitedBlock()  //持久化块
                .persistTrans()  //持久化交易
                .persistWorldState() //持久化世界状态
                .persistChannel() //持久化通道
                .finish(); //结束
    }


    public void updateStateInBlock(Block block) {
        List<WorldState> putState = new ArrayList<>();
        List<WorldState> deleteState = new ArrayList<>();
        for (ExecutedTransaction tx : block.getTransactionList()) {
            for (WorldStateModifyRecord record : tx.getModifiedWorldStateList()) {
                if (record.getOldState() != null && record.getNewState() == null) {
                    deleteState.add(record.getOldState());
                } else if (record.getNewState() != null) {
                    putState.add(record.getNewState());
                }
            }
        }
    }

    /**
     * @param stateModel
     * @param stateModels
     * @return
     */
    private boolean repeatStateKey(StateModel stateModel, List<StateModel> stateModels) {
        int count = 0;
        for (int i = 0; i < stateModels.size(); i++) {
            if (count == 2) {
                return true;
            }
            if (stateModel.getKey().equals(stateModels.get(i).getKey())) {
                count++;
            }
        }
        return false;
    }

    /**
     * 持久化某个交易引起的状态变更
     *
     * @param trans
     */
   /* protected void persistStatesOfTransaction(ExecutedTransaction trans, String channelId) {
        int deleteCnt = 0, updateCnt = 0, insertCnt = 0;
        for (WorldStateModifyRecord sRecord : trans.getModifiedWorldStateList()) {
            if (sRecord.getOldState() == null && sRecord.getNewState() != null) {//插入
                stateModelMapper.insert(StateModel.createInstance(sRecord.getNewState(), channelId,));
                ++insertCnt;
            } else if (sRecord.getOldState() != null && sRecord.getNewState() == null) {//删除
                stateModelMapper.deleteByPrimaryKey(channelId, sRecord.getOldState().getKey());
                ++deleteCnt;
            } else {//更新
                stateModelMapper.updateByPrimaryKey(StateModel.createInstance(sRecord.getNewState(), channelId));
                ++updateCnt;
            }
        }
        log.info(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",为ChannelId持久化Block:插入了" + insertCnt + "个状态记录，更新了" + updateCnt + "个状态记录，删除了" + deleteCnt + "个状态记录");
    }*/

    /**
     * 持久化某个交易引起的通道变更，只有系统交易才会引起通道变更
     *
     * @param
     */
    /*protected void persistTransChannel(Block block) {
        for (ExecutedTransaction trans : block.getTransactionList()) {
            if (trans.getModifiedChannelRecord() != null && trans.getPass().equals("1")) {//交易引起的通道变更，必然是更新
                Channel newChannel = trans.getModifiedChannelRecord().getNewChannel();
                newChannel.setLatestChannelChangeHeight(block.getBlockHeader().getHeight());
                //持久化交易引起的通道参数变更
                //获取交易hash
                String tranHash = trans.getSDKTransactionHash();
                //为新通道的角色赋值id
                if (!CollectionUtils.isEmpty(newChannel.getRoles())) {
                    for (int i = 0; i < newChannel.getRoles().size(); i++) {
                        //如果newChannel中的role的roleid为空，则对roleid进行赋值
                        if (StringUtils.isEmpty(newChannel.getRoles().get(i).getRoleId())) {
                            newChannel.getRoles().get(i).setRoleId(tranHash + i);
                        }
                    }
                }
                this.updateChannelForPersist(block.getHash());
                //todo:这里会不会因为查询不到最新的数据库数据？
                log.info(ModuleClassification.LedM_LMI_ + "query new channel from database");
                consensusMsgProcessor.processChannelUpdate(queryChannel(newChannel.getChannelId()));
                log.info(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",持久化Block，交易hash" + trans.getSdkTransaction().getHash() + ", 更新了Channel:" + newChannel.getChannelId());
            }
        }
    }*/
    @Override
    public List<Channel> readAllChannels() {
        List<Channel> list = new ArrayList<>();
        //在channel表中查询到所有的通道
        List<ChannelModel> listChannelRecord = channelModelMapper.selectAll();
        //Map的key为channelId，value为对应的Peer列表，查询peer_channel表
        Map<String, List<Peer>> channelPeerMap = queryChannelPeerMap();
        Map<String, List<SmartContract>> scListOfChannelMap = queryScListOfChannelMap();

        for (ChannelModel ch : listChannelRecord) {
            Channel channel = ch.toChannel();
            channel.setMemberPeerList(channelPeerMap.get(ch.getChannelId()));
            channel.setSmartContractList(scListOfChannelMap.get(ch.getChannelId()));
            channel.setLatestChannelChangeHeight(this.queryLatestChannelChangeHeight(channel.getChannelId()));
            list.add(channel);
        }
        return list;
    }

    /**
     * key为channelId，value为该channel的所有智能合约
     *
     * @return
     */
    public Map<String, List<SmartContract>> queryScListOfChannelMap() {
        Map<String, List<SmartContract>> map = new HashMap<>();
        List<SmartContract> smartContractList = queryAllSmartContract();
        for (SmartContract sc : smartContractList) {
            String key = sc.getChannelId() + sc.getName() + sc.getVersion();
            //smartContractConcurrentHashMap.put(key, sc);
            List<SmartContract> scList = map.get(sc.getChannelId());
            if (scList == null)
                scList = new ArrayList<>();
            scList.add(sc);
            map.put(sc.getChannelId(), scList);
        }
        return map;
    }

    public List<SmartContract> queryAllSmartContract() {
        List<SmartContractModelWithBLOBs> smartContractList = smartContractModelMapper.selectAll();
        return scModelListToSmartContract(smartContractList);
    }

    /**
     * 查询通道变动的最大高度
     * @param channelId
     * @return
     */
    private Long queryLatestChannelChangeHeight(String channelId) {
        Long height = 0l;
        PeerChannelModel peerChannelModel = peerChannelModelMapper.queryLatestChannelChangeHeight(channelId);
        if (null != peerChannelModel) {
            height = peerChannelModel.getInBlockHeight() > peerChannelModel.getOutBlockHeight() ? peerChannelModel.getInBlockHeight() : peerChannelModel.getOutBlockHeight();
        }
        return height;
    }

    private List<SmartContract> scModelListToSmartContract(List<SmartContractModelWithBLOBs> smartContractList) {
        List<SmartContract> contractList = new ArrayList<>();
        for (SmartContractModelWithBLOBs model : smartContractList) {
            SmartContract sc = model.toSmartContract();
            contractList.add(sc);
        }
        return contractList;
    }

    private List<SmartContract> querySmartContractsOfChannel(String channelId) {
        List<SmartContractModelWithBLOBs> smartContractList = smartContractModelMapper.selectByChannelId(channelId);
        if (null == smartContractList || smartContractList.size() == 0) {
            return Collections.emptyList();
        }
        return scModelListToSmartContract(smartContractList);
    }

    /**
     * 按照channelId分类，返回channel的所有节点列表
     * Map的key为channelId，value为对应的Peer列表
     *
     * @return
     */
    public Map<String, List<Peer>> queryChannelPeerMap() {
        Map<String, List<Peer>> peerChannelMap = new HashMap<>();
        //查询到peer_channel表中每一个分组最新的节点信息
        List<PeerChannelModelWithBLOBs> peerChannelList = peerChannelModelMapper.selectAllLatest();
        //查询Peer表中所有节点的信息
        Map<String, Peer> allPeerMap = this.queryAllPeerMap();
        for (PeerChannelModelWithBLOBs peerChannelModelWithBLOBs : peerChannelList) {
            String channelId = peerChannelModelWithBLOBs.getChannelId();
            List<Peer> peerOfCurChannel = peerChannelMap.get(channelId);
            if (peerOfCurChannel == null) {
                peerOfCurChannel = new ArrayList<>();
            }
            //这里需要复制，因为内存中的Peer的extendedData属性是与Channel相关联的
            //从节点的缓存中根据peerId拿到节点基本信息
            Peer peer = allPeerMap.get(peerChannelModelWithBLOBs.getPeerId()).clone();
            //设置节点和通道相关联的信息
            peer.getPeerId().setChannelId(channelId);
            //前面查询到的就是最新的和通道相关的操作记录
            peer.setPeerChannelRelation(peerChannelModelWithBLOBs.toPeerChannelRelation());
            //查询和通道相关的所有历史记录，并按照updateTime进行排序
            List<PeerChannelModelWithBLOBs> peerChannelHistoryRelationModels = peerChannelModelMapper.selectHistoryPeerChannelRelation(peer.getPeerId().getChannelId(),peer.getPeerId().getValue());
            List<PeerChannelRelation> peerChannelHistoryRelations = peerChannelHistoryRelationModels.stream().map(peerChannelRelation -> peerChannelRelation.toPeerChannelRelation()).collect(Collectors.toList());
            Collections.sort(peerChannelHistoryRelations);
            peer.setPeerChannelRelationList(peerChannelHistoryRelations);
            peer.setPeerCert(queryPeerCertificateList(peer.getPeerId().getValue(), channelId));
            peerOfCurChannel.add(peer);
            peerChannelMap.put(channelId, peerOfCurChannel);
        }
        return peerChannelMap;
    }

    /*public Map<String, List<Peer>> queryLatestChannelPeerMap(List<ChannelModel> channelModelList) {
        Map<String, List<Peer>> channelPeerMap = new HashMap<>();
        for (ChannelModel channelModel : channelModelList) {
            Block latestBlock = queryBlock(channelModel.getChannelId());
            Long height;
            if (latestBlock == null) {
                height = new Long(0);
            } else {
                height = latestBlock.getBlockHeader().getHeight();
            }
            List<Peer> peerList = queryPeersOfChannelByHeight(channelModel.getChannelId(), height);
            channelPeerMap.put(channelModel.getChannelId(), peerList);
        }
        return channelPeerMap;
    }*/

    public List<Peer> queryPeersOfChannel(String channelId) {
        //查询到peer_channel表中每一个分组最新的节点信息
        List<PeerChannelModelWithBLOBs> peerChannelList = peerChannelModelMapper.selectAllLatest();
        //查询Peer表中所有节点的信息
        Map<String, Peer> allPeerMap = this.queryAllPeerMap();
        List<Peer> peerOfCurChannel = new ArrayList<>();
        for (PeerChannelModelWithBLOBs peerChannelModelWithBLOBs : peerChannelList) {
            if (peerChannelModelWithBLOBs.getChannelId().equalsIgnoreCase(channelId)) {
                //这里需要复制，因为内存中的Peer的extendedData属性是与Channel相关联的
                //从节点的缓存中根据peerId拿到节点基本信息
                Peer peer = allPeerMap.get(peerChannelModelWithBLOBs.getPeerId()).clone();
                //设置节点和通道相关联的信息
                peer.getPeerId().setChannelId(channelId);
                //前面查询到的就是最新的和通道相关的操作记录
                peer.setPeerChannelRelation(peerChannelModelWithBLOBs.toPeerChannelRelation());
                //查询和通道相关的所有历史记录，并按照updateTime进行排序
                List<PeerChannelModelWithBLOBs> peerChannelHistoryRelationModels = peerChannelModelMapper.selectHistoryPeerChannelRelation(peer.getPeerId().getChannelId(),peer.getPeerId().getValue());
                List<PeerChannelRelation> peerChannelHistoryRelations = peerChannelHistoryRelationModels.stream().map(peerChannelRelation -> peerChannelRelation.toPeerChannelRelation()).collect(Collectors.toList());
                Collections.sort(peerChannelHistoryRelations);
                peer.setPeerChannelRelationList(peerChannelHistoryRelations);
                peer.setPeerCert(queryPeerCertificateList(peer.getPeerId().getValue(), channelId));
                peerOfCurChannel.add(peer);
            }


        }
        return peerOfCurChannel;
    }

    /**
     * 根据时间范围提取已经共识的交易列表
     * 时间格式为, eg: System.currentTimeMillis()
     *
     * @param channelID
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    public List<TransactionResp> queryTransAlreadyConsensusByTimeRegion(String channelID, Long startTime, Long endTime) {
        if (startTime == null) {
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM);
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        if (endTime < startTime) {
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM);
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date startDate = new Date(startTime);
        Date endDate = new Date(endTime);
        if (endDate.getTime() - startDate.getTime() > 24 * 60 * 60 * 1000) {//间隔大于24小时不允许查询，防止数据跨域太大引起阻塞
            throw new NewspiralException(NewSpiralErrorEnum.QUERY_TOO_LONG_TIME_INTERVAL);
        }
        String startDateTime = simpleDateFormat.format(startDate);
        String endDateTime = simpleDateFormat.format(endDate);

        List<TransactionResp> transactionRespList = new ArrayList<TransactionResp>();
        List<TransactionModelWithBLOBs> transactionModelWithBLOBsList = this.transactionModelMapper.selectByCreateTimestamp(channelID, startDateTime, endDateTime);
        for (TransactionModelWithBLOBs transactionModelWithBLOBs : transactionModelWithBLOBsList) {
            if (transactionModelWithBLOBs != null) {
                TransactionResp transactionResp = TransactionResp.transferTransactionResp(transactionModelWithBLOBs.toTransaction());
                TransactionAttached transactionAttached = new TransactionAttached();
                transactionAttached.setBlockHeight(transactionModelWithBLOBs.getBlockId());
                transactionResp.setTransactionAttached(transactionAttached);
                Channel channel = getChannel(transactionModelWithBLOBs.getChannelId());
                transactionResp.setChannelId(channel.getChannelId());
                transactionResp.setChannelName(channel.getName());
                transactionRespList.add(transactionResp);
            }
        }
        return transactionRespList;
    }

    private PeerOrganization queryPeerOrganization(String organizationId) {
        MemberModel memberModel = memberMapper.selectOrganziationMember("", organizationId);
        if (null == memberModel) return null;
        PeerOrganization peerOrganization = memberModel.toPeerOrganization();
        return peerOrganization;
    }

    @Override
    public List<Peer> queryPeersOfChannelByHeight(String channelId, Long height) {
        if (height < 0) {
            height = new Long(0);
        }
        List<Peer> peerList = new ArrayList<>();
        List<PeerModel> peerModels = peerModelMapper.selectValidPeers(channelId, height);
        for (PeerModel model : peerModels) {
            PeerChannelModelWithBLOBs peerChannelModelWithBLOBs = peerChannelModelMapper.selectByPeerIdAndChannel(channelId, model.getPeerIdValue());
            Peer peer = model.toPeer();
            peer.setPeerChannelRelation(peerChannelModelWithBLOBs.toPeerChannelRelation());
            peer.setPeerCert(queryPeerCertificateList(peer.getPeerId().getValue(), channelId));
            peerList.add(peer);
        }
        return peerList;
    }

    public PeerChannelRelation queryPeerByPeerAndChannelId(String peerId, String channelId) {
        PeerChannelModelWithBLOBs model = peerChannelModelMapper.selectByPeerIdAndChannel(channelId, peerId);
        return model == null ? null : model.toPeerChannelRelation();
    }


    /**
     * 返回的map的key为peerId，即IdentityKey
     * 查询peer表中存在的所有节点的信息，并将其放在map中
     * @return
     */
    public Map<String, Peer> queryAllPeerMap() {
        Map<String, Peer> peerMap = new HashMap<>();
        //查询peer表中所有节点的信息
        List<Peer> peerList = this.queryAllPeers();
        for (Peer peer : peerList) {
            peerMap.put(peer.getPeerId().getValue(), peer);
        }
        return peerMap;
    }


    @Override
    public Channel getChannel(String channelId) {
        Channel channel = channelModelConcurrentHashMap.get(channelId);
        if (channel == null) {
            channel = queryChannel(channelId);
        }
        if (null != channel) {
            channelModelConcurrentHashMap.put(channelId, channel);
            //channelModelConcurrentHashMap.put(channelId, CloneUtils.clone(channel,Channel.class));
        }
        return channel;
    }

    @Override
    public Channel queryChannel(String channelId) {
        ChannelModel record = channelModelMapper.selectByPrimaryKey(channelId);
        if (null == record) {
            log.info(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",Cannot find channel {} in DB", channelId);
            return null;
        }
        Channel channel = record.toChannel();
        channel.setMemberPeerList(queryPeersOfChannel(channelId));
        channel.setSmartContractList(querySmartContractsOfChannel(channelId));
        channel.setRoles(memberLedgerMgr.getRoleList(channelId));
        QueryMemberListReq queryMemberListReq = new QueryMemberListReq();
        queryMemberListReq.setChannelId(channelId);
        channel.setMembers(memberLedgerMgr.getMemberList(queryMemberListReq));
        channel.setLatestChannelChangeHeight(this.queryLatestChannelChangeHeight(channelId));
        List<Block> blockList = this.queryBlockByChannelIdAndHeightRegion(channelId, 0L, 3L);
        channel.setBlockList(blockList);
        return channel;
    }

    /**
     * 支持持久化的原子性
     *
     * @param channel
     */
    @Override
    @Transactional
    public void insertChannel(Channel channel) {
        channelModelConcurrentHashMap.remove(channel.getChannelId());
        ChannelModel model = ChannelModel.createInstance(channel);
        int nRow = channelModelMapper.updateByPrimaryKeySelective(model);
        if (nRow <= 0) {
            channelModelMapper.insert(model);
        }

        //如果节点不存在，插入节点;插入节点与Channel的映射关系
        List<Peer> peerList = channel.getMemberPeerList();
        //for debug
        log.info(ModuleClassification.LedM_LMI_ + "insert channel");
        //TODO 判断一下是不是location节点
        Peer localPeer = this.queryLocalPeer();
        for (Peer peer : peerList) {
            //for debug
            //组织证书是插入节点的时候一起插入的，会进行一个判断，组织证书不和通道绑定哈
            insertPeer(peer, localPeer, channel);
            //判断节点身份是否存在，没有就插入
            String identityKeyStr = JSON.toJSONString(transforIdentityKeyBrief(peer.getPeerId()));
            if (identityModelMapper.selectByPrimaryKey(identityKeyStr) == null) {
                Identity identity = new Identity();
                identity.setKey(peer.getPeerId());
                identity.setParentKey(peer.getOrgId());
                IdentityModel identityModel = IdentityModel.createInstance(identity);
                identityModel.setSetupTimestamp(new Date());
                identityConcurrentHashMap.put(identityKeyStr, identity);
                identityModelMapper.insert(identityModel);
            }
            //插入节点证书
            if (peer.getPeerChannelRelation().getInBlockHeight() == 0) {
                // 插入节点证书
                insertPeerChannelCertificate(peer, channel, localPeer, 0L);
                PeerChannelModelWithBLOBs inOutPeerRecord = peerChannelModelMapper.selectLatestRecordByActionType(peer.getPeerId().getValue(), channel.getChannelId(), PeerActionTypeEnum.IN_OUT.getCode());
                if (inOutPeerRecord == null) {
                    PeerChannelModelWithBLOBs peerChannelModel = PeerChannelModelWithBLOBs.createInstance(channel, peer);
                    //for debug
                    log.info(ModuleClassification.LedM_LMI_ + "insert channel in init");
                    log.info(ModuleClassification.LedM_LMI_ + "peer {}, in {}, out {}", peerChannelModel.getPeerId(), peerChannelModel.getInBlockHeight(), peerChannelModel.getOutBlockHeight());
                    if (peerChannelModel.getJoinTimestamp() == null) {
                        peerChannelModel.setJoinTimestamp(new Date());
                    }
                    if (peerChannelModel.getUpdateTimestamp() == null) {
                        peerChannelModel.setUpdateTimestamp(new Date());
                    }
                    if (peerChannelModel.getOutBlockHeight() == null) {
                        peerChannelModel.setOutBlockHeight(0L);
                    }
                    log.info("insertpeerChannel peerChannelModel={}", JSONObject.toJSON(peerChannelModel));
                    int insertRow = peerChannelModelMapper.insert(peerChannelModel);
                    if (insertRow > 0) {
                        log.info("insertChannel.peerChannel peerId={}", peerChannelModel.getPeerId());
                        peersConcurrentHashMap.remove(channel.getChannelId());
                    }
                }

            }
        }

        //插入智能合约
        for (SmartContract sc : channel.getSmartContractList()) {
            insertSmartContract(sc);
        }
        //插入成员相关信息
        if (!CollectionUtils.isEmpty(channel.getRoles())) {
            for (int i = 0; i < channel.getRoles().size(); i++) {
                Role role = channel.getRoles().get(i);
                if (role.getRoleFlag() != 1) {
                    //插入不是预定义的通道角色列表
                    memberLedgerMgr.insertCustomRole(role);
                }
            }
        }
        if (!CollectionUtils.isEmpty(channel.getMembers())) {
            for (int i = 0; i < channel.getMembers().size(); i++) {
                Member member = channel.getMembers().get(i);
                memberLedgerMgr.insertMember(member);
            }
        }
    }

    @Override
    public void insertSmartContract(SmartContract sc) {
        SmartContractModelWithBLOBs smartContractModelWithBLOBs = SmartContractModelWithBLOBs.createInstance(sc);
        String key = sc.getChannelId() + sc.getName() + sc.getVersion();
        if (null != smartContractModelMapper.selectByPrimaryKey(smartContractModelWithBLOBs)) {
            int updateRow = smartContractModelMapper.updateByPrimaryKeySelective(smartContractModelWithBLOBs);
            if (updateRow > 0) {
                smartContractConcurrentHashMap.remove(key, sc);
                //smartContractConcurrentHashMap.put(key, sc);
                //smartContractsConcurrentHashMap.remove(sc.getChannelId());
            }
        } else {
            smartContractModelWithBLOBs.setScClassHash(dataSecurityMgr.getHash(smartContractModelWithBLOBs.getScClassFile()));
            int insertRow = smartContractModelMapper.insert(smartContractModelWithBLOBs);
            if (insertRow > 0) {
                //smartContractConcurrentHashMap.put(key, sc);
                //smartContractsConcurrentHashMap.remove(sc.getChannelId());
            }
        }
    }


    /**
     * @param peer
     * @param localPeer
     * @param channel
     */
    private void insertPeer(Peer peer, Peer localPeer, Channel channel) {
        PeerModel peerModelForm = peerModelMapper.selectByPrimaryKey(peer.getPeerId().getValue());
        if (peerModelForm == null) {
            PeerModel peerModel = PeerModel.createInstance(peer);
            if (!peer.equals(localPeer)) {
                peerModel.setCertificateKeyStoreFile("");
                peerModel.setIsLocalPeer(0);
            }
            peerModelMapper.insert(peerModel);
        }
        //处理组织证书
        insertOrganizationMember(peer.getPeerOrganization(), channel.getChannelId());
    }

    /*private void insertSmartContractList(List<SmartContract> scList) {
        int updateTotal = 0;
        int insertTotal = 0;
        for (SmartContract sc : scList) {
            //插入之前先判断是否存在，
            SmartContractModelWithBLOBs smartContractModelWithBLOBs = SmartContractModelWithBLOBs.createInstance(sc);
            String key = sc.getChannelId() + sc.getName() + sc.getVersion();
            if (null != smartContractModelMapper.selectByPrimaryKey(smartContractModelWithBLOBs)) {
                int updateRow = smartContractModelMapper.updateByPrimaryKeySelective(smartContractModelWithBLOBs);
                if (updateRow > 0) {
                    updateTotal++;
                    smartContractConcurrentHashMap.remove(key, sc);
                    //smartContractsConcurrentHashMap.remove(sc.getChannelId());
                }
            } else {
                smartContractModelWithBLOBs.setScClassHash(dataSecurityMgr.getHash(smartContractModelWithBLOBs.getScClassFile()));
                int insertRow = smartContractModelMapper.insert(smartContractModelWithBLOBs);
                //for debug
                log.info(ModuleClassification.LedM_LMI_ + "insert smartcontract");
                if (insertRow > 0) {
                    insertTotal++;
                    //smartContractConcurrentHashMap.put(key, sc);
                    //smartContractsConcurrentHashMap.remove(sc.getChannelId());
                }
            }
        }
        log.info(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",insertSmartContractList 新增的智能合约:" + insertTotal + "条,修改的智能合约：" + updateTotal + "条");

    }*/


    @Override
    public void updateChannel(Channel newChannel) {
        if (LedgerThreadLocalContext.stateAccessMode.get().equals(StateAccessModeEnum.TRANS_CREATE_FOR_FIXQC_VERIFY)
                || LedgerThreadLocalContext.stateAccessMode.get().equals(StateAccessModeEnum.TRANS_CREATE_FOR_STEPQC_VERIFY)) {
            updateChannelForTxExecute(newChannel);
            return;
        }
        throw new NewspiralException(NewSpiralErrorEnum.INVALID_STATE_ACCESS_MODE, "updateChannel只允许设置为PERSIST_STATE或TRANS_CREATE_FOR_BLOCK_CREATE");
    }

    /**
     * 将通道变更记录放置到 快照里
     *
     * @param newChannel 新的通道
     */
    protected void updateChannelForTxExecute(Channel newChannel) {
        log.info("update Channel for execute,changes:{}", newChannel.getChannelChange());
        Channel cloneChannel = CloneUtils.clone(newChannel, Channel.class);
        ChannelChange changes = newChannel.getChannelChange();
        //由于clone后,object类型的actionData会保留jsonObject类型，故给channelChange重新附个值
        cloneChannel.setChannelChange(changes);
        ChannelModifyRecord modifyRecord = new ChannelModifyRecord();
        modifyRecord.setNewChannel(cloneChannel);
        modifyRecord.setOldChannel(channelModelConcurrentHashMap.get(newChannel.getChannelId()));
        LedgerThreadLocalContext.blockChangesSnapshots.get().get(0).addChangesToChannel(changes, cloneChannel);
        LedgerThreadLocalContext.getCurrTransModifiedChannelsQueue().get().add(modifyRecord);
    }

    /**
     * 更新入参至数据库
     */
    /*@Transactional
    protected void updateChannelForPersist(String blockHash) {
        BlockChangesSnapshots snapshoot = LedgerThreadLocalContext.blockChangesMap.getNeedSnapShootByBlockHash(blockHash);
        if (snapshoot == null) {
            log.info(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "持久化Channel错误,worldStateSnapShot is null");
        }
        ConcurrentLinkedQueue<Pair<PersistConstant.PersistTarget, Channel>> channelChanges = snapshoot.getChannelChanges();
        while (!channelChanges.isEmpty()) {
            Pair<PersistConstant.PersistTarget, Channel> pair = channelChanges.poll();
            PersistConstant.PersistTarget key = pair.getKey();
            Channel channel = pair.getValue();
            if (key.equals(peerRemove)) {
                removeDeletedPeerOfChannel(channel, (Peer) channel.getChannelChange().getActionData());
            }
            persistActionHandler.doPersist(key, channel);
            log.info(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "标签动作:" + key + ",开始持久化Channel:channelId=" + channel.getChannelId());
        }
    }*/

    /**
     * 更新通道信息，将删除的节点的通道禁用了
     *
     * @param newChannel  通道
     * @param removedPeer 删除的节点
     */
    public void removeDeletedPeerOfChannel(Channel newChannel, Peer removedPeer) {
        ChannelModel channelModel = ChannelModel.createInstance(newChannel);
        log.info(ModuleClassification.LedM_LMI_ + "," + newChannel.getChannelId() + ",succeed to set available");
        Peer localPeer = this.queryLocalPeer();

        if (removedPeer.equals(localPeer)) {
            channelModel.setAvailable(0);
            StateHistory.getDeleteChannelIdSet().add(newChannel.getChannelId());
            log.info(ModuleClassification.LedM_LMI_ + "," + newChannel.getChannelId() + ",set available = 0");
        }
        channelModelMapper.updateByPrimaryKey(channelModel);
    }

    /**
     * @param peer
     * @param newChannel
     */
    public void insertPeerChannelCertificate(Peer peer, Channel newChannel, Peer localPeer, Long lastHeight) {
        PeerCertificateModel peerCertificateModel = new PeerCertificateModel();

        peerCertificateModel.setChannelId(newChannel.getChannelId());
        peerCertificateModel.setPeerId(peer.getPeerId().getValue());
        peerCertificateModel.setFlag("0");//正常状态
        try {
            peerCertificateModel.setCertificateCerFile((new String(peer.getCertificateCerFile(), "UTF-8")));
        } catch (Exception e) {
            log.warn(ModuleClassification.LedM_LMI_ + "," + newChannel.getChannelId() + "TError", e);
        }
        peerCertificateModel.setCertificateHash(dataSecurityMgr.getHash(peer.getCertificateCerFile()));
        peerCertificateModel.setCreateTime(new Date());
        peerCertificateModel.setBlockHeight(lastHeight);

        if (!peer.equals(localPeer)) {
            peerCertificateModel.setIsLocalPeer(0);
            peerCertificateModel.setCertificateKeyStoreFile("");
        } else {
            peerCertificateModel.setIsLocalPeer(1);
            try {
                peerCertificateModel.setCertificateKeyStoreFile((new String(localPeer.getCertificateKeyStoreFile(), "UTF-8")));
            } catch (Exception e) {
                log.warn(ModuleClassification.LedM_LMI_ + "TError" + "," + newChannel.getChannelId(), e);
            }
        }
        peerCertificateModelMapper.deleteBycertificateHash(peerCertificateModel);
        log.info("insertPeerChannelCertificate peerCertificateModel={}", JSONObject.toJSON(peerCertificateModel));
        int nRow = peerCertificateModelMapper.insert(peerCertificateModel);
        if (nRow > 0) {
            log.info("insertPeerChannelCertificate peerId={}", peerCertificateModel.getPeerId());
        }
        peerConcurrentHashMap.put(peer.getPeerId().getValue(), peer);
    }

    /**
     * @param peerOrganization
     */
    public void insertOrganizationMember(PeerOrganization peerOrganization, String channelId) {
        MemberModel memberModel = MemberModel.createOrganizationInstance(peerOrganization);
        MemberModel form = memberMapper.selectOrganziationMember("", peerOrganization.getOrganizationId());
        if (null == form) {
            //插入组织根证书
            memberMapper.insert(memberModel);
        }

        //以通道的身份添加一条相同的组织成员
        if (null == memberMapper.selectByPublicKey(channelId, memberModel.getPublicKey())) {
            memberModel.setId(null);
            memberModel.setChannelId(channelId);
            // xxm解析证书的其他信息
            Member member = new Member();
            member.setCertificateCerFile(memberModel.getCertificateCerFile());
            if (processMemberCertificate(member)) {
                memberModel.setSignAlgorithm(member.getSignAlgorithm());
                memberModel.setName(member.getName());
                memberModel.setIssuerId(member.getIssuerId());
                memberModel.setPublicKey(member.getPublicKey());
            }
            memberMapper.insert(memberModel);
            //组织角色
            MemberRoleModel orgMemberRoleModel = MemberRoleModel.createInstance(memberModel.getId(), "DEE826D175964131B7362E2DD99BB058", memberModel.getChannelId());
            memberRoleMapper.insert(orgMemberRoleModel);
            //应用角色
            MemberRoleModel applicationMemberRoleModel = MemberRoleModel.createInstance(memberModel.getId(), "D956376A79E4425B9DDE1D18B142D2C2", memberModel.getChannelId());
            memberRoleMapper.insert(applicationMemberRoleModel);
            //运维监控角色
            MemberRoleModel maintenanceMemberRoleModel = MemberRoleModel.createInstance(memberModel.getId(), "EE2DE129F32B4A5E8E94321E457AA79A", memberModel.getChannelId());
            memberRoleMapper.insert(maintenanceMemberRoleModel);
        }
    }

    @Override
    public List<Identity> queryAllIdentities() {
        List<IdentityModel> modelList = identityModelMapper.selectAll();
        List<Identity> identityList = new ArrayList<>();
        for (IdentityModel model : modelList) {
            identityList.add(model.toIdentity());
        }
        log.info(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",queryAllIdentities返回了" + modelList.size() + "个身份");
        return identityList;
    }

    /**
     * 查询peer表中所有节点的信息
     * @return
     */
    @Override
    public List<Peer> queryAllPeers() {
        List<Peer> peerList = new ArrayList<>();
        //查询peer表中的全部节点信息
        List<PeerModel> peerModelList = peerModelMapper.selectAll();
        for (PeerModel peerModel : peerModelList) {
            Peer peer = peerModel.toPeer();
            //peer.setPeerCert(queryPeerCertificateList(peer.getPeerId().getValue()));
            //xxm 测试，去掉证书
            //peer.setCertificateCerFile(null);
            //peer.setCertificateKeyStoreFile(null);
            peer.setPeerOrganization(queryPeerOrganization(peer.getOrgId().getValue()));
            peerList.add(peer);
        }
        return peerList;
    }


    /**
     * @param peerId
     * @return
     */
    public List<PeerCert> queryPeerCertificateList(String peerId, String channelId) {
        List<PeerCertificateModel> peerCertificateModels = peerCertificateModelMapper.listByPeerId(peerId, channelId);
        List<PeerCert> peerCert = new ArrayList<>();
        for (PeerCertificateModel peerCertificateModel : peerCertificateModels) {
            peerCert.add(peerCertificateModel.toPeerCert());
        }
        return peerCert;
    }

    /**
     * 查询所有本地存储的节点数据
     *
     * @return
     */
    @Override
    public Peer queryLocalPeer() {

        List<Peer> peerList = queryAllPeers();
        for (Peer peer : peerList) {
            if (peer.getIsLocalPeer()) {
                return peer;
            }
        }
        return null;
    }

    @Override
    public void insertTask(Task persistTask) {
        log.info(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",insertTask.star.Task:TaskId=" + persistTask.getTaskId());
        PersistTaskModel persistTaskModel = PersistTaskModel.createInstance(persistTask);
        int nRow = persistTaskModelMapper.insert(persistTaskModel);
        log.info(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",insertTask.end.affected row=" + nRow);
    }

    @Override
    public int updateTask(Task task) {
        log.info(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",updateTask.star.Task:TaskId=" + task.getTaskId());
        PersistTaskModel persistTaskModel = PersistTaskModel.createInstance(task);
        int nRow = persistTaskModelMapper.updateByPrimaryKey(persistTaskModel);
        log.info(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",updateTask.end.affected row=" + nRow);
        return nRow;
    }

    @Override
    public List<Task> getTaskListByStatus() {
        List<Task> tasks = new ArrayList<>();
        List<PersistTaskModel> persistTaskModels = persistTaskModelMapper.selectAllByStatus();
        for (PersistTaskModel persistTaskModel : persistTaskModels) {
            Task task = persistTaskModel.toTask();
            tasks.add(task);
        }
        return tasks;
    }

    @Override
    public Identity queryLocalIdentity() {
        List<PeerModel> localPeerList = peerModelMapper.selectLocalPeer();
        if (localPeerList == null || localPeerList.size() != 1) {
            //目前本地只能配置一个节点
            log.warn(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",存储中没有配置本地节点或者配置有多个本地节点");
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "存储中没有配置本地节点或者配置有多个本地节点");
        } else {
            Peer peer = localPeerList.get(0).toPeer();
            log.info("identityId={}", JSON.toJSONString(peer.getPeerId()));
            IdentityModel identityModel = identityModelMapper.selectByPrimaryKey(JSON.toJSONString(transforIdentityKeyBrief(peer.getPeerId())));
            if (identityModel == null) {
                log.warn(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",存储中IDENTITY表中没有与本地节点对应的身份记录");
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "存储中IDENTITY表中没有与本地节点对应的身份记录");
            } else {
                return identityModel.toIdentity();
            }
        }
    }

    @Override
    public Identity getIdentityByPrimaryKey(IdentityKey identityKey) {

        String key = JSON.toJSONString(transforIdentityKeyBrief(identityKey));
        Identity identity = identityConcurrentHashMap.get(key);
        if (null != identity) return identity;
        IdentityModel identityModel = identityModelMapper.selectByPrimaryKey(key);
        if (identityModel == null) {
            log.error("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",存储中IDENTITY表中没有记录节点对应的身份记录");
            return null;
        } else {
            identity = identityModel.toIdentity();
            identityConcurrentHashMap.put(key, identity);
            return identity;
        }
    }


    public void putIntoIdentityMap(String key, Identity identity) {
        identityConcurrentHashMap.put(key, identity);
    }

    public void removePeersFromMap(String key) {
        peersConcurrentHashMap.remove(key);
    }

    public void removeContractFromMap(String key, SmartContract sc) {
        smartContractConcurrentHashMap.remove(key, sc);
    }

    @Override
    public Peer getPeerByPrimaryKey(String peerIdValue, String channelId) {
        //Peer peer = peerConcurrentHashMap.get(peerIdValue);
        //if (null != peer) return peer;
        PeerModel peerModel = peerModelMapper.selectByPrimaryKey(peerIdValue);
        if (peerModel == null) {
            log.error("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channelId + ",存储中PEER表中没有记录节点对应的记录");
            return null;
        } else {
            Peer peer = peerModel.toPeer();
            if (null != channelId) {
                peer.setPeerCert(queryPeerCertificateList(peer.getPeerId().getValue(), channelId));
            }
            //peerConcurrentHashMap.put(peerIdValue, peer);
            return peer;
        }
    }


    /**
     * 这个方法是给业务智能合约使用的，因此不能混入真正修改数据库的代码，否则就是留了一个后门，智能合约中如果修改了线程变量，则会出现越界访问
     *
     * @param key
     * @return
     */
    @Override
    public byte[] getState(String key) {

        StateAccessModeEnum stateAccessModeEnum = LedgerThreadLocalContext.stateAccessMode.get();
        if (!(StateAccessModeEnum.TRANS_CREATE_FOR_FIXQC_VERIFY.equals(stateAccessModeEnum)
                || StateAccessModeEnum.TRANS_CREATE_FOR_STEPQC_VERIFY.equals(stateAccessModeEnum))) {

            throw new NewspiralException(NewSpiralErrorEnum.INVALID_STATE_ACCESS_MODE, "putState只允许设置为TRANS_CREATE_FOR_BLOCK_CREATE，" +
                    "实际上是" + LedgerThreadLocalContext.stateAccessMode.get());
        } else {
            //addKey(LedgerThreadLocalContext.modifiedWorldStateKeyList, key);
            addKey(LedgerThreadLocalContext.getExecuteWorldStateKeyList(), key);
            if (StateAccessModeEnum.TRANS_CREATE_FOR_STEPQC_VERIFY.equals(stateAccessModeEnum)
                    || StateAccessModeEnum.TRANS_CREATE_FOR_FIXQC_VERIFY.equals(stateAccessModeEnum)) {
                // TODO：整个getState函数应该迁移到共识逻辑中，只留下存储逻辑
                WorldStateModifyRecord record = LedgerThreadLocalContext.getCurrTransModifiedStateMap().get().get(key);
                if (record != null) {
                    if (record.getNewState() != null) {
                        return record.getNewState().getValue();
                    }
                }
                List<BlockChangesSnapshots> blockChangesSnapshotsList = LedgerThreadLocalContext.blockChangesSnapshots.get();
                //log.info("getState from sslist {}", worldStateSnapshootList);
                for (BlockChangesSnapshots blockChangesSnapShots : blockChangesSnapshotsList) {
                    if (blockChangesSnapShots != null && blockChangesSnapShots.getWorldStateModifyRecordMap().containsKey(key)) {
                        return blockChangesSnapShots.getWorldStateModifyRecordMap().get(key).getNewState().getValue();
                    }
                }
            }
            return getKeyFromPersistWorldState(key, LedgerThreadLocalContext.currChannelId.get());
        }
    }

    private void addKey(ThreadLocal<Set<String>> set, String key) {
        set.get().add(key);
    }

    private void putKey(ThreadLocal<Map<String, WorldStateModifyRecord>> set, String key, WorldStateModifyRecord worldStateModifyRecord) {
        set.get().put(key, worldStateModifyRecord);
    }

    @Override
    public byte[] queryState(String key) {
        StateAccessModeEnum stateAccessModeEnum = LedgerThreadLocalContext.stateAccessMode.get();
        addKey(LedgerThreadLocalContext.getExecuteWorldStateKeyList(), key);
        if (StateAccessModeEnum.TRANS_CREATE_FOR_STEPQC_VERIFY.equals(stateAccessModeEnum)
                || StateAccessModeEnum.TRANS_CREATE_FOR_FIXQC_VERIFY.equals(stateAccessModeEnum)) {
            WorldStateModifyRecord record = LedgerThreadLocalContext.getCurrTransModifiedStateMap().get().get(key);
            if (record != null) {
                if (record.getNewState() != null) {
                    return record.getNewState().getValue();
                }
            }
            List<BlockChangesSnapshots> blockChangesSnapshotsList = LedgerThreadLocalContext.blockChangesSnapshots.get();
            for (BlockChangesSnapshots blockChangesSnapShots : blockChangesSnapshotsList) {
                if (blockChangesSnapShots != null && blockChangesSnapShots.getWorldStateModifyRecordMap().containsKey(key)) {
                    return blockChangesSnapShots.getWorldStateModifyRecordMap().get(key).getNewState().getValue();
                }
            }
        }

        return getKeyFromPersistWorldState(key, LedgerThreadLocalContext.currChannelId.get());
    }

    /**
     * 检查是否存在世界状态
     *
     * @param key
     * @return
     */
    private byte[] getKeyFromPersistWorldState(String key, String channelId) {
        StateModel stateModel = stateModelMapper.selectByPrimaryKey(channelId, key, false);
        if (stateModel == null) {
            return null;
        } else {
            return stateModel.getValue();
        }
    }

    @Override
    public void insertState(String key, byte[] value) {
        StateAccessModeEnum stateAccessModeEnum = LedgerThreadLocalContext.stateAccessMode.get();
        if (StateAccessModeEnum.TRANS_CREATE_FOR_FIXQC_VERIFY.equals(stateAccessModeEnum)
                || StateAccessModeEnum.TRANS_CREATE_FOR_STEPQC_VERIFY.equals(stateAccessModeEnum)) {

            //addKey(LedgerThreadLocalContext.modifiedWorldStateKeyList, key);
            addKey(LedgerThreadLocalContext.getExecuteWorldStateKeyList(), key);
            if (this.getState(key) != null) {
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "欲插入的key=" + key + "已经存在");
            } else {
                WorldStateModifyRecord stateModifyRecord = new WorldStateModifyRecord();
                stateModifyRecord.setOldState(null);
                stateModifyRecord.setNewState(WorldState.createInstance(key, value));
                // 加入读写集
                if (StateAccessModeEnum.TRANS_CREATE_FOR_STEPQC_VERIFY.equals(stateAccessModeEnum)) {
                    LedgerThreadLocalContext.blockChangesSnapshots.get().get(0).put(key, stateModifyRecord);
                }
                //putKey(LedgerThreadLocalContext.currTransModifiedStateMap, key, stateModifyRecord);
                putKey(LedgerThreadLocalContext.getCurrTransModifiedStateMap(), key, stateModifyRecord);
            }
        } else {
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_STATE_ACCESS_MODE, "putState只允许设置为TRANS_CREATE_FOR_BLOCK_CREATE，" +
                    "实际上是" + LedgerThreadLocalContext.stateAccessMode.get());
        }
    }

    @Override
    public void updateState(String key, byte[] newValue) {
        StateAccessModeEnum stateAccessModeEnum = LedgerThreadLocalContext.stateAccessMode.get();
        if (StateAccessModeEnum.TRANS_CREATE_FOR_FIXQC_VERIFY.equals(stateAccessModeEnum)
                || StateAccessModeEnum.TRANS_CREATE_FOR_STEPQC_VERIFY.equals(stateAccessModeEnum)) {
            addKey(LedgerThreadLocalContext.getExecuteWorldStateKeyList(), key);
            byte[] oldValue = getState(key);
            if (oldValue == null) {
                throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "欲更新的key=" + key + "不存在");
            } else {
                WorldStateModifyRecord stateModifyRecord = new WorldStateModifyRecord();
                stateModifyRecord.setOldState(WorldState.createInstance(key, oldValue));
                stateModifyRecord.setNewState(WorldState.createInstance(key, newValue));
                if (StateAccessModeEnum.TRANS_CREATE_FOR_STEPQC_VERIFY.equals(stateAccessModeEnum)) {
                    LedgerThreadLocalContext.blockChangesSnapshots.get().get(0).put(key, stateModifyRecord);
                }
                putKey(LedgerThreadLocalContext.getCurrTransModifiedStateMap(), key, stateModifyRecord);
            }

        } else {
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_STATE_ACCESS_MODE, "putState只允许设置为TRANS_CREATE_FOR_BLOCK_CREATE，" +
                    "实际上是" + LedgerThreadLocalContext.stateAccessMode.get());
        }
    }

    @Override
    public void putState(String key, byte[] value) {
        StateAccessModeEnum stateAccessModeEnum = LedgerThreadLocalContext.stateAccessMode.get();
        if (StateAccessModeEnum.TRANS_CREATE_FOR_FIXQC_VERIFY.equals(stateAccessModeEnum)
                || StateAccessModeEnum.TRANS_CREATE_FOR_STEPQC_VERIFY.equals(stateAccessModeEnum)) {//使用DAG并行执行的时候，这里需要改一下，否则可能会出现并发问题

            //addKey(LedgerThreadLocalContext.modifiedWorldStateKeyList, key);
            addKey(LedgerThreadLocalContext.getExecuteWorldStateKeyList(), key);
            if (existKey(key)) {
                this.updateState(key, value);
            } else {
                this.insertState(key, value);
            }
        } else {
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_STATE_ACCESS_MODE, "putState只允许设置为TRANS_CREATE_FOR_BLOCK_CREATE，" +
                    "实际上是" + stateAccessModeEnum);
        }
    }

    @Override
    public boolean existKey(String key) {
        // 加入读写集

        //addKey(LedgerThreadLocalContext.modifiedWorldStateKeyList, key);
        addKey(LedgerThreadLocalContext.getExecuteWorldStateKeyList(), key);
        return getState(key) != null;
    }

    @Override
    public boolean deleteKey(String key) {
        StateAccessModeEnum stateAccessModeEnum = LedgerThreadLocalContext.stateAccessMode.get();
        if (StateAccessModeEnum.TRANS_CREATE_FOR_FIXQC_VERIFY.equals(stateAccessModeEnum)
                || StateAccessModeEnum.TRANS_CREATE_FOR_STEPQC_VERIFY.equals(stateAccessModeEnum)) {
            //使用DAG并行执行的时候，这里需要改一下，否则可能会出现并发问题

            // 加入读写集
            addKey(LedgerThreadLocalContext.getExecuteWorldStateKeyList(), key);

            byte[] oldValue = getState(key);
            if (oldValue == null) {
                return false;
            } else {
                WorldStateModifyRecord record = new WorldStateModifyRecord();
                record.setOldState(WorldState.createInstance(key, oldValue));
                record.setNewState(null);
                if (StateAccessModeEnum.TRANS_CREATE_FOR_STEPQC_VERIFY.equals(stateAccessModeEnum)) {
                    LedgerThreadLocalContext.blockChangesSnapshots.get().get(0).put(key, record);
                }

                //putKey(LedgerThreadLocalContext.currTransModifiedStateMap, key, record);
                putKey(LedgerThreadLocalContext.getCurrTransModifiedStateMap(), key, record);
                return true;
            }
        } else {
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_STATE_ACCESS_MODE, "putState只允许设置为TRANS_CREATE_FOR_BLOCK_CREATE，" +
                    "实际上是" + LedgerThreadLocalContext.stateAccessMode.get());
        }
    }


    @Override
    public Map<String, Map<String, String>> queryPeerPrivateKeyMap() {
        Map<String, Map<String, String>> peerPrivateKeyMap = new HashMap<>();
        Map<String, String> channelPrivateKeyMap;
        List<PeerChannelModelWithBLOBs> peerChannelList = peerChannelModelMapper.selectAll();
        int total = peerChannelList.size();
        for (int i = 0; i < total; i++) {
            PeerChannelModelWithBLOBs peerChannelModelWithBLOBs = peerChannelList.get(i);
            channelPrivateKeyMap = new HashMap<>();
            for (int j = i; j < total; j++) {
                PeerChannelModelWithBLOBs peerChannelModelWithBLOBs1 = peerChannelList.get(j);
                if (peerChannelModelWithBLOBs.getPeerId().equals(peerChannelModelWithBLOBs1.getPeerId())) {
                    channelPrivateKeyMap.put(peerChannelModelWithBLOBs.getChannelId(), peerChannelModelWithBLOBs.getUserPrivateKey());
                }
            }
            if (null == peerPrivateKeyMap.get(peerChannelModelWithBLOBs.getPeerId())) {
                peerPrivateKeyMap.put(peerChannelModelWithBLOBs.getPeerId(), channelPrivateKeyMap);
            }
        }
        return peerPrivateKeyMap;
    }

    @Override
    public void updateChannelPeerPrivateKey(Channel channel, List<Peer> peers) {
        for (Peer peer : peers) {
            PeerChannelModelWithBLOBs peerChannelModel = new PeerChannelModelWithBLOBs();
            peerChannelModel.setUserPrivateKey(peer.getPeerChannelRelation().getUserPrivateKey());
            peerChannelModel.setChannelId(channel.getChannelId());
            peerChannelModel.setPeerId(peer.getPeerId().getValue());
            peerChannelModelMapper.updateUserPrivateKetByConditionKey(peerChannelModel);
        }
    }

    @Override
    public void insertGenericMsg(String channelId, GenericMsg genericMsg) {
        MessageModel messageModel = new MessageModel();
        messageModel.setViewNumber(genericMsg.getViewNo());
        messageModel.setContent(JSON.toJSONString(genericMsg, new SerializerFeature[]{SerializerFeature.DisableCircularReferenceDetect}));
        messageModel.setChannelId(channelId);
        messageModelMapper.insert(messageModel);
    }

    @Override
    public void deleteGenericMsg(String channelId, Long viewNo) {
        messageModelMapper.deleteByViewNo(channelId, viewNo);
    }

    @Override
    public Map<Long, GenericMsg> selectGenericMsg(String channelId, Long viewNo) {
        List<MessageModel> messageModels = messageModelMapper.selectByViewNo(channelId, viewNo);
        Map<Long, GenericMsg> genericMsgs = new HashMap<>();
        for (MessageModel model :
                messageModels) {
            GenericMsg genericMsg = model.toGenericMsg();
            genericMsgs.put(genericMsg.getViewNo(), genericMsg);
        }
        return genericMsgs;
    }

    @Override
    public Map<String, PeerCertificateCipher> queryPeerCertificateMap() {
        Map<String, PeerCertificateCipher> peerCertificateCipherMap = new HashMap<>();
        List<PeerModel> peerModels = peerModelMapper.selectAll();
        for (PeerModel peerModel : peerModels) {
            PeerCertificateCipher peerCertificateCipher = new PeerCertificateCipher();
            peerCertificateCipher.setPeerId(peerModel.getPeerIdValue());
            peerCertificateCipher.setCertificateCerFile(peerModel.getCertificateCerFile().getBytes());
            //log.info(ModuleClassification.LedM_LMI+"encrypt, peer={},storeKey={},CerFile={}",peerModel.getPeerIdValue(),peerModel.getCertificateKeyStoreFile(),peerModel.getCertificateCerFile());
            if (StringUtils.isEmpty(peerModel.getCertificateKeyStoreFile())) {
                peerCertificateCipher.setCertificateKeyStoreFile(null);
            } else {
                peerCertificateCipher.setCertificateKeyStoreFile(peerModel.getCertificateKeyStoreFile().getBytes());
            }

            peerCertificateCipher.setCertificateAlias(peerModel.getCertificateAlias());
            peerCertificateCipher.setCertificateStorePass(peerModel.getCertificateStorePass());
            peerCertificateCipherMap.put(peerModel.getPeerIdValue(), peerCertificateCipher);
        }
        return peerCertificateCipherMap;
    }

    @Override
    public PeerCertificateCipher queryPeerCertificateByPeerId(String peerId) {
        PeerModel peerModel = peerModelMapper.selectByPrimaryKey(peerId);
        if (null == peerModel) return null;
        PeerCertificateCipher peerCertificateCipher = new PeerCertificateCipher();
        peerCertificateCipher.setPeerId(peerModel.getPeerIdValue());
        peerCertificateCipher.setCertificateCerFile(peerModel.getCertificateCerFile().getBytes());
        //log.info(ModuleClassification.LedM_LMI+"encrypt, peer={},storeKey={},CerFile={}",peerModel.getPeerIdValue(),peerModel.getCertificateKeyStoreFile(),peerModel.getCertificateCerFile());
        if (StringUtils.isEmpty(peerModel.getCertificateKeyStoreFile())) {
            peerCertificateCipher.setCertificateKeyStoreFile(null);
        } else {
            peerCertificateCipher.setCertificateKeyStoreFile(peerModel.getCertificateKeyStoreFile().getBytes());
        }
        peerCertificateCipher.setCertificateAlias(peerModel.getCertificateAlias());
        peerCertificateCipher.setCertificateStorePass(peerModel.getCertificateStorePass());
        return peerCertificateCipher;
    }

    @Override
    public Map<String, Map<String, PeerCertificateCipher>> querypeerChannelCertificateCipherMap() {
        Map<String, Map<String, PeerCertificateCipher>> peerChannelCertificateCipherMap = new ConcurrentHashMap<>();
        List<ChannelModel> listChannelRecord = channelModelMapper.selectAll();
        Map<String, List<Peer>> channelPeerMap = queryChannelPeerMap();
        for (ChannelModel ch : listChannelRecord) {
            Map<String, PeerCertificateCipher> peerCertificateCipherMap = new HashMap<>();
            List<Peer> peers = channelPeerMap.get(ch.getChannelId());
            if (CollectionUtils.isEmpty(peers)) continue;
            for (Peer peer : peers) {
                PeerCert peerCert = peer.getPeerCert().get(0);
                PeerCertificateCipher peerCertificateCipher = new PeerCertificateCipher();
                peerCertificateCipher.setPeerId(peerCert.getPeerId());
                peerCertificateCipher.setCertificateCerFile(peerCert.getCertificateCerFile());
                peerCertificateCipher.setCertificateKeyStoreFile(peerCert.getCertificateKeyStoreFile());
                peerCertificateCipherMap.put(peerCert.getPeerId(), peerCertificateCipher);
            }

            if (peerCertificateCipherMap.size() > 0) {
                peerChannelCertificateCipherMap.put(ch.getChannelId(), peerCertificateCipherMap);
            }
        }
        return peerChannelCertificateCipherMap;
    }

    @Override
    @Transactional
    public void updatePeerCertificateByPrimaryKey(Peer peer, String channelId) {
        // TODO 新增删除证书逻辑哈
        //PeerModel form = peerModelMapper.selectByPrimaryKey(peer.getPeerId().getValue());
        PeerCertificateModel peerCertificateModel = new PeerCertificateModel();
        peerCertificateModel.setChannelId(channelId);
        peerCertificateModel.setPeerId(peer.getPeerId().getValue());
        peerCertificateModel.setFlag("0");//正常状态

        try {

            peerCertificateModel.setCertificateCerFile(new String(peer.getCertificateCerFile(), "UTF-8"));
        } catch (Exception e) {
            log.warn(ModuleClassification.LedM_LMI_ + "," + channelId + "TError", e);
        }
        peerCertificateModel.setCertificateHash(dataSecurityMgr.getHash(peer.getCertificateCerFile()));
        peerCertificateModel.setCreateTime(new Date());
        peerCertificateModel.setUpdateTime(new Date());

        PeerModel peerModel = PeerModel.createInstance(peer);
        if (peerModel == null || StringUtils.isEmpty(peerModel.getPeerIdValue())
                || StringUtils.isEmpty(channelId)) {
            log.error("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channelId + ",updatePeerCertificateByPrimaryKey 参数错误");
            return;
        }
        boolean locationFlag = false;
        //TODO 判断一下是不是location节点 不是的话干掉私钥文件
        List<PeerModel> locals = peerModelMapper.selectLocalPeer();
        if (!locals.get(0).getPeerIdValue().equals(peerModel.getPeerIdValue())) {
            locationFlag = true;

        }
        if (!locationFlag) {
            peerModel.setCertificateKeyStoreFile(null);
        }
        int nRow = peerModelMapper.updatePeerCertificateByPrimaryKey(peerModel);
        if (nRow > 0) {
            ChannelModel channelModel = channelModelMapper.selectByPrimaryKey(channelId);
            SecurityService securityService = securityServiceMgr.getMatchSecurityService(channelModel.getSecurityServiceKey());
            securityService.clearPeerCertificateCipherMap(peerModel.getPeerIdValue(), channelId);
        }
        log.info(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channelId + ",存储中PEER表修改的记录{}条,", nRow);

        // 原正常证书如果存在则吊销，新证书插入
        int updateRow = peerCertificateModelMapper.revokePeerCertificate(peerCertificateModel);
        peerCertificateModel.setUpdateTime(null);
        if (null == peerCertificateModel.getBlockHeight()) {
            peerCertificateModel.setBlockHeight(0L);
        }
        if (!locationFlag) {
            peerCertificateModel.setCertificateKeyStoreFile("");
            peerCertificateModel.setIsLocalPeer(0);
        } else {
            peerCertificateModel.setIsLocalPeer(1);
            try {

                peerCertificateModel.setCertificateKeyStoreFile(new String(peer.getCertificateKeyStoreFile(), "UTF-8"));
            } catch (Exception e) {
                log.warn(ModuleClassification.LedM_LMI_ + "," + channelId + "TError", e);
            }
        }
        int insertRow = peerCertificateModelMapper.insert(peerCertificateModel);
        log.info(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channelId + ",存储中PEER表修改的记录{}条,peer_certificate表修改{}条新增{}条", nRow, updateRow, insertRow);
        peerConcurrentHashMap.remove(peerModel.getPeerIdValue());
        peersConcurrentHashMap.remove(channelId);
        // TODO 修改缓存
        consensusMsgProcessor.processPeerCertificateByPeerId(queryPeerCertificateList(peerModel.getPeerIdValue(), channelId), channelId);
    }


    @Override
    public List<Role> getRolesOfMember(Long memberId) {
        List<RoleModel> roleModelList = roleMapper.selectByMemberId(memberId);
        List<Role> roleList = new ArrayList<>();
        for (RoleModel model :
                roleModelList) {
            roleList.add(model.toRole());
        }
        return roleList;
    }

    @Override
    public List<Member> getMembersOfRole(String channelId, String roleName) {
        List<MemberModel> memberModelList = memberMapper.selectMembersOfRole(channelId, roleName);
        List<Member> memberList = new ArrayList<>();
        for (MemberModel m :
                memberModelList) {
            memberList.add(m.toMember());
        }
        return memberList;
    }

    @Override
    public SmartContract getSmartContractByKey(SmartContractModelKeyReq smartContractModelKeyReq) {
        if (StringUtils.isEmpty(smartContractModelKeyReq.getScChannelId())
                || StringUtils.isEmpty(smartContractModelKeyReq.getScVersion())
                || (StringUtils.isEmpty(smartContractModelKeyReq.getScName())
                && StringUtils.isEmpty(smartContractModelKeyReq.getAlisa()))) {
            return null;
        }
        SmartContractModelKey smartContractModelKey = new SmartContractModelKey();
        BeanUtils.copyProperties(smartContractModelKeyReq, smartContractModelKey);
        //先从缓存里面取
/*        SmartContract smartContract = smartContractConcurrentHashMap.get(scChannelId + scName + scVersion);
        if (null != smartContract) return smartContract;*/
        SmartContractModelWithBLOBs smartContractModelWithBLOBs = smartContractModelMapper.selectByPrimaryKey(smartContractModelKey);
        if (null == smartContractModelWithBLOBs) return null;
        SmartContract form = smartContractModelWithBLOBs.toSmartContract();
        //smartContractConcurrentHashMap.put(scChannelId + scName + scVersion,form);
        return form;
    }


    @Override
    public PeerCertificate getPeerCertificate(IdentityKey identityKey, String channelId) {
        if (null == identityKey
                || StringUtils.isEmpty(identityKey.getValue())) {
            return null;
        }
        PeerModel peerModel = peerModelMapper.selectByPrimaryKey(identityKey.getValue());
        if (null == peerModel) {
            return null;
        }
        List<PeerCertificateModel> peerCertificateModels = peerCertificateModelMapper.listByPeerId(identityKey.getValue(), channelId);
        List<PeerCert> peerCerts = new ArrayList<>();
        for (PeerCertificateModel peerCertificateModel : peerCertificateModels) {
            peerCerts.add(peerCertificateModel.toPeerCert());
        }
        PeerCertificate peerCertificate = new PeerCertificate();
        Peer peer = peerModel.toPeer();
        peer.setCertificateHash(dataSecurityMgr.getHash(peer.getCertificateCerFile()));
        peerCertificate.setPeer(peer);
        peerCertificate.setPeerCert(peerCerts);
        return peerCertificate;
    }

    public PeerCert getPeerCertificate(String peerId, String channelId) {
        PeerCertificateModel peerCertificateModel = peerCertificateModelMapper.selectByPeerIdAndChannelId(peerId,channelId);
        PeerCert peerCert = null;
        if (peerCertificateModel!=null) {
            peerCert = peerCertificateModel.toPeerCert();
        }
        return peerCert;
    }


    @Override
    public void updatePeerCertificateState(PeerCertificate peerCertificate, String flag, String channelId) {
        PeerCertificateModel peerCertificateModel = new PeerCertificateModel();
        peerCertificateModel.setChannelId(channelId);
        peerCertificateModel.setPeerId(peerCertificate.getPeer().getPeerId().getValue());
        peerCertificateModel.setFlag(flag);
        try {
            peerCertificateModel.setCertificateCerFile(new String(peerCertificate.getPeer().getCertificateCerFile(), "UTF-8"));
        } catch (Exception e) {
            log.warn(ModuleClassification.LedM_LMI_ + "," + channelId + "TError", e);
        }

        peerCertificateModel.setCertificateHash(peerCertificate.getPeer().getCertificateHash());
        peerCertificateModel.setCreateTime(new Date());
        peerCertificateModel.setUpdateTime(new Date());
        //修改一下状态
        int nRow = peerCertificateModelMapper.updatePeerCertificateFlagByPeerHash(peerCertificateModel);
        log.info(ModuleClassification.LedM_LMI_ + "," + channelId + "peerCertificateModel={}", JSONObject.toJSON(peerCertificateModel));
        log.info(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channelId + ",存储中peer_certificate表修改的记录{}条,", nRow);

        if (nRow > 0) {
            peerConcurrentHashMap.remove(peerCertificate.getPeer().getPeerId().getValue());
            peersConcurrentHashMap.remove(channelId);
        }
        // TODO 修改缓存
        consensusMsgProcessor.processPeerCertificateByPeerId(queryPeerCertificateList(peerCertificate.getPeer().getPeerId().getValue(), channelId), channelId);
    }

    @Override
    public boolean verifyPeerCertificateRepeat(byte[] certificateCerFile, String channelId) {
        String peerCertificateHash = dataSecurityMgr.getHash(certificateCerFile);
        List<Peer> peers = queryPeersOfChannel(channelId);
        for (Peer form : peers) {
            for (PeerCert peerCert : form.getPeerCert()) {
                if (peerCert.getCertificateHash().equals(peerCertificateHash)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<PeerCert> getPeerCertList(String peerId, String channelId) {
        List<PeerCert> peerCerts = new ArrayList<>();
        List<PeerCertificateModel> peerCertificateModels = peerCertificateModelMapper.listByPeerId(peerId, channelId);
        for (PeerCertificateModel peerCertificateModel : peerCertificateModels) {
            PeerCert peerCert = peerCertificateModel.toPeerCert();
            peerCert.setCertificateCerFile(peerCertificateModel.getCertificateCerFile().getBytes(StandardCharsets.UTF_8));
            peerCerts.add(peerCert);
        }

        return peerCerts;
    }

    @Override
    public void updateChannelCache(Channel channel) {
        channelModelConcurrentHashMap.put(channel.getChannelId(), channel);
    }


    @Override
    public String getHash(Object o) {
        return dataSecurityMgr.getHash(o);
    }

    @Override
    public int getTransactionCount(String channelId) {
        return transactionModelMapper.countTx(channelId);
    }

    @Override
    public int getTransactionCountByHeight(String channelId, Long height) {
        BlockModel blockModel = blockModelMapper.selectByHeight(channelId, height);
        if (null == blockModel) return 0;
        return transactionModelMapper.countTxByHash(channelId, blockModel.getHash());
    }


    @Override
    public List<WorldState> selectAllState(String channelId) {
        List<StateModel> stateModels = stateModelMapper.selectAll(channelId);
        List<WorldState> worldStates = new ArrayList<>();
        for (StateModel stateModel : stateModels) {
            worldStates.add(stateModel.toWorldState());
        }
        return worldStates;
    }

    //@Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteChannelData(String channelId) {
        channelModelMapper.deleteByPrimaryKey(channelId);
        blockModelMapper.deleteCachedBlockByChannelId(channelId);
        blockModelMapper.deleteCommittedBlockByChannelId(channelId);
        transactionModelMapper.deleteCacheTransactionByChannelId(channelId);
        transactionModelMapper.deleteTransactionByChannelId(channelId);
        stateModelMapper.deleteByChannelId(channelId);
        smartContractModelMapper.deleteByChannelId(channelId);
        peerChannelModelMapper.deleteByChannelId(channelId);
        peerCertificateModelMapper.deleteByChannelId(channelId);
        memberRoleMapper.deleteByChannelId(channelId);
        memberMapper.deleteByChannelId(channelId);

        channelModelConcurrentHashMap.remove(channelId);
        peersConcurrentHashMap.remove(channelId);
        memberLedgerMgr.clearChannelCache(channelId);
    }

    @Override
    public void deleteChannelBasicData(String channelId){
        channelModelMapper.deleteByPrimaryKey(channelId);
        blockModelMapper.deleteCachedBlockByChannelId(channelId);
        transactionModelMapper.deleteCacheTransactionByChannelId(channelId);
        smartContractModelMapper.deleteByChannelId(channelId);
        memberRoleMapper.deleteByChannelId(channelId);
        memberMapper.deleteByChannelId(channelId);
    }

    //@Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteBusinessDataByChannelId(String channelId) {
        blockModelMapper.deleteCachedBlockByChannelId(channelId);
        blockModelMapper.deleteCommittedBlockByChannelId(channelId);
        transactionModelMapper.deleteCacheTransactionByChannelId(channelId);
        transactionModelMapper.deleteTransactionByChannelId(channelId);
        stateModelMapper.deleteByChannelId(channelId);

    }

    @Override
    public void deleteBusinessDataByBlockHeight(String channelId, Long height) {
        BlockModel blockModel = blockModelMapper.selectByHeight(channelId, height);
        String blockHash = blockModel.getHash();
        transactionModelMapper.deleteCacheTransactionByBlockHeight(channelId, blockHash);
        transactionModelMapper.deleteTransactionByBlockHeight(channelId, blockHash);
        blockModelMapper.deleteCachedBlockByBlockHeight(channelId, height);
        blockModelMapper.deleteCommittedBlockByBlockHeight(channelId, height);
        stateModelMapper.deleteByBlockHeight(channelId, height);
    }


    public void processInvalidCertificates(List<Peer> peers, List<Member> members) {
        List<Channel> channelList = readAllChannels();
        for (Channel channel : channelList) {
            for (Peer peer : channel.getMemberPeerList()) {
                if (!peers.contains(peer)) continue;
                PeerCertificateModel peerCertificateModel = new PeerCertificateModel();
                peerCertificateModel.setUpdateTime(new Date());
                peerCertificateModel.setChannelId(channel.getChannelId());
                peerCertificateModel.setBlockHeight(channel.getLatestChannelChangeHeight());
                peerCertificateModel.setPeerId(peer.getPeerId().getValue());
                peerCertificateModelMapper.revokePeerCertificate(peerCertificateModel);
                peerConcurrentHashMap.remove(peer.getPeerId().getValue());
                peersConcurrentHashMap.remove(channel.getChannelId());
                consensusMsgProcessor.processPeerCertificateByPeerId(queryPeerCertificateList(peer.getPeerId().getValue(), channel.getChannelId()), channel.getChannelId());
            }
        }

        //TODO 吊销无效成员证书
        for (Member member : members) {
            member.setStatus(1);
            memberLedgerMgr.updateMember(member);
        }
    }

    @Override
    public boolean verifySign(VerifiableData verifiableData, String publicKeyStr) {
        return dataSecurityMgr.verifySignatureByGMCertificateKey(verifiableData, publicKeyStr);
    }

    @Override
    public boolean processMemberCertificate(Member member) {
        return dataSecurityMgr.processMemberCertificate(member);
    }


    @Override
    public BizVO getSmartContractAllList(QuerySmartContractListReq vo) {
        //取一下pageNo
        Integer pageNo = vo.getPageNo();
        //计算查询页码
        vo.setPageNo((vo.getPageNo() - 1) * vo.getPageSize());
        //查询总记录数 为0直接返回
        Integer totalSize = smartContractModelMapper.fetchAllCount(vo);

        if (null == totalSize || 0 == totalSize) {
            return null;
        }
        //查询列表
        List<SmartContract> list = new ArrayList<>();
        List<SmartContractModelWithBLOBs> smartContractModels = smartContractModelMapper.getAllList(vo);
        for (SmartContractModelWithBLOBs smartContractModelWithBLOBs : smartContractModels) {
            SmartContract smartContract = smartContractModelWithBLOBs.toSmartContract();
            list.add(smartContract);
        }
        return transferBizVO(list, totalSize, vo, pageNo);
    }

    private BizVO<SmartContract> transferBizVO(List<SmartContract> list, int totalSize, QuerySmartContractListReq request, Integer pageNo) {
        BizVO<SmartContract> bizVO = new BizVO<>();
        if (list.size() > 0) {
            bizVO.setList(list);
            bizVO.setRecordCount(totalSize);
            bizVO.setPageCount(totalSize / request.getPageSize() + 1);
            bizVO.setPageNo(pageNo);
            bizVO.setPageSize(request.getPageSize());
        }
        return bizVO;
    }

    @Override
    public SmartContract getSmartContractInfo(QuerySmartContractReq vo) {
        SmartContractModelKey smartContractModelKey = new SmartContractModelKey();
        smartContractModelKey.setScChannelId(vo.getChannelId());
        smartContractModelKey.setScVersion(vo.getVersion());
        smartContractModelKey.setAlisa(vo.getAlisa());
        SmartContractModelWithBLOBs smartContractModelWithBLOBs = smartContractModelMapper.selectByPrimaryKey(smartContractModelKey);
        if (null == smartContractModelWithBLOBs) {
            return null;
        }
        SmartContract smartContract = smartContractModelWithBLOBs.toSmartContract();
        return smartContract;
    }


    @Override
    public SmartContract getActiveSmartContract(QuerySmartContractReq vo) {
        SmartContractModelKey smartContractModelKey = new SmartContractModelKey();
        smartContractModelKey.setScChannelId(vo.getChannelId());
        smartContractModelKey.setScVersion(vo.getVersion());
        smartContractModelKey.setAlisa(vo.getAlisa());
        SmartContractModelWithBLOBs smartContractModelWithBLOBs = smartContractModelMapper.selectActiveByPrimaryKey(smartContractModelKey);
        if (null == smartContractModelWithBLOBs) {
            return null;
        }
        SmartContract smartContract = smartContractModelWithBLOBs.toSmartContract();
        return smartContract;
    }


    /**
     * 查询某个时间范围内的世界状态记录
     *
     * @param channelId 通道id
     * @param startTime 开始时间 时间戳
     * @param endTime   结束时间 时间戳
     * @return 多个世界状态记录
     */
    @Override
    public List<WorldStateResp> queryStatesByTimeRegion(String channelId, Long startTime, Long endTime) {
        if (startTime == null) {
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM);
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        if (endTime < startTime) {
            //结束时间 < 开始时间
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date sDate = new Date(startTime);
        Date eDate = new Date(endTime);
        if (eDate.getTime() - sDate.getTime() > 24 * 60 * 60 * 1000) {
            //间隔大于24小时
            throw new NewspiralException(NewSpiralErrorEnum.QUERY_TOO_LONG_TIME_INTERVAL);
        }
        String startDate = sdf.format(sDate);
        String endDate = sdf.format(eDate);
        log.info(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channelId + "queryStatesHistory,startTime = {},endTime = {},channelId = {}", startDate, endDate, channelId);
        Future<List<StateModel>> future = executorService.submit(() -> stateModelMapper.selectByTime(channelId, startDate, endDate));
        int count = stateModelMapper.selectCountByTime(channelId, startDate, endDate);
        if (count > 10000) {
            //超过10000
            future.cancel(true);
            throw new NewspiralException(NewSpiralErrorEnum.QUERY_TOO_BIG_DATA);
        }
        List<StateModel> stateModels = null;
        try {
            stateModels = future.get();
        } catch (Exception e) {
            future.cancel(true);
            throw new NewspiralException(NewSpiralErrorEnum.PROCESS_ERROR);
        }
        Channel channel = getChannel(channelId);
        String channelName = channel.getName();
        return stateModels.stream().map(state -> {
            WorldStateResp worldStateResp = StateModel.createWorldStateRespInstance(state);
            worldStateResp.setChannelName(channelName);
            worldStateResp.setChannelId(channelId);
            return worldStateResp;
        }).collect(Collectors.toList());
    }


    /**
     * 查询某个世界状态的变更历史
     *
     * @param channelId 通道id
     * @param startTime 开始时间 时间戳
     * @param endTime   结束时间 时间戳
     * @param key       世界状态key
     * @return 多个交易记录
     */
    public List<TransactionResp> queryStatesHistory(String channelId, Long startTime, Long endTime, String key) {
        if (startTime == null) {
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM);
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        if (endTime < startTime) {
            //结束时间 < 开始时间
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date sDate = new Date(startTime);
        Date eDate = new Date(endTime);
        if (eDate.getTime() - sDate.getTime() > 24 * 60 * 60 * 1000) {
            //间隔大于24小时
            throw new NewspiralException(NewSpiralErrorEnum.QUERY_TOO_LONG_TIME_INTERVAL);
        }
        String startDate = sdf.format(sDate);
        String endDate = sdf.format(eDate);
        log.info(ModuleClassification.LedM_LMI_ + "MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channelId + ",queryStatesHistory,startTime = {},endTime = {},key = {},channelId = {}", startDate, endDate, key, channelId);
        List<TransactionModelWithBLOBs> results = transactionModelMapper.selectByCreateTimestamp(channelId, startDate, endDate);
        if (results.size() > 1000) {
            //超过1000
            throw new NewspiralException(NewSpiralErrorEnum.QUERY_TOO_BIG_DATA);
        }
        Channel channel = getChannel(channelId);
        String channelName = channel.getName();
        return results.stream().filter(tx -> {
            //过滤key,挑选出指定的key
            List<WorldStateModifyRecord> records = JSONObject.parseArray(tx.getModifiedWorldStateList(), WorldStateModifyRecord.class);
            return records.parallelStream().anyMatch(
                    record -> {
                        JSONObject newState = (JSONObject) JSONObject.toJSON(record.getNewState());
                        JSONObject oldState = (JSONObject) JSONObject.toJSON(record.getOldState());
                        return key.equals(newState == null ? null : newState.getString("key")) ||
                                key.equals(oldState == null ? null : oldState.getString("key"));
                    }
            );
        }).sorted(Comparator.comparing(TransactionModel::getCreateTimestamp))
                .map(txResp -> {
                    TransactionResp resp = TransactionResp.transferTransactionResp(txResp.toTransaction());
                    resp.setChannelId(channelId);
                    resp.setChannelName(channelName);
                    return resp;
                }).collect(Collectors.toList());
    }

    /**
     * 查询某个通道内所有的世界状态
     *
     * @param channelId
     * @return
     */
    @Override
    public List<WorldState> queryAllWorldState(String channelId, Long from, Long to) {
        List<StateModel> stateModels = stateModelMapper.selectAllWorldState(channelId, from, to);
        List<WorldState> worldStates = new ArrayList<>();
        if (!StringUtils.isEmpty(stateModels)) {
            for (StateModel stateModel : stateModels) {
                WorldState worldState = stateModel.toWorldState();
                worldStates.add(worldState);
            }
        }
        return worldStates;
    }

    /**
     * 根据通道Id和高度范围查询区块列表
     */
    @Override
    public List<Block> queryBlockByChannelIdAndHeightRegion(String channelId, Long from, Long to) {
        List<BlockModel> models = blockModelMapper.selectBlockByChannelIdAndHeightRegion(channelId, from, to);
        ArrayList<Block> blockList = new ArrayList<>();
        for (BlockModel model : models) {
            Block block = new Block();
            block.setBlockHeader(model.toBlockHeader());
            List<TransactionModelWithBLOBs> txRecord = transactionModelMapper.selectByBlockHash(channelId, model.getHash());

            List<ExecutedTransaction> txList = new LinkedList<>();
            for (TransactionModelWithBLOBs tx : txRecord) {
                txList.add(tx.toExecutionTransaction());
            }
            //对于fabric的区块的顺序重新排列
            if (VersionEnum.FABRIC.getCode().equals(block.getBlockHeader().getVersion())) {
                Collections.sort(txList);
            }
            block.addTransactionList(txList);
            //计算一下merkleRoot
            ArrayList<byte[]> bytesOfTx = new ArrayList<>();
            for (ExecutedTransaction tx : block.getTransactionList()) {
                bytesOfTx.add(tx.getSdkTransaction().getHash().getBytes());
            }
            String merkleRoot = Hex.encodeHexString(MerkleUtil.merkle(bytesOfTx));
            block.getBlockHeader().setMerkleRoot(merkleRoot);
            blockList.add(block);
        }
        return blockList;
    }

    @Transactional
    @Override
    public void persistInterfaceRecord(List<InterfaceRecord> interfaceRecords, List<InterfaceRecordSummary> interfaceRecordSummaryList) {
        List<InterfaceRecordModel> interfaceRecordModelList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(interfaceRecords)) {
            for (InterfaceRecord interfaceRecord : interfaceRecords) {
                interfaceRecordModelList.add(InterfaceRecordModel.createInstance(interfaceRecord));
                if (interfaceRecordModelList.size() % 200 == 0) {
                    interfaceRecordMapper.batchInsert(interfaceRecordModelList);
                    interfaceRecordModelList.clear();
                }
            }
        }
        //最后剩余批量插入
        if (!CollectionUtils.isEmpty(interfaceRecordModelList)) {
            interfaceRecordMapper.batchInsert(interfaceRecordModelList);
        }

        List<InterfaceRecordSummaryModel> interfaceRecordSummaryModelList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(interfaceRecordSummaryList)) {
            for (InterfaceRecordSummary interfaceRecordSummary : interfaceRecordSummaryList) {
                interfaceRecordSummaryModelList.add(InterfaceRecordSummaryModel.createInstance(interfaceRecordSummary));
                if (interfaceRecordSummaryModelList.size() % 200 == 0) {
                    interfaceRecordSummaryMapper.duplicateKeyInsertAndUpdate(interfaceRecordSummaryModelList);
                    interfaceRecordSummaryModelList.clear();
                }
            }
        }
        //最后剩余批量插入
        if (!CollectionUtils.isEmpty(interfaceRecordSummaryModelList)) {
            interfaceRecordSummaryMapper.duplicateKeyInsertAndUpdate(interfaceRecordSummaryModelList);
        }
    }

    @Override
    public void persistInterfaceRecordSummary(List<InterfaceRecordSummary> interfaceRecordSummaryList) {
        List<InterfaceRecordSummaryModel> interfaceRecordSummaryModelList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(interfaceRecordSummaryList)) {
            for (InterfaceRecordSummary interfaceRecordSummary : interfaceRecordSummaryList) {
                interfaceRecordSummaryModelList.add(InterfaceRecordSummaryModel.createInstance(interfaceRecordSummary));
            }
        }
        //批量插入
        if (!CollectionUtils.isEmpty(interfaceRecordSummaryModelList)) {
            interfaceRecordSummaryMapper.duplicateKeyInsertAndUpdate(interfaceRecordSummaryModelList);
        }
    }

    @Override
    public PageInfo<InterfaceRecord> queryInterfaceRecord(InterfaceRecordBO interfaceRecordBO) {
        PageInfo<InterfaceRecord> pageInfo = new PageInfo();
        //总数
        Long totalCount = interfaceRecordMapper.slectTotalCount(interfaceRecordBO);
        pageInfo.setTotalCount(totalCount);
        //总页数
        long totalPage = totalCount % interfaceRecordBO.getPageSize() == 0 ? totalCount / interfaceRecordBO.getPageSize() : totalCount / interfaceRecordBO.getPageSize() + 1;
        pageInfo.setTotalPage(totalPage);
        //当前页序号
        pageInfo.setCurPage(interfaceRecordBO.getCurPage());
        // 页大小
        pageInfo.setPageSize(interfaceRecordBO.getPageSize());
        //查询记录
        List<InterfaceRecord> interfaceRecordList = interfaceRecordMapper.selectByPage(interfaceRecordBO);
        pageInfo.setRecord(interfaceRecordList);
        return pageInfo;
    }

    @Override
    public List<InterfaceRecordSummary> queryInterfaceRecordSummary() {
        List<InterfaceRecordSummary> interfaceRecordSummaryList = new ArrayList<>();
        List<InterfaceRecordSummaryModel> interfaceRecordSummaryModelList = interfaceRecordSummaryMapper.selectInterfaceRecordSummary();
        if (!StringUtils.isEmpty(interfaceRecordSummaryModelList)) {
            for (InterfaceRecordSummaryModel interfaceRecordSummaryModel : interfaceRecordSummaryModelList) {
                interfaceRecordSummaryList.add(interfaceRecordSummaryModel.toInterfaceRecordSummary(interfaceRecordSummaryModel));
            }
        }
        return interfaceRecordSummaryList;
    }

    public void cleanUpRecord(String beforeDayTime) {
        interfaceRecordMapper.deleteFromDayTime(beforeDayTime);
    }

    public Long queryMaxBlockHightByChannelId(String channelId) {
        Long height = blockModelMapper.queryMaxBlockHightByChannelId(channelId);
        return height;
    }


    public void insertLatestBlockIntoCachedBlock(String channelId, Integer count) {
        List<BlockModel> blockModelList = blockModelMapper.selectLatestBlockList(channelId, count);
        if (!CollectionUtils.isEmpty(blockModelList)) {
            for (int i = blockModelList.size() - 1; i >= 0; i--) {
                blockModelMapper.insertCachedBlock(blockModelList.get(i));
            }
        }
    }

    /**
     * 添加新的预编译智能合约信息
     *
     * @param txCompileID
     * @param txCompile
     */
    @Override
    public void setTransactionCompile(String txCompileID, TransactionCompile txCompile) {
        if (this.mapTXCompile.containsKey(txCompileID) == false) {
            this.mapTXCompile.put(txCompileID, txCompile);
        }
    }

    /**
     * 获取对应的预编译交易智能合约信息
     *
     * @param txCompileID
     * @return
     */
    @Override
    public TransactionCompile getTransactionCompile(String txCompileID) {
        if (this.mapTXCompile.containsKey(txCompileID) == true) {
            return this.mapTXCompile.get(txCompileID);
        }
        return null;
    }

    @Override
    public PageInfo<StateHistoryResp> queryStateHistory(StateHistoryBO stateHistoryBO) {
        PageInfo<StateHistoryResp> pageInfo = new PageInfo<>();
        //当前页
        pageInfo.setCurPage(stateHistoryBO.getCurPage());
        //页大小
        Integer pageSize = stateHistoryBO.getPageSize();
        pageInfo.setPageSize(pageSize);
        //总数，符合条件的交易总数
        Long totalCount = stateHistoryModelMapper.selectTotalCount(stateHistoryBO);
        pageInfo.setTotalCount(totalCount);
        //总页数
        Long totalPage = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        pageInfo.setTotalPage(totalPage);
        //交易列表
        Long start = stateHistoryBO.getStart();
        List<StateHistoryModel> stateHistoryBOList = stateHistoryModelMapper.selectPageTransactionList(stateHistoryBO);
        List<StateHistoryResp> stateHistoryRespList = null;
        if (!CollectionUtils.isEmpty(stateHistoryBOList)) {
            stateHistoryRespList = stateHistoryBOList.stream().map(stateHistoryModel -> stateHistoryModel.transferToStateHistoryResp(stateHistoryModel)).collect(Collectors.toList());
        }
        pageInfo.setRecord(stateHistoryRespList);
        return pageInfo;
    }

    @Override
    public void deleteLatestBlockData(String channelId) {
        stateHistoryModelMapper.deleteLatestBlockData(channelId);
    }

    @Override
    public Long queryLatestBlockId(String channelId) {
        Long blockId = stateHistoryModelMapper.selectLatestBlockId(channelId);
        return blockId;
    }

    @Override
    public void persisitStateHistory(List<StateHistoryModel> batchInsertList) {
        //Long version = stateHistoryModelMapper.selectLatestInsertVersion();
        //if(null == version) version = 0L;
        if (!CollectionUtils.isEmpty(batchInsertList)) {
            /*for (StateHistoryModel stateHistoryModel : batchInsertList) {
                stateHistoryModel.setInsertVersion(1L);
            }*/
            stateHistoryModelMapper.batchInsert(batchInsertList);
        }
    }

    @Override
    public Long queryIfExistBlocksAfterFromBlock(String channelId, Long fromBlockId) {
        Long blockCount = blockModelMapper.selectIfExistAfterFromBLock(channelId, fromBlockId);
        return blockCount;
    }

    @Override
    public Long queryBlockTableLatestBlockId(String channelId) {
        return blockModelMapper.selectLatestBlockId(channelId);
    }

    @Override
    public Map<Long, List<StateHistoryModel>> queryStateHistoryList(String channelId, Long blockId, Long toBlockId) {
        Map<Long, List<StateHistoryModel>> stateHistoryMap = null;
        //用于存放所有的解析结果
        List<StateHistoryModel> stateHistoryModels = new ArrayList<>();
        //查询交易列表
        List<TransactionModelExpandStateHistory> transactionModelExpandStateHistories = transactionModelMapper.selectTransListByBlockIdRegion(channelId, blockId);
        //遍历解析
        List<WorldStateModifyRecord> worldStateModifyRecordListLists = new ArrayList<WorldStateModifyRecord>();
        if (!CollectionUtils.isEmpty(transactionModelExpandStateHistories)) {
            for (int i = 0; i < transactionModelExpandStateHistories.size(); i++) {
                TransactionModelExpandStateHistory transactionModelExpandStateHistory = transactionModelExpandStateHistories.get(i);
                //解析worldStateList
                worldStateModifyRecordListLists = JSON.parseArray(transactionModelExpandStateHistory.getModifiedWorldStateList(), WorldStateModifyRecord.class);
                if (!CollectionUtils.isEmpty(worldStateModifyRecordListLists)) {
                    //如果worldStateList不为空，则说明交易成功了，进行相关的解析
                    for (WorldStateModifyRecord worldStateModifyRecord : worldStateModifyRecordListLists) {
                        //TODO 构造返回值 (对于每一个stateKey，对应着一条交易记录)
                        StateHistoryModel stateHistoryModel = new StateHistoryModel();
                        //注意，如果是新增，oldState是null；如果是删除newState是null
                        String stateKey = worldStateModifyRecord.getOldState() != null ? worldStateModifyRecord.getOldState().getKey() : worldStateModifyRecord.getNewState().getKey();
                        stateHistoryModel.setStateKey(stateKey);
                        transferToStateHistory(transactionModelExpandStateHistory, stateHistoryModel);
                        stateHistoryModels.add(stateHistoryModel);
                    }
                }
            }
        }
        //对解析完成的交易根据blockId进行分组
        if (!CollectionUtils.isEmpty(stateHistoryModels)) {
            stateHistoryMap = stateHistoryModels.stream().collect(Collectors.groupingBy(StateHistoryModel::getBlockId));
        }
        return stateHistoryMap;
    }

    public Set<String> getServiceUrlList() {
        Set<String> peerServiceUrlSet = new HashSet<>();
        List<String> serviceUrlList = peerModelMapper.selecetServiceUrls();
        for (String serviceUrls : serviceUrlList) {
            PeerServiceUrls peerServiceUrls = JSON.parseObject(serviceUrls, PeerServiceUrls.class);
            Collection<PeerServiceUrl> peerServiceUrlList = peerServiceUrls.getServiceUrlMap().values();
            for (PeerServiceUrl peerServiceUrl : peerServiceUrlList) {
                peerServiceUrlSet.add(peerServiceUrl.getUrl().split(":")[0]);
            }
        }
        return peerServiceUrlSet;
    }

    ;

    /**
     * 通过交易记录构造stateHistory返回体
     *
     * @param transactionModelExpandStateHistory
     * @param stateHistoryModel
     */
    private void transferToStateHistory(TransactionModelExpandStateHistory transactionModelExpandStateHistory, StateHistoryModel stateHistoryModel) {
        stateHistoryModel.setChannelId(transactionModelExpandStateHistory.getChannelId());
        stateHistoryModel.setTransHashStr(transactionModelExpandStateHistory.getTransHashStr());
        stateHistoryModel.setClientIdentityKey(transactionModelExpandStateHistory.getClientIdentityKey());
        stateHistoryModel.setSmartContractId(transactionModelExpandStateHistory.getSmartContractId());
        stateHistoryModel.setSmartContractMethodName(transactionModelExpandStateHistory.getSmartContractMethodName());
        stateHistoryModel.setBlockId(transactionModelExpandStateHistory.getBlockId());
        stateHistoryModel.setIndexInBlock(transactionModelExpandStateHistory.getIndexInBlock());
        stateHistoryModel.setBlockHashStr(transactionModelExpandStateHistory.getBlockHash());
        stateHistoryModel.setSuccessed(transactionModelExpandStateHistory.getSuccessed());
        stateHistoryModel.setErrorMsg(transactionModelExpandStateHistory.getErrorMsg());
        stateHistoryModel.setClientTimestamp(transactionModelExpandStateHistory.getClientTimestamp());
        stateHistoryModel.setConsensusTimestamp(transactionModelExpandStateHistory.getConsensusTimeStamp());
        stateHistoryModel.setTransactionId(String.valueOf(transactionModelExpandStateHistory.getId()));
        stateHistoryModel.setInsertVersion(0l);
    }


    /**
     * 将transaction转化为transactionResp
     *
     * @param transaction
     * @return
     */
    @Override
    public TransactionResp transferTransactionResp(Transaction transaction) {
        TransactionResp transactionResp = TransactionResp.transferTransactionResp(transaction);
        Channel channel = this.getChannel(transaction.getChannelId());
        transactionResp.setChannelId(channel.getChannelId());
        transactionResp.setChannelName(channel.getName());
        TransactionAttached transactionAttached = new TransactionAttached();
        Long blockHeight = this.blockModelMapper.selectHeightByHash(transaction.getChannelId(), transaction.getBlockHashStr());
        transactionAttached.setBlockHeight(blockHeight);
        transactionResp.setTransactionAttached(transactionAttached);
        return transactionResp;
    }

    /**
     * 将transactionlist转化为transactionRespList
     *
     * @param transactionList
     * @return
     */
    public List<TransactionResp> transferTransactionRespList(List<Transaction> transactionList) {
        List<TransactionResp> transactionResps = new ArrayList<>();
        if (!CollectionUtils.isEmpty(transactionList)) {
            for (Transaction transaction : transactionList) {
                TransactionResp transactionResp = TransactionResp.transferTransactionResp(transaction);
                Channel channel = this.getChannel(transaction.getChannelId());
                transactionResp.setChannelId(channel.getChannelId());
                transactionResp.setChannelName(channel.getName());
                TransactionAttached transactionAttached = new TransactionAttached();
                Long blockHeight = this.blockModelMapper.selectHeightByHash(transaction.getChannelId(), transaction.getBlockHashStr());
                transactionAttached.setBlockHeight(blockHeight);
                transactionResp.setTransactionAttached(transactionAttached);
                transactionResps.add(transactionResp);
            }
        }
        return transactionResps;
    }

    @Override
    public Map<String, Long> queryChannelMaxBlockId() {
        return blockModelMapper.queryChannelMaxBlockId();
    }

    @Override
    public VersionResp queryLatestSystemVersion() {
        SystemVersionModel systemVersionModel = systemVersionMapper.selectMaxId();
        if (null == systemVersionModel) return null;
        return systemVersionModel.toVersionResp();
    }

    public PeerChannelRelation selectLatestRecordByPeerIdAndChanneId (String peerId, String channelId,String actionType) {
        PeerChannelModelWithBLOBs peerChannelModelWithBLOBs = peerChannelModelMapper.selectLatestRecordByActionType(peerId, channelId, actionType);
        PeerChannelRelation peerChannelRelation = null;
        if (peerChannelModelWithBLOBs != null) {
            peerChannelRelation = peerChannelModelWithBLOBs.toPeerChannelRelation();
        }
        return peerChannelRelation;
    }
}

