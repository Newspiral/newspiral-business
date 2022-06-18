package com.jinninghui.newspiral.ledger.mgr.impl.persist;

import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.common.persist.PersistActionInterface;
import com.jinninghui.newspiral.ledger.mgr.impl.domain.ChannelModel;
import com.jinninghui.newspiral.ledger.mgr.impl.mapper.ChannelModelMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChannelAction implements PersistActionInterface<Channel> {

    @Autowired
    private ChannelModelMapper channelMapper;


    @Override
    public void doAdd(Channel newChannel) {

    }

    @Override
    public void doRemove(Channel newChannel) {

    }

    @Override
    public void doModify(Channel newChannel) {
        ChannelModel newChannelModel = new ChannelModel();
        newChannelModel.setChannelId(newChannel.getChannelId());
        log.info("class_name:{}",newChannel.getChannelChange().getActionData().getClass().getName());
        if(newChannel.getChannelChange().getActionData()  instanceof Integer)
        {
            newChannelModel.setBlockMaxSize(((Integer) newChannel.getChannelChange().getActionData()).longValue());
        }
        else {
            newChannelModel.setBlockMaxSize((Long) newChannel.getChannelChange().getActionData());
        }
        //修改通道的最大区块size
        channelMapper.updateByPrimaryKeySelective(newChannelModel);
    }

    @Override
    public void doRest(Channel newChannel) {

    }
}
