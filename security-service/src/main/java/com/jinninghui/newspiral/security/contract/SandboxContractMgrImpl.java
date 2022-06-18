package com.jinninghui.newspiral.security.contract;


import com.alipay.sofa.runtime.api.annotation.SofaReference;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContract;
import com.jinninghui.newspiral.common.entity.state.WorldStateResp;
import com.jinninghui.newspiral.common.entity.transaction.TransactionResp;
import com.jinninghui.newspiral.ledger.mgr.SmartContractMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Component
@Slf4j
public class SandboxContractMgrImpl implements SmartContractMgr {

    private final ThreadLocal<ExecutorService> executorTX = new InheritableThreadLocal<>();

    private final ThreadLocal<Thread.UncaughtExceptionHandler> localHandler=new InheritableThreadLocal<>();


    @SofaReference
    private SmartContractMgr smartContractMgr;

    public synchronized void init(ExecutorService executor,Thread.UncaughtExceptionHandler handler) {
        executorTX.set(executor);
        localHandler.set(handler);
    }

    @Override
    public byte[] getState(String key) {
        if (!Thread.interrupted()) {
            try {
                return executorTX.get().submit(() -> smartContractMgr.getState(key)).get();
            } catch (Exception e) {
                localHandler.get().uncaughtException(Thread.currentThread(),e);
            }
        }
        return null;
    }


    @Override
    public String getStrState(String key) {
        if (!Thread.interrupted()) {
            try {
                byte[] state =  executorTX.get().submit(() -> smartContractMgr.getState(key)).get();
                return new String(state, StandardCharsets.UTF_8);
            } catch (Exception e) {
                localHandler.get().uncaughtException(Thread.currentThread(),e);
            }
        }
        return null;
    }

    @Override
    public void insertState(String key, byte[] value) {
        if (!Thread.interrupted()) {
            try {
                executorTX.get().submit(() -> smartContractMgr.insertState(key, value)).get();
            } catch (Exception e) {
                localHandler.get().uncaughtException(Thread.currentThread(),e);
            }
        }
    }

    @Override
    public void insertStrState(String key, String value) {
        if (!Thread.interrupted()) {
            try {
                executorTX.get().submit(() -> smartContractMgr.insertState(key, value.getBytes(StandardCharsets.UTF_8))).get();
            } catch (Exception e) {
                localHandler.get().uncaughtException(Thread.currentThread(),e);
            }
        }
    }

    @Override
    public void updateState(String key, byte[] newValue) {
        if (!Thread.interrupted()) {
            try {
                executorTX.get().submit(() -> smartContractMgr.updateState(key, newValue)).get();
            } catch (Exception e) {
                localHandler.get().uncaughtException(Thread.currentThread(),e);
            }
        }
    }


    @Override
    public void updateStrState(String key, String newValue) {
        if (!Thread.interrupted()) {
            try {
                executorTX.get().submit(() -> smartContractMgr.updateState(key, newValue.getBytes(StandardCharsets.UTF_8))).get();
            } catch (Exception e) {
                localHandler.get().uncaughtException(Thread.currentThread(),e);
            }
        }
    }

    @Override
    public void putState(String key, byte[] value) {
        if (!Thread.interrupted()) {
            try {
                executorTX.get().submit(() -> smartContractMgr.putState(key, value)).get();
            } catch (Exception e) {
                localHandler.get().uncaughtException(Thread.currentThread(),e);
            }
        }
    }

    @Override
    public void putStrState(String key, String value) {
        if (!Thread.interrupted()) {
            try {
                executorTX.get().submit(() -> smartContractMgr.putState(key, value.getBytes(StandardCharsets.UTF_8))).get();
            } catch (Exception e) {
                localHandler.get().uncaughtException(Thread.currentThread(),e);
            }
        }
    }

    @Override
    public boolean existKey(String key) {
        if (!Thread.interrupted()) {
            try {
                return executorTX.get().submit(() -> smartContractMgr.existKey(key)).get();
            } catch (Exception e) {
                localHandler.get().uncaughtException(Thread.currentThread(),e);
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean deleteKey(String key) {
        if (!Thread.interrupted()) {
            try {
                return executorTX.get().submit(() -> smartContractMgr.deleteKey(key)).get();
            } catch (Exception e) {
                localHandler.get().uncaughtException(Thread.currentThread(),e);
                return false;
            }
        }
        return false;
    }

    @Override
    public String getBlockTest(String channelId, Integer num) {
        if (!Thread.interrupted()) {
            try {
                return executorTX.get().submit(() -> smartContractMgr.getBlockTest(channelId, num)).get();
            } catch (Exception e) {
                localHandler.get().uncaughtException(Thread.currentThread(),e);
                return null;
            }
        }
        return null;
    }

    @Override
    public SmartContract getSmartContact(String scName, String scVersion, String scChannelId) {
        if (!Thread.interrupted()) {
            try {
                return executorTX.get().submit(() -> smartContractMgr.getSmartContact(scName, scVersion, scChannelId)).get();
            } catch (Exception e) {
                localHandler.get().uncaughtException(Thread.currentThread(),e);
                return null;
            }
        }
        return null;
    }

    @Override
    public List<TransactionResp> queryTxHistory(String key) {
        if (!Thread.interrupted()) {
            try {
                return executorTX.get().submit(() -> smartContractMgr.queryTxHistory(key)).get();
            } catch (Exception e) {
                localHandler.get().uncaughtException(Thread.currentThread(),e);
                return null;
            }
        }
        return null;
    }

    @Override
    public List<WorldStateResp> queryStatesByTimeRegion(Long startTime, Long endTime) {
        if (!Thread.interrupted()) {
            try {
                return executorTX.get().submit(() -> smartContractMgr.queryStatesByTimeRegion(startTime, endTime)).get();
            } catch (Exception e) {
                localHandler.get().uncaughtException(Thread.currentThread(),e);
                return null;
            }
        }
        return null;
    }

    @Override
    public List<TransactionResp> queryStatesHistory(Long startTime, Long endTime, String key) {
        if (!Thread.interrupted()) {
            try {
                return executorTX.get().submit(() -> smartContractMgr.queryStatesHistory(startTime, endTime, key)).get();
            } catch (Exception e) {
                localHandler.get().uncaughtException(Thread.currentThread(),e);
                return null;
            }
        }
        return null;
    }
}
