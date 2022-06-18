package com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor.contract;

import com.jinninghui.newspiral.common.entity.exception.NewSpiralErrorEnum;
import com.jinninghui.newspiral.common.entity.exception.NewspiralException;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContract;
import com.jinninghui.newspiral.common.entity.smartcontract.SmartContractCompileResp;
import com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor.contract.compile.*;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version V1.0
 * @Title: SmartContractCompile
 * @Package com.jinninghui.newspiral.consensus.impl.hotstuff.txexecutor
 * @Description:
 * @author: xuxm
 * @date: 2020/9/23 18:05
 */
@Slf4j
public class SmartContractCompile {

    /**
     * 包名匹配关键字
     */
    static final Pattern packagePattern = Pattern.compile("(?<=package\\s).*(?=;)");

    /**
     * 类名匹配
     */
    static final Pattern classNamePattern = Pattern.compile("(?<=public class\\s).*(?=\\{)");

    /**
     * 替换之后后的包名
     */
    static final String RESOURCE_PACKAGE = "package com.jinninghui.newspiral.ledger.mgr";


    /**
     * 根据关键字获取特定的字符串
     *
     * @param bytes      文件字节
     * @param containStr 包含的字符串
     * @param pattern    匹配
     * @return
     */
    private String getInterceptString(byte[] bytes, String containStr, Pattern pattern) {
        String result = null;
        BufferedReader br;
        InputStream inputStream;
        InputStreamReader isr;
        try {
            inputStream = new ByteArrayInputStream(bytes);
            isr = new InputStreamReader(inputStream);
            br = new BufferedReader(isr);
            String data = br.readLine();
            while (data != null) {
                if (data.indexOf(containStr) != -1) {
                    Matcher m = pattern.matcher(data);
                    if (m.find()) {
                        result = m.group();
                    }
                    break;
                }
                data = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            log.error("SmartContractCompile.getInterceptString:", e);
        }
        return result;
    }


    /**
     * 替换源文件包名和类名
     * 原包后面添加 channelId
     *
     * @param sourceContent
     * @param channelId
     */
    private SmartContractCompileResp replacePackageAndClassName(String sourceContent, String channelId) {
        byte[] sourceContentByte = DatatypeConverter.parseBase64Binary(sourceContent);
        //获取类名
        //String packageStr = "package " + getInterceptString(sourceContentByte, "package", packagePattern);
        //String packageStrH = packageStr.split(";")[0] + "." + channelId ;
        String sourceContentStr = "";
        try {
            sourceContentStr = new String(sourceContentByte, "UTF-8");
        } catch (Exception ex) {

        }
        //log.info("source_befor={}",sourceContentStr);
        String packageStr = "package " + getInterceptString(sourceContentByte, "package", packagePattern);
        String classNameStr = getInterceptString(sourceContentByte, "public class", classNamePattern);
        String oldClassName = classNameStr.split(" ")[0];
//        String pubClassNameBefor = "public class " + oldClassName;
//        String pubClassNameAfter = "public class " + oldClassName + channelId;
        String pubClassNameBefor = oldClassName;
        String pubClassNameAfter = oldClassName + channelId;

        String logClassBefor = oldClassName + ".class";
        String logClassAfter = oldClassName + channelId + ".class";
        sourceContentStr = sourceContentStr.replaceAll(packageStr, RESOURCE_PACKAGE)
                .replaceAll(pubClassNameBefor, pubClassNameAfter)
                .replaceAll(logClassBefor, logClassAfter);
        //log.info("source_after={}",sourceContentStr);
        SmartContractCompileResp smartContractCompileResp = new SmartContractCompileResp();
        smartContractCompileResp.setClassName(oldClassName + channelId);
        smartContractCompileResp.setSourceContent(sourceContentStr);
        return smartContractCompileResp;
    }

    /**
     * 编译java文件
     * 从输入的对象中读取Java源文件并编译，编译完成后会设置入参的字节码ClassFileBytes属性和name属性(全类名）
     *
     * @param sc
     */
    public void compileSmartContract(SmartContract sc) throws Exception{
        try {
            //替换类名
            SmartContractCompileResp smartContractCompileResp = replacePackageAndClassName(sc.getSourceContent(), sc.getChannelId());
            byte[] sourceContentByte = smartContractCompileResp.getSourceContent().getBytes();
            String className = smartContractCompileResp.getClassName() + ".java";
            String packageStr = getInterceptString(sourceContentByte, "package", packagePattern);
            //获取全类名称
            String name = packageStr.split(";")[0] + "." + smartContractCompileResp.getClassName();
            sc.setName(name);
            //JavaStringCompiler javaStringCompiler = new JavaStringCompiler();
            //Map<String, byte[]> results = javaStringCompiler.compile(className, smartContractCompileResp.getSourceContent());

            CusCompiler compiler = new CusCompiler((URLClassLoader) Thread.currentThread().getContextClassLoader());
            Map<String, byte[]> results = compiler.compile(name, smartContractCompileResp.getSourceContent());
            HashMap<String, byte[]> map = new HashMap<>();
            for (Map.Entry<String, byte[]> vo : results.entrySet()) {
                if (vo.getKey().equals(name)) {
                    sc.setClassFileBytes(vo.getValue());
                } else {
                    map.put(vo.getKey(), vo.getValue());
                }
            }
            sc.setInnerClassFileList(map);
            //记得对 class字节 hash
        } catch (RuntimeException e)
        {
            log.error("SmartContractCompile.compileSmartContract,error=", e);
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "合约编译运行时异常");
        }
        catch (Exception e) {
            log.error("SmartContractCompile.compileSmartContract,error=", e);
            throw new NewspiralException(NewSpiralErrorEnum.INVALID_PARAM, "合约编译报错");
        }
    }

/*

    public static void main(String[] args) {
        byte[] javaByte = readSmartContractClassFile("E:\\test\\CAContract.java");
        String base64Str = DatatypeConverter.printBase64Binary(javaByte);
        //String  str=Base64.getEncoder().encodeToString(javaByte);
        SmartContract smartContract = new SmartContract();
        smartContract.setChannelId("D071960503354579AFFB6CEEAB831AE0");
        smartContract.setSourceContent(base64Str);
        SmartContractCompile.compileSmartContract(smartContract);
        System.out.println(JSONObject.toJSON((smartContract)));


    }
    private static byte[] readSmartContractClassFile(String filePath) {
        File file = new File(filePath);

        try {
            InputStream fileIn = new FileInputStream(file);

            // 使用缓存区读入对象效率更快
            BufferedInputStream in = new BufferedInputStream(fileIn);
            int bytesCnt = (int) file.length();//测试代码，临时强制转换一下，class文件也不会太大
            byte[] bytes = new byte[bytesCnt];
            in.read(bytes);
            return bytes;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
*/

}
