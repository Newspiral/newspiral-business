/*
package com.jinninghui.newspiral.ledger.mgr.impl.domain;

import com.alibaba.fastjson.JSON;
import com.jinninghui.newspiral.common.entity.block.Block;
import com.jinninghui.newspiral.common.entity.chain.ChannelModifyRecord;
import com.jinninghui.newspiral.common.entity.state.WorldStateModifyRecord;
import com.jinninghui.newspiral.common.entity.transaction.ExecutedTransaction;
import com.jinninghui.newspiral.common.entity.transaction.PooledTransaction;
import com.jinninghui.newspiral.common.entity.transaction.SDKTransaction;

import java.util.Date;
import java.util.List;

public class TransactionModelWithBLOBs extends TransactionModel {
    private String pooledTrans;

    private String modifiedWorldStateList;

    private String modifiedChannelRecord;

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

    public String getModifiedChannelRecord() {
        return modifiedChannelRecord;
    }

    public void setModifiedChannelRecord(String modifiedChannelRecord) {
        this.modifiedChannelRecord = modifiedChannelRecord;
    }

    public static TransactionModelWithBLOBs createInstance(ExecutedTransaction transaction, Block block, int indexInBlock)
    {
        TransactionModelWithBLOBs model = new TransactionModelWithBLOBs();
        PooledTransaction pt = transaction.getSdkTransaction();
        SDKTransaction st = pt.getSdkTransaction();
        model.setAdd2PoolTimestamp(new Date(pt.getAdd2PoolTimestamp()));
        model.setBlockHashStr(block.getHash());
        model.setChannelId(block.getBlockHeader().getChannelId());
        model.setClientIdentityKey(st.getCallerIdentity().getIdentityKey().getValue());
        model.setClientTimestamp(new Date(st.getClientTimestamp()));
        model.setClientTransId(st.getClientTxId());
        model.setErrorMsg(transaction.getErrorMsg());
        model.setExecuteTimestamp(new Date(transaction.getExecuteTimestamp()));
        model.setExecutedMs(transaction.getExecutedMs());
        model.setIndexInBlock(indexInBlock);
        model.setModifiedChannelRecord(JSON.toJSONString(transaction.getModifiedChannelRecord()));
        model.setModifiedWorldStateList(JSON.toJSONString(transaction.getModifiedWorldStateList()));
        model.setPooledTrans(JSON.toJSONString(pt));
        model.setPooledTransVersion(pt.getVersion());
        model.setSdkTransVersion(st.getVersion());
        model.setSmartContractId(st.getSmartContractCallInstnace().getSmartContractId());
        model.setSmartContractMethodName(st.getSmartContractCallInstnace().getMethodName());
        if(transaction.isSuccessed()) {
            model.setPass(Byte.valueOf("1"));
        }
        else {
            model.setPass(Byte.valueOf("0"));
        }
        model.setTransHashStr(pt.getHash());
        model.setVersion(transaction.getVersion());

        return model;
    }

    public ExecutedTransaction toExecutionTransaction() {
        ExecutedTransaction et = new ExecutedTransaction();
        et.setErrorMsg(this.getErrorMsg());
        et.setExecutedMs(this.getExecutedMs());
        et.setExecuteTimestamp(this.getExecuteTimestamp().getTime());
        et.setModifiedChannelRecord(JSON.parseObject(this.getModifiedChannelRecord(), ChannelModifyRecord.class));
        et.setModifiedWorldStateList(JSON.parseArray(this.getModifiedWorldStateList(), WorldStateModifyRecord.class));
        et.setSdkTransaction(JSON.parseObject(this.getPooledTrans(),PooledTransaction.class));
        if(this.getIfSuccessful()>0) {
            et.setPass(true);
        }
        else
        {
            et.setPass(false);
        }
        et.setVersion(this.getVersion());
        return et;
    }
}*/
