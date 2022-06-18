
use newspiral_cluster;
-- 配置新增接口权限 限流接口
INSERT INTO `interface` (`ID`, `INTERFACE_NAME`, `CLASS_PATH`, `CLASS_NAME`, `METHOD_NAME`, `METHOD_ARGS`, `REMARK`, `CREATE_TIME`, `UPDATE_TIME`) VALUES ('103', '根据类名和接口名批量修改接口的限流参数', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'modifyLimitRequestParam', 'LimitParamReq', NULL, '2021-03-02 18:51:27', '2021-03-02 18:51:30');
INSERT INTO `interface` (`ID`, `INTERFACE_NAME`, `CLASS_PATH`, `CLASS_NAME`, `METHOD_NAME`, `METHOD_ARGS`, `REMARK`, `CREATE_TIME`, `UPDATE_TIME`) VALUES ('104', '根据类名批量修改类下的所有限流接口的限流参数', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'modifyClassLimitRequestParam', 'ClassLimitParamReq', NULL, '2021-03-02 18:51:32', '2021-03-02 18:51:34');
-- 配置新增接口权限 客户端黑名单
INSERT INTO `interface` (`ID`, `INTERFACE_NAME`, `CLASS_PATH`, `CLASS_NAME`, `METHOD_NAME`, `METHOD_ARGS`, `REMARK`, `CREATE_TIME`, `UPDATE_TIME`) VALUES ('105', '添加限制操作的ip信息', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'addIpIntoIpConstraintList', 'IpConstraintReq', NULL, '2021-03-15 15:19:07', '2021-03-15 15:19:09');
INSERT INTO `interface` (`ID`, `INTERFACE_NAME`, `CLASS_PATH`, `CLASS_NAME`, `METHOD_NAME`, `METHOD_ARGS`, `REMARK`, `CREATE_TIME`, `UPDATE_TIME`) VALUES ('106', '修改限制操作的ip信息', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'modifyIpConstraintList', 'IpConstraintReq', NULL, '2021-03-15 15:20:27', '2021-03-15 15:20:31');
-- 更新权限
UPDATE `auth` SET `ID`='8', `AUTH_NAME`='节点监控', `AUTH_CODE`='monitor_peer', `AUTH_DESCRIPTION`='节点监控权限', `INTERFACE_ID`='17,20,85,88,89,90,91,92,93,96,97,98,99,100,101,102,103,104,105,106', `CREATE_TIME`='2020-10-16 14:19:26', `UPDATE_TIME`='2020-10-16 14:19:27' WHERE (`ID`='8');

-- interface增加限流参数列
ALTER TABLE `interface`
ADD COLUMN `LIMIT_ACCESS`  varchar(2) NULL COMMENT '接口是否限流' AFTER `METHOD_ARGS`,
ADD COLUMN `CREATE_TOKEN_RATE`  int(5) NOT NULL COMMENT '每秒产生令牌的速率' AFTER `LIMIT_ACCESS`,
ADD COLUMN `WAIT_TOKEN_TIME`  int(3) NOT NULL COMMENT '等待令牌的时候，单位为秒' AFTER `CREATE_TOKEN_RATE`;
-- 初始化限流参数
update interface set LIMIT_ACCESS = '1' , CREATE_TOKEN_RATE = '50' ,WAIT_TOKEN_TIME = '3' ;

-- 客户端黑名单
SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for ip_constraint_list
-- ----------------------------
CREATE TABLE IF NOT EXISTS `ip_constraint_list` (
  `ID` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `IP_ADDR` varchar(128) NOT NULL COMMENT 'IP地址',
  `CONSTRAINT_TYPE` varchar(128) NOT NULL COMMENT '限制操作类型，比如禁止访问，允许访问，限流等',
  `ACTIVE` varchar(2) NOT NULL DEFAULT '1' COMMENT '是否生效，默认为生效',
  `REMARK` varchar(256) DEFAULT NULL COMMENT '补充信息',
  `CREATE_TIME` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `UPDATE_TIME` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `ip_addr_operation` (`IP_ADDR`,`CONSTRAINT_TYPE`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;


