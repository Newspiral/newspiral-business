package com.jinninghui.newspiral.security.contract;


import com.jinninghui.newspiral.common.entity.smartcontract.SmartContract;
import com.jinninghui.newspiral.security.asm.SandboxClassVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.springframework.stereotype.Component;

@Component
public class ContractByteCodeSearcher implements ContractByteSource {

    @Override
    public byte[] getMainByteCode(String name) {
        SmartContract cache = SandBoxCache.getContractNameCache(name);
        byte[] bytes = cache.getClassFileBytes();
        ClassWriter classWriter = getClassWriter(bytes);
        //getFile(classWriter.toByteArray(),"D:/","scClassFIle.class");
        return classWriter.toByteArray();
    }

    private ClassWriter getClassWriter(byte[] bytes) {
        //读取类文件
        ClassReader classReader = new ClassReader(bytes);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        //处理，通过classVisitor修改类
        SandboxClassVisitor classVisitor = new SandboxClassVisitor(classWriter);
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
        if (!classVisitor.isPassed()){
            throw new SandboxException("native or finalize method find in this contract!");
        }
        return classWriter;
    }

    @Override
    public byte[] getInnerByteCode(byte[] bytes) {
        ClassWriter classWriter = getClassWriter(bytes);
        //getFile(classWriter.toByteArray(),"D:/","InnerClasses.class");
        return classWriter.toByteArray();
    }

    /*public static void getFile(byte[] bfile, String filePath,String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            boolean isDir = dir.isDirectory();
            if (!isDir) {// 目录不存在则先建目录
                try {
                    dir.mkdirs();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            file = new File(filePath + File.separator + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }*/
}
