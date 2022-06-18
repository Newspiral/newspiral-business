/*
 Navicat Premium Data Transfer

 Source Server         : NewSpiral节点1
 Source Server Type    : MySQL
 Source Server Version : 50724
 Source Host           : 192.168.0.17:3306
 Source Schema         : newspiral1

 Target Server Type    : MySQL
 Target Server Version : 50724
 File Encoding         : 65001

 Date: 09/10/2019 09:06:51
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for BLOCK
-- ----------------------------
DROP TABLE IF EXISTS `BLOCK`;
CREATE TABLE `BLOCK`  (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `BLOCK_ID` bigint(20) NOT NULL,
  `VERSION` varchar(45) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `HASH` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `PREV_BLOCK_HASH` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `PREV_BLOCK_HEIGHT` bigint(20) DEFAULT NULL,
  `BLOCK_BUILDER_ID` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `CHANNEL_ID` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `PACK_TIMESTAMP` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  `BLOCK_CONSENSUS` varchar(45) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `BLOCK_WITNESS` text CHARACTER SET utf8 COLLATE utf8_bin COMMENT '见证数据，对于HotStuff，应该是QC数据',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 688 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;


-- ----------------------------
-- Table structure for CHANNEL
-- ----------------------------
DROP TABLE IF EXISTS `CHANNEL`;
CREATE TABLE `CHANNEL`  (
  `CHANNEL_ID` char(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '使用UUID去除分隔符生成，固定32个字符；使用UUID保证全局唯一',
  `NAME` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `BLOCK_MAX_SIZE` bigint(20) NOT NULL,
  `BLOCK_MAX_INTERVAL` bigint(20) DEFAULT NULL,
  `MAX_PEER_COUNT` int(11) DEFAULT NULL,
  `PEER_ADD_STRATEGY` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `MODIFY_STRATEGY` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `SC_DEPOLY_STRATEGY` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `CONSENSUS_ALGORITHM` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `TX_POOL_SIZE` bigint(20) DEFAULT NULL,
  `ALLOW_TIME_ERROR_SECONDS` bigint(20) DEFAULT NULL,
  `SECURITY_SERVICE_KEY` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `EXTENDS_PARAMS` varchar(4096) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`CHANNEL_ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;



-- ----------------------------
-- Records of IDENTITY
-- ----------------------------
INSERT INTO `IDENTITY` VALUES ('{\"type\":\"CHINA_PKI\",\"value\":\"peerId1OfOrg1Test\"}', '{\"type\":\"CHINA_PKI\",\"value\":\"Org1Test\"}', NULL, '2019-09-28 19:44:06');

-- ----------------------------
-- Table structure for PEER
-- ----------------------------
DROP TABLE IF EXISTS `PEER`;
CREATE TABLE `PEER`  (
  `PEER_ID_VALUE` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '节点本身的身份ID，对应IdentityKey.value',
  `PEER_ID_TYPE` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `SERVICE_URLS` varchar(1024) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '节点的服务地址，PeerServiceUrls对应的JSON字符串',
  `ORGANIZATION_ID_TYPE` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `ORGANIZATION_ID_VALUE` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '节点所属组织的身份ID',
  `IS_LOCAL_PEER` int(11) DEFAULT NULL,
  PRIMARY KEY (`PEER_ID_VALUE`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of PEER
-- ----------------------------
INSERT INTO `PEER` VALUES ('peerId1OfOrg1Test', '\"CHINA_PKI\"', '{\"serviceUrlMap\":{\"FOR_SDK\":{\"type\":\"FOR_SDK\",\"url\":\"192.168.0.236:12200\"},\"FOR_PEER\":{\"type\":\"FOR_PEER\",\"url\":\"192.168.0.236:12200\"}}}', '\"CHINA_PKI\"', 'org1IdTest', 1);

-- ----------------------------
-- Table structure for PEER_CHANNEL
-- ----------------------------
DROP TABLE IF EXISTS `PEER_CHANNEL`;
CREATE TABLE `PEER_CHANNEL`  (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `PEER_ID` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `CHANNEL_ID` char(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `USER_ID_LIST` text CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '该节点所属组织在该通道的的非admin用户ID列表',
  `ADMIN_ID_LIST` text CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '节点所属组织在该通道的管理员身份标识',
  `JOIN_TIMESTAMP` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '取本地确认该节点加入的时刻点',
  `EXTENDED_DATA` varchar(2048) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '节点与通道关联的扩展字段',
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;



-- ----------------------------
-- Table structure for SMART_CONTRACT
-- ----------------------------
DROP TABLE IF EXISTS `SMART_CONTRACT`;
CREATE TABLE `SMART_CONTRACT`  (
  `SC_NAME` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `SC_VERSION` varchar(45) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `SC_CHANNEL_ID` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `SC_SOURCE_CODE` longtext CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `SC_CLASS_FILE` longblob,
  `SC_CLASS_HASH` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `SETUP_TIMESTAMP` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`SC_NAME`, `SC_VERSION`, `SC_CHANNEL_ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;


-- ----------------------------
-- Table structure for STATE
-- ----------------------------
DROP TABLE IF EXISTS `STATE`;
CREATE TABLE `STATE`  (
  `STATE_KEY` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `STATE_VALUE` blob NOT NULL,
  PRIMARY KEY (`STATE_KEY`) USING BTREE,
  UNIQUE INDEX `KEY_UNIQUE`(`STATE_KEY`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;


-- ----------------------------
-- Table structure for TRANSACTION
-- ----------------------------
DROP TABLE IF EXISTS `TRANSACTION`;
CREATE TABLE `TRANSACTION`  (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `VERSION` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'ExecutedTransaction的版本号',
  `POOLED_TRANS` longtext CHARACTER SET utf8 COLLATE utf8_bin,
  `POOLED_TRANS_VERSION` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '冗余字段，便于查询统计',
  `ADD2_POOL_TIMESTAMP` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '冗余字段，便于查询统计',
  `TRANS_HASH_STR` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '交易hash的16进制字符串',
  `SDK_TRANS_VERSION` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '冗余字段，便于查询统计',
  `CLIENT_TIMESTAMP` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '冗余字段，便于查询统计',
  `CLIENT_TRANS_ID` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `CHANNEL_ID` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '冗余字段，便于查询统计',
  `SMART_CONTRACT_ID` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '智能合约ID，含版本号',
  `SMART_CONTRACT_METHOD_NAME` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '所调用的方法名',
  `CLIENT_IDENTITY_KEY` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '调用者身份',
  `MODIFIED_WORLD_STATE_LIST` longtext CHARACTER SET utf8 COLLATE utf8_bin,
  `MODIFIED_CHANNEL_RECORD` longtext CHARACTER SET utf8 COLLATE utf8_bin COMMENT 'Channel的修改记录，只有系统合约调用才会有此值',
  `EXECUTE_TIMESTAMP` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `EXECUTED_MS` bigint(20) DEFAULT NULL COMMENT '执行耗时',
  `BLOCK_HASH_STR` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `INDEX_IN_BLOCK` int(10) DEFAULT NULL COMMENT '从1开始',
  `SUCCESSED` tinyint(4) DEFAULT NULL,
  `ERROR_MSG` varchar(1024) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;



SET FOREIGN_KEY_CHECKS = 1;
