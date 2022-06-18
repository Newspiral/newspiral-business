package com.jinninghui.newspiral.member.mgr.impl;

import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.p2p.P2pClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @version V1.0
 * @Title: AsynMemberTask
 * @Package com.jinninghui.newspiral.member.mgr.impl
 * @Description:
 * @author: xuxm
 * @date: 2020/7/28 15:25
 */
@Slf4j
@Component
public class AsynMemberTask {

    @SofaReference
    private P2pClient p2pClient;


}
