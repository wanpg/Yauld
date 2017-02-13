package com.wanpg.yauld.diff;

import com.wanpg.yauld.utils.FileUtils;
import com.wanpg.yauld.utils.MD5;
import com.wanpg.yauld.utils.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangjinpeng on 2016/12/31.
 */
public class BaseDiff {

    public static List<String> internalDiff(File oldFolder, File newFolder, File tempFolder) {
        List<String> oldFileList = getFileList(oldFolder.getAbsolutePath(), "");
        List<String> newFileList = getFileList(newFolder.getAbsolutePath(), "");

        List<String> deleteFilePathArray = new ArrayList<>();
        List<String> addedFilePathArray = new ArrayList<>();
        for(String oldFileName : oldFileList){
            if(newFileList.contains(oldFileName)){
                String oldMd5 = MD5.md5File(new File(oldFolder, oldFileName));
                String newMd5 = MD5.md5File(new File(newFolder, oldFileName));
                if(newMd5.equals(oldMd5)){
                    // 相等
                }else{
                    addedFilePathArray.add(oldFileName);
                }
                newFileList.remove(oldFileName);
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

    public static List<String> getFileList(String folderPath, String baseDir) {
        ArrayList<String> result = new ArrayList<>();
        File folder = new File(folderPath);
        if(folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isHidden()) {
                        continue;
                    }
                    String fileName = file.getName();
                    String pathName = TextUtils.isEmpty(baseDir) ? fileName : (baseDir + File.separator + fileName);
                    if (file.isDirectory()) {
                        result.addAll(getFileList(file.getAbsolutePath(), pathName));
                    } else if (file.isFile()) {
                        result.add(pathName);
                    }
                }
            }
        }
        return result;
    }

}
