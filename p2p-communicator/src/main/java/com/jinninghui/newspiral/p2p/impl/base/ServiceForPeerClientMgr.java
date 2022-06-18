package com.jinninghui.newspiral.p2p.impl.base;

import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.common.logmodule.LogModuleCodes;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.ledger.mgr.LedgerMgr;
import com.jinninghui.newspiral.p2p.impl.base.ChannelClientMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lida
 * @date 2019/7/29 17:31
 * 管理所有ServiceForPeer服务接口的Client，每个节点的服务在这里都有一个对应的SOAFClient
 * 更确切的说，是每个通道中的每个节点在这里都有一个对应的SOAFClient
 * 使用用时加载方法，不需要启动的时候主动加载
 */
@Slf4j
@Component
public class ServiceForPeerClientMgr {
    @SofaReference
    private LedgerMgr ledgerMgr;

    /**
     * key为ChannelID，value为该Channel的客户端管理者
     */
    private Map<String, ChannelClientMgr> channelClientsMap = new ConcurrentHashMap<>();

    /**
     * 如果没有直接可用的，而本节点又加入了该通道，会创建并初始化一个新的返回
     * 考虑了线程安全
     * @param channelId
     * @return
     */
    public ChannelClientMgr getChannelClientMgr(String channelId)
    {
        ChannelClientMgr mgr = channelClientsMap.get(channelId);
        if(mgr==null)
        {
            return initChannelClientMgr(channelId);
        }
        else {
            return mgr;
        }
    }

    /**
     * 有二次检查避免重复加载
     * @param channelId
     * @return
     */
    synchronized private ChannelClientMgr initChannelClientMgr(String channelId)
    {
        log.info(ModuleClassification.P2PM_SFPCM_ +"MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",ServiceForPeerClientMgr.ChannelClientMgr start");
        Channel channel = ledgerMgr.queryChannel(channelId);
        if(channel==null) {
            log.error(ModuleClassification.P2PM_SFPCM_ +"TError"+"MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",ServiceForPeerClientMgr.ChannelClientMgr,无法为输入的ChannelId={}初始化ChannelClientMgr对象，因为本节点没有加入该Channel" , channelId );
            return null;
        }
        else
        {
            if (channelClientsMap.get(channelId) != null) {
                return channelClientsMap.get(channelId);
            }
            ChannelClientMgr mgr = null;
            try {
                mgr = new ChannelClientMgr();
                mgr.init(channel);
                log.info(ModuleClassification.P2PM_SFPCM_ +"MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",ServiceForPeerClientMgr初始化一个Channel(channelId={})完毕",channelId);
                channelClientsMap.put(channel.getChannelId(),mgr);
            }
            catch(Exception ex)
            {//尽力而为策略
                log.error(ModuleClassification.P2PM_SFPCM_ +"TError"+"MODULE="+ LogModuleCodes.SYSTEM_PLANTFORM_ACTION+",为channel初始化ChannelClientMgr出现异常,channel={},error={}",channel.toString(),ex);
            }
            return mgr;
        }
    }

    public void removeChannelClientsMap(String channelId)
    {
        channelClientsMap.remove(channelId);
    }

    /**
     * 更新通道客户端
     * @param channel
     */
    synchronized  public void updateChannelClientsMap(Channel channel)
    {
        channelClientsMap.remove(channel.getChannelId());

        ChannelClientMgr mgr = null;
        try {
            mgr = new ChannelClientMgr();
            mgr.init(channel);
            channelClientsMap.put(channel.getChannelId(),mgr);
        }
        catch (Exception e)
        {

        }

    }
}
