package com.wanpg.yauld;

import com.wanpg.yauld.diff.DexDiff;
import com.wanpg.yauld.diff.ResourceDiff;
import com.wanpg.yauld.utils.FileUtils;
import com.wanpg.yauld.utils.LogUtils;
import com.wanpg.yauld.utils.TextUtils;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.util.ArrayList;

public class Main {

    private static String oldApkPath, newApkPath, patchFilePath;

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

        // 如果没有设置patchfile的路径，则会在newApk所在的文件夹生成update.zip
        if(TextUtils.isEmpty(patchFilePath)){
            patchFilePath = new File(new File(newApkPath).getParentFile(), "update.zip").getAbsolutePath();
        }

        // 开始做差分
        diff();
    }

    /**
     * 输出帮助信息
     */
    private static void printHelp(){
        LogUtils.print("用法 java -jar diff-tool.jar [old.apk] [new.apk] [patch name]");
        LogUtils.print("[old.apk] , 必选 , 且文件必须存在");
        LogUtils.print("[new.apk] , 必选 , 且文件必须存在");
        LogUtils.print("[patch name] , 可选 , 如果不传将会在 [new.apk] 所在的目录生成 update.zip ");
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
    private static void diff(){
        // 创建临时文件夹
        File patchFile = new File(patchFilePath);
        File patchParentFolder = patchFile.getParentFile();
        if(!patchParentFolder.exists()){
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
        FileUtils.delete(tempFolder, true);
    }
}
