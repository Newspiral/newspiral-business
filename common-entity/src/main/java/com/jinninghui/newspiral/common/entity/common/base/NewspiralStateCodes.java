package com.jinninghui.newspiral.common.entity.common.base;

/**
 * @version V1.0
 * @Title: NewspiralStateCodes
 * @Package com.jinninghui.newspiral.gateway.base
 * @Description:
 * @author: xuxm
 * @date: 2019/12/6 10:41
 */
public enum NewspiralStateCodes {
    SUCCESS("100", "Success"),
    SYSTEM_PERMISSION_DENIED("1000", "Permission denied"),
    SYSTEM_SIGNATURE_ERROR("1001", "Signature error"),
    SYSTEM_PARAM_ERROR("1002", "System param error"),
    SYSTEM_INVALID_REQUEST_PARAM("1003", "Invalid request param"),
    SYSTEM_ERROR("1004", "System error"),
    SYSTEM_DB_ERROR("1005", "DB error"),
    SYSTEM_RATE_LIMIT_REACHED("1006", "Rate limit reached"),
    SYSTEM_RESPONSE_NO_RECORD("1007", "Response no record"),
    SYSTEM_CHANNEL_NOT_EXIST("1008", "Response channel not exist"),
    SYSTEM_IDENTITY_NOT_EXIST("1009", "Response identity not exist"),
    SYSTEM_PEER_NOT_EXIST("1010", "Response peer not exist"),
    SYSTEM_PEER_SDKURL_ERROR("1011", "Response peer sdkurl error"),
    SYSTEM_PEER_USER_PRIVATEKEY_NOT_EXIST("1012", "Response peer privatekey not exist"),
    SYSTEM_BUSINESS_SMARTCONTACT_NOT_EXIST("1013", "Response smartcontract not exist"),
    SYSTEM_BUSINESS_SMARTCONTACT_FROZEN("1014", "Response smartcontract frozen"),
    SYSTEM_BUSINESS_SMARTCONTACT_DESTORY("1015", "Response smartcontract destory"),
    SYSTEM_BUSINESS_PEERCERT_INVALID("1016", "Response peer cert is not valid"),
    SYSTEM_PEER_STOP("1017", "Response peer is stop"),
    SYSTEM_DELELTCHANNELPEER_NOT_ONLY_ONE("1018", "Response peer is not only one"),
    SYSTEM_PEER_IN_CHANNEL("1019", "Response peer is still in channel "),
    SYSTEM_SMARTCONTRACT_NO_AUTHORIZED("1020", "Response smartcontract is not authorized"),
    SYSTEM_SMARTCONTRACT_EXECUTE_ERROR("1021", "Response smartcontract execute error"),
    SYSTEM_ANTI_REPLAY_ERROR("1022","Anti replay verify failed!"),
    SYSTEM_AUTHENTICATION_ERROR("1023","Identity authentication failed"),
    SYSTEM_PEER_NOT_IN_CHANNEL("1024", "Response peer not in channel"),
    SYSTEM_UNSTRAT_CHECKING("1025","The height has not yet started to be verified, please verify first, and then query"),
    SYSTEM_START_CHECKING("1026", "Start verification this height"),
    SYSTEM_IN_CHECKING("1027", "Checking the height of this channel,please wait a minute"),
    SYSTEM_COMPLETE_CHECKING("1028", "Accomplish verification, the data is correct"),
    SYSTEM_ERROR_IN_CHECKING("1029", "Accomplish verification, the data is wrong"),
    SYSTEM_UPTO_MAX_PROCESSORNUM("1030", "Up to the max check number"),
    SYSTEM_SHARDING_REFRESH_ERROR("1031","sharding config refresh error"),
    SYSTEM_SHARDING_ADD_CONFIG_ERROR("1032","system_sharding_add_config_error"),
    SYSTEM_MODIFY_LIMIT_PARAM_ERROR("1033","modify limit param error"),
    SYSTEM_ADD_IP_INTO_IP_CONSTRAINT_LIST_ERROR("1034","add ip into ipConstraintList error"),
    SYSTEM_MODIFY_IP_CONSTRAINT_LIST_ERROR("1035","modify ipConstraintList error"),
    SYSTEM_PEER_NOTNORMAL("1036","peer is not normal"),
    SYSTEM_TRANSACTION_POOL_MAXIMUM("1037","up to maximum of transaction pool"),
    SYSTEM_TRANSACTION_DATA_MODIFIED("1038","transaction data may be modified"),
    SYSTEM_TRANSACTION_DATA_REPEAT("1039","transaction  may be repeat");




    private final String code;
    private final String msg;

    private NewspiralStateCodes(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }
}
