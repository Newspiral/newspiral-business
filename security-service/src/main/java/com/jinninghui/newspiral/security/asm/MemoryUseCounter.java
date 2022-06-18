package com.jinninghui.newspiral.security.asm;

import com.jinninghui.newspiral.security.contract.SandboxException;

import java.lang.management.ManagementFactory;
import java.util.HashMap;

public class MemoryUseCounter {

    private static final ThreadLocal<ThreadLocalCounter> threadLocal = new InheritableThreadLocal<>();

    // 最大的内存消耗
    public static final long costMemoryMax = 20 * 1024 * 1024;

    public static void incr() {

        // 判断线程执行是否被中止
        if (Thread.interrupted()) {
            throw new SandboxException("Thread execution has been interrupted!");
        }

        // 检查内存使用是否超出上限
        ThreadLocalCounter threadLocalCounter = threadLocal.get();
        if (threadLocalCounter == null) {
            threadLocalCounter = new ThreadLocalCounter();
            threadLocal.set(threadLocalCounter);
        }
        if (threadLocalCounter.calculateMemory() > costMemoryMax) {
            throw new SandboxException("Memory use exceed limit");
        }
    }

    public static void init() {

        // 判断线程执行是否被中止
        if (Thread.interrupted()) {
            throw new SandboxException("Thread execution has been interrupted!");
        }
        // 检查内存使用是否超出上限
        ThreadLocalCounter threadLocalCounter = threadLocal.get();
        if (threadLocalCounter == null) {
            threadLocalCounter = new ThreadLocalCounter();
            threadLocal.set(threadLocalCounter);
        }
        threadLocalCounter.initMemory();
    }

    public static void clear() {
        threadLocal.remove();
    }

    // 标记类
    private static final class ThreadLocalCounter {

        com.sun.management.ThreadMXBean memoryThreadMXBean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        long startMemory;
        HashMap<Long, Long> map = new HashMap<>();

        // 计算内存使用
        long calculateMemory() {
            ThreadGroup group = Thread.currentThread().getThreadGroup();
            Thread[] threads = new Thread[group.activeCount()];
            group.enumerate(threads);
            for (Thread thread : threads) {
                if (thread != null) {
                    map.put(thread.getId(),memoryThreadMXBean.getThreadAllocatedBytes(thread.getId()));
                }
            }
            long currentMemory = map.values().stream().mapToLong(l -> l).sum();
            return currentMemory - startMemory;
        }

        //计算初始的内存使用
        void initMemory() {
            if (startMemory != 0) {
                return;
            }
            ThreadGroup group = Thread.currentThread().getThreadGroup();
            Thread[] threads = new Thread[group.activeCount()];
            group.enumerate(threads);
            long currentMemory = 0;
            for (Thread thread : threads) {
                if (thread != null) {
                    currentMemory += memoryThreadMXBean.getThreadAllocatedBytes(thread.getId());
                }
            }
            startMemory = currentMemory;
        }
    }
}
