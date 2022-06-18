-- 2020/11/5  interface表新增 --author:whj
INSERT INTO `newspiral`.`interface` (`ID`, `INTERFACE_NAME`, `CLASS_PATH`, `CLASS_NAME`, `METHOD_NAME`, `METHOD_ARGS`, `REMARK`, `CREATE_TIME`, `UPDATE_TIME`) VALUES ('90', '动态修改日志级别', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'changeLoggerLevel', 'ChangeLoggerLevelReq', NULL, '2020-11-04 16:17:31', '2020-11-04 16:17:34');
INSERT INTO `newspiral`.`interface` (`ID`, `INTERFACE_NAME`, `CLASS_PATH`, `CLASS_NAME`, `METHOD_NAME`, `METHOD_ARGS`, `REMARK`, `CREATE_TIME`, `UPDATE_TIME`) VALUES ('91', '查询日志级别', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'queryLoggerLevel', 'RPCParam', NULL, '2020-11-04 16:20:00', '2020-11-04 16:20:02');
-- 更新auth表
UPDATE `newspiral`.`auth` SET `INTERFACE_ID`='17,20,85,88,89,90,91' WHERE (`ID`='8');

-- 2020/11/13-- 调整表结构 --author:whj
-- peer_channel表USER_ID_LIST和ADMIN_ID_LIST字段删除
ALTER TABLE `peer_channel`
DROP COLUMN `USER_ID_LIST`,
DROP COLUMN `ADMIN_ID_LIST`;
-- peer_user表删除
drop table `peer_user`;
-- CHANNEL表MASTER_PUBLICK_KEY（SM9签名主公钥，加密后）字段删除
ALTER TABLE `channel`
DROP COLUMN `MASTER_PUBLICK_KEY`;

-- 20201120修改state索引
drop index KEY_UNIQUE on state;
create unique index KEY_UNIQUE
	on state (CHANNEL_ID, STATE_KEY);

-- 2020/11/22  --author: whj
-- 接口调用记录表：interface_record
DROP TABLE IF EXISTS `interface_record`;
CREATE TABLE `interface_record` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `METHOD_NAME` varchar(255) NOT NULL COMMENT '被调用的接口名称',
  `PROTOCOL_NAME` varchar(20) NOT NULL COMMENT '被调用的接口协议类型',
  `IDENTITY_TYPE` varchar(20) DEFAULT NULL COMMENT '调用方身份，IdentityTypeEnum',
  `IDENTITY_VALUE` varchar(300) DEFAULT NULL COMMENT '调用方身份，key',
  `CHANNEL_ID` varchar(32) DEFAULT NULL COMMENT '调用所属通道ID',
  `START_TIME` timestamp(3) NOT NULL COMMENT '调用发生时间戳，毫秒',
  `END_TIME` timestamp(3) NOT NULL COMMENT '调用完成时间戳，毫秒',
  `CALL_TIME` bigint(20) NOT NULL COMMENT '接口处理时长，毫秒',
  `SUCCESSED` tinyint(4) NOT NULL COMMENT '调用结果，1：Success:；0：Failed',
  `ERROR_MSG` varchar(1024) DEFAULT NULL COMMENT '调用错误信息',
  `SC_ALISA` varchar(128) DEFAULT NULL COMMENT '调用合约别名',
  `SC_METHOD_NAME` varchar(128) DEFAULT NULL COMMENT '合约方法名',
  `SC_VERSION` varchar(45) DEFAULT NULL COMMENT '调用合约版本号',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8;

