/*
    sharding分支增量sql
    默认5000范围，2个分库：zone1和zone2
    可在默认的newspiral库执行，但需将application.properties中jdbcUrl的链接改为newspiral库的链接。
*/


-- 主库，是否要改名个人决定，不改名就要把配置文件中的连接改为 newspiral，改名需将数据库名改为newspiral_cluster

alter table block change `hash` BLOCK_HASH varchar(64) not null ;

alter table block_cache change `hash` BLOCK_HASH varchar(64) not null ;

alter table transaction add column `BLOCK_ID` bigint(20) not null after `VERSION`;

alter table transaction_cache add column `BLOCK_ID` bigint(20) not null after `VERSION`;

INSERT INTO `interface` (`ID`, `INTERFACE_NAME`, `CLASS_PATH`, `CLASS_NAME`, `METHOD_NAME`, `METHOD_ARGS`, `REMARK`,
                         `CREATE_TIME`, `UPDATE_TIME`)
values (96, '查询sharding配置接口', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'queryShardingConfig',
        'RPCParam', NULL, '2021-01-15 00:48:11', '2021-01-15 00:48:11'),
       (97, '修改sharding的分库规则配置接口', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'modifySharidngRuleConfig',
        'ShardingRuleReq', NULL, '2021-01-15 00:48:11', '2021-01-15 00:48:11'),
       (98, '修改sharding的数据源配置接口', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl','modifyShardingDataSourceConfig',
        'ModifyDataSourceConfigReq', NULL, '2021-01-15 00:48:11', '2021-01-15 00:48:11'),
       (99, '新增sharding的数据源配置接口', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl','addShardingDataSourceConfig',
        'AddDataSourceConfigReq', NULL, '2021-01-15 00:48:11', '2021-01-15 00:48:11'),
       (100, '启用sharding配置接口', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'refreshShardingConfig',
        'RPCParam', NULL, '2021-01-15 00:48:11', '2021-01-15 00:48:11');

update auth set INTERFACE_ID=concat(INTERFACE_ID,',96,97,98,99,100') where AUTH_NAME='节点监控';


