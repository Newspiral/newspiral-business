package com.jinninghui.newspiral.member.mgr.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.Enum.ActionTypeEnum;
import com.jinninghui.newspiral.common.entity.Enum.PeerActionTypeEnum;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.chain.*;
import com.jinninghui.newspiral.common.entity.common.base.BaseResponse;
import com.jinninghui.newspiral.common.entity.common.base.BaseTransHashResp;
import com.jinninghui.newspiral.common.entity.common.base.NewspiralStateCodes;
import com.jinninghui.newspiral.common.entity.common.base.ResponseUtil;
import com.jinninghui.newspiral.common.entity.exception.NewSpiralErrorEnum;
import com.jinninghui.newspiral.common.entity.exception.NewspiralException;
import com.jinninghui.newspiral.common.entity.identity.SignerIdentityKey;
import com.jinninghui.newspiral.common.entity.identity.Identity;
import com.jinninghui.newspiral.common.entity.identity.IdentityKey;
import com.jinninghui.newspiral.common.entity.state.StateHistory;
import com.jinninghui.newspiral.common.entity.task.Task;
import com.jinninghui.newspiral.common.entity.task.TaskStatus;
import com.jinninghui.newspiral.common.entity.task.TaskTypeEnum;
import com.jinninghui.newspiral.common.entity.transaction.ExecutedTransaction;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import com.jinninghui.newspiral.consensus.hotstuff.ConsensusMsgProcessor;
import com.jinninghui.newspiral.gateway.entity.QueryChannelReq;
import com.jinninghui.newspiral.ledger.mgr.LedgerMgr;
import com.jinninghui.newspiral.ledger.mgr.MemberLedgerMgr;
import com.jinninghui.newspiral.member.mgr.MemberMgr;
import com.jinninghui.newspiral.p2p.P2pClient;
import com.jinninghui.newspiral.security.DataSecurityMgr;
import com.jinninghui.newspiral.security.SecurityService;
import com.jinninghui.newspiral.security.SecurityServiceMgr;
import com.jinninghui.newspiral.transaction.mgr.TransactionMgr;
import lombok.extern.slf4j.Slf4j;
import com.jinninghui.newspiral.common.entity.common.logmodule.LogModuleCodes;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * @author lida
 * @date 2019/9/20 20:36
 */
@Slf4j
public class MemberMgrImpl implements MemberMgr {

    @SofaReference
    private LedgerMgr ledgerMgr;

    @SofaReference
    private MemberLedgerMgr memberLedgerMgr;

    @SofaReference
    private SecurityServiceMgr securityServiceMgr;

    @SofaReference
    private TransactionMgr tranasctionMgr;

    @SofaReference
    private P2pClient p2pClient;

    @SofaReference
    private ConsensusMsgProcessor consensusMsgProcessor;
    @SofaReference
    private DataSecurityMgr dataSecurityMgr;




    /**
     * 私有变量，用于处理成员管理的一些后台任务
     */
    private MemberMgrTaskRunnable memberMgrTaskRunnable;

    //**************************缓存数据对象****************************//
    /**
     * key为idntityKey的String
     */
    Map<String, Identity> identityMap = new HashMap<>();

    /**
     * key为Peer的PeerId的String
     */
    Map<String, Peer> peerMap = new HashMap<>();

    Peer localPeer;
    @Autowired
    private AsynMemberTask asynMemberTask;
    //**************************缓存数据对象****************************//

