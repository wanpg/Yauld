package com.wanpg.yauld.diff;

import com.wanpg.yauld.bs.JBDiff;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by wangjinpeng on 2016/12/30.
 */
public class ResourceDiff extends BaseDiff{

    /**
     * @param oldApkFolder 老版本的apk文件夹
     * @param newApkFolder 新版本的apk文件夹
     * @param tempFolder   输出的临时文件夹
     */
    public static void diff(String oldApkFolder, String newApkFolder, String tempFolder) {
        File resourceOutFolder = new File(tempFolder, "resource");
        // 差分assets
        List<String> assetsDeleteFiles = internalDiff(new File(oldApkFolder, "assets"), new File(newApkFolder, "assets"), new File(resourceOutFolder, "assets"));
        // 差分res
        List<String> resDeleteFiles = internalDiff(new File(oldApkFolder, "res"), new File(newApkFolder, "res"), new File(resourceOutFolder, "res"));
        // 差分resources.arsc
        try {
            JBDiff.bsdiff(new File(oldApkFolder, "resources.arsc"), new File(newApkFolder, "resources.arsc"), new File(tempFolder, "resources.arsc.patch"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
