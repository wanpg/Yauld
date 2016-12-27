package com.wanpg.yauld.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by wangjinpeng on 2016/12/11.
 */

public class FileUtils {

    public static void copyFile(String src, String outFolder, String outName) {
        try {
            FileInputStream input = new FileInputStream(src);
            FileOutputStream output = new FileOutputStream(new File(outFolder, outName));
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = input.read(b)) != -1) {
                output.write(b, 0, len);
            }
            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyStream(InputStream input, String outFolder, String outName) {
        try {
            FileOutputStream output = new FileOutputStream(new File(outFolder, outName));
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = input.read(b)) != -1) {
                output.write(b, 0, len);
            }
            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean mkdirs(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return file.mkdirs();
        }
        return false;
    }


    /**
     * 解压到指定目录
     *
     * @param zipPath
     * @param descDir
     * @author isea533
     */
    public static void unZipFiles(String zipPath, String descDir) throws IOException {
        unZipFiles(new File(zipPath), descDir);
    }

    /**
     * 解压文件到指定目录
     *
     * @param zipFile
     * @param descDir
     * @author isea533
     */
    public static void unZipFiles(File zipFile, String descDir) throws IOException {
        File pathFile = new File(descDir);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        ZipFile zip = new ZipFile(zipFile);
        for (Enumeration entries = zip.entries(); entries.hasMoreElements(); ) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            String zipEntryName = entry.getName();
            InputStream in = zip.getInputStream(entry);
            String outPath = ((descDir.endsWith(File.separator) ? descDir : (descDir + File.separator)) + zipEntryName).replaceAll("\\*", "/");
            //判断路径是否存在,不存在则创建文件路径
            File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
            if (!file.exists()) {
                file.mkdirs();
            }
            //判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
            if (new File(outPath).isDirectory()) {
                continue;
            }
            //输出文件路径信息
            System.out.println(outPath);

            OutputStream out = new FileOutputStream(outPath);
            byte[] buf1 = new byte[1024];
            int len;
            while ((len = in.read(buf1)) > 0) {
                out.write(buf1, 0, len);
            }
            in.close();
            out.close();
        }
        System.out.println("******************解压完毕********************");
    }


    public static void delete(String path){
        delete(path, true);
    }

    public static void delete(String path, boolean includeSelf){
        delete(new File(path), includeSelf);
    }

    public static void delete(File file, boolean includeSelf){
        if(file.isDirectory()){
            File[] children = file.listFiles();
            for(File child : children){
                delete(child, true);
            }
        }
        if(includeSelf) {
            file.delete();
        }
    }

    public static boolean exists(String path) {
        return new File(path).exists();
    }
}
