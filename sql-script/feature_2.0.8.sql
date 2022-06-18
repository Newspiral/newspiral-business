-- 20201015通道新增相关策略
ALTER TABLE CHANNEL ADD  `ROLE_ADD_STRATEGY` varchar(128)  DEFAULT NULL COMMENT '角色新增控制策略';
ALTER TABLE CHANNEL ADD  `ROLE_MODIFY_STRATEGY` varchar(128)  DEFAULT NULL COMMENT '角色修改控制策略';
ALTER TABLE CHANNEL ADD  `ROLE_DEL_STRATEGY` varchar(128)  DEFAULT NULL COMMENT '角色删除控制策略';

-- 20201016新增权限相关表结构
-- 接口表
DROP TABLE IF EXISTS interface;
create table interface
(
    ID             int(10) auto_increment
        primary key,
    INTERFACE_NAME varchar(64)  not null comment '接口名称("别名")',
    CLASS_PATH     varchar(128) not null comment '类路径',
    CLASS_NAME     varchar(128) not null comment '类名',
    METHOD_NAME    varchar(128) null comment '方法名',
    METHOD_ARGS    varchar(128) null comment '方法参数',
    REMARK         varchar(255) null comment '说明备注',
    CREATE_TIME    timestamp    null,
    UPDATE_TIME    timestamp    null
);

