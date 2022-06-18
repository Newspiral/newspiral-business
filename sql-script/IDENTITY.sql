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

 Date: 09/10/2019 10:10:11
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for IDENTITY
-- ----------------------------
DROP TABLE IF EXISTS `IDENTITY`;
CREATE TABLE `IDENTITY`  (
  `IDENTITY_ID` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT 'IdentityKey的JSON序列化字符串，数量很少，无所谓性能',
  `PARENT_ID` varchar(128) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `EXTENDED_PROPS` varchar(1024) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '扩展属性',
  `SETUP_TIMESTAMP` timestamp(0) DEFAULT NULL,
  PRIMARY KEY (`IDENTITY_ID`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO `IDENTITY` VALUES ('{\"type\":\"CHINA_PKI\",\"value\":\"peerId1OfOrg1Test\"}', '{\"type\":\"CHINA_PKI\",\"value\":\"Org1Test\"}', NULL, '2019-09-28 19:44:06');
