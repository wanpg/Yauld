package com.wanpg.yauld;

import java.io.*;

/**
 * Created by wangjinpeng on 16/7/27.
 */
public class FileUtils {

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

    public static String getSuffix(String path){
        int index = path.lastIndexOf(".");
        return path.substring(index + 1);
    }

    public static void copyFile(String src, String outFolder, String outName){
        try {
            FileInputStream input = new FileInputStream(src);
            FileOutputStream output = new FileOutputStream(outFolder + (outFolder.endsWith(File.separator) ? "" : File.separator) + outName);
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
            FileOutputStream output = new FileOutputStream(outFolder + (outFolder.endsWith(File.separator) ? "" : File.separator) + outName);
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

    /**
     * 复制整个文件夹的内容(含自身)
     * @param oldPath 准备拷贝的目录
     * @param newFolder 指定绝对路径的新目录
     * @return
     */
    public static void copyFolderWithSelf(String oldPath, String newFolder, String newName) {
        try {
            new File(newFolder).mkdirs(); //如果文件夹不存在 则建立新文件夹
            File dir = new File(oldPath);
            // 目标
            String newPath =  newFolder + File.separator + newName;
            File moveDir = new File(newPath);
            if(dir.isDirectory()){
                if (!moveDir.exists()) {
                    moveDir.mkdirs();
                }
            }
            String[] file = dir.list();
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file[i]);
                } else {
                    temp = new File(oldPath + File.separator + file[i]);
                }
                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + File.separator + (temp.getName()).toString());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) { //如果是子文件夹
                    copyFolderWithSelf(temp.getAbsolutePath(), newPath, temp.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFolderWithSelf(String oldPath, String newFolder) {
        File dir = new File(oldPath);
        copyFolderWithSelf(oldPath, newFolder, dir.getName());
    }

    public static boolean exists(String path){
        return exists(new File(path));
    }

    public static boolean exists(File file){
        return file != null && file.exists();
    }

    public static boolean mkdirs(File file){
        if(!file.exists()){
            return file.mkdirs();
        }
        return false;
    }

    public static boolean mkdirs(String file){
        return mkdirs(new File(file));
    }

    public static void createFile(String path) {
        File file = new File(path);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