INSERT INTO `interface` VALUES ('1', '业务写智能合约', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'callExecuteBusinessSmartContract', 'SDKTransactionReq', null, '2020-10-16 10:20:40', '2020-10-16 10:20:43');
INSERT INTO `interface` VALUES ('2', '业务读合约调用权限', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'callQueryBusinessSmartContract', 'SDKTransactionReq', null, '2020-10-16 10:33:55', '2020-10-16 10:33:57');
INSERT INTO `interface` VALUES ('3', '系统读合约调用', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'callQuerySmartContract', 'SDKTransactionReq', null, '2020-10-16 10:38:21', '2020-10-16 10:38:23');
INSERT INTO `interface` VALUES ('4', '根据交易的客户端ID查询交易', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'queryTransactionByTransClientTxId', 'TransClientTxIdReq', null, '2020-10-16 11:40:13', '2020-10-16 11:40:14');
INSERT INTO `interface` VALUES ('5', '根据交易的全局ID（即hash）查询交易', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'queryTransactionByTransHash', 'TransHashReq', null, '2020-10-16 11:40:47', '2020-10-16 11:40:48');
INSERT INTO `interface` VALUES ('6', '根据交易所属区块Hash和交易在区块中的序号查询交易', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'queryTransactionByBlockHash', 'TransBlockHashReq', null, '2020-10-16 11:41:18', '2020-10-16 11:41:19');
INSERT INTO `interface` VALUES ('7', '查询某个区块的所有交易', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'getTransactionsByHash', 'QueryTransactionsByBlockHashReq', null, '2020-10-16 11:43:01', '2020-10-16 11:43:03');
INSERT INTO `interface` VALUES ('8', '根据世界状态key查询相关的交易历史', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'queryTxHistory', 'QueryTxReq', null, '2020-10-16 11:43:23', '2020-10-16 11:43:24');
INSERT INTO `interface` VALUES ('9', '根据区块哈希查询区块（不包含交易列表）', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'queryBlockByHash', 'BlockHashReq', null, '2020-10-16 11:44:07', '2020-10-16 11:44:08');
INSERT INTO `interface` VALUES ('10', '根据区块高度查询区块（不包含交易列表）', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'queryBlockByHeight', 'BlockHeightRegionReq', null, '2020-10-16 11:44:38', '2020-10-16 11:44:39');
INSERT INTO `interface` VALUES ('11', '查询某个通道在本节点的最新的区块信息', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'queryLatestBlockList', 'SummaryBlockReq', null, '2020-10-16 11:45:35', '2020-10-16 11:45:36');
INSERT INTO `interface` VALUES ('12', '根据交易哈希查询所属区块（不包含交易列表）', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'queryBlockByTransHash', 'TransHashReq', null, '2020-10-16 11:46:47', '2020-10-16 11:46:48');
INSERT INTO `interface` VALUES ('13', '根据交易的客户端ID查询所属区块（不包含交易列表）', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'queryBlockByTransClientTxId', 'TransClientTxIdReq', null, '2020-10-16 11:47:13', '2020-10-16 11:47:17');
INSERT INTO `interface` VALUES ('14', '根据高度范围查询区块列表（不包含交易列表）', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'queryBlockByHeightRegion', 'BlockHeightRegionReq', null, '2020-10-16 11:48:03', '2020-10-16 11:48:04');
INSERT INTO `interface` VALUES ('15', '查询世界状态', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'queryWorldState', 'QueryWorldStateReq', null, '2020-10-16 11:48:17', '2020-10-16 11:48:16');
INSERT INTO `interface` VALUES ('17', '节点程序版本号查询', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'version', null, null, '2020-10-16 11:49:53', '2020-10-16 11:49:54');
INSERT INTO `interface` VALUES ('20', '查询节点当前状态：状态、服务地址、系统负载信息、加入的通道简要信息', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'getPeerCurrentState', 'PeerIdentityReq', null, '2020-10-16 11:51:51', '2020-10-16 11:51:53');
INSERT INTO `interface` VALUES ('21', '删除所有通道', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'removeAllChannel', null, null, '2020-10-16 11:52:56', '2020-10-16 11:52:58');
INSERT INTO `interface` VALUES ('22', '从通道中删除一个节点', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'removePeerFromChannel', 'RemovePeerFromChannelRequest', null, '2020-10-16 11:52:55', '2020-10-16 11:52:59');
INSERT INTO `interface` VALUES ('23', '删除通道', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'deleteChannel', 'DeleteChannelReq', null, '2020-10-16 11:53:22', '2020-10-16 11:53:24');
INSERT INTO `interface` VALUES ('24', '创建通道', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'createChannel', 'ChannelInitReq', null, '2020-10-16 11:54:05', '2020-10-16 11:54:06');
INSERT INTO `interface` VALUES ('25', '系统写合约调用', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'callExecuteSmartContract', 'SDKTransactionReq', null, '2020-10-16 11:55:00', '2020-10-16 11:55:02');
INSERT INTO `interface` VALUES ('26', '停止节点', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'stopPeer', 'PeerIdentityReq', null, '2020-10-16 11:56:04', '2020-10-16 11:56:06');
INSERT INTO `interface` VALUES ('27', '将本节点退出某个通道', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'exitMySelfFromChannel', 'ExitMySelfFromChannelReq', null, '2020-10-16 11:56:26', '2020-10-16 11:56:27');
INSERT INTO `interface` VALUES ('28', '处理通道的BlockVote消息', 'com.jinninghui.newspiral.gateway', 'ServiceForPeerImpl', 'addBlockVoteMsg', 'BlockVoteMsg', null, '2020-10-16 13:41:12', '2020-10-16 13:41:13');
INSERT INTO `interface` VALUES ('29', '增加GenericMsg消息', 'com.jinninghui.newspiral.gateway', 'ServiceForPeerImpl', 'addGenericMsg', 'GenericMsg', null, '2020-10-16 13:43:26', '2020-10-16 13:43:27');
INSERT INTO `interface` VALUES ('30', '处理某个通道的NewView消息', 'com.jinninghui.newspiral.gateway', 'ServiceForPeerImpl', 'addNewViewMsg', 'NewViewMsg,String', null, '2020-10-16 13:45:59', '2020-10-16 13:46:00');
INSERT INTO `interface` VALUES ('46', '新交易加入交易池', 'com.jinninghui.newspiral.gateway', 'ServiceForPeerImpl', 'addSDKTranscation', 'SDKTransaction', null, '2020-10-16 13:46:50', '2020-10-16 13:46:51');
INSERT INTO `interface` VALUES ('47', '根据区块Hash查询某个区块', 'com.jinninghui.newspiral.gateway', 'ServiceForPeerImpl', 'queryBlockByHash', 'String,String', null, '2020-10-16 13:46:50', '2020-10-16 13:46:51');
INSERT INTO `interface` VALUES ('48', '根据区块高度查询某个区块', 'com.jinninghui.newspiral.gateway', 'ServiceForPeerImpl', 'queryBlockByHeight', 'String,Long', null, '2020-10-16 13:46:50', '2020-10-16 13:46:51');
INSERT INTO `interface` VALUES ('49', '参与共识之前获取链状态', 'com.jinninghui.newspiral.gateway', 'ServiceForPeerImpl', 'queryChainState', 'QueryChainStateReq', null, '2020-10-16 13:46:50', '2020-10-16 13:46:51');
INSERT INTO `interface` VALUES ('50', '查询某个通道的区块链概要信息', 'com.jinninghui.newspiral.gateway', 'ServiceForPeerImpl', 'queryChainSummary', 'QueryChannelReq', null, '2020-10-16 13:46:50', '2020-10-16 13:46:51');
INSERT INTO `interface` VALUES ('51', '返回简单的可以向通道外公布的通道信息', 'com.jinninghui.newspiral.gateway', 'ServiceForPeerImpl', 'queryChannel', 'QueryChannelReq', null, '2020-10-16 13:46:50', '2020-10-16 13:46:51');
INSERT INTO `interface` VALUES ('52', '查询某个节点保存的某个通道基础参数', 'com.jinninghui.newspiral.gateway', 'ServiceForPeerImpl', 'queryChannelBasicParams', 'QueryChannelReq', null, '2020-10-16 13:46:50', '2020-10-16 13:46:51');
INSERT INTO `interface` VALUES ('53', '查询历史块', 'com.jinninghui.newspiral.gateway', 'ServiceForPeerImpl', 'queryHistoryBlock', 'QueryHistoryBlockReq', null, '2020-10-16 13:46:50', '2020-10-16 13:46:51');
INSERT INTO `interface` VALUES ('54', '查询历史QC', 'com.jinninghui.newspiral.gateway', 'ServiceForPeerImpl', 'queryHistoryQC', 'QueryHistoryQCReq', null, '2020-10-16 13:46:50', '2020-10-16 13:46:51');
INSERT INTO `interface` VALUES ('58', '获取当前节点的view值', 'com.jinninghui.newspiral.gateway', 'ServiceForPeerImpl', 'queryViewNo', 'QueryViewNoReq', null, '2020-10-16 13:46:50', '2020-10-16 13:46:51');
INSERT INTO `interface` VALUES ('59', '从通道里移除节点', 'com.jinninghui.newspiral.gateway', 'ServiceForPeerImpl', 'removePeerFromChannel', 'SDKTransaction', null, '2020-10-16 13:46:50', '2020-10-16 13:46:51');
INSERT INTO `interface` VALUES ('60', '查询节点组织状态', 'com.jinninghui.newspiral.gateway', 'ServiceForPeerImpl', 'queryPeerOrganizationState', 'String', null, '2020-10-16 13:46:50', '2020-10-16 13:46:51');
INSERT INTO `interface` VALUES ('61', '通道参数管理', 'com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor', 'SystemSmartContract', 'updateChannelBlockMaxSize', 'List<ChannelBlockMaxSizeApproval>', null, '2020-10-16 14:00:30', '2020-10-16 14:00:31');
INSERT INTO `interface` VALUES ('62', '添加节点至通道', 'com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor', 'SystemSmartContract', 'addOnePeer', 'List<AddPeer2ChannelToken>', null, '2020-10-16 14:00:30', '2020-10-16 14:00:31');
INSERT INTO `interface` VALUES ('63', '从通道删除某节点', 'com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor', 'SystemSmartContract', 'removeOnePeer', 'List<RemovePeerFromChannelToken>', null, '2020-10-16 14:00:30', '2020-10-16 14:00:31');
INSERT INTO `interface` VALUES ('64', '更新节点证书', 'com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor', 'SystemSmartContract', 'updatePeerCertificate', 'List<PeerCertificateUpdateApproval>', null, '2020-10-16 14:00:30', '2020-10-16 14:00:31');
INSERT INTO `interface` VALUES ('65', '更新节点证书状态', 'com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor', 'SystemSmartContract', 'updatePeerCertificateState', 'List<PeerCertificateStateApproval>', null, '2020-10-16 14:00:30', '2020-10-16 14:00:31');
INSERT INTO `interface` VALUES ('66', '查询角色', 'com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor', 'SystemSmartContract', 'getRole', 'List<QueryRoleApproval>', null, '2020-10-16 14:00:30', '2020-10-16 14:00:31');
INSERT INTO `interface` VALUES ('67', '查询角色列表', 'com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor', 'SystemSmartContract', 'getRoleList', 'List<QueryRoleApproval>', null, '2020-10-16 14:00:30', '2020-10-16 14:00:31');
INSERT INTO `interface` VALUES ('68', '添加自定义角色', 'com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor', 'SystemSmartContract', 'addCustomRole', 'List<RoleAddApproval>', null, '2020-10-16 14:00:30', '2020-10-16 14:00:31');
INSERT INTO `interface` VALUES ('69', '修改自定义角色', 'com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor', 'SystemSmartContract', 'updateCustomRole', 'List<RoleUpdateApproval>', null, '2020-10-16 14:00:30', '2020-10-16 14:00:31');
INSERT INTO `interface` VALUES ('70', '成员查询', 'com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor', 'SystemSmartContract', 'getMember', 'List<QueryMemberApproval>', null, '2020-10-16 14:00:30', '2020-10-16 14:00:31');
INSERT INTO `interface` VALUES ('71', '成员列表查询', 'com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor', 'SystemSmartContract', 'getMemberList', 'List<QueryMemberListApproval>', null, '2020-10-16 14:00:30', '2020-10-16 14:00:31');
INSERT INTO `interface` VALUES ('72', '添加成员（组织和业务应用）', 'com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor', 'SystemSmartContract', 'addMember', 'List<MemberAddApproval>', null, '2020-10-16 14:00:30', '2020-10-16 14:00:31');
INSERT INTO `interface` VALUES ('73', '更新成员（组织和业务应用）', 'com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor', 'SystemSmartContract', 'updateMember', 'List<MemberUpdateApproval>', null, '2020-10-16 14:00:30', '2020-10-16 14:00:31');
INSERT INTO `interface` VALUES ('74', '智能合约查询', 'com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor', 'SystemSmartContract', 'getSmartContract', 'QuerySmartContractApproval', null, '2020-10-16 14:00:30', '2020-10-16 14:00:31');
INSERT INTO `interface` VALUES ('75', '智能合约列表查询', 'com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor', 'SystemSmartContract', 'getSmartContractList', 'QuerySmartContractListApproval', null, '2020-10-16 14:00:30', '2020-10-16 14:00:31');
INSERT INTO `interface` VALUES ('76', '业务智能合约管理', 'com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor', 'SystemSmartContract', 'updateSmartContractAuthorized', 'List<SmartContractAuthorizedApproval>', null, '2020-10-16 14:00:30', '2020-10-16 14:00:31');
INSERT INTO `interface` VALUES ('77', '业务智能合约管理', 'com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor', 'SystemSmartContract', 'updateSmartContractFlag', 'List<SmartContractDeployToken>', null, '2020-10-16 14:00:30', '2020-10-16 14:00:31');
INSERT INTO `interface` VALUES ('78', '部署和升级业务智能合约', 'com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor', 'SystemSmartContract', 'deploySmartContract', 'List<SmartContractDeployToken>', null, '2020-10-16 14:00:30', '2020-10-16 14:00:31');
INSERT INTO `interface` VALUES ('79', '删除成员（组织和业务应用）', 'com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor', 'SystemSmartContract', 'deleteMember', 'List<MemberUpdateApproval>', null, '2020-10-16 14:00:30', '2020-10-16 14:00:31');
INSERT INTO `interface` VALUES ('80', '根据区块Hash查询某个区块完整信息', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'queryWholeBlockByHash', 'BlockHashReq', null, '2020-10-27 15:19:46', '2020-10-27 15:19:50');
INSERT INTO `interface` VALUES ('81', '根据区块高度查询某个区块完整信息', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'queryWholeBlockByHeight', 'BlockHeightReq', null, '2020-10-27 15:24:24', '2020-10-27 15:24:26');
INSERT INTO `interface` VALUES ('82', '根据交易Hash查询某个区块完整信息', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'queryWholeBlockByTransHash', 'TransHashReq', null, '2020-10-27 15:26:01', '2020-10-27 15:26:02');
INSERT INTO `interface` VALUES ('83', '根据交易客户端Id查询某个区块完整信息', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'queryWholeBlockByTransClientTxId', 'TransClientTxIdReq', null, '2020-10-27 15:27:05', '2020-10-27 15:27:07');

