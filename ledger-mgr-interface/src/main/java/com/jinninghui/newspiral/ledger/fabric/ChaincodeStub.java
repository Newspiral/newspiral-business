package com.jinninghui.newspiral.ledger.fabric;

import com.jinninghui.newspiral.ledger.mgr.SmartContractMgr;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @version V1.0
 * @Title: ChaincodeStub
 * @Package com.jinninghui.newspiral.ledger.mgr.impl.fabric
 * @Description:
 * @author: xuxm
 * @date: 2020/1/2 14:09
 */
@Component
public class ChaincodeStub {
    public SmartContractMgr smartContractMgr;
    public String channelId;
    public String txId;
    public List<String> args;


    public List<byte[]> getArgs() {
        return args.stream().map(x -> x.getBytes()).collect(toList());
    }

    public List<String> getStringArgs() {
        return args;
    }

    public String getFunction() {
        return getStringArgs().size() > 0 ? getStringArgs().get(0) : null;
    }

    public List<String> getParameters() {
        return getStringArgs().stream().skip(1).collect(toList());
    }

    public String getChannelId() {
        return channelId;
    }

    public String getTxId() {
        return txId;
    }

    public byte[] getState( String key) {
        return this.smartContractMgr.getState(key);
    }

    public void putState( String key,  byte[] value) {
        //validateKey(key);
        this.smartContractMgr.putState(key, value);
    }

    public void putStringState( String key,  String value) {
        putState(key, value.getBytes());
    }

    public void delState( String key) {
        this.smartContractMgr.deleteKey(key);
    }
}
