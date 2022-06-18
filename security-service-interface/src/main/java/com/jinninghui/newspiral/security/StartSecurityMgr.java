package com.jinninghui.newspiral.security;

/**
 * @version V1.0
 * @Title: StartSecurityMgr
 * @Package com.jinninghui.newspiral.security
 * @Description:peer项目是否有权限启动方法
 * @author: xuxm
 * @date: 2019/11/8 14:21
 */
public interface StartSecurityMgr {

    void init();
    /**
     * 是否可以正常启动（配置文件做法，已经暂停使用）
     * @return
     */
    boolean getStarSecurityFlag();

    /**
     * 证书做法
     * @return
     */
    boolean initVerifyCertificateValidity(String channelId);

    /**
     * 国密证书初始化
     * @return
     */
    boolean initVerifyGMCertificateValidity(String channelId);

    /**
     * 检查所有证书的有效性
     */
    void checkAllCertificateValidity();
}
