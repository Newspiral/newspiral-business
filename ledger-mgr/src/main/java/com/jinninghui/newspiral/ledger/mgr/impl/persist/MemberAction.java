package com.jinninghui.newspiral.ledger.mgr.impl.persist;

import com.jinninghui.newspiral.common.entity.chain.Channel;
import com.jinninghui.newspiral.common.entity.common.persist.PersistActionInterface;
import com.jinninghui.newspiral.common.entity.member.Member;
import com.jinninghui.newspiral.ledger.mgr.impl.MemberLedgerMgrImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MemberAction  implements PersistActionInterface<Channel> {

    @Autowired
    private MemberLedgerMgrImpl memberMgr;




    @Override
    public void doAdd(Channel newChannel) {
        Member member = (Member) newChannel.getChannelChange().getActionData();
        memberMgr.insertMember(member);
    }

    @Override
    public void doRemove(Channel newChannel) {

    }

    @Override
    public void doModify(Channel newChannel) {
        Member member = (Member) newChannel.getChannelChange().getActionData();
        memberMgr.updateMember(member);
    }

    @Override
    public void doRest(Channel newChannel) {

    }

}
