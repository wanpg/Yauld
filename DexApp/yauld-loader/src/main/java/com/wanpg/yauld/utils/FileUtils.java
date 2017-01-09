package com.wanpg.yauld.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

    public static void copyFile(File src, File outFolder, String outName) {
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

    public static void copy(File srcFolder, File destFolder){
        if(!destFolder.exists()){
            destFolder.mkdirs();
        }
        if(srcFolder.isFile()){
            copyFile(srcFolder, destFolder, srcFolder.getName());
        }else{
            File[] children = srcFolder.listFiles();
            if(children != null) {
                for (File child : children){
                    copy(child, new File(destFolder, srcFolder.getName()));
                }
            }
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
