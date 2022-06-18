INSERT INTO `interface` (`ID`, `INTERFACE_NAME`, `CLASS_PATH`, `CLASS_NAME`, `METHOD_NAME`, `METHOD_ARGS`, `REMARK`,
                         `CREATE_TIME`, `UPDATE_TIME`)
values (101, '生成根证书', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl','createRootCert',
        'CertDataReq', NULL, '2021-02-03 00:48:11', '2021-02-03 00:48:11'),
       (102, '生成用户证书', 'com.jinninghui.newspiral.gateway', 'ServiceForSDKImpl', 'createUserCert',
        'CertDataReq', NULL, '2021-02-03 00:48:11', '2021-02-03 00:48:11');

update auth set INTERFACE_ID=concat(INTERFACE_ID,',101,102') where AUTH_NAME='节点监控';


