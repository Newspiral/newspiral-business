package com.jinninghui.newspiral.security.contract.sandbox;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class SandboxThread extends Thread {

    public SandboxThread(Runnable runnable, String alias) {
        super(new SandboxThreadGroup("SandboxThreadGroup " + alias + System.nanoTime()), runnable, "Sanbox- " + alias + System.nanoTime());
    }

    public static SandboxThread getSandbox(Thread thread) {
        if (thread instanceof SandboxThread) {
            return (SandboxThread) thread;
        }
        ThreadGroup group = thread.getThreadGroup();
        while (group != null) {
            if (group instanceof SandboxThread.SandboxThreadGroup) {
                return ((SandboxThread.SandboxThreadGroup) group).getMainThread();
            }
            group = group.getParent();
        }
        return null;
    }

    public SandboxThreadGroup getSandboxThreadGroup() {
        return (SandboxThreadGroup) super.getThreadGroup();
    }

    @Override
    public void run() {
        if (Thread.currentThread() != this) {
            throw new IllegalStateException("call .start() to start a SandboxThread");
        }
        ((SandboxThreadGroup) getThreadGroup()).setMainThread(this);
        super.run();
    }

    /**
     * 每个沙盒线程的ThreadGroup必须直接或间接成为其成员。用于识别线程是否具有受限的权限
     */
    public static class SandboxThreadGroup extends ThreadGroup {

        public SandboxThreadGroup(String name) {
            super(name);
        }

        public SandboxThreadGroup(ThreadGroup parentGroup, String name) {
            super(parentGroup, name);
        }

        private SandboxThread mainThread = null;

        public SandboxThread getMainThread() {
            return mainThread;
        }

        void setMainThread(SandboxThread mainThread) {
            this.mainThread = mainThread;
        }

        /**
         * 尝试终止线程组, 首先 Thread.interrupt(), 终止不掉再Thread.stop().
         *
         * @return whether we succeeded
         * @throws InterruptedException
         */
        @SuppressWarnings("deprecation")
        public boolean forceStop() {
            if (getSandbox(Thread.currentThread()) == mainThread) {
                throw new IllegalAccessError("Can't forcestop a Sandbox from inside");
            }
            try {
                this.interrupt();
                if (this.join(100)) {
                    return true;
                }
            } catch (Exception e) {
                log.error("force stop occured error:", e);
            }
            /*log.error(getName() + " refused to be interrupted , try direct stop");
            try {
                this.stop();
                if (this.join(50)) {
                    return true;
                }
            } catch (Exception e) {
                log.error("force stop occured error:",e);
            }
            log.error(getName() + " refused to be stopped");*/
            return false;
        }

        /**
         * 暂停执行，直到该组的所有非守护程序线程都停止为止
         *
         * @param timeout 等待多长时间（以毫秒为单位）。 0表示永远等待。
         * @return whether we succeeded
         * @throws InterruptedException
         */
        public boolean join(final long timeout) throws InterruptedException {
            Thread[] toJoinList = new Thread[8];
            long currTimeout = timeout;
            long timeStart = System.currentTimeMillis();
            while (true) {
                int nThread = this.enumerate(toJoinList);
                //System.out.println("线程组的线程数：" + nThread);
                for (int iThread = 0; iThread < nThread; iThread++) {
                    Thread toJoin = toJoinList[iThread];
                    //System.out.println("线程名：" + toJoin.getName()+"，线程状态："+toJoin.getState()+"，线程堆栈："+ Arrays.toString(toJoin.getStackTrace()));
                    if (toJoin.isDaemon()) {
                        System.out.println("-----daemon:" + toJoin.getName() + "-----" + Arrays.toString(toJoin.getStackTrace()) + "----" + toJoin.getState());
                        continue;
                    }
                    toJoin.join(currTimeout);
                    if (toJoin.isAlive()) {
                        return false;
                    }
                    if (timeout != 0) {
                        currTimeout = timeout - (int) (System.currentTimeMillis() - timeStart);
                        if (currTimeout <= 0) return false;
                    }
                }
                if (nThread != toJoinList.length) {
                    return true;
                }
                // Grow the list since we need to make space
                toJoinList = new Thread[toJoinList.length * 2];
            }
        }
    }
}
