package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import com.alibaba.fastjson.JSON;
import com.jinninghui.newspiral.common.entity.chain.PeerOrganization;
import com.jinninghui.newspiral.common.entity.member.Member;
import lombok.Data;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;

/**
 * @version V1.0
 * @Title: MemberModel
 * @Package com.jinninghui.newspiral.ledger.mgr.impl.domain
 * @Description:
 * @author: xuxm
 * @date: 2020/1/10 11:11
 */
@Data
public class MemberModel {
    /**
     * 主键
     */
    private Long id;

    /**
     * 数字证书的公钥
     */
    private String publicKey;
    /**
     * 名称
     */
    private String name;
    /**
     * 签发者ID
     */
    private String issuerId;
    /**
     * 扩展属性
     */
    private String extendedData;
    /**
     * 通道编号
     */
    private String channelId;
    /**
     * 证书
     */
    private String certificateCerFile;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 状态0正常，1证书过期，2业务冻结，3业务删除
     */
    private Integer status;

    private String signAlgorithm;

    /**
     *
     * @return
     */
    public Member toMember()
    {
        Member member=new Member();
        member.setId(this.getId());
        member.setPublicKey(this.getPublicKey());
        member.setName(this.getName());
        member.setIssuerId(this.getIssuerId());
        member.setChannelId(this.getChannelId());
        member.setCertificateCerFile(this.certificateCerFile);
        member.setStatus(this.status);
        if(!StringUtils.isEmpty(this.getExtendedData())) {
            member.setExtendedData(JSON.parseObject(this.getExtendedData(),Map.class) );
        }
        member.setSignAlgorithm(this.signAlgorithm);
        member.setCreateTime(this.createTime!=null?this.createTime.getTime():null);
        member.setUpdateTime(this.updateTime!=null?this.updateTime.getTime():null);
        return member;
    }

    /**
     *
     * @param member
     * @return
     */
    public static MemberModel createInstance(Member member)
    {
        MemberModel memberModel=new MemberModel();
        memberModel.setId(member.getId());
        memberModel.setPublicKey(member.getPublicKey());
        memberModel.setName(member.getName());
        memberModel.setIssuerId(member.getIssuerId());
        memberModel.setChannelId(member.getChannelId());
/*        String str = "";
        try {
            str = new String(member.getCertificateCerFile(), "UTF-8");
        } catch (Exception ex) {

        }*/
        memberModel.setCertificateCerFile(member.getCertificateCerFile());
        if(!CollectionUtils.isEmpty(member.getExtendedData())) {
            memberModel.setExtendedData(JSON.toJSONString(member.getExtendedData()));
        }
        //memberModel.setCreateTime(new Date());
        //memberModel.setUpdateTime(new Date());
        memberModel.setStatus(member.getStatus());
        memberModel.setSignAlgorithm(member.getSignAlgorithm());
        return memberModel;
    }

    public static MemberModel createOrganizationInstance(PeerOrganization member)
    {
        MemberModel memberModel=new MemberModel();
        memberModel.setPublicKey(member.getPublicKey());
        memberModel.setName(member.getOrganizationName());
        memberModel.setIssuerId(member.getOrganizationId());
        memberModel.setChannelId("");
        memberModel.setCertificateCerFile(member.getCertificateCerFile());
        memberModel.setCreateTime(new Date());
        memberModel.setUpdateTime(new Date());
        memberModel.setStatus(0);
        return memberModel;
    }

    public PeerOrganization toPeerOrganization()
    {
        PeerOrganization member=new PeerOrganization();
        member.setPublicKey(this.getPublicKey());
        member.setOrganizationName(this.getName());
        member.setOrganizationId(this.getIssuerId());
        member.setCertificateCerFile(this.certificateCerFile);


        return member;
    }
}
