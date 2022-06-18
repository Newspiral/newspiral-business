package com.jinninghui.newspiral.ledger.mgr.impl;

import com.alibaba.fastjson.JSON;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContract;
import com.jinninghui.newspiral.common.entity.state.ModuleClassification;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContractModelKeyReq;
import com.jinninghui.newspiral.common.entity.state.WorldState;
import com.jinninghui.newspiral.common.entity.state.WorldStateModifyRecord;
import com.jinninghui.newspiral.common.entity.state.WorldStateResp;
import com.jinninghui.newspiral.common.entity.transaction.Transaction;
import com.jinninghui.newspiral.common.entity.transaction.TransactionResp;
import com.jinninghui.newspiral.ledger.mgr.LedgerMgr;
import com.jinninghui.newspiral.ledger.mgr.LedgerThreadLocalContext;
import com.jinninghui.newspiral.ledger.mgr.SmartContractMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lida
 * @date 2019/7/5 18:46
 */
@Component
public class SmartContractMgrImpl implements SmartContractMgr {

    private static Logger log = LoggerFactory.getLogger(SmartContractMgrImpl.class);

    @Autowired
    private LedgerMgr ledgerMgr;

    @Override
    public byte[] getState(String key) {
        return ledgerMgr.queryState(key);
    }

    @Override
    public String getStrState(String key) {
        return new String(ledgerMgr.queryState(key),StandardCharsets.UTF_8);
    }

    @Override
    public void insertState(String key, byte[] value) {
        //log.info("调用智能合约插入世界状态key=" + key);
        ledgerMgr.insertState(key, value);
    }

    @Override
    public void insertStrState(String key, String value) {
        //log.info("调用智能合约插入世界状态key=" + key);
        ledgerMgr.insertState(key, value.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void updateState(String key, byte[] newValue) {
        //log.info("调用智能合约更新世界状态key=" + key);
        ledgerMgr.updateState(key, newValue);
        //log.info("update state time {}", System.currentTimeMillis() - curr);
    }

    @Override
    public void updateStrState(String key, String newValue) {
        ledgerMgr.updateState(key, newValue.getBytes(StandardCharsets.UTF_8));
    }


    @Override
    public void putState(String key, byte[] value) {
        //Long curr = System.currentTimeMillis();
        ledgerMgr.putState(key, value);
        //log.info("put state time {}", System.currentTimeMillis() - curr);
    }


    @Override
    public void putStrState(String key, String value) {
        //Long curr = System.currentTimeMillis();
        ledgerMgr.putState(key, value.getBytes(StandardCharsets.UTF_8));
        //log.info("put state time {}", System.currentTimeMillis() - curr);
    }

    @Override
    public boolean existKey(String key) {
        //log.info("调用智能合约检查世界状态存在key=" + key);
        return ledgerMgr.existKey(key);
    }

    @Override
    public boolean deleteKey(String key) {
        //log.info("调用智能合约删除世界状态存在key=" + key);
        return ledgerMgr.deleteKey(key);
    }

    @Override
    public String getBlockTest(String channelId,Integer num) {
        log.info(ModuleClassification.LedM_SCMI_ +"调用智能合约读取区块channelId=" + channelId);
        return JSON.toJSONString(ledgerMgr.queryBlock(channelId,num));
    }

    @Override
    public SmartContract getSmartContact(String scName, String scVersion, String scChannelId) {
        log.info(ModuleClassification.LedM_SCMI_ +"调用智能合约读取业务智能合约channelId:{},scName:{},scVersion:{}", scChannelId, scName, scVersion);
        SmartContractModelKeyReq smartContractModelKeyReq=new SmartContractModelKeyReq();
        smartContractModelKeyReq.setScName(scName);
        smartContractModelKeyReq.setScChannelId(scChannelId);
        smartContractModelKeyReq.setScVersion(scVersion);
        return ledgerMgr.getSmartContractByKey(smartContractModelKeyReq);
    }

    @Override
    public List<TransactionResp> queryTxHistory(String key) {
        String channelId = LedgerThreadLocalContext.currChannelId.get();
        if (StringUtils.isEmpty(channelId) ||
                StringUtils.isEmpty(key)
        ) {
            return null;
        }
        List<TransactionResp> result = new ArrayList<>();
        List<Transaction> transactions = ledgerMgr.queryTxHistory(channelId, key);
        for (Transaction tx : transactions) {
            if (tx.getChannelId().equals(channelId) == false) {
                continue;
            }
            List<WorldStateModifyRecord> records = JSON.parseArray(tx.getModifiedWorldStateList(), WorldStateModifyRecord.class);
            for (WorldStateModifyRecord record : records) {
                WorldState state = record.getOldState() == null ? record.getNewState() : record.getOldState();
                if (state.getKey().equals(key)) {
                    result.add(TransactionResp.transferTransactionResp(tx));
                    break;
                }
            }
        }
        if (CollectionUtils.isEmpty(result)) {
            //查询数据不存在
            return null;
        }
        return result;
    }

    @Override
    public List<WorldStateResp> queryStatesByTimeRegion(Long startTime, Long endTime) {
        String channelId = LedgerThreadLocalContext.currChannelId.get();
        if (StringUtils.isEmpty(channelId)) {
            return null;
        }
        log.info(ModuleClassification.LedM_SCMI_ +"调用智能合约读取时间范围内的世界状态列表，channelId={},startTime={},endTime={}" , channelId,startTime,endTime);
        List<WorldStateResp> worldStateResps = ledgerMgr.queryStatesByTimeRegion(channelId, startTime, endTime);
        return worldStateResps;
    }

    @Override
    public List<TransactionResp> queryStatesHistory(Long startTime, Long endTime, String key){
        String channelId = LedgerThreadLocalContext.currChannelId.get();
        if (StringUtils.isEmpty(channelId)) {
            return null;
        }
        log.info(ModuleClassification.LedM_SCMI_ +"调用智能合约读取时间范围内的世界状态历史，channelId={},startTime={},endTime={},key={}" , channelId,startTime,endTime,key);
        return ledgerMgr.queryStatesHistory(channelId,startTime, endTime, key);
    }
}
