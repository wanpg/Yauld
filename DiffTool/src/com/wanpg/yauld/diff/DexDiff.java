package com.wanpg.yauld.diff;

import com.wanpg.yauld.Command;
import com.wanpg.yauld.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangjinpeng on 2016/12/28.
 */
public class DexDiff extends BaseDiff {

    public static void diff(String oldApkFolder, String newApkFolder, String tempFolder){
        diff(filterDexFile(oldApkFolder), filterDexFile(newApkFolder), tempFolder);
    }

    public static void diff(List<String> oldDexes, List<String> newDexes, String tempFolder){
        File dexFolder = new File(tempFolder, "dex");
        FileUtils.delete(dexFolder,true);
        if(!dexFolder.exists()){
            dexFolder.mkdirs();
        }
        File oldDexSmaliFolder = new File(dexFolder, "old");
        File newDexSmaliFolder = new File(dexFolder, "new");
        File tempDexSmaliFolder = new File(dexFolder, "temp");
        oldDexSmaliFolder.mkdirs();
        newDexSmaliFolder.mkdirs();
        tempDexSmaliFolder.mkdirs();
        try {
            // 转化old.dex到old文件夹为smali
            for(String dex : oldDexes){
                dex2smali(dex, oldDexSmaliFolder.getAbsolutePath());
            }
            // 转化new.dex到new文件夹为smali
            for(String dex : newDexes){
                dex2smali(dex, newDexSmaliFolder.getAbsolutePath());
            }

            internalDiff(oldDexSmaliFolder, newDexSmaliFolder, tempDexSmaliFolder);
            smali2dex(tempDexSmaliFolder.getAbsolutePath(), new File(tempFolder, "patch.dex").getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
//            FileUtils.delete(dexFolder, true);
        }
    }

    /**
     * 获取文件夹下的dex列表
     * @param apkFolderPath
     * @return
     */
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

    public static void dex2smali(String dex, String outPut) throws IOException {
        String jar = System.getProperty("user.dir") + File.separator + "tools" + File.separator + "baksmali-2.2b4.jar";
        Command.execute("java", "-jar", jar, "disassemble", "-o", outPut, dex);
    }

    public static void smali2dex(String smalifolder, String outDex){
        String jar = System.getProperty("user.dir") + File.separator + "tools" + File.separator + "smali-2.2b4.jar";
        Command.execute("java", "-jar", jar, "assemble", "-o", outDex, smalifolder);
    }
}
