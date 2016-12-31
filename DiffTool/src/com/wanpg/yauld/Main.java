package com.wanpg.yauld;

import com.wanpg.yauld.diff.DexDiff;
import com.wanpg.yauld.diff.ResourceDiff;
import com.wanpg.yauld.utils.FileUtils;
import com.wanpg.yauld.utils.LogUtils;
import com.wanpg.yauld.utils.TextUtils;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static String oldApkPath, newApkPath, patchFilePath;
    private static String rootDir;
    public static void main(String[] args) {
        ArrayList<String> argArray = new ArrayList<>();
        for(String arg : args){
            argArray.add(arg);
        }
        try {
            oldApkPath = argArray.get(0);
            newApkPath = argArray.get(1);
            patchFilePath = argArray.get(2);
        } catch (Exception e) {
        }
        if(TextUtils.isEmpty(oldApkPath) || TextUtils.isEmpty(newApkPath)){
            printHelp();
            return;
        }
        if(!oldApkPath.endsWith(".apk") || !newApkPath.endsWith(".apk")){
            printHelp();
            return;
        }

        if(!FileUtils.exists(oldApkPath) || !FileUtils.exists(newApkPath)){
            printHelp();
            return;
        }

        if(TextUtils.isEmpty(patchFilePath)){
            patchFilePath = new File(new File(newApkPath).getParentFile(), "update.zip").getAbsolutePath();
        }

        diff();
    }

    /**
     * 做一些准备工作
     * 比如临时文件夹创建，解压等等
     */
    private static void diff(){
        // 创建临时文件夹
        File patchFile = new File(patchFilePath);
        File patchParentFolder = patchFile.getParentFile();
        if(!patchParentFolder.exists()){
            patchParentFolder.mkdirs();
        }

        File tempFolder = new File(patchParentFolder, ".yauld-temp");
        File oldApkFolder = new File(tempFolder, "old-apk");
        File newApkFolder = new File(tempFolder, "new-apk");
        ZipUtil.unpack(new File(oldApkPath), oldApkFolder);
        ZipUtil.unpack(new File(newApkPath), newApkFolder);
        File patchFolder = new File(tempFolder, "patch");

        rootDir = System.getProperty("user.dir");

        DexDiff.diff(filterDexFile(oldApkFolder.getAbsolutePath()), filterDexFile(newApkFolder.getAbsolutePath()), patchFolder.getAbsolutePath());
        ResourceDiff.diff(oldApkFolder.getAbsolutePath(), newApkFolder.getAbsolutePath(), patchFolder.getAbsolutePath());
        ZipUtil.pack(patchFolder, patchFile);
    }

    private static void printHelp(){
        LogUtils.print("用法 java -jar diff-tool.jar [old.apk] [new.apk] [patch name]");
        LogUtils.print("[old.apk] , 必选 , 且文件必须存在");
        LogUtils.print("[new.apk] , 必选 , 且文件必须存在");
        LogUtils.print("[patch name] , 可选 , 如果不传将会在 [new.apk] 所在的目录生成 update.zip ");
    }

    private static List<String> filterDexFile(String apkFolderPath){
        File apkFolder = new File(apkFolderPath);
        if(!apkFolder.exists()){
            return null;
        }
        List<String> results = new ArrayList<>();
        File[] children = apkFolder.listFiles();
        if(children != null){
            for(File string : children){
                if(!string.isHidden() && string.getName().endsWith(".dex")){
                    results.add(string.getAbsolutePath());
                }
            }
        }
        return results;
    }
}
