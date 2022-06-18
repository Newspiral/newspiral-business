package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.chain.ChannelModifyRecord;
import com.jinninghui.newspiral.common.entity.identity.IdentityKey;
import com.jinninghui.newspiral.common.entity.state.WorldStateModifyRecord;
import com.jinninghui.newspiral.common.entity.transaction.ExecutedTransaction;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;
import com.jinninghui.newspiral.common.entity.transaction.Transaction;

import java.util.Date;

public class TransactionModelWithBLOBs extends TransactionModel {
    private String pooledTrans;

    private String modifiedWorldStateList;

    private String modifiedChannelRecordList;


    public String getPooledTrans() {
        return pooledTrans;
    }

    public void setPooledTrans(String pooledTrans) {
        this.pooledTrans = pooledTrans;
    }

    public String getModifiedWorldStateList() {
        return modifiedWorldStateList;
    }

    public void setModifiedWorldStateList(String modifiedWorldStateList) {
        this.modifiedWorldStateList = modifiedWorldStateList;
    }

    public String getModifiedChannelRecordList() {
        return modifiedChannelRecordList;
    }

    public void setModifiedChannelRecordList(String modifiedChannelRecordList) {
        this.modifiedChannelRecordList = modifiedChannelRecordList;
    }

    public static TransactionModelWithBLOBs createInstance(ExecutedTransaction transaction, Block block, int indexInBlock)
    {
        TransactionModelWithBLOBs model = new TransactionModelWithBLOBs();
        SDKTransaction st = transaction.getSdkTransaction();
        model.setAdd2PoolTimestamp(new Date(st.getClientTimestamp()));
        model.setBlockHash(block.getHash());
        model.setChannelId(block.getBlockHeader().getChannelId());
        model.setBlockId(block.getBlockHeader().getHeight());
        model.setClientIdentityKey(JSON.toJSONString(st.getSignerIdentityKey().getIdentityKey()));
        model.setClientTimestamp(new Date(st.getClientTimestamp()));
        model.setClientTransId(st.getClientTxId());
        model.setErrorMsg(transaction.getErrorMsg());
        model.setExecuteTimestamp(new Date(transaction.getExecuteTimestamp()));
        model.setExecutedMs(transaction.getExecutedMs());
        model.setIndexInBlock(indexInBlock);
        //model.setModifiedChannelRecordList(JSON.toJSONString(transaction.getModifiedChannelRecordList()));
        model.setModifiedChannelRecordList(JSON.toJSONString(transaction.getModifiedChannelRecordList(),new SerializerFeature[]{SerializerFeature.DisableCircularReferenceDetect}));
        model.setModifiedWorldStateList(JSON.toJSONString(transaction.getModifiedWorldStateList()));
        model.setPooledTrans(JSON.toJSONString(st,new SerializerFeature[]{SerializerFeature.DisableCircularReferenceDetect}));
        model.setPooledTransVersion(st.getVersion());
        model.setSdkTransVersion(st.getVersion());
        model.setSmartContractId(st.getSmartContractCallInstnace().getSmartContractId());
        model.setSmartContractMethodName(st.getSmartContractCallInstnace().getMethodName());
        if(transaction.getPass().equals("1")) {
            model.setSuccessed(Byte.valueOf("1"));
        }
        else {
            model.setSuccessed(Byte.valueOf("0"));
        }
        model.setVersion(transaction.getVersion());
        model.setCreateTimestamp(new Date());
        model.setTransHashStr(st.getHash());
        return model;
    }

    public ExecutedTransaction toExecutionTransaction() {
        ExecutedTransaction et = new ExecutedTransaction();
        et.setErrorMsg(this.getErrorMsg());
        et.setExecutedMs(this.getExecutedMs());
        et.setExecuteTimestamp(this.getExecuteTimestamp().getTime());
        et.setModifiedChannelRecordList(JSON.parseArray(this.getModifiedChannelRecordList(), ChannelModifyRecord.class));
        et.setModifiedWorldStateList(JSON.parseArray(this.getModifiedWorldStateList(), WorldStateModifyRecord.class));
        et.setSdkTransaction(JSON.parseObject(this.getPooledTrans(),SDKTransaction.class));
        if(this.getSuccessed()>0) {
            et.setPass("1");
        }
        else
        {
            et.setPass("0");
        }
        et.setVersion(this.getVersion());
        return et;
    }

    public  Transaction toTransaction()
    {
        Transaction model = new Transaction();
        model.setAdd2PoolTimestamp(this.getAdd2PoolTimestamp());
        model.setBlockHashStr(this.getBlockHash());
        model.setChannelId(this.getChannelId());
        model.setBlockId(this.getBlockId());
        model.setClientIdentityKey(JSON.parseObject(this.getClientIdentityKey(), IdentityKey.class));
        model.setClientTimestamp(this.getClientTimestamp());
        model.setClientTransId(this.getClientTransId());
        model.setErrorMsg(this.getErrorMsg());
        model.setExecuteTimestamp(this.getExecuteTimestamp());
        model.setExecutedMs(this.getExecutedMs());
        model.setIndexInBlock(this.getIndexInBlock());
        model.setModifiedChannelRecordList(this.getModifiedChannelRecordList());
        model.setModifiedWorldStateList(this.getModifiedWorldStateList());
        model.setPooledTrans(this.getPooledTrans());
        model.setPooledTransVersion(this.getPooledTransVersion());
        model.setSdkTransVersion(this.getSdkTransVersion());
        model.setSmartContractId(this.getSmartContractId());
        model.setSmartContractMethodName(this.getSmartContractMethodName());
        if(this.getSuccessed()>0) {
            model.setSuccessed(true);
        }
        else {
            model.setSuccessed(false);
        }
        model.setTransHashStr(this.getTransHashStr());
        model.setVersion(this.getVersion());
        model.setCreateTimestamp(this.getCreateTimestamp());
        return model;
    }
}