DROP TABLE IF EXISTS `local_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `local_config`
(
    `id`           int(11) NOT NULL AUTO_INCREMENT,
    `key`          varchar(100) NOT NULL,
    `value`        varchar(2000)         DEFAULT NULL,
    `show`         tinyint(1) NOT NULL DEFAULT '0',
    `type`         varchar(32)  NOT NULL,
    `updated_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `created_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `sharding_config_key_uindex` (`key`)
) ENGINE=InnoDB AUTO_INCREMENT=936 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `local_config`
--

LOCK
TABLES `local_config` WRITE;
/*!40000 ALTER TABLE `local_config` DISABLE KEYS */;
INSERT INTO `local_config` (`id`, `key`, `value`, `show`, `type`, `updated_time`, `created_time`)
VALUES (0, 'sharding.config.isActive', 'true', 1, 'shardingsphere', '2021-01-15 10:06:31', '2021-01-12 07:57:56'),
       (100, 'sharding.datasource.names', 'ds0,ds1', 1, 'shardingsphere', '2021-01-15 10:04:17',
        '2021-01-07 02:55:01'),
       (101, 'sharding.datasource.ds0.type', 'com.zaxxer.hikari.HikariDataSource', 0, 'shardingsphere',
        '2021-01-12 08:46:34', '2021-01-07 02:58:18'),
       (102, 'sharding.datasource.ds0.poolName', 'HikariPool-ds0', 0, 'shardingsphere', '2021-01-13 10:18:51',
        '2021-01-12 08:44:14'),
       (103, 'sharding.datasource.ds0.driver-class-name', 'com.mysql.jdbc.Driver', 0, 'shardingsphere',
        '2021-01-12 08:46:34', '2021-01-07 03:00:20'),
       (104, 'sharding.datasource.ds0.jdbc-url',
        'jdbc:mysql://127.0.0.1:3306/newspiral_zone1?characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowMultiQueries=true',
        1, 'shardingsphere', '2021-01-14 05:23:45', '2021-01-07 03:00:20'),
       (105, 'sharding.datasource.ds0.username', 'root', 0, 'shardingsphere', '2021-01-12 08:46:34',
        '2021-01-07 03:00:20'),
       (106, 'sharding.datasource.ds0.password', 'Newspiral@2019', 0, 'shardingsphere', '2021-01-12 08:46:34',
        '2021-01-07 03:00:20'),
       (107, 'sharding.datasource.ds1.type', 'com.zaxxer.hikari.HikariDataSource', 0, 'shardingsphere',
        '2021-01-12 08:46:34', '2021-01-07 02:58:18'),
       (108, 'sharding.datasource.ds1.poolName', 'HikariPool-ds1', 0, 'shardingsphere', '2021-01-13 10:18:51',
        '2021-01-12 08:44:39'),
       (109, 'sharding.datasource.ds1.driver-class-name', 'com.mysql.jdbc.Driver', 0, 'shardingsphere',
        '2021-01-12 08:46:34', '2021-01-07 03:01:30'),
       (110, 'sharding.datasource.ds1.jdbc-url',
        'jdbc:mysql://127.0.0.1:3306/newspiral_zone2?characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowMultiQueries=true',
        1, 'shardingsphere', '2021-01-15 10:27:41', '2021-01-07 03:01:30'),
       (111, 'sharding.datasource.ds1.username', 'root', 0, 'shardingsphere', '2021-01-12 08:46:34',
        '2021-01-07 03:01:30'),
       (112, 'sharding.datasource.ds1.password', 'Newspiral@2019', 0, 'shardingsphere', '2021-01-12 08:46:34',
        '2021-01-07 03:01:30'),
       (400, 'sharding.rule.block.actualDataNodes', 'ds${0..#{sharding.dbindex}}.block', 0, 'shardingsphere',
        '2021-01-14 09:08:48', '2021-01-07 03:09:09'),
       (401, 'sharding.rule.transaction.actualDataNodes', 'ds${0..#{sharding.dbindex}}.transaction', 0,
        'shardingsphere', '2021-01-14 09:08:48', '2021-01-07 03:15:01'),
       (402, 'sharding.rule.table.names', 'block,transaction', 0, 'shardingsphere', '2021-01-12 09:23:45',
        '2021-01-07 07:42:50'),
       (403, 'sharding.rule.databaseStrategy.shardingColumn', 'block_id', 1, 'shardingsphere', '2021-01-14 05:23:45',
        '2021-01-07 03:09:58'),
       (404, 'sharding.rule.databaseStrategy.algorithmExpression', 'ds${block_id.intdiv(#{sharding.range})}', 0,
        'shardingsphere', '2021-01-13 10:14:13', '2021-01-07 03:10:22'),
       (900, '/orchestration-sharding-data-source/config/schema/logic_db/datasource',
        '#{dsName}: !!org.apache.shardingsphere.orchestration.yaml.config.YamlDataSourceConfiguration\r\n  dataSourceClassName: com.zaxxer.hikari.HikariDataSource\r\n  properties:\r\n    password: #{sharding.datasource.#{dsName}.password}\r\n    dataSourceJNDI: null\r\n    transactionIsolation: null\r\n    connectionTestQuery: null\r\n    initializationFailTimeout: 1\r\n    driverClassName: com.mysql.jdbc.Driver\r\n    jdbcUrl: #{sharding.datasource.#{dsName}.jdbc-url}\r\n    minimumIdle: 10\r\n    maxLifetime: 1800000\r\n    maximumPoolSize: 10\r\n    schema: null\r\n    validationTimeout: 5000\r\n    dataSourceClassName: null\r\n    connectionTimeout: 30000\r\n    idleTimeout: 600000\r\n    leakDetectionThreshold: 0\r\n    connectionInitSql: null\r\n    username: #{sharding.datasource.#{dsName}.username}\r\n    poolName: #{sharding.datasource.#{dsName}.poolName}\r\n    catalog: null',
        0, 'shardingsphere', '2021-01-13 05:25:40', '2021-01-08 03:59:44'),
       (901, '/orchestration-sharding-data-source/config/schema/logic_db/rule',
        'defaultDatabaseStrategy:\r\n  inline:\r\n    algorithmExpression: #{sharding.rule.databaseStrategy.algorithmExpression}\r\n    shardingColumn: #{sharding.rule.databaseStrategy.shardingColumn}\r\ntables:\r\n  #{shardingStrategys}',
        0, 'shardingsphere', '2021-01-12 09:09:51', '2021-01-08 04:02:17'),
       (902, 'sharding.tableStrategy',
        '#{tableName}:\r\n    actualDataNodes: #{sharding.rule.#{tableName}.actualDataNodes}\r\n    keyGenerator:\r\n      column: id\r\n      type: SNOWFLAKE\r\n    logicTable: #{tableName}',
        0, 'shardingsphere', '2021-01-13 10:11:48', '2021-01-11 02:52:34'),
       (903, 'sharding.dbActiveNum', '2', 1, 'shardingsphere', '2021-01-15 10:27:35', '2021-01-13 10:12:30'),
       (904, 'sharding.range', '5000', 1, 'shardingsphere', '2021-01-14 10:40:32', '2021-01-13 10:13:03'),
       (905, 'sharding.dbindex', '1', 0, 'shardingsphere', '2021-01-15 10:27:35', '2021-01-14 09:08:12');
/*!40000 ALTER TABLE `local_config` ENABLE KEYS */;
/*修改local_config*/
UNLOCK TABLES;
UPDATE `local_config` t SET t.`key` = '/orchestration-sharding-config/config/schema/logic_db/datasource' WHERE t.id = 900;
UPDATE `local_config` t SET t.`key` = '/orchestration-sharding-config/config/schema/logic_db/rule' WHERE t.id = 901;
UPDATE `local_config` t SET t.value = '#{dsName}: !!org.apache.shardingsphere.orchestration.core.configuration.YamlDataSourceConfiguration
                     dataSourceClassName: com.zaxxer.hikari.HikariDataSource
                     properties:
                       password: #{sharding.datasource.#{dsName}.password}
                       dataSourceJNDI: null
                       transactionIsolation: null
                       connectionTestQuery: null
                       initializationFailTimeout: 1
                       driverClassName: com.mysql.jdbc.Driver
                       jdbcUrl: #{sharding.datasource.#{dsName}.jdbc-url}
                       minimumIdle: 10
                       maxLifetime: 1800000
                       maximumPoolSize: 10
                       schema: null
                       validationTimeout: 5000
                       dataSourceClassName: null
                       connectionTimeout: 30000
                       idleTimeout: 600000
                       leakDetectionThreshold: 0
                       connectionInitSql: null
                       username: #{sharding.datasource.#{dsName}.username}
                       poolName: #{sharding.datasource.#{dsName}.poolName}
                       catalog: null' WHERE t.id = 900;
UPDATE newspiral_cluster.local_config t SET t.value = '#{dsName}: !!org.apache.shardingsphere.orchestration.core.configuration.YamlDataSourceConfiguration
                     dataSourceClassName: com.zaxxer.hikari.HikariDataSource
                     properties:
                       password: #{sharding.datasource.#{dsName}.password}
                       dataSourceJNDI: null
                       transactionIsolation: null
                       connectionTestQuery: null
                       initializationFailTimeout: 1
                       driverClassName: com.mysql.jdbc.Driver
                       jdbcUrl: #{sharding.datasource.#{dsName}.jdbc-url}
                       minimumIdle: 20
                       maxLifetime: 1800000
                       maximumPoolSize: 100
                       schema: null
                       validationTimeout: 5000
                       dataSourceClassName: null
                       connectionTimeout: 15000
                       idleTimeout: 20000
                       leakDetectionThreshold: 0
                       connectionInitSql: null
                       username: #{sharding.datasource.#{dsName}.username}
                       poolName: #{sharding.datasource.#{dsName}.poolName}
                       catalog: null' WHERE t.id = 900

-- 设置分库newspiral_zone1
/*

                        设置分库1...

*/
use mysql;
ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'Newspiral@2019';
create database newspiral_zone1;
use newspiral_zone1;

DROP TABLE IF EXISTS `block`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `block`
(
    `ID`                    bigint(20)                     NOT NULL AUTO_INCREMENT,
    `CHANNEL_ID`            varchar(64) COLLATE utf8_bin   NOT NULL COMMENT '通道ID',
    `BLOCK_ID`              bigint(20)                     NOT NULL,
    `VERSION`               varchar(45) COLLATE utf8_bin   NOT NULL,
    `block_hash`            varchar(64) COLLATE utf8_bin   NOT NULL,
    `PREV_BLOCK_HASH`       varchar(64) COLLATE utf8_bin   NOT NULL,
    `PREV_BLOCK_HEIGHT`     bigint(20)                              DEFAULT NULL,
    `BLOCK_BUILDER_ID`      varchar(1024) COLLATE utf8_bin NOT NULL COMMENT '创建者身份',
    `PACK_TIMESTAMP`        timestamp(3)                   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `BLOCK_CONSENSUS`       varchar(45) COLLATE utf8_bin   NOT NULL,
    `BLOCK_WITNESS`         text COLLATE utf8_bin          NOT NULL,
    `CONSENSUS_TIMESTAMP`   timestamp                      NULL     DEFAULT NULL,
    `PERSISTENCE_TIMESTAMP` timestamp                      NULL     DEFAULT NULL,
    PRIMARY KEY (`ID`, `CHANNEL_ID`),
    UNIQUE KEY `block_hash` (`block_hash`),
    KEY `block_block_id` (`BLOCK_ID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  COLLATE = utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;



