package com.jinninghui.newspiral.security.asm;

import com.jinninghui.newspiral.security.contract.SandboxException;

import java.util.HashMap;
import java.util.Map;

public class LoopCounter {

    private static ThreadLocal<LoopTimesRecorder> local = new ThreadLocal<>();


    // 单循环最大循环次数
    public static final long singleLoopMax = 500;

    /**
     * 检测循环体执行次数，如果执行次数超出设定的阈值，则抛出异常
     */
    public static void incr(String label) {

        // 判断线程执行是否被中止
        if (Thread.interrupted()) {
            throw new SandboxException("Thread execution has been interrupted!");
        }
        LoopTimesRecorder loopTimes = local.get();
        if (loopTimes == null) {
            loopTimes = new LoopTimesRecorder();
            local.set(loopTimes);
        }
        if (loopTimes.incrLabel(label) > singleLoopMax) {
            throw new SandboxException("Loop counter exceed limit");
        }
    }

    public static void clear(){
        local.remove();
    }


    // 标记类
    private static final class LoopTimesRecorder {

        // 记录每个label计数的次数
        private Map<String, Long> labelCounter = new HashMap<>();

        // 计数器+1
        long incrLabel(String label) {
            Long counter = labelCounter.get(label);
            if (counter == null) {
                labelCounter.put(label, 1L);
                return 1;
            }
            labelCounter.put(label, ++counter);
            return counter;
        }

        // 计数器清零
        void clearLabel() {
            this.labelCounter.clear();
        }
    }

}
