package com.jinninghui.newspiral.common.entity.chain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @version V1.0
 * @Title: PeerCertificate
 * @Package com.jinninghui.newspiral.common.entity.chain
 * @Description:
 * @author: xuxm
 * @date: 2020/4/14 11:28
 */
@Data
public class PeerCertificate {

    /**
     * 节点信息
     */
    private Peer peer;

    /**
     * 证书记录
     */
    private List<PeerCert> peerCert =new ArrayList<>();
}
