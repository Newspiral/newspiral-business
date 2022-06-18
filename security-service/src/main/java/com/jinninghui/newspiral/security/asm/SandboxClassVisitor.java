package com.jinninghui.newspiral.security.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;


import static org.objectweb.asm.Opcodes.*;

public class SandboxClassVisitor extends ClassVisitor {

    private boolean pass = Boolean.TRUE;
    private String owner;

    public SandboxClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM5, classVisitor);
    }


    public boolean isPassed() {
        return pass;
    }


    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        owner = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        final MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        if (isIgnore(methodVisitor, access, name)) {
            return methodVisitor;
        }
        if (isNative(access)) {
            pass = Boolean.FALSE;
        }
        if (isFinalize(name, desc)) {
            pass = Boolean.FALSE;
        }
        return new SandboxMethodAdapter(access, name, desc, methodVisitor, owner);
    }

    public class SandboxMethodAdapter extends AdviceAdapter {

        private final String name;
        private final String owner;

        public SandboxMethodAdapter(int access, String name, String descriptor, MethodVisitor methodVisitor, String owner) {
            super(ASM5, methodVisitor, access, name, descriptor);
            this.name = name;
            this.owner = owner;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (this.name.equals(name) && this.owner.equals(owner)) {
                //方法名为当前方法，且是调用本类的方法，则意味着是递归调用，深度不可超过500
                this.visitLdcInsn(name);
                this.visitMethodInsn(INVOKESTATIC,
                        "com/jinninghui/newspiral/security/asm/LoopCounter",
                        "incr",
                        "(Ljava/lang/String;)V",
                        false);
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }

        @Override
        protected void onMethodEnter() {
            super.onMethodEnter();
            this.visitMethodInsn(INVOKESTATIC,
                    "com/jinninghui/newspiral/security/asm/MemoryUseCounter",
                    "init",
                    "()V",
                    false);
        }

        @Override
        protected void onMethodExit(int opcode) {
            /*mv.visitMethodInsn(INVOKESTATIC,
                    "com/jinninghui/newspiral/security/asm/MemoryUseCounter",
                    "clear",
                    "()V",
                    false);*/
            /*mv.visitMethodInsn(INVOKESTATIC,
                    "com/jinninghui/newspiral/security/asm/LoopCounter",
                    "clear",
                    "()V",
                    false);*/
            super.onMethodExit(opcode);
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            if (opcode == GOTO && label != null) {
                // 在goto指令前插入计数器执行，统计循环体执行次数
                this.visitLdcInsn(label.toString());
                this.visitMethodInsn(INVOKESTATIC,
                        "com/jinninghui/newspiral/security/asm/LoopCounter",
                        "incr",
                        "(Ljava/lang/String;)V",
                        false);
            }
            super.visitJumpInsn(opcode, label);
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            super.visitTypeInsn(opcode, type);
            if (opcode == ANEWARRAY || opcode == NEW) {
                mv.visitMethodInsn(INVOKESTATIC,
                        "com/jinninghui/newspiral/security/asm/MemoryUseCounter",
                        "incr",
                        "()V",
                        false);
            }
        }

        @Override
        public void visitIntInsn(int opcode, int operand) {
            super.visitIntInsn(opcode, operand);
            if (opcode == NEWARRAY) {
                mv.visitMethodInsn(INVOKESTATIC,
                        "com/jinninghui/newspiral/security/asm/MemoryUseCounter",
                        "incr",
                        "()V",
                        false);
            }
        }
    }

    /**
     * 忽略构造方法、类加载初始化方法，final方法和 abstract 方法
     *
     * @param mv
     * @param access
     * @param methodName
     * @return
     */
    private boolean isIgnore(MethodVisitor mv, int access, String methodName) {
        return null == mv
                || isAbstract(access)
                || "<clinit>".equals(methodName)
                || "<init>".equals(methodName);
    }

    private boolean isAbstract(int access) {
        return (ACC_ABSTRACT & access) == ACC_ABSTRACT;
    }

    private boolean isFinalMethod(int methodAccess) {
        return (ACC_FINAL & methodAccess) == ACC_FINAL;
    }

    private boolean isNative(int access) {
        return (ACC_NATIVE & access) == ACC_NATIVE;
    }


    private boolean isFinalize(String name, String desc) {
        return name.equals("finalize") && desc.equals("()V");
    }
}
