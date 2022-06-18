package com.jinninghui.newspiral.security.contract;

import com.jinninghui.newspiral.security.contract.sandbox.SandboxThread;

import java.lang.ref.WeakReference;
import java.security.Permission;

public class SwitchableSecurityManager extends SecurityManager {

    private static WeakReference<SwitchableSecurityManager> instance = new WeakReference<>(null);

    private SwitchableSecurityManager() {
    }

    public synchronized static SecurityManager getInstance() {
        SwitchableSecurityManager result;
        if (instance == null || (result = instance.get()) == null) {
            result = new SwitchableSecurityManager();
            instance = new WeakReference<>(result);
            // Pre-load the Sandbox class before this SecurityManager is activated
            try {
                Class.forName("com.jinninghui.newspiral.security.contract.sandbox.CallableSandbox");
                Class.forName("com.jinninghui.newspiral.security.contract.sandbox.RunnableSandbox");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /**
     * 标志位，用于标识线程操作是否是受限制的
     */
    public static ThreadLocal<Boolean> isRestricted = ThreadLocal.withInitial(() -> {
        Thread thread = Thread.currentThread();
        return SandboxThread.getSandbox(thread) != null;
    });

    private final Object reentryCheck = new Object();

    @Override
    public void checkPermission(Permission perm) {
        //在执行isRestricted.get()方法时，也会检查权限，而检查权限也会调用isRestricted.get()，存在循环调用
        // 当且仅当当前线程持有reentryCheck对象的锁时，返回true。
        if (Thread.holdsLock(reentryCheck)) {
            //线程保有锁，返回
            return;
        }
        Boolean bool;
        synchronized (reentryCheck) {
            //持有reentryCheck对象的锁
            bool = isRestricted.get();
        }
        if (bool) {
            super.checkPermission(perm);
        }
    }
}
