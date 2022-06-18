package com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor.contract;

import com.jinninghui.newspiral.security.contract.BussinessContractCallerMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ConcurrentReferenceHashMap;


@Slf4j
public class DefaultContractMonitor implements BussinessContractCallerMonitor {

    private ConcurrentReferenceHashMap<Long, Throwable> throwableMap = new ConcurrentReferenceHashMap<>(10, ConcurrentReferenceHashMap.ReferenceType.WEAK);


    protected ConcurrentReferenceHashMap<Long, Throwable> getThrowableMap() {
        return throwableMap;
    }




    /*@Override
    public boolean monitoring() {
        //监控
        start=Boolean.TRUE;
        long memoryByte = 0;
        try {
            monitorCountDownLatch.await();
            final long startMemory = memoryThreadMXBean.getThreadAllocatedBytes(threadToMonitor.getId());
            long currentMemiory = startMemory;
            while (!stop) {
                currentMemiory = Math.max(getMemoryAllocated(), currentMemiory);
                memoryByte = currentMemiory - startMemory;
                boolean memoryExceeded = memoryByte > maxMemory;
                if (memoryExceeded) {
                    log.error("当前合约内存消耗：{} 字节,已超出最大资源限制,强制终止!", memoryByte);
                    System.gc();
                    return false;
                }
            }
        } catch (Exception e) {
           log.error("monitoring occured error:",e);
        }
        log.info("当前合约内存消耗：{} 字节",memoryByte);
        return true;
    }*/


    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Throwable throwable = throwableMap.get(t.getId());
        if (throwable != null) {
            e.initCause(throwable);
        }
        throwableMap.put(t.getId(), e);
    }
}
