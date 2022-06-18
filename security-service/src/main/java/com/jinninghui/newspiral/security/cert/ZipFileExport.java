package com.jinninghui.newspiral.security.cert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class ZipFileExport {

        private static final Logger logger = LoggerFactory.getLogger(ZipFileExport.class);

        /**
         * 文件导出下载到----客户端
         * @param response
         * @param filename
         * @param path
         */
        public void downImgClient(HttpServletResponse response, String filename, String path ){
                if (filename != null) {
                        FileInputStream inputStream = null;
                        BufferedInputStream bs = null;
                        ServletOutputStream servletOutputStream = null;
                        try{
                                response.setHeader("Content-Type","application/octet-stream");
                                response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                                response.addHeader("charset", "utf-8");
                                response.addHeader("Pragma", "no-cache");
                                String encodeName = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString());
                                response.setHeader("Content-Disposition", "attachment; filename=\"" + encodeName + "\"; filename*=utf-8''" + encodeName);
                                File file = new File(path);
                                inputStream = new FileInputStream(file);
                                bs =new BufferedInputStream(inputStream);
                                servletOutputStream = response.getOutputStream();
                                writeBytes(bs, servletOutputStream);
                        } catch (Exception e) {
                                logger.error("文件导出下载到客户端异常",e);
                        } finally {
                                try {
                                        if (servletOutputStream != null) {
                                                servletOutputStream.close();
                                                //servletOutputStream = null;
                                        }
                                        if (bs!=null){
                                                bs.close();
                                        }
                                        if (inputStream != null) {
                                                inputStream.close();
                                        }
                                } catch (Exception e) {
                                        logger.error("文件导出下载到客户端异常",e);
                                }
                        }

                }

        }

        //writeBytes()构造方法
        private void writeBytes(InputStream in, OutputStream out) throws IOException {
                byte[] buffer= new byte[1024];
                int length = -1;
                while ((length = in.read(buffer))!=-1){
                        out.write(buffer,0,length);

                }
                in.close();
                out.close();
        }
        /**
         * 单文件导出下载
         * @param response
         * @param filename
         * @param path
         */
        public void downImg(HttpServletResponse response, String filename, String path ){
                if (filename != null) {
                        FileInputStream is = null;
                        BufferedInputStream bs = null;
                        OutputStream os = null;
                        try {
                                File file = new File(path);
                                if (file.exists()) {
                                        is = new FileInputStream(file);
                                        bs =new BufferedInputStream(is);
                                        os = response.getOutputStream();
                                        byte[] buffer = new byte[1024];
                                        int len = 0;
                                        while((len = bs.read(buffer)) != -1){
                                                os.write(buffer,0,len);
                                        }
                                }else{
                                        String error = "下载的文件资源不存在";
                                        System.out.println(error);
                                }
                        }catch(IOException ex){
                                logger.error("单文件导出下载异常",ex);
                        }finally {
                                try{
                                        if(is != null){
                                                is.close();
                                        }
                                        if( bs != null ){
                                                bs.close();
                                        }
                                        if( os != null){
                                                os.flush();
                                                os.close();
                                        }
                                }catch (IOException e){
                                        logger.error("单文件导出下载异常",e);
                                }
                        }
                        downImgClient(response,filename,path);
                }
        }
        /**
         * 多文件打包下载
         * @param response
         * @param fileMap
         * @param directoryPath //临时存放--服务器上--zip文件的目录
         */
        public void FileDownload(HttpServletResponse response, Map<String,byte[]> fileMap, String directoryPath, String zipFileNameEn) {

                File directoryFile=new File(directoryPath);
                if(!directoryFile.isDirectory() && !directoryFile.exists()){
                        directoryFile.mkdirs();
                }
                //设置最终输出zip文件的目录+文件名
                SimpleDateFormat formatter  = new SimpleDateFormat("yyyyMMdd-HHmmss");
                String zipFileName =zipFileNameEn+ formatter.format(new Date())+".zip";
                String strZipPath = directoryPath+"/"+zipFileName;

                ZipOutputStream zipStream = null;
                FileInputStream zipSource = null;
                BufferedInputStream bufferStream = null;
                File zipFile = new File(strZipPath);
                try{
                        //构造最终压缩包的输出流
                        zipStream = new ZipOutputStream(new FileOutputStream(zipFile));
                        for (Map.Entry<String, byte[]> entry : fileMap.entrySet()) {
                                //解码获取真实路径与文件名
                                String realFileName = java.net.URLDecoder.decode(entry.getKey(),"UTF-8");
                                if(entry.getValue()!=null){
                                        InputStream sbs = new ByteArrayInputStream(entry.getValue());
                                        /**
                                         * 压缩条目不是具体独立的文件，而是压缩包文件列表中的列表项，称为条目，就像索引一样这里的name就是文件名,
                                         * 文件名和之前的重复就会导致文件被覆盖
                                         */
                                        ZipEntry zipEntry = new ZipEntry(realFileName);//在压缩目录中文件的名字
                                        zipStream.putNextEntry(zipEntry);//定位该压缩条目位置，开始写入文件到压缩包中
                                        bufferStream = new BufferedInputStream(sbs, 1024 * 10);
                                        int read = 0;
                                        byte[] buf = new byte[1024 * 10];
                                        while((read = bufferStream.read(buf, 0, 1024 * 10)) != -1)
                                        {
                                                zipStream.write(buf, 0, read);
                                        }
                                }
                         }
                } catch (Exception e) {
                        logger.error("多文件打包下载异常",e);
                } finally {
                        //关闭流
                        try {
                                if(null != bufferStream) {
                                        bufferStream.close();
                                }

                                if(null != zipStream){
                                        zipStream.flush();
                                        zipStream.close();
                                }
                                if(null != zipSource){
                                        zipSource.close();
                                }
                        } catch (IOException e) {
                                logger.error("多文件打包下载异常",e);
                        }
                }
                //判断当前压缩文件是否生成存在：true-把该压缩文件通过流输出给客户端后删除该压缩文件
                if(zipFile.exists()){
                         //发送给客户端
                        downImgClient(response,zipFileName,strZipPath);
                        //删除本地存储的文件
                        if (!zipFile.delete()) {
                                logger.error("多文件打包下载，删除文件失败");
                        }
                }
        }
}