-- 权限表
DROP TABLE IF EXISTS auth;
create table auth
(
    ID               int(10) auto_increment
        primary key,
    AUTH_NAME        varchar(64)  not null comment '权限名称',
    AUTH_CODE        varchar(64)  null comment '权限code',
    AUTH_DESCRIPTION varchar(512) null comment '权限说明',
    INTERFACE_ID     varchar(255) null comment '接口id，可多个',
    CREATE_TIME      timestamp    null comment '创建时间',
    UPDATE_TIME      timestamp    null comment '更新时间'
);

INSERT INTO `auth` VALUES ('1', '业务写合约调用', 'write_business_contract', '业务写合约调用权限', '1', '2020-10-16 10:36:21', '2020-10-16 10:36:22');
INSERT INTO `auth` VALUES ('2', '业务读合约调用', 'read_business_contract', '业务读合约调用权限', '2', '2020-10-16 10:37:03', '2020-10-16 10:37:04');
INSERT INTO `auth` VALUES ('3', '单个交易查询', 'query_transaction', '单个交易查询权限', '4,5,6', '2020-10-16 14:19:26', '2020-10-16 14:19:27');
INSERT INTO `auth` VALUES ('4', '批量交易查询', 'query_batch_transaction', '批量交易查询权限', '7,8', '2020-10-16 14:19:26', '2020-10-16 14:19:27');
INSERT INTO `auth` VALUES ('5', '区块查询', 'query_block', '区块查询权限', '9,10,12,13', '2020-10-16 14:26:43', '2020-10-16 14:26:44');
INSERT INTO `auth` VALUES ('6', '批量区块查询', 'query_batch_block', '批量区块查询权限', '14,11', '2020-10-16 14:19:26', '2020-10-16 14:19:27');
INSERT INTO `auth` VALUES ('7', '单个世界状态查询', 'query_world_state', '单个世界状态查询权限', '15', '2020-10-16 14:19:26', '2020-10-16 14:19:27');
INSERT INTO `auth` VALUES ('8', '节点监控', 'monitor_peer', '节点监控权限', '17,20', '2020-10-16 14:19:26', '2020-10-16 14:19:27');
INSERT INTO `auth` VALUES ('9', '通道管理', 'manage_channel', '通道管理权限', '21,22,23,24,25,61', '2020-10-16 14:19:26', '2020-10-16 14:19:27');
INSERT INTO `auth` VALUES ('10', '节点管理', 'manage_peer', '节点管理权限', '25,26,27,62,63,64,65', '2020-10-16 14:19:26', '2020-10-16 14:19:27');
INSERT INTO `auth` VALUES ('11', '共识节点', 'consensus_peer', '共识节点权限', '28,29,30,46,47,48,49,50,51,52,53,54,58,59,60', '2020-10-16 14:19:26', '2020-10-16 14:19:27');
INSERT INTO `auth` VALUES ('12', '角色查询', 'query_role', '角色查询权限', '3,66,67', '2020-10-16 14:19:26', '2020-10-16 14:19:27');
INSERT INTO `auth` VALUES ('13', '角色管理', 'manage_role', '角色管理权限', '25,68,69', '2020-10-16 14:19:26', '2020-10-16 14:19:27');
INSERT INTO `auth` VALUES ('14', '成员查询（成员包括组织和业务应用）', 'query_member', '成员查询（成员包括组织和业务应用权限', '3,70,71', '2020-10-16 14:19:26', '2020-10-16 14:19:27');
INSERT INTO `auth` VALUES ('15', '成员管理', 'manage_member', '成员管理权限', '3,72,73,79', '2020-10-16 14:19:26', '2020-10-16 14:19:27');
INSERT INTO `auth` VALUES ('16', '业务合约查询', 'query_contract', '业务合约查询权限', '3,74,75', '2020-10-16 14:19:26', '2020-10-16 14:19:27');
INSERT INTO `auth` VALUES ('17', '业务合约管理', 'manage_contract', '业务合约管理权限', '25,76,77,78', '2020-10-16 14:19:26', '2020-10-16 14:19:27');
INSERT INTO `auth` VALUES ('18', '区块完整信息查询', 'query_whole_block', '区块完整信息查询权限', '80,81,82,83', '2020-10-27 15:34:07', '2020-10-27 15:34:09');
-- 角色表增加 auth_id字段，用于记录角色对应的权限id
alter table role
	add AUTH_ID VARCHAR(1000) null comment '多个权限id，以逗号（“,”)分割' after SHORT_NAME;

