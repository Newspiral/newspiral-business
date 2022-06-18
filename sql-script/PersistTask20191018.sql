

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for persist_task
-- ----------------------------
DROP TABLE IF EXISTS `persist_task`;
CREATE TABLE `persist_task`  (
  `TASK_ID` char(128) COLLATE utf8_bin NOT NULL COMMENT '使用UUID去除分隔符生成，固定32个字符；使用UUID保证全局唯一',
  `CREATE_TIME` bigint(20) DEFAULT NULL,
  `EXECUTE_END_TIME` bigint(20) DEFAULT NULL,
  `PARAMAS_STR` text COLLATE utf8_bin DEFAULT NULL,
  `TYPE` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `STATUS` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`TASK_ID`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;

--注意设置max_allowed_packet为1024M
--SHOW VARIABLES LIKE '%max_allowed_packet%';
--max_allowed_packet = 1024M;
