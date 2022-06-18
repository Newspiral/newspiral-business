package com.jinninghui.newspiral.ledger.mgr.impl.persist;

import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.common.persist.PersistActionInterface;
import com.jinninghui.newspiral.common.entity.member.Role;
import com.jinninghui.newspiral.ledger.mgr.impl.MemberLedgerMgrImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RoleAction implements PersistActionInterface<Channel> {

    @Autowired
    private MemberLedgerMgrImpl memberMgr;

    @Override
    public void doAdd(Channel newChannel) {
        Role role = (Role) newChannel.getChannelChange().getActionData();
        memberMgr.insertCustomRole(role);
    }

    @Override
    public void doRemove(Channel newChannel) {

    }

    @Override
    public void doModify(Channel newChannel) {
        Role role = (Role) newChannel.getChannelChange().getActionData();
        memberMgr.updateCustomRole(role);
    }

    @Override
    public void doRest(Channel newChannel) {

    }

}
