package com.jinninghui.newspiral.security;

/**
 * @author lida
 * @date 2019/7/13 17:44
 * 安全服务管理者，当一个安全服务实现模块实现了多组安全服务接口时（例如一个模块同时实现了国密和国际密码算法）
 * 则每组实现均有一个唯一的key来确定对应的安全服务具体接口实现组，安全服务管理者用于管理这种关系
 * key值的合法范围与具体实现模块相关，每个链可以配置使用的安全服务接口组
 */
public interface SecurityServiceMgr {
    /**
     * 需保证返回的所有Security
     * @param key
     * @return
     */
    SecurityService getMatchSecurityService(String key);

    /**
     * 完成该Mgr管理的所有Security服务的初始化，框架会在初始化Bean的时候调用此方法
     * 外部如果使用其他方式，则应保证
     */
    void init();
}