LOCK TABLES `block` WRITE;
/*!40000 ALTER TABLE `block`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `block`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transaction`
--

DROP TABLE IF EXISTS `transaction`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `transaction`
(
    `ID`                         bigint(20)                    NOT NULL AUTO_INCREMENT,
    `CHANNEL_ID`                 varchar(64) COLLATE utf8_bin  NOT NULL COMMENT '通道ID',
    `VERSION`                    varchar(10) COLLATE utf8_bin  NOT NULL COMMENT 'ExecutedTransaction的版本号',
    `BLOCK_ID`                   bigint(20)                    NOT NULL,
    `POOLED_TRANS`               longtext COLLATE utf8_bin,
    `POOLED_TRANS_VERSION`       varchar(10) COLLATE utf8_bin           DEFAULT NULL COMMENT '冗余字段，便于查询统计',
    `ADD2_POOL_TIMESTAMP`        timestamp                     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '冗余字段，便于查询统计',
    `TRANS_HASH_STR`             varchar(128) COLLATE utf8_bin          DEFAULT NULL COMMENT '交易hash的16进制字符串',
    `SDK_TRANS_VERSION`          varchar(10) COLLATE utf8_bin           DEFAULT NULL COMMENT '冗余字段，便于查询统计',
    `CLIENT_TIMESTAMP`           timestamp                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '冗余字段，便于查询统计',
    `CLIENT_TRANS_ID`            varchar(128) COLLATE utf8_bin NOT NULL,
    `SMART_CONTRACT_ID`          varchar(128) COLLATE utf8_bin          DEFAULT NULL COMMENT '智能合约ID，含版本号',
    `SMART_CONTRACT_METHOD_NAME` varchar(128) COLLATE utf8_bin          DEFAULT NULL COMMENT '所调用的方法名',
    `CLIENT_IDENTITY_KEY`        varchar(255) COLLATE utf8_bin          DEFAULT NULL COMMENT '调用者身份',
    `MODIFIED_WORLD_STATE_LIST`  longtext COLLATE utf8_bin,
    `MODIFIED_CHANNEL_RECORD`    longtext COLLATE utf8_bin COMMENT 'Channel的修改记录，只有系统合约调用才会有此值',
    `EXECUTE_TIMESTAMP`          timestamp                     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `EXECUTED_MS`                bigint(20)                             DEFAULT NULL COMMENT '执行耗时',
    `BLOCK_HASH_STR`             varchar(128) COLLATE utf8_bin NOT NULL,
    `INDEX_IN_BLOCK`             int(10)                                DEFAULT NULL COMMENT '从1开始',
    `SUCCESSED`                  tinyint(4)                             DEFAULT NULL,
    `ERROR_MSG`                  varchar(1024) COLLATE utf8_bin         DEFAULT NULL,
    `CREATE_TIMESTAMP`           timestamp(3)                  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`ID`),
    UNIQUE KEY `transaction_trans_hash_str` (`TRANS_HASH_STR`),
    KEY `transaction_block_hash_str` (`BLOCK_HASH_STR`, `CHANNEL_ID`),
    KEY `transaction_channel_id_create_timestamp` (`CHANNEL_ID`, `CREATE_TIMESTAMP`),
    KEY `transaction_client__identity_key` (`CLIENT_IDENTITY_KEY`),
    KEY `transaction_client_trans_id` (`CLIENT_TRANS_ID`),
    KEY `transaction_index_in_block` (`INDEX_IN_BLOCK`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  COLLATE = utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transaction`
--

LOCK TABLES `transaction` WRITE;
/*!40000 ALTER TABLE `transaction`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `transaction`
    ENABLE KEYS */;