-- 接口调用记录汇总表：interface_record_summary
DROP TABLE IF EXISTS `interface_record_summary`;
CREATE TABLE `interface_record_summary` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `RECORD_HASH` varchar(256) NOT NULL COMMENT '记录的唯一标识,唯一索引',
  `METHOD_NAME` varchar(128) NOT NULL COMMENT '被调用的接口名称',
  `PROTOCOL_NAME` varchar(20) NOT NULL COMMENT '被调用的接口协议类型',
  `CHANNEL_ID` char(32) DEFAULT NULL COMMENT '调用所属通道ID',
  `SUCCESSED` tinyint(4) NOT NULL COMMENT '调用结果，1：Success:；0：Failed',
  `ERROR_MSG` varchar(1024) DEFAULT NULL COMMENT '调用错误信息',
  `SC_ALISA` varchar(128) DEFAULT NULL COMMENT '调用合约别名',
  `SC_METHOD_NAME` varchar(128) DEFAULT NULL COMMENT '合约方法名',
  `SC_VERSION` varchar(45) DEFAULT NULL COMMENT '调用合约版本号',
  `TOTAL_CALLS` bigint(20) NOT NULL COMMENT '调用总次数',
  `TOTAL_CALL_TIME` bigint(20) NOT NULL COMMENT '调用总耗时，毫秒',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `record_hash` (`RECORD_HASH`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- 20201123 将区块hash改为独立索引
drop index block_hash on block;
create unique index block_hash
	on block (HASH);

create unique index block_hash
	on block_cache (HASH);

-- 20201123 将交易hash设置为独立索引
drop index transaction_trans_hash_str on transaction;
create unique index transaction_trans_hash_str
	on transaction (TRANS_HASH_STR);

drop index transaction_trans_hash_str on transaction_cache;
create unique index transaction_trans_hash_str
	on transaction_cache (TRANS_HASH_STR);

-- 20201123 设置为独立索引
drop index unique_index on member_role;
create unique index unique_index
	on member_role (MEMBER_ID, ROLE_ID, CHANNEL_ID);

-- 20201123 设置为独立索引
drop index unique_index on peer_channel;
create unique index unique_index
	on peer_channel (PEER_ID, CHANNEL_ID);

-- 20201123 设置为独立索引
drop index unique_index on peer_certificate;
create unique index unique_index
	on peer_certificate (PEER_ID, CHANNEL_ID, FLAG, CERTIFICATE_HASH);




-- interface表新增 --author:whj
INSERT INTO `newspiral`.`interface` (`ID`, `INTERFACE_NAME`, `CLASS_PATH`, `CLASS_NAME`, `METHOD_NAME`, `METHOD_ARGS`, `REMARK`, `CREATE_TIME`, `UPDATE_TIME`) VALUES ('92', '查询接口调用详细记录', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'queryInterfaceRecord', 'InterfaceRecordReq', NULL, '2020-11-26 08:46:38', '2020-11-26 08:46:41');
INSERT INTO `newspiral`.`interface` (`ID`, `INTERFACE_NAME`, `CLASS_PATH`, `CLASS_NAME`, `METHOD_NAME`, `METHOD_ARGS`, `REMARK`, `CREATE_TIME`, `UPDATE_TIME`) VALUES ('93', '查询接口调用总体情况', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'queryInterfaceRecordSummary', 'RPCParam', NULL, '2020-11-26 08:48:11', '2020-11-26 08:48:14');
-- 更新auth表
UPDATE `newspiral`.`auth` SET `INTERFACE_ID`='17,20,85,88,89,90,91,92,93' WHERE (`ID`='8');

-- interface_record表建立索引 2020/12/7
alter table interface_record add index start_time_index (start_time);
alter table interface_record add index end_time_index (end_time);

--增加数据一致性校验接口
INSERT INTO `newspiral`.`interface` (`ID`, `INTERFACE_NAME`, `CLASS_PATH`, `CLASS_NAME`, `METHOD_NAME`, `METHOD_ARGS`, `REMARK`, `CREATE_TIME`, `UPDATE_TIME`) VALUES ('94', '区块数据合法性检查', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'blockLegalCheck', 'BlockHeightReq', NULL, '2020-12-07 17:13:00', '2020-12-07 17:13:02');
INSERT INTO `newspiral`.`interface` (`ID`, `INTERFACE_NAME`, `CLASS_PATH`, `CLASS_NAME`, `METHOD_NAME`, `METHOD_ARGS`, `REMARK`, `CREATE_TIME`, `UPDATE_TIME`) VALUES ('95', '区块数据合法性检查结果查询', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'queryBlockLegalCheckResult', 'BlockHeightReq', NULL, '2020-12-07 17:13:53', '2020-12-07 17:13:56');
-- 更新权限
UPDATE `newspiral`.`auth` SET `INTERFACE_ID`='17,20,85,88,89,90,91,92,93,94,95' WHERE (`ID`='8');
-- 去除PEER_CERTIFICATE表自动更新时间
ALTER TABLE `PEER_CERTIFICATE` CHANGE `CREATE_TIME` `CREATE_TIME` TIMESTAMP  NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';

