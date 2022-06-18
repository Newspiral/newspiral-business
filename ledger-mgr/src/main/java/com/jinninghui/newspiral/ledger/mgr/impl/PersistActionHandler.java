package com.jinninghui.newspiral.ledger.mgr.impl;

import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.common.persist.Action;
import com.jinninghui.newspiral.common.entity.common.persist.PersistConstant;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.StateModel;
import com.jinninghui.newspiral.ledger.mgr.impl.persist.ChannelAction;
import com.jinninghui.newspiral.ledger.mgr.impl.persist.ContractAction;
import com.jinninghui.newspiral.ledger.mgr.impl.persist.MemberAction;
import com.jinninghui.newspiral.ledger.mgr.impl.persist.PeerAction;
import com.jinninghui.newspiral.ledger.mgr.impl.persist.PeerStateAction;
import com.jinninghui.newspiral.ledger.mgr.impl.persist.RoleAction;
import com.jinninghui.newspiral.ledger.mgr.impl.persist.WorldStateAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.jinninghui.newspiral.common.entity.common.persist.PersistConstant.PersistTarget.*;

/**
 * 持久化处理器
 *
 * @param <K> 动作枚举值
 */
@Slf4j
@Component
public class PersistActionHandler<K> {

    @Autowired
    private PeerAction peerStrategy;

    @Autowired
    private RoleAction roleStrategy;

    @Autowired
    private MemberAction memberStrategy;

    @Autowired
    private ContractAction contractStrategy;

    @Autowired
    private WorldStateAction stateStrategy;

    @Autowired
    private ChannelAction channelStrategy;

    @Autowired
    private PeerStateAction peerStateStrategy;


    private Map<K, Action> map = new HashMap<>();


    /**
     * 当调用构造方法时，调用初始化方法
     */
    public PersistActionHandler() {
        init();
    }

    /**
     * 放置持久化动作到map中
     *
     * @param key    actionEnum
     * @param action 不同的动作
     * @return PersistActionHandler<K>
     */
    private PersistActionHandler<K> putAction(K key, Action action) {
        this.map.put(key, action);
        return this;
    }

    /**
     * 初始化时，将枚举值和对应的动作的映射维护至map中
     */
    private void init() {
        this.putAction((K) peerAdd, (channel) -> peerStrategy.doAdd((Channel) channel)).
                putAction((K) peerRemove, (channel) -> peerStrategy.doRemove((Channel) channel)).
                putAction((K) peerModify, (channel) -> peerStrategy.doModify((Channel) channel)).
                putAction((K) roleAdd, (channel) -> roleStrategy.doAdd((Channel) channel)).
                putAction((K) roleModify, (channel) -> roleStrategy.doModify((Channel) channel)).
                putAction((K) memberAdd, (channel) -> memberStrategy.doAdd((Channel) channel)).
                putAction((K) memberModify, (channel) -> memberStrategy.doModify((Channel) channel)).
                putAction((K) contractAdd, (channel) -> contractStrategy.doAdd((Channel) channel)).
                putAction((K) stateAdd, (stateModel) -> stateStrategy.doAdd((StateModel) stateModel)).
                putAction((K) stateRemove, (stateModel) -> stateStrategy.doRemove((StateModel) stateModel)).
                putAction((K) stateModify, (stateModel) -> stateStrategy.doModify((StateModel) stateModel)).
                putAction((K) stateRest, (stateModel) -> stateStrategy.doRest((StateModel) stateModel)).
                putAction((K) channelBlockMaxSizeModify,(channel)->channelStrategy.doModify((Channel) channel)).
                putAction((K) peerFrozen,(channel)->peerStateStrategy.doAdd((Channel) channel));
    }



    /**
     * 入口方法
     *
     * @param key 标签
     * @param obj
     */
    public void doPersist(PersistConstant.PersistTarget key, Object obj) {
        if (map.containsKey(key)) {
            map.get(key).invoke(obj);
            log.debug("try to persist tag:{},actionData:{}", key, obj);
        }
    }
}
