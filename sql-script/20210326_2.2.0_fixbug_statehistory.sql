
use mysql;
ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'Newspiral@2019';

-- ------------------------------------------------------
use newspiral_cluster;
-- 分库分表配置修改
INSERT INTO `newspiral_cluster`.`local_config` (`key`, `value`, `show`, `type`, `updated_time`, `created_time`) VALUES ('sharding.rule.state_history.actualDataNodes', 'ds${0..#{sharding.dbindex}}.state_history', '0', 'shardingsphere', '2021-03-24 16:36:31', '2021-03-24 16:35:27');
UPDATE `newspiral_cluster`.`local_config` SET `value`='block,transaction,state_history' WHERE (`key`='sharding.rule.table.names');

-- 更改权限
UPDATE `newspiral_cluster`.`interface` SET `INTERFACE_NAME`='根据世界状态key查询相关的交易历史', `CLASS_PATH`='com.jinninghui.newspiral.gateway', `CLASS_NAME`='ServiceForSDKImpl', `METHOD_NAME`='queryWorldStateHistory', `METHOD_ARGS`='StateHistoryReq' WHERE (`ID`='8');

-- -------------------------------------------
use newspiral_zone1;
-- ----------------------------
-- Table structure for state_history
-- ----------------------------
DROP TABLE IF EXISTS `state_history`;
CREATE TABLE `state_history` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键，自带唯一索引',
  `STATE_KEY` text CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '世界状态key，索引',
  `CHANNEL_ID` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '通道id，非空',
  `TRANSACTION_ID` bigint(20) NOT NULL COMMENT '交易表中的id',
  `TRANS_HASH_STR` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '交易hash',
  `CLIENT_IDENTITY_KEY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '交易发起方身份id',
  `SMART_CONTRACT_ID` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '智能合约ID',
  `SMART_CONTRACT_METHOD_NAME` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '智能合约方法名',
  `BLOCK_ID` bigint(20) NOT NULL COMMENT '交易所属区块高度',
  `INDEX_IN_BLOCK` int(10) NOT NULL COMMENT '交易在区块中的索引',
  `BLOCK_HASH_STR` varchar(128) NOT NULL COMMENT '交易所属区块哈希',
  `CONSENSUS_TIMESTAMP` timestamp NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '交易所属区块的共识时间：时间戳',
  `SUCCESSED` tinyint(4) NOT NULL COMMENT '交易执行成功与否标志(1-true/0-false)',
  `ERROR_MSG` varchar(1024) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '错误信息',
  `CLIENT_TIMESTAMP` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '交易的客户端时间CLIENT_TIMESTAMP：时间戳',
  `INSERT_VERSION` bigint(20) NOT NULL COMMENT '插入版本，随插入次数递增',
  PRIMARY KEY (`ID`),
  KEY `state_block_id_channel_id` (`BLOCK_ID`,`CHANNEL_ID`) USING BTREE,
  KEY `query_index` (`STATE_KEY`(200),`CHANNEL_ID`,`CLIENT_TIMESTAMP`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


-- -------------------------------------------------------
use newspiral_zone2;

DROP TABLE IF EXISTS `state_history`;
CREATE TABLE `state_history` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键，自带唯一索引',
  `STATE_KEY` text CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '世界状态key，索引',
  `CHANNEL_ID` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '通道id，非空',
  `TRANSACTION_ID` bigint(20) NOT NULL COMMENT '交易表中的id',
  `TRANS_HASH_STR` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '交易hash',
  `CLIENT_IDENTITY_KEY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '交易发起方身份id',
  `SMART_CONTRACT_ID` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '智能合约ID',
  `SMART_CONTRACT_METHOD_NAME` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '智能合约方法名',
  `BLOCK_ID` bigint(20) NOT NULL COMMENT '交易所属区块高度',
  `INDEX_IN_BLOCK` int(10) NOT NULL COMMENT '交易在区块中的索引',
  `BLOCK_HASH_STR` varchar(128) NOT NULL COMMENT '交易所属区块哈希',
  `CONSENSUS_TIMESTAMP` timestamp NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '交易所属区块的共识时间：时间戳',
  `SUCCESSED` tinyint(4) NOT NULL COMMENT '交易执行成功与否标志(1-true/0-false)',
  `ERROR_MSG` varchar(1024) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '错误信息',
  `CLIENT_TIMESTAMP` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '交易的客户端时间CLIENT_TIMESTAMP：时间戳',
  `INSERT_VERSION` bigint(20) NOT NULL COMMENT '插入版本，随插入次数递增',
  PRIMARY KEY (`ID`),
  KEY `state_block_id_channel_id` (`BLOCK_ID`,`CHANNEL_ID`) USING BTREE,
  KEY `query_index` (`STATE_KEY`(200),`CHANNEL_ID`,`CLIENT_TIMESTAMP`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

