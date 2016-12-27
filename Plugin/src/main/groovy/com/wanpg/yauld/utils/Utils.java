package com.wanpg.yauld.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wangjinpeng on 16/4/19.
 */
public class Utils {

    public static void print(String str) {
        System.out.println(str);
    }

    public static void print(Object str) {
        System.out.println(str.toString());
    }

    public static void createFile(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            File fileParent = file.getParentFile();
            if (!fileParent.exists()) {
                fileParent.mkdirs();
            }
            file.createNewFile();
        }
    }

    public static boolean isFileExist(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static void writeToLocalXml(String path, ArrayList<String> list) throws IOException {
        FileWriter fileWriter = null;
        BufferedWriter writer = null;
        try {
            Utils.createFile(path);
            fileWriter = new FileWriter(path, false);
            writer = new BufferedWriter(fileWriter);
            writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            writer.newLine();
            writer.write("<resources>");
            writer.newLine();
            for (String string : list) {
                writer.write("<string>" + string + "</string>");
                writer.newLine();
            }
            writer.write("</resources>");
            writer.newLine();
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
            if (fileWriter != null) {
                fileWriter.close();
            }
        }
    }

    public static String getSuffix(String path){
        int index = path.lastIndexOf(".");
        return path.substring(index + 1);
    }

    public static boolean isExcel(String path){
        String suffix = getSuffix(path);
        return "xls".equalsIgnoreCase(suffix) || "xlsx".equalsIgnoreCase(suffix);
    }

    public static String replaceMatch(String src){
        String result = src;
        String[] strs = checkMatch(src);
        if(strs.length > 0){
            for(String match : strs){
                result = result.replaceAll("%[0-9]{0,1}[\\$]{0,1}[l,z]{0,1}[@,s,d,u]{1}", "%@");
            }
        }
        return trim(result);
    }

    public static String[] checkMatch(String src) {
        String matcher = "%[0-9]{0,1}[\\$]{0,1}[l,z]{0,1}[@,s,d,u]{1}";
        Pattern p = Pattern.compile(matcher);
        Matcher m = p.matcher(src);
        ArrayList<String> strs = new ArrayList<String>();
        while (m.find()) {
            strs.add(m.group(0));
        }
        return strs.toArray(new String[strs.size()]);
    }

    public static String trim(String src){
        src = src.trim();
        int start = 0;
        int end = src.length();
        if(src.startsWith("\\n")){
            start = src.indexOf("\\n") + "\\n".length();
        }
        if(src.endsWith("\\n")){
            end = src.lastIndexOf("\\n");
        }
        src = src.substring(start, end).trim();
        end = src.length();
        if(src.endsWith("\\t")){
            end = src.lastIndexOf("\\t");
        }
        return src.substring(0, end).trim();
    }

    public static ArrayList<File> listAllFile(String folderPath, String suffix){
        File folder = new File(folderPath);
        ArrayList<File> list = new ArrayList<>();
        if(folder.exists() && folder.isDirectory()){
            File[] files = folder.listFiles();
            for(File file : files){
                if(file.exists()){
                    String path = file.getAbsolutePath();
                    if(file.isFile() && suffix.equalsIgnoreCase(getSuffix(path))){
                        list.add(file);
                    }else if(file.isDirectory()){
                        ArrayList<File> fileListTmp = listAllFile(path, suffix);
                        if(fileListTmp.size() > 0){
                            list.addAll(fileListTmp);
                        }
                    }
                }
            }
        }
        return list;
    }

    public static boolean isStringEmpty(String src){
        return src == null || "".equals(src);
    }

    public static void deleteFile(String path){
        deleteFile(new File(path));
    }

    public static void deleteFile(File file){
        if(file != null){
            if(file.isDirectory()){
                File[] files = file.listFiles();
                for(File file1 : files){
                    deleteFile(file1);
                }
            }else{
                file.delete();
            }
        }
    }

    public static void writeToFile(String path, String content){
        try {
            byte[] bytes = content.getBytes();
            FileOutputStream fos=new FileOutputStream(path);
            fos.write(bytes,0, bytes.length);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭资源
     *
     * @param closeable 资源
     */
    public static void close(Closeable... closeable) {
        if (closeable == null) {
            return;
        }
        for (Closeable c : closeable) {
            if (c != null) {
                try {
                    c.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getSystemName(){
        String osName = System.getProperty("os.name");
        if(osName != null){
            String lowerCase = osName.toLowerCase();
            if(lowerCase.contains("mac")){
                return "mac";
            }else if(lowerCase.contains("windows")){
                return "windows";
            }else if(lowerCase.contains("linux")){
                return "linux";
            }
        }
        return "";
    }
}
