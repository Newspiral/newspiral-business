package com.jinninghui.newspiral.common.entity.exception;

import com.alibaba.fastjson.JSON;

import static com.jinninghui.newspiral.common.entity.config.ConfigConstant.blockHeightBuffer;

/**
 * @author lida
 * @date 2019/7/11 17:50
 *  错误码，用于构造异常
 */
public enum NewSpiralErrorEnum {

    CHANNEL_NOT_EXIST("CHANNEL_NOT_EIXST","链不存在"),
    UN_IMPLEMENTED("UN_IMPLEMENTED", "方法尚未实现"),
    PERMISSION_DENIED("PERMISSION_DENIED","权限不足"),
    INVALID_VERIFICATION("INVALID_VERIFICATION","验签失败"),
    PEER_ADD_TO_CHANNEL_FAILED("PEER_ADD_TO_CHANNEL_FAILED","节点加入通道失败"),
    PEER_ALREDAY_IN_CHANNEL("PEER_ALREDAY_IN_CHANNEL","节点已经在通道中,不需重复加入"),
    PROCESS_ERROR("PROCESS_ERROR","处理错误"),
    TRANSACTION_POOL_FULL("TRANSACTION_POOL_FULL","交易池已经满了"),
    SMART_CONTRACT_LOAD_ERROR("SMART_CONTRACT_LOAD_ERROR", "智能合约加载出错"),
    SMART_CONTRACT_NOT_EXIST("SMART_CONTRACT_NOT_EXIST","智能合约不存在"),
    SMART_CONTRACT_ALREADY_EXIST("SMART_CONTRACT_ALREADY_EXIST","智能合约已经存在"),
    SMART_CONTRACT_UPGRADE_VERSION_EXIST("SMART_CONTRACT_ALREADY_EXIST","智能合约升级时版本号不能一致"),
    SMART_CONTRACT_UPGRADE_NOEXIST("SMART_CONTRACT_UPGRADE_NOEXIST","智能合约升级时该合约不存在通道中，请先安装"),
    SMART_CONTRACT_UPGRADE_ALISA_NOTEXIST("SMART_CONTRACT_UPGRADE_ALISA_NOTEXIST","智能合约升级时别名不一致"),
    SMART_CONTRACT_FLAG_NOCHANGE("SMART_CONTRACT_FLAG_NOCHANGE","智能合约状态没有改变"),
    SMART_CONTRACT_FLAG_DESTORY("SMART_CONTRACT_FLAG_DESTORY","智能合约已经销毁，不能进行此操作"),
    SMART_CONTRACT_FLAG_ERROR("SMART_CONTRACT_FLAG_ERROR","智能合约的状态有误，不能进行此操作"),
    SMART_CONTRACT_FLAG_PARAMETER_ERROR("SMART_CONTRACT_FLAG_PARAMETER_ERROR","智能合约的状态修改的参数有误"),
    PEER_CERTIFICATE_FLAG_ERROR("PEER_CERTIFICATE_FLAG_ERROR","证书的状态有误，不能进行此操作"),
    PEER_CERTIFICATE_IDENTITY_ERROR("PEER_CERTIFICATE_IDENTITY_ERROR","节点证书的身份有误，不能进行此操作"),
    PEER_NOT_EXIST("PEER_NOT_EXIST","所要进行操作的节点不存在"),
    PEER_CERTIFICATE_FLAG_NOCHANGE("PEER_CERTIFICATE_FLAG_NOCHANGE","节点证书状态无变化，非法操作"),
    PEER_CERTIFICATE_REVOKE_NORECOVERY("PEER_CERTIFICATE_REVOKE_NORECOVERY","节点证书吊销后不可以恢复"),
    PEER_CERTIFICATE_REVOKE_NOFROZEN("PEER_CERTIFICATE_REVOKE_NOFROZEN","节点证书吊销后不可以冻结"),
    PEER_CERTIFICATE_REPEAT("PEER_CERTIFICATE_REPEAT","节点证书重复"),
    INVALID_PARAM("INVALID_PARAM","参数非法"),
    INVALID_BASIC_CONFIG("INVALID_BASIC_CONFIG","基础配置非法"),
    INVALID_STATE_ACCESS_MODE("INVALID_STATE_ACCESS_MODE","状态存取模式非法"),
    INVALID_EXECUTION_ENVIRONMENT("INVALID_EXECUTION_ENVIRONMENT", "交易执行环境非法"),
    QUERY_TOO_LONG_TIME_INTERVAL("QUERY_TOO_LONG_TIME_INTERVAL","单次查询时间间隔过大，请缩小时间间隔至一天内"),
    QUERY_TOO_BIG_DATA("QUERY_TOO_BIG_DATA","单次查询数据量过大，请缩小时间间隔"),
    SMART_CONTRACT_NAMESPACE_MISMATCH("SMART_CONTRACT_NAMESPACE_MISMATCH", "智能合约命名空间不匹配或者内存中已经存在该命名空间的智能合约"),
    QUERY_BEYOND_TIME_INTERVAL("QUERY_BEYOND_TIME_INTERVAL","查询时间范围超出限制，请核实后重新输入"),
    //校验数据用
    ERROR_IN_CHECKING("ERROR_IN_CHECKING","查询出错"),
    OLD_HEIGHT_CHECK_ERROR("OLD_HEIGHT_CHECK_ERROR", "当前第一个库的剩余容量不足" + blockHeightBuffer + "，如果没有数据迁移计划，不允许修改范围参数，扩容请直接加数据源!"),
    NEW_HEIGHT_CHECK_ERROR("NEW_HEIGHT_CHECK_ERROR", "修改后，剩余容量依然不足，请继续调大范围参数"),
    NEW_TOTAL_CHECK_ERROR("NEW_TOTAL_CHECK_ERROR", "修改后，总容量没有修改前的多，请重新审视分片参数"),
    NOT_ENOUGH_DATABASE("NOT_ENOUGH_DATABASE_IN_CONFIG","配置中没有足够多的数据源能使用"),
    DATA_ALREADY_EXISTS_IN_DB("DATA_ALREADY_EXISTS_IN_DB","该数据库中已经存在有数据，不允许修改！"),
    ERROR_IN_BUSINESS_CONTRACT("ERROR_IN_BUSINESS_CONTRACT","业务合约执行出错"),
    ERROR_RUN_BUSINESS_CONTRACT("ERROR_IN_BUSINESS_CONTRACT","业务合约不存在或已被吊销，不可执行"),
    ERROR_STOP_BUSINESS_CONTRACT("ERROR_STOP_BUSINESS_CONTRACT","强制终止业务合约失败"),
    DONT_OBTAIN_TOKEN("DONT_OBTAIN_TOKEN","未获得令牌"),
    IP_HAVE_NO_ACCESS("IP_HAVE_NO_ACCESS","ip没有访问权限"),
    IP_PATTERN_ERROR("IP_PATTERN_ERROR","ip不符合规范"),
    PARAM_NOT_IN_VALUE_RANGE("PARAM_NOT_IN_VALUE_RANGE","参数取值不在规定范围内");


    public final String code;
    public final String message;
    NewSpiralErrorEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
    public String getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }


    public static String getMessage(String code) {
        NewSpiralErrorEnum[] newSpiralErrorEnums = values();
        for (NewSpiralErrorEnum newSpiralErrorEnum : newSpiralErrorEnums) {
            if (newSpiralErrorEnum.code.equals(code)) {
                return newSpiralErrorEnum.message;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
