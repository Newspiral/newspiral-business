package com.jinninghui.newspiral.common.entity.config;

public interface ConfigConstant {

    /**
     * 数据源前缀
     */
    String TYPE_PREFIX = "sharding.";

    /**
     * 数据源模块
     */
    String DATASOURCE_MODULE = "datasource.";


    String ORCHESTRATION_SHARDING_CONFIG_PREFIX="orchestration-sharding-config";

    String ORCHESTRATION_SHARDING_REGISTER_PREFIX="orchestration-sharding-register";
    /**
     * 数据源模板的key
     */
    String DATASOURCE_TEMPLATE_KEY = "/" + ORCHESTRATION_SHARDING_CONFIG_PREFIX + "/config/schema/logic_db/datasource";

    /**
     * 分片规则模板的key
     */
    String RULE_TEMPLATE_KEY = "/" + ORCHESTRATION_SHARDING_CONFIG_PREFIX + "/config/schema/logic_db/rule";


    String TABLE_TEMPLATE_KEY = "sharding.tableStrategy";

    /**
     * 分表数据源名称
     */
    String SHARDING_DATA_SOURCE = "sharding";


    /**
     * 逗号分隔符
     */
    String SPLIT_COMMA = ",";
    /**
     * 本地注册中心类型
     */
    String LOCAL_REGISTRY_TYPE = "shardingLocalRegisterCenter";

    /**
     * 本地配置中心类型
     */
    String LOCAL_CONFIG_TYPE = "shardingLocalConfigCenter";

    /**
     * local_config中sharding类型配置 所属的类型名
     */
    String SHARDING_CONFIG_TYPE = "shardingsphere";


    String SHARDING_DATASOURCE_NAMES = "sharding.datasource.names";


    String SHARDING_RULE_ACTUALDATANODES = "sharding.rule.#{tableName}.actualDataNodes";


    String SHARDING_RULE_TABLE_NAMES = "sharding.rule.table.names";


    /**
     * 分片字段
     */
    String SHARDING_RULE_DATABASESTRATEGY_SHARDINGCOLUMN = "sharding.rule.databaseStrategy.shardingColumn";


    /**
     * 分片表达式
     */
    String SHARDING_RULE_DATABASESTRATEGY_ALGORITHMEXPRESSION = "sharding.rule.databaseStrategy.algorithmExpression";


    /**
     * 分片里的表策略
     */
    String SHARDING_TABLESTRATEGY = "sharding.tableStrategy";

    /**
     * local_config中是否已启用配置的key
     */
    String SHARDING_CONFIG_ISACTIVE = "sharding.config.isActive";


    String SHARDING_DB_ACTIVE_NUM = "sharding.dbActiveNum";


    String SHARDING_RANGE = "sharding.range";


    String SHARDING_DB_INDEX = "sharding.dbindex";

    /**
     * sharding配置未启用
     */
    boolean NO_ACTIVE = false;

    /**
     * sharding配置已启用
     */
    boolean ACTIVE = true;


    /**
     * 冗余的块高度
     */
    int blockHeightBuffer = 50;

    String DRIVER_CLASS_NAME="com.mysql.jdbc.Driver";

    String JDBC_URL_TEMPLATE = "jdbc:mysql://#{ip}:#{port}/#{dbName}?characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowMultiQueries=true";

    String DS_TYPE_TEMPLATE_KEY = "sharding.datasource.ds#{i}.type";

    String DS_POOLNAME_TEMPLATE_KEY = "sharding.datasource.ds#{i}.poolName";

    String DS_JDBC_URL_TEMPLATE_KEY = "sharding.datasource.ds#{i}.jdbc-url";

    String DS_DRIVER_TEMPLATE_KEY = "sharding.datasource.ds#{i}.driver-class-name";

    String DS_USERNAME_TEMPLATE_KEY = "sharding.datasource.ds#{i}.username";

    String DS_PASSWORD_TEMPLATE_KEY = "sharding.datasource.ds#{i}.password";
}
