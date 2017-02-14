package com.wanpg.yauld.diff;

import com.wanpg.yauld.Command;
import com.wanpg.yauld.utils.FileUtils;
import com.wanpg.yauld.utils.MD5;
import com.wanpg.yauld.utils.SmaliUtils;
import com.wanpg.yauld.utils.VersionUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangjinpeng on 2016/12/28.
 */
public class DexDiff extends BaseDiff {

    public static final String APPINFO_CLASS_NAME = "com.wanpg.yauld.AppInfo";
    public static final String VERSION_FIELD_NAME = "VERSION";

    public static String diff(String oldApkFolder, String newApkFolder, String tempFolder){
        return diff(filterDexFile(oldApkFolder), filterDexFile(newApkFolder), tempFolder);
    }

    public static String diff(List<String> oldDexes, List<String> newDexes, String tempFolder){
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

            String oldVersion = SmaliUtils.getFieldValue(new File(oldDexSmaliFolder, APPINFO_CLASS_NAME.replace(".", File.separator) + ".smali").getAbsolutePath(), APPINFO_CLASS_NAME, VERSION_FIELD_NAME);
            String newVersion = SmaliUtils.getFieldValue(new File(newDexSmaliFolder, APPINFO_CLASS_NAME.replace(".", File.separator) + ".smali").getAbsolutePath(), APPINFO_CLASS_NAME, VERSION_FIELD_NAME);

            if(VersionUtils.compareVersion(oldVersion, newVersion) > 0) {
                internalDiff(oldDexSmaliFolder, newDexSmaliFolder, tempDexSmaliFolder);
                smali2dex(tempDexSmaliFolder.getAbsolutePath(), new File(tempFolder, "patch.dex").getAbsolutePath());
                return newVersion;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FileUtils.delete(dexFolder, true);
        }
        return null;
    }

    public static List<String> internalDexDiff(File oldFolder, File newFolder, File tempFolder) {
        List<String> oldFileList = getFileList(oldFolder.getAbsolutePath(), "");
        List<String> newFileList = getFileList(newFolder.getAbsolutePath(), "");
        List<String> newFileTempList = new ArrayList<>();
        newFileTempList.addAll(newFileList);

        List<String> deleteFilePathArray = new ArrayList<>();
        List<String> addedFilePathArray = new ArrayList<>();

        for(String oldFileName : oldFileList){
            if(!addedFilePathArray.contains(oldFileName) && newFileList.contains(oldFileName)){
                String oldMd5 = MD5.md5File(new File(oldFolder, oldFileName));
                String newMd5 = MD5.md5File(new File(newFolder, oldFileName));
                List<String> tempAddFileList = new ArrayList<>();
                if(newMd5.equals(oldMd5)){
                    // 相等
                    tempAddFileList.add(oldFileName);
                }else{
                    String baseName = oldFileName.replaceAll("\\$[0-9]+", "");
                    for (String newFileName : newFileTempList){
                        if(baseName.equals(newFileName.replaceAll("\\$[0-9]+", ""))){
                            addedFilePathArray.add(newFileName);
                            tempAddFileList.add(newFileName);
                        }
                    }
                }
                newFileList.removeAll(tempAddFileList);
            }else{
                // 则是删除的资源
                deleteFilePathArray.add(oldFileName);
            }
        }
        addedFilePathArray.addAll(newFileList);
        for(String name : addedFilePathArray){
            File file = new File(newFolder, name);
            File fileTemp = new File(tempFolder, name);
            FileUtils.copyFile(file, fileTemp);
        }
        return deleteFilePathArray;
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
