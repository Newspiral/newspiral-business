package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import com.jinninghui.newspiral.common.entity.ddos.IpConstraintList;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@ToString
public class IpConstraintListModel {

    /**
     * 自增主键
     */
    @NotNull
    private Integer id;

    /**
     * ip地址
     */
    @NotNull
    private String ipAddr;

    /**
     * 禁止该ip访问的类名，该类名下的所有接口禁止访问
     */
    @NotNull
    private String constraintType;

    /**
     * 黑名单是否生效，1为生效，0为不生效，默认为1
     */
    private String active;

    /**
     * 补充信息，用于说明该ip地址的额外信息
     */
    private String Remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    public static IpConstraintListModel createInsantce(IpConstraintList ipConstraintList){
        IpConstraintListModel ipConstraintListModel = new IpConstraintListModel();
        ipConstraintListModel.setIpAddr(ipConstraintList.getIpAddr());
        ipConstraintListModel.setConstraintType(ipConstraintList.getConstraintType());
        ipConstraintListModel.setActive(ipConstraintList.getActive());
        ipConstraintListModel.setRemark(ipConstraintList.getRemark());
        ipConstraintListModel.setCreateTime(ipConstraintList.getCreateTime()!=null?new Date(ipConstraintList.getCreateTime()):null);
        ipConstraintListModel.setUpdateTime(ipConstraintList.getUpdateTime()!=null?new Date(ipConstraintList.getUpdateTime()):null);
        return ipConstraintListModel;
    }

}