UNLOCK TABLES;



-- 设置分库newspiral_zone2
/*

                        设置分库2...

*/
use mysql;
ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'Newspiral@2019';
create database newspiral_zone2;
use newspiral_zone2;

DROP TABLE IF EXISTS `block`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `block`
(
    `ID`                    bigint(20)                     NOT NULL AUTO_INCREMENT,
    `CHANNEL_ID`            varchar(64) COLLATE utf8_bin   NOT NULL COMMENT '通道ID',
    `BLOCK_ID`              bigint(20)                     NOT NULL,
    `VERSION`               varchar(45) COLLATE utf8_bin   NOT NULL,
    `block_hash`            varchar(64) COLLATE utf8_bin   NOT NULL,
    `PREV_BLOCK_HASH`       varchar(64) COLLATE utf8_bin   NOT NULL,
    `PREV_BLOCK_HEIGHT`     bigint(20)                              DEFAULT NULL,
    `BLOCK_BUILDER_ID`      varchar(1024) COLLATE utf8_bin NOT NULL COMMENT '创建者身份',
    `PACK_TIMESTAMP`        timestamp(3)                   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `BLOCK_CONSENSUS`       varchar(45) COLLATE utf8_bin   NOT NULL,
    `BLOCK_WITNESS`         text COLLATE utf8_bin          NOT NULL,
    `CONSENSUS_TIMESTAMP`   timestamp                      NULL     DEFAULT NULL,
    `PERSISTENCE_TIMESTAMP` timestamp                      NULL     DEFAULT NULL,
    PRIMARY KEY (`ID`, `CHANNEL_ID`),
    UNIQUE KEY `block_hash` (`block_hash`),
    KEY `block_block_id` (`BLOCK_ID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  COLLATE = utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;



LOCK TABLES `block` WRITE;
/*!40000 ALTER TABLE `block`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `block`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transaction`
--

DROP TABLE IF EXISTS `transaction`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `transaction`
(
    `ID`                         bigint(20)                    NOT NULL AUTO_INCREMENT,
    `CHANNEL_ID`                 varchar(64) COLLATE utf8_bin  NOT NULL COMMENT '通道ID',
    `VERSION`                    varchar(10) COLLATE utf8_bin  NOT NULL COMMENT 'ExecutedTransaction的版本号',
    `BLOCK_ID`                   bigint(20)                    NOT NULL,
    `POOLED_TRANS`               longtext COLLATE utf8_bin,
    `POOLED_TRANS_VERSION`       varchar(10) COLLATE utf8_bin           DEFAULT NULL COMMENT '冗余字段，便于查询统计',
    `ADD2_POOL_TIMESTAMP`        timestamp                     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '冗余字段，便于查询统计',
    `TRANS_HASH_STR`             varchar(128) COLLATE utf8_bin          DEFAULT NULL COMMENT '交易hash的16进制字符串',
    `SDK_TRANS_VERSION`          varchar(10) COLLATE utf8_bin           DEFAULT NULL COMMENT '冗余字段，便于查询统计',
    `CLIENT_TIMESTAMP`           timestamp                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '冗余字段，便于查询统计',
    `CLIENT_TRANS_ID`            varchar(128) COLLATE utf8_bin NOT NULL,
    `SMART_CONTRACT_ID`          varchar(128) COLLATE utf8_bin          DEFAULT NULL COMMENT '智能合约ID，含版本号',
    `SMART_CONTRACT_METHOD_NAME` varchar(128) COLLATE utf8_bin          DEFAULT NULL COMMENT '所调用的方法名',
    `CLIENT_IDENTITY_KEY`        varchar(255) COLLATE utf8_bin          DEFAULT NULL COMMENT '调用者身份',
    `MODIFIED_WORLD_STATE_LIST`  longtext COLLATE utf8_bin,
    `MODIFIED_CHANNEL_RECORD`    longtext COLLATE utf8_bin COMMENT 'Channel的修改记录，只有系统合约调用才会有此值',
    `EXECUTE_TIMESTAMP`          timestamp                     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `EXECUTED_MS`                bigint(20)                             DEFAULT NULL COMMENT '执行耗时',
    `BLOCK_HASH_STR`             varchar(128) COLLATE utf8_bin NOT NULL,
    `INDEX_IN_BLOCK`             int(10)                                DEFAULT NULL COMMENT '从1开始',
    `SUCCESSED`                  tinyint(4)                             DEFAULT NULL,
    `ERROR_MSG`                  varchar(1024) COLLATE utf8_bin         DEFAULT NULL,
    `CREATE_TIMESTAMP`           timestamp(3)                  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`ID`),
    UNIQUE KEY `transaction_trans_hash_str` (`TRANS_HASH_STR`),
    KEY `transaction_block_hash_str` (`BLOCK_HASH_STR`, `CHANNEL_ID`),
    KEY `transaction_channel_id_create_timestamp` (`CHANNEL_ID`, `CREATE_TIMESTAMP`),
    KEY `transaction_client__identity_key` (`CLIENT_IDENTITY_KEY`),
    KEY `transaction_client_trans_id` (`CLIENT_TRANS_ID`),
    KEY `transaction_index_in_block` (`INDEX_IN_BLOCK`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  COLLATE = utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transaction`
--

LOCK TABLES `transaction` WRITE;
/*!40000 ALTER TABLE `transaction`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `transaction`
    ENABLE KEYS */;
UNLOCK TABLES;