-- 20201018
-- 角色表新增状态字段
ALTER TABLE `role`
ADD COLUMN `STATE`  int(2) NULL COMMENT '状态(1正常 ;2业务删除)' AFTER `MEMBER_DEL_STRATEGY`;
TRUNCATE TABLE `role`;
-- 预定义角色内容更新
INSERT INTO `role` VALUES (1, 'DEE826D175964131B7362E2DD99BB058', '组织', 'organization', '10,9,13,15,17', '\"MANAGER_AGREE\"', 1, '', NULL, '代表现实世界中的组织，组织的最核心的特点是可以颁发子证书，也即创建新的成员。', '2020-7-8 15:45:44', '2020-7-16 18:34:36', '\"PARENT_AGREE\"', 1);
INSERT INTO `role` VALUES (2, 'D956376A79E4425B9DDE1D18B142D2C2', '应用', 'application', '1,2,3,7', '\"MANAGER_AGREE\"', 1, '', NULL, '应用代表使用区块链服务的一个其他业务应用，应用不可颁发子证书。', '2020-7-8 15:45:44', '2020-7-16 18:34:36', '\"PARENT_AGREE\"', 1);
INSERT INTO `role` VALUES (3, 'A6F120875E2843D49D63806C4A1A8146', '管理员', 'admin', '10,9,13,15,17', '\"MANAGER_AGREE\"', 1, '', NULL, '拥有通道中组织的全部权限，同时作为治理策略的一个角色来控制策略，一个组织创建通道后，系统自动添加该组织为通道管理员角色。', '2020-10-18 10:36:11', '2020-10-18 10:36:20', '\"PARENT_AGREE\"', 1);
INSERT INTO `role` VALUES (4, 'E6620884872F4F0298E08385975CE39F', '节点', 'peer', '11', '\"MANAGER_AGREE\"', 1, NULL, NULL, '完成交易和共识', '2020-10-18 10:42:40', '2020-10-18 10:42:45', '\"PARENT_AGREE\"', 1);
INSERT INTO `role` VALUES (5, 'EE2DE129F32B4A5E8E94321E457AA79A', '运维监控', 'maintenance', '4,5,6,7,12,14,16,8,18', '\"MANAGER_AGREE\"', 1, NULL, NULL, '采集通道、节点运行的各个数据产生告警、图表等', '2020-10-18 10:47:42', '2020-10-18 10:47:46', '\"PARENT_AGREE\"', 1);

