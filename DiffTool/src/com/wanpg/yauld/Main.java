package com.wanpg.yauld;

import com.wanpg.yauld.diff.DexDiff;
import com.wanpg.yauld.diff.ResourceDiff;
import com.wanpg.yauld.utils.*;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class Main {

    private static String oldApkPath, newApkPath, patchFilePath;

    public static void main(String[] args) {
        try {
            ArrayList<String> argArray = new ArrayList<>();
            Collections.addAll(argArray, args);

            argArray.remove(0);
            if ("d".equalsIgnoreCase(args[0])) {
                // 差分apk
                diffApk(argArray);
            } else if ("i".equalsIgnoreCase(args[0])) {
                // 版本信息等
                getApkInfo(argArray);
            }
        }catch (Exception e){
            printHelp();
        }
    }

    private static void getApkInfo(ArrayList<String> argArray) {
        if(argArray == null || argArray.size() <= 0){
            printHelp();
            return;
        }
        String apkPath = null;
        try {
            apkPath = argArray.remove(argArray.size() - 1);
        } catch (Exception e) {

        }

        if(TextUtils.isEmpty(apkPath)){
            printHelp();
            return;
        }

        StringBuilder result = new StringBuilder();
        if(argArray.size() > 0){
            for(String arg : argArray){
                String res = null;
                if("package".equalsIgnoreCase(arg)){
                    res = "package=" + ApkUtils.getApkPackage(apkPath);
                }else if("version".equalsIgnoreCase(arg)){
                    res = "version=" + ApkUtils.getApkVersionName(apkPath);
                }else if("md5".equalsIgnoreCase(arg)){
                    res = "md5=" + ApkUtils.getApkMd5(apkPath);
                }
                if(!TextUtils.isEmpty(res)){
                    if(!TextUtils.isEmpty(result.toString())){
                        result.append(";");
                    }
                    result.append(res);
                }
            }
        }
        if(TextUtils.isEmpty(result.toString())){
            result.append("package=").append(ApkUtils.getApkPackage(apkPath))
                    .append(";")
                    .append("version=").append(ApkUtils.getApkVersionName(apkPath))
                    .append(";")
                    .append("md5=").append(ApkUtils.getApkMd5(apkPath));
        }
        LogUtils.print(result.toString());
    }

    private static void printHelp(){
        LogUtils.print("用法 java -jar diff-tool.jar d [old.apk] [new.apk] [patch name]");
        LogUtils.print("[old.apk] , 必选 , 且文件必须存在");
        LogUtils.print("[new.apk] , 必选 , 且文件必须存在");
        LogUtils.print("[patch name] , 可选 , 如果不传将会在 [new.apk] 所在的目录生成 update.zip ");

        LogUtils.print("");

        LogUtils.print("用法 java -jar diff-tool.jar i [package | version | md5] [***.apk]");
        LogUtils.print("[package | version | md5] , 可选 , 如果不传，则返回所有的，以\";\"分隔");
        LogUtils.print("[***.apk] , 必选 , 且文件必须存在");
    }

    private static void diffApk(ArrayList<String> argArray) {
        try {
            oldApkPath = argArray.get(0);
            newApkPath = argArray.get(1);
            patchFilePath = argArray.get(2);
        } catch (Exception e) {
        }
        if (TextUtils.isEmpty(oldApkPath) || TextUtils.isEmpty(newApkPath)) {
            printHelp();
            return;
        }
        if (!oldApkPath.endsWith(".apk") || !newApkPath.endsWith(".apk")) {
            printHelp();
            return;
        }

        if (!FileUtils.exists(oldApkPath) || !FileUtils.exists(newApkPath)) {
            printHelp();
            return;
        }

        // 如果没有设置patchfile的路径，则会在newApk所在的文件夹生成update.zip
        if (TextUtils.isEmpty(patchFilePath)) {
            patchFilePath = new File(new File(newApkPath).getParentFile(), "update.zip").getAbsolutePath();
        }

        // 开始做差分
        diff();
    }

    /**
     * 开始差分
     * 1.创建临时文件夹
     * 2.分别解压新旧apk到临时文件夹
     * 3.创建patch文件夹
     * 4.dex diff
     * 5.resource diff
     * 6.压缩diff后的 dex res assets resources.arsc.patch 等到指定文件(默认update.zip)
     * 7.删除临时文件夹
     */
    private static void diff() {
        // 创建临时文件夹
        File patchFile = new File(patchFilePath);
        File patchParentFolder = patchFile.getParentFile();
        if (!patchParentFolder.exists()) {
            patchParentFolder.mkdirs();
        }

        // 创建临时文件夹并分别加压apk
        File tempFolder = new File(patchParentFolder, ".yauld-temp");
        FileUtils.delete(tempFolder, true);
        tempFolder.mkdirs();

        File oldApkFolder = new File(tempFolder, "old-apk");
        File newApkFolder = new File(tempFolder, "new-apk");
        ZipUtil.unpack(new File(oldApkPath), oldApkFolder);
        ZipUtil.unpack(new File(newApkPath), newApkFolder);
        File patchFolder = new File(tempFolder, "patch");

        // dex 差分
        DexDiff.diff(oldApkFolder.getAbsolutePath(), newApkFolder.getAbsolutePath(), patchFolder.getAbsolutePath());
        // 资源差分
        ResourceDiff.diff(oldApkFolder.getAbsolutePath(), newApkFolder.getAbsolutePath(), patchFolder.getAbsolutePath());
        // 压缩所有差分资源
        ZipUtil.pack(patchFolder, patchFile);
        // 删除临时文件夹
//        FileUtils.delete(tempFolder, true);
    }
}