    /**
     * 初始化流程，由Spring框架调用
     * 主要作用在于加载持久化的所有身份
     */
    public void init() {
        try {
            log.info("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",成员管理模块开始初始化...");
            List<Identity> identityList = ledgerMgr.queryAllIdentities();
            List<Peer> peerList = ledgerMgr.queryAllPeers();
            //for debug
            for (Peer peer : peerList) {
                log.info("certificate file {}", peer.getCertificateCerFile().toString());
            }
            for (Identity identity : identityList) {
                identityMap.put(identity.getKey().toString(), identity);
            }
            for (Peer peer : peerList) {
                peerMap.put(peer.getPeerId().toString(), peer);
                if (peer.getIsLocalPeer()) {
                    log.info("init.localPeer.peerIdValue={}", peer.getPeerId().getValue());
                    localPeer = peer;
                }
            }
            memberMgrTaskRunnable = new MemberMgrTaskRunnable(this);
            new Thread(memberMgrTaskRunnable).start();
            log.info("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",成员管理模块开始初始化完毕，加载了" + identityList.size() + "个身份,加载了" + peerList.size() + "个节点,本地节点ID:" + localPeer.getPeerId().getValue());
        } catch (Exception ex) {
            log.error("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",成员管理模块初始化异常,这会导致系统无法正常工作，退出系统:", ex);
            System.exit(1);
        }

    }

    @Override
    public CreateChannel createChannel(ChannelInitParams initParams) {
        CreateChannel createChannel = new CreateChannel();
        //TODO 权限检查还是应该放到统一的认证鉴权中去做，通过一个“接口和身份的映射关系”配置，即可大幅减少重复的鉴权和错误记录代码
        //创建仅包含一个节点的通道并不需要走共识流程
        //SecurityService securityService = securityServiceMgr.getMatchSecurityService(initParams.getSecurityServiceKey());
        Peer peer = localPeer.clone();
        peer.getPeerChannelRelation().setExtendedData("0");
        peer.getPeerChannelRelation().setActionType(PeerActionTypeEnum.IN_OUT.getCode());

        Channel newChannel = Channel.createInstance(initParams, peer, dataSecurityMgr.getHash(peer.getCertificateCerFile()));
  /*      //TODO 添加其他节点初始化,预留一下
        for (Peer addPeer:initParams.getAddPeerList())
        {
            PeerCert.clonePeerCers(addPeer,newChannel.getChannelId(),dataSecurityMgr.getHash(addPeer.getCertificateCerFile()));
            newChannel.getMemberPeerList().add(addPeer);
        }*/
        ledgerMgr.insertChannel(newChannel);
/*        Member member = createDefaultMember(newChannel.getChannelId(), initParams);
        memberLedgerMgr.insertMember(member);*/
        //TODO 可以异步执行查询后调p2p接口，实现其他节点也完成该通道的初始化并加入该通道
        Channel channel = ledgerMgr.queryChannel(newChannel.getChannelId());
        //本地新建一个通道，等同于本地节点加入了该新建通道
        consensusMsgProcessor.processLocalPeerAdd2Channel(channel);
/*        Set<Peer> peers=new HashSet<>();
        for(Peer onePeer:channel.getMemberPeerList())
        {
            //TODO 不是本地节点就要去初始化
            if(!onePeer.getIsLocalPeer())
            {
                try {
                    p2pClient.initChannel(channel,onePeer);
                }
                catch (Exception e)
                {
                    //失败记录下来异步触发几次
                    peers.add(onePeer);
                }
            }
        }
        if(!CollectionUtils.isEmpty(peers))
        {
            asynMemberTask.ininChannel(channel,peers);
        }*/
        createChannel.setChannelId(newChannel.getChannelId());
        createChannel.setCreateTimestamp(newChannel.getCreateTimestamp());
        return createChannel;
    }

/*    private Member createDefaultMember(String channelId, ChannelInitParams initParams) {
        Member member = new Member();
        member.setChannelId(channelId);
        member.setMemberId(UUID.randomUUID().toString().replaceAll("-","").toUpperCase());
        member.setName(initParams.getOrganizationName());
        member.setIssuerId(initParams.getOrganizationId());
        member.setCertificateCerFile(initParams.getCertificateFile());
        //CA证书获取公钥
        member.setPublicKey(dataSecurityMgr.getCertificatePublicKey(initParams.getCertificateFile()));
        List<Role> roles = memberLedgerMgr.getRoleByRoleFlag(1);
        member.setRoles(roles);
        return member;
    }*/

    @Override
    public void initChannel(Channel channel) {
        //如果通道存在直接返回
        if (ledgerMgr.getChannel(channel.getChannelId()) != null) return;
        Peer peer = localPeer.clone();
        peer.getPeerChannelRelation().setExtendedData("0");
        String certificateHash = dataSecurityMgr.getHash(peer.getCertificateCerFile());
        PeerCert.clonePeerCers(peer, channel.getChannelId(), certificateHash);
        Iterator<Peer> memberPeers = channel.getMemberPeerList().iterator();
        while (memberPeers.hasNext()) {
            if (peer.getPeerId().getValue().equals(memberPeers.next().getPeerId().getValue())) {
                memberPeers.remove();
            }
        }
        channel.getMemberPeerList().add(peer);
        for (Peer onePeer : channel.getMemberPeerList()) {
            for (PeerCert peerCert : onePeer.getPeerCert()) {
                peerCert.setId(null);
            }
        }
        ledgerMgr.insertChannel(channel);
        //可以异步执行查询后调p2p接口，实现其他节点也完成该通道的初始化并加入该通道
        Channel queryChannel = ledgerMgr.queryChannel(channel.getChannelId());
        //本地新建一个通道，等同于本地节点加入了该新建通道
        consensusMsgProcessor.processLocalPeerAdd2Channel(queryChannel);

    }

    @Override
    public Channel queryChannel(String channelId) {
        log.info("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + "," + channelId + ",queryChannel in MemberMgrImpl");
        return ledgerMgr.queryChannel(channelId);
    }

    @Override
    public void createGenesisBlockForNewChannel(Channel channel) {
        ExecutedTransaction executedTransaction = new ExecutedTransaction();
        ChannelModifyRecord channelModifyRecord = new ChannelModifyRecord();
        channelModifyRecord.setNewChannel(channel);
        executedTransaction.setModifiedChannelRecordList(Arrays.asList(channelModifyRecord));
        Block block = new Block();
        List<ExecutedTransaction> list = new LinkedList<>();
        list.add(executedTransaction);
        block.addTransactionList(list);

        ledgerMgr.persistBlock(block);
        //生成一个ExecuteTransaction
        //生成一个包含上述交易的block已经相应的QC
        //生成两个空块到缓存数据库表（此处对数据库的操作需要用事务来完成）
        return;
    }

    @Override
    public BaseResponse addMySelf2Channel(AddMySelfToChannelRequest request) {
        //TODO 可以增加检查，是否拿到了足够多的通道成员的签名
        //要加入通道的节点向已经在通道中的节点发送请求信息，获取已经在通道中的节点的基本参数
        ChannelBasicParams channelBasicParams = getChannelBasicParams(request);
        //如果该节点要加入的通道以及存在，并且available是1,则抛出异常
        Channel channel = ledgerMgr.queryChannel(request.getNewMemberTransaction().getChannelId());
        if (channel !=null
            && channel.getAvailable()==1L) {
            return ResponseUtil.error(NewspiralStateCodes.SYSTEM_PEER_IN_CHANNEL);
        }
        //如果该节点要加入的通道存在，并且available是0，说明节点已经加入过，现在退出了，将旧的节点信息删除
        /*if (channel !=null
            && channel.getAvailable()==0L) {
            //todo 删除channel表、peer_channel表、member表，member_role表，peer_certificate表
            ledgerMgr.deleteChannelBasicData(channel.getChannelId());
        }*/
/*        if(false == securityServiceMgr.getMatchSecurityService(channelBasicParams.getSecurityServiceKey()).verifySignature(request))
        {
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM,"验签未通过");
        }*/
        //先创建任务，避免调用成功后，没有创建此任务;
        // 如果创建了任务但是后面发请求给各个通道未成功，顶多就是该任务最终失败
        //调整发起交易调用之后执行任务
        Task persistTask = createAndRunResultQueryTask(request);
        BaseTransHashResp baseTransHashResp = createAndSendAddPeerTransaction(request);
        if (null == baseTransHashResp.getTransHash()) {
            return ResponseUtil.error(baseTransHashResp.getNewspiralStateCodes());
        } else {
            //设置任务执行时间
            persistTask.setExecuteEndTime(System.currentTimeMillis() + channelBasicParams.getBlockMaxInterval() * 200);
            memberMgrTaskRunnable.addTask(persistTask);
            return ResponseUtil.success(baseTransHashResp.getTransHash());
        }
    }

    @Override
    public String removePeerFromChannel(RemovePeerFromChannelRequest removePeerFromChannelRequest) {
        return createAndSendRemovePeerTransaction(removePeerFromChannelRequest);
    }

    private String createAndSendRemovePeerTransaction(RemovePeerFromChannelRequest request) {
        //extract sdktransaction from request and put it in the txs pool.
        SDKTransaction rmPeerTransaction = request.getRmPeerTransaction();
        BaseTransHashResp baseTransHashResp = tranasctionMgr.addTransaction(rmPeerTransaction, true);
        Channel channel = ledgerMgr.queryChannel(rmPeerTransaction.getChannelId());
        log.info("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",queryChannel in createAndSendRemovePeerTransaction");
        List<Peer> channelPeerList = channel.getMemberPeerList();
        for (Peer peer : channelPeerList) {
            if (peer.getIsLocalPeer()) {
                continue;
            }
            p2pClient.removePeerFromChannel(request.getRmPeerTransaction(), peer);
        }
        return baseTransHashResp.getTransHash();
    }

    /**
     * 构建一个通道添加节点的系统交易并发送给通道中的多个组织
     *
     * @param request
     */
    private BaseTransHashResp createAndSendAddPeerTransaction(AddMySelfToChannelRequest request) {
        //返回交易hash
        BaseTransHashResp baseTransHashResp = new BaseTransHashResp(null);
        BaseTransHashResp tempTransHash;
        SDKTransaction newMemberTransaction = request.getNewMemberTransaction();
        List<String> serviceUrlForPeerList = request.getServiceUrlForPeerList();
        for (String ServiceUrl : serviceUrlForPeerList) {
            tempTransHash = p2pClient.addNewPeer2Channel(newMemberTransaction, ServiceUrl);
            if (!StringUtils.isEmpty(tempTransHash.getTransHash())) {
                baseTransHashResp = tempTransHash;
            }
        }
        return baseTransHashResp;
    }

    /**
     * @param request
     * @return
     */
    private boolean isAlreadyInChannel(AddMySelfToChannelRequest request) {
        Channel channel = ledgerMgr.queryChannel(request.getNewMemberTransaction().getChannelId());
        if (null == channel) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 创建一个后台线程来异步的轮询加入通道的结果
     * 并将该任务持久化，持久化是为了保证异常宕机时，下次启动仍然可以继续执行这些任务
     *
     * @param request
     */
    private Task createAndRunResultQueryTask(AddMySelfToChannelRequest request) {
        Task persistTask = createAndPersistTask(request);
        return persistTask;
    }

    /**
     * 执行结果直接更新到入参Task的状态中，并不会持久化
     *
     * @param task
     */
    public void executeAddChannelResultTask(Task task) {
        log.info("executeAddChannelResultTask.star:{}", task);
        if (task.getStatus().isFianlStatus() == false) {
            // log.info("executeAddChannelResultTask.status:{}",task.getStatus().isFianlStatus());
            //查询其他节点的Channel，如果任意一个已经将本节点加入，则说明本节点已经成功加入到Channel中
            AddMySelfToChannelRequest addMySelfToChannelRequest = JSON.parseObject(task.getParamsList().get(0), AddMySelfToChannelRequest.class);

            QueryChannelReq queryChannelReq = new QueryChannelReq();
            queryChannelReq.setChannelId(addMySelfToChannelRequest.getNewMemberTransaction().getChannelId());
            //TODO 这里本来应该要签名的，但是怎么获得该通道的签名策略，还没细想，最小功能集合不做权限检查，后期再做
            // securityServiceMgr.getMatchSecurityService()
            //log.info("executeAddChannelResultTask.peerList:{}",addMySelfToChannelRequest.getChannelPeerList());
            for (String serviceUrl : addMySelfToChannelRequest.getServiceUrlForPeerList()) {
                try {
                    //log.info("executeAddChannelResultTask.query之前：{}",peer);
                    Channel channel = p2pClient.queryChannel(queryChannelReq, serviceUrl);
                    //for debug
                    log.info("peer list in channel from peer {}:", serviceUrl);
                    for (Peer peer1 : channel.getMemberPeerList()) {
                        log.info("peer {}", peer1.getPeerId().getValue());
                    }
                    //log.info("executeAddChannelResultTask.query之后：{}",channel);
                    //如果通道中的其他节点已经将本地节点添加成功了，那么就可以把通道的消息写入到本地了
                    Peer optPeer = channel.getOptPeer(this.localPeer);
                    if (optPeer != null
                        && !optPeer.isExitChannel()) {
                        //log.info("executeAddChannelResultTask.containPeer：{}",this.localPeer);
                        //对本地节点进行更新，因为本地节点还没有所加入通道的信息
                        replaceLocationPeer(channel);
                        //更新通道客户端
                        p2pClient.updateChannelClientsMap(channel);
                        //写入通道信息到本地，并且启动通道
                        startChannelProcessModule(channel);
                        task.setStatus(TaskStatus.SUCCESS);
                        break;//查到了就不用再查
                    }
                    //一个节点没有，并不结束此次循环，因此有可能实际上已经成功加入通道，但是第一个targetPeer的数据并非最新的
                } catch (Exception ex) {//出异常就继续调用别的节点查询
                    log.info("Exception in execute add channel result task, ex: {}", ex);
                    continue;
                }
            }
        } else {
            log.warn("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",Task的状态是终态，但是调用了executeAddChannelResultTask，这说明程序有非预期逻辑");
        }
    }


    /**
     * @param channel
     */
    private void replaceLocationPeer(Channel channel) {
        Peer peer = localPeer.clone();

        String certificateHash = dataSecurityMgr.getHash(peer.getCertificateCerFile());
        PeerCert.clonePeerCers(peer, channel.getChannelId(), certificateHash);
        Iterator<Peer> memberPeers = channel.getMemberPeerList().iterator();
        while (memberPeers.hasNext()) {
            Peer nextPeer=memberPeers.next();
            //找到本地节点
            if (peer.getPeerId().getValue().equals(nextPeer.getPeerId().getValue())) {
                //设置节点通道id
                peer.getPeerId().setChannelId(channel.getChannelId());
                //设置节点与通道相关的参数
                peer.getPeerChannelRelation().setChannelId(channel.getChannelId());
                peer.getPeerChannelRelation().setExtendedData(nextPeer.getPeerChannelRelation().getExtendedData());
                peer.getPeerChannelRelation().setInBlockHeight(nextPeer.getPeerChannelRelation().getInBlockHeight());
                peer.getPeerChannelRelation().setOutBlockHeight(nextPeer.getPeerChannelRelation().getOutBlockHeight());
                peer.getPeerChannelRelation().setJoinTimeStamp(nextPeer.getPeerChannelRelation().getJoinTimeStamp());
                peer.getPeerChannelRelation().setUpdateTimeStamp(nextPeer.getPeerChannelRelation().getUpdateTimeStamp());
                peer.getPeerChannelRelation().setActionType(nextPeer.getPeerChannelRelation().getActionType());
                peer.getPeerChannelRelation().setUserPrivateKey(nextPeer.getPeerChannelRelation().getUserPrivateKey());
                List<PeerChannelRelation> peerChannelRelationList = nextPeer.getPeerChannelRelationList();
                Collections.sort(peerChannelRelationList);
                peer.setPeerChannelRelationList(peerChannelRelationList);
                PeerCert peerCert = nextPeer.getPeerCert().parallelStream().filter(peerCert1 -> peerCert1.getFlag().equals("0")).findFirst().get();
                peer.getPeerCert().get(0).setBlockHeight(peerCert.getBlockHeight());
                memberPeers.remove();
                //更新一下同步世界状态的高度数据
                reloadSyncHistoryParam(channel);
                break;
            }
        }
        for (int i = 0; i < channel.getMemberPeerList().size(); i++) {
            Peer onePeer = channel.getMemberPeerList().get(i);
            //onePeer.getPeerChannelRelation().setExtendedData(i+1+"");
            {
                for (PeerCert peerCert : onePeer.getPeerCert()) {
                    peerCert.setId(null);
                }
            }
        }
        channel.getMemberPeerList().add(peer);
    }

    /**
     * 更新一下本地节点在该通道中的同步高度参数
     * @param channel
     */
    private void reloadSyncHistoryParam(Channel channel) {
        StateHistory.getChannelTransferHeight().remove(channel.getChannelId());
        //在stateHistory表最新的一个解析区块删除,为了防止脏读
        ledgerMgr.deleteLatestBlockData(channel.getChannelId());
        //在stateHistory表中按照通道查询每个通道的开始区块
        Long fromBlockId = ledgerMgr.queryLatestBlockId(channel.getChannelId());
        //查询开始区块，包含这个区块
        fromBlockId = (fromBlockId==null)?0:fromBlockId+1;
        //log.info("reload,fromBlockId:{}",fromBlockId);
        //在map中记录每个通道已经解析完成的高度
        StateHistory.getChannelTransferHeight().put(channel.getChannelId(),fromBlockId);
        //在block表中查询最新的高度
        Long latestBlockId = ledgerMgr.queryBlockTableLatestBlockId(channel.getChannelId());
        //为了保证最新区块的交易完全写入了数据库，只能转化到latestBlock-1个区块
        if(latestBlockId == null || latestBlockId == 0){
            latestBlockId = 0L;
        }else {
            latestBlockId = latestBlockId - 1;
        }
        //log.info("reload,latestBlockId:{}",latestBlockId);
        StateHistory.getChannelLatestBlockId().put(channel.getChannelId(),latestBlockId);
    }

    private void addPeerChannel(Channel channel) {
        List<Peer> peerList = channel.getMemberPeerList();
        for (Peer peer : peerList) {

        }
    }


    /**
     * 持久化通道并为通道启动相关模块
     *
     * @param channel
     */
    private void startChannelProcessModule(Channel channel) {
        Peer localPeer = ledgerMgr.queryLocalPeer();
        for (Peer peer : channel.getMemberPeerList()) {
            if (peer.equals(localPeer)) {
                peer.setIsLocalPeer(true);
            } else {
                peer.setIsLocalPeer(false);
            }
        }
        ledgerMgr.insertChannel(channel);
        consensusMsgProcessor.processLocalPeerAdd2Channel(channel);
    }

    /**
     * @param channel
     * @return
     */
    private List<Peer> getLatestChannelPeerList(Channel channel) {
        List<Peer> peerList = ledgerMgr.queryPeersOfChannelByHeight(channel.getChannelId(), channel.getLatestChannelChangeHeight());
        return peerList;
    }

    /**
     * 查询不到会抛出异常
     *
     * @param request
     * @return
     */
    private ChannelBasicParams getChannelBasicParams(AddMySelfToChannelRequest request) {
        List<String> serviceUrlForPeerList = request.getServiceUrlForPeerList();
        QueryChannelReq queryChannelReq = new QueryChannelReq();
        queryChannelReq.setChannelId(request.getNewMemberTransaction().getChannelId());//这个请求不需要签名
        ChannelBasicParams channelBasicParams = null;
        for (String serviceUrl : serviceUrlForPeerList) {
            try {
                channelBasicParams = p2pClient.queryChannelBasicParams(queryChannelReq, serviceUrl);
                break;//查到了就不用再查
            } catch (Exception ex) {//出异常就继续调用别的节点查询
                log.warn("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",通过节点" + serviceUrl + "查询通道基本信息异常,尝试该通道的下一个节点", ex);
                continue;
            }
        }
        if (channelBasicParams == null) {
            log.error("MODULE=" + LogModuleCodes.SYSTEM_PLANTFORM_ACTION + ",入参通道中的所有节点服务均无法调用成功，无法获得通道的基本参数");
            throw new NewspiralException(NewSpiralErrorEnum.PROCESS_ERROR, "入参通道中的所有节点服务均无法调用成功，无法获得通道的基本参数");
        }
        return channelBasicParams;
    }

    private Task createAndPersistTask(AddMySelfToChannelRequest request) {
        Task persistTask = new Task();
        persistTask.setTaskId(UUID.randomUUID().toString().replaceAll("-", "").toUpperCase());
        persistTask.setCreateTime(System.currentTimeMillis());
        ArrayList<String> paramList = new ArrayList<>();
        paramList.add(JSON.toJSONString(request, new SerializerFeature[]{SerializerFeature.DisableCircularReferenceDetect}));
        persistTask.setParamsList(paramList);
        persistTask.setType(TaskTypeEnum.LOCAL_PEER_ADD_TO_CHANNEL_RESULT_QUERY);
        persistTask.setStatus(TaskStatus.WAIT_EXEXUTE);
        ledgerMgr.insertTask(persistTask);
        return persistTask;
    }


    /**
     * query task
     *
     * @return
     */
    public List<Task> getTaskListByStatus() {
        return ledgerMgr.getTaskListByStatus();
    }

    @Override
    public BaseTransHashResp addNewPeer2Channel(SDKTransaction peerAddChannelTransaction) {
        // 默认全网广播
        BaseTransHashResp baseTransHashResp = tranasctionMgr.addTransaction(peerAddChannelTransaction, true);
        return baseTransHashResp;
    }

    @Override
    public String removePeerFromChannel(SDKTransaction rmPeerTransaction) {
        BaseTransHashResp baseTransHashResp = tranasctionMgr.addTransaction(rmPeerTransaction, true);
        return baseTransHashResp.getTransHash();
    }

    /**
     * 这里不使用返回true或false的方式，是因为里面可能抛出各种不同的异常
     * 不抛异常则说明校验通过
     *
     * @param peerAddChannelApproval
     * @param channel
     */
    private void verifyPeerAddChannelApproval(PeerAddChannelApproval peerAddChannelApproval, Channel channel) {
        long now = System.currentTimeMillis();
        long msOfOneMonth = 30 * 24 * 60 * 60 * 1000L;
        if (peerAddChannelApproval.getTimestamp() + msOfOneMonth <= now) {
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "新节点加入通道的审批已超过1个月,过期了");
        }

        SecurityService securityService = securityServiceMgr.getMatchSecurityService(channel.getSecurityServiceKey());
        if (!securityService.verifySignatureByGMCertificate(peerAddChannelApproval, channel.getChannelId()))
        //if (!securityService.sm9VerifySignature(peerAddChannelApproval, channel.getChannelId()))
        {
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "新节点加入通道的审批数据验签失败");
        }
    }


    @Override
    public void addMyIdentity(IdentityKey identityKey, SignerIdentityKey callerIdentity) {
        throw new NewspiralException(NewSpiralErrorEnum.UN_IMPLEMENTED);
    }

    @Override
    public void deleteMyIdentity(IdentityKey identityKey, SignerIdentityKey callerIdentity) {

    }


    @Override
    public void addOrgIdentity(IdentityKey identityKey, SignerIdentityKey callerIdentity) {
        throw new NewspiralException(NewSpiralErrorEnum.UN_IMPLEMENTED);
    }

    @Override
    public void deleteOrgIdentity(IdentityKey identityKey, SignerIdentityKey callerIdentity) {
        throw new NewspiralException(NewSpiralErrorEnum.UN_IMPLEMENTED);
    }

    public int updateTask(Task task) {
        return ledgerMgr.updateTask(task);
    }

    @Override
    public boolean getPeerState() {
        return true;
    }

}