-- 修改索引
ALTER TABLE `smart_contract`
DROP INDEX ALISA,
ADD UNIQUE ALISA  (`ALISA`, `SC_VERSION`,`SC_CHANNEL_ID`) USING BTREE;

-- 新增通道字段
ALTER TABLE `CHANNEL`
ADD  `MEMBER_MODIFY_STRATEGY` varchar(128)  DEFAULT NULL COMMENT '成员修改控制策略' AFTER `MEMBER_ADD_STRATEGY`;

-- short_name唯一
create unique index role_SHORT_NAME_uindex
	on role (SHORT_NAME);

-- 20201026
alter table block_cache add PERSISTENCE_TIMESTAMP timestamp null after BLOCK_WITNESS;
alter table block_cache add CONSENSUS_TIMESTAMP timestamp null after BLOCK_WITNESS;
alter table block add PERSISTENCE_TIMESTAMP timestamp null after BLOCK_WITNESS;
alter table block add CONSENSUS_TIMESTAMP timestamp null after BLOCK_WITNESS;

-- 20201028
-- interface`表insert记录
INSERT INTO `interface` (`ID`, `INTERFACE_NAME`, `CLASS_PATH`, `CLASS_NAME`, `METHOD_NAME`, `METHOD_ARGS`, `REMARK`, `CREATE_TIME`, `UPDATE_TIME`) VALUES ('84', '删除自定义角色', 'com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor', 'SystemSmartContract', 'deleteCustomRole', 'List<RoleDeleteApproval>', NULL, '2020-10-28 09:44:56', '2020-10-28 09:44:58');
INSERT INTO `interface` (`ID`, `INTERFACE_NAME`, `CLASS_PATH`, `CLASS_NAME`, `METHOD_NAME`, `METHOD_ARGS`, `REMARK`, `CREATE_TIME`, `UPDATE_TIME`) VALUES ('85', '查询节点在某个通道的共识状态', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'queryConsensusStateOfPeer', 'RPCParam', NULL, '2020-10-28 10:01:57', '2020-10-28 10:01:59');
INSERT INTO `interface` (`ID`, `INTERFACE_NAME`, `CLASS_PATH`, `CLASS_NAME`, `METHOD_NAME`, `METHOD_ARGS`, `REMARK`, `CREATE_TIME`, `UPDATE_TIME`) VALUES ('86', '根据区块中的交易序号范围查询某区块的交易列表', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'queryTransOfBlockByBlockHash', 'TransRegionBlockHashReq', NULL, '2020-10-28 10:24:09', '2020-10-28 10:24:11');
UPDATE `interface` SET `ID`='20', `INTERFACE_NAME`='查询节点当前状态：状态、服务地址、系统负载信息、加入的通道简要信息', `CLASS_PATH`='com.jinninghui.newspiral.gateway', `CLASS_NAME`='ServiceForSDKImpl', `METHOD_NAME`='getPeerCurrentState', `METHOD_ARGS`='RPCParam', `REMARK`=NULL, `CREATE_TIME`='2020-10-16 11:51:51', `UPDATE_TIME`='2020-10-16 11:51:53' WHERE (`ID`='20');
UPDATE `interface` SET `ID`='6', `INTERFACE_NAME`='根据交易所属区块Hash和交易在区块中的序号查询交易', `CLASS_PATH`='com.jinninghui.newspiral.gateway', `CLASS_NAME`='ServiceForSDKImpl', `METHOD_NAME`='queryTransByBlockHashAndTransIndex', `METHOD_ARGS`='TransBlockHashReq', `REMARK`=NULL, `CREATE_TIME`='2020-10-16 11:41:18', `UPDATE_TIME`='2020-10-16 11:41:19' WHERE (`ID`='6');
-- update auth表
UPDATE `auth` SET `ID`='13', `AUTH_NAME`='角色管理', `AUTH_CODE`='manage_role', `AUTH_DESCRIPTION`='角色管理权限', `INTERFACE_ID`='25,68,69,84', `CREATE_TIME`='2020-10-16 14:19:26', `UPDATE_TIME`='2020-10-16 14:19:27' WHERE (`ID`='13');
UPDATE `auth` SET `ID`='8', `AUTH_NAME`='节点监控', `AUTH_CODE`='monitor_peer', `AUTH_DESCRIPTION`='节点监控权限', `INTERFACE_ID`='17,20,85', `CREATE_TIME`='2020-10-16 14:19:26', `UPDATE_TIME`='2020-10-16 14:19:27' WHERE (`ID`='8');
UPDATE `auth` SET `ID`='4', `AUTH_NAME`='批量交易查询', `AUTH_CODE`='query_batch_transaction', `AUTH_DESCRIPTION`='批量交易查询权限', `INTERFACE_ID`='7,8,86', `CREATE_TIME`='2020-10-16 14:19:26', `UPDATE_TIME`='2020-10-16 14:19:27' WHERE (`ID`='4');
UPDATE `interface` SET  `METHOD_NAME`='updateSmartContractState',`METHOD_ARGS`='List<SmartContractUpdateStateToken>' WHERE (`ID`='77');
INSERT INTO `interface` (`ID`, `INTERFACE_NAME`, `CLASS_PATH`, `CLASS_NAME`, `METHOD_NAME`, `METHOD_ARGS`, `REMARK`, `CREATE_TIME`, `UPDATE_TIME`) VALUES ('87', '节点加入通道', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'addMySelf2Channel', 'AddMySelfToChannelReq', NULL, '2020-10-28 10:24:09', '2020-10-28 10:24:11');
UPDATE auth set INTERFACE_ID='25,26,27,62,63,64,65,87' WHERE (`ID`='10');
UPDATE `interface` SET  `CLASS_PATH`='com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor', `CLASS_NAME`='SystemSmartContract', `METHOD_NAME`='exitMyPeerFromChannel',`METHOD_ARGS`='List<RemovePeerFromChannelToken>' WHERE (`ID`='27');


-- 2020/10/30 role表去掉short_name唯一索引
ALTER TABLE `role`
DROP INDEX `role_SHORT_NAME_uindex`;

-- 20201105
-- 修改世界状态附属表
-- auto-generated definition
DROP TABLE IF EXISTS state_attach;
create table state_attach
(
    id                  bigint auto_increment
        primary key,
    CHANNEL_ID          varchar(128) not null comment '通道id',
    STATE_KEY           varchar(128) not null comment '世界状态key',
    LATEST_BLOCK_HASH   varchar(100) null comment '最新区块哈希',
    LATEST_BLOCK_HEIGHT bigint       null comment '最新区块高度',
    LATEST_TRANS_HASH   varchar(100) null comment '最新更新的交易hash'
)
    collate = utf8_bin;
    comment '世界状态附属信息';


-- 20201106
-- 将state_value改为blob类型
alter table state modify STATE_VALUE blob not null;
-- 创建索引
create index transaction_channel_id_create_timestamp
	on transaction (CHANNEL_ID, CREATE_TIMESTAMP);

-- 20201112 脚本微调
DROP TABLE IF EXISTS state_attach;
create table state_attach
(
    id                  bigint auto_increment
        primary key,
    CHANNEL_ID          varchar(128) not null comment '通道id',
    STATE_KEY           varchar(128) not null comment '世界状态key',
    LATEST_BLOCK_HASH   varchar(100) null comment '最新区块哈希',
    LATEST_BLOCK_HEIGHT bigint       null comment '最新区块高度',
    LATEST_TRANS_HASH   varchar(100) null comment '最新更新的交易hash'
)
    collate = utf8_bin
    comment '世界状态附属信息';
   -- 修改state索引
drop index KEY_UNIQUE on state;
create unique index KEY_UNIQUE
	on state (CHANNEL_ID, STATE_KEY);
	-- 修改权限接口
INSERT INTO `interface` VALUES ('88', '查询节点的所有通道信息', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'queryAllChannelOfPeer', 'RPCParam', null, '2020-10-27 15:26:01', '2020-10-27 15:26:02');
INSERT INTO `interface` VALUES ('89', '查询节点所属最新通道信息', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'queryOneChannelOfPeer', 'RPCParam', null, '2020-10-27 15:27:05', '2020-10-27 15:27:07');
UPDATE auth set INTERFACE_ID='17,20,88,89' WHERE (`ID`='8');
