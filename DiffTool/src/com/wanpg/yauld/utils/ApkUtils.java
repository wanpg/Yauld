package com.wanpg.yauld.utils;

import com.wanpg.yauld.Command;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wangjinpeng on 2017/2/6.
 */
public class ApkUtils {

    public static void main(String[] args){

        LogUtils.print(getApkPackage("/Users/wangjinpeng/Desktop/app-yifangyun-debug.apk"));
        LogUtils.print(getApkVersionName("/Users/wangjinpeng/Desktop/app-yifangyun-debug.apk"));
        LogUtils.print(getApkMd5("/Users/wangjinpeng/Desktop/app-yifangyun-debug.apk"));
    }


    public static String getApkPackage(String apkPath){
        List<String> resultArray = Command.executeWithResult("/Users/wangjinpeng/Program/android-sdk-macosx/build-tools/25.0.2/aapt", "d", "badging", apkPath);

        for(String resultStr : resultArray){
            if(resultStr.startsWith("package:")){
                Pattern pattern = Pattern.compile("name='.+?'");
                Matcher matcher = pattern.matcher(resultStr);
                if( matcher.find()){
                    String group = matcher.group();
                    return group.split("=")[1].replace("'", "");
                }
                break;
            }
        }
        return null;
    }

    public static String getApkVersionName(String apkPath){
        List<String> resultArray = Command.executeWithResult("/Users/wangjinpeng/Program/android-sdk-macosx/build-tools/25.0.2/aapt", "d", "badging", apkPath);

        for(String resultStr : resultArray){
            if(resultStr.startsWith("package:")){
                Pattern pattern = Pattern.compile("versionName='.+?'");
                Matcher matcher = pattern.matcher(resultStr);
                if( matcher.find()){
                    String group = matcher.group();
                    return group.split("=")[1].replace("'", "");
                }
                break;
            }
        }
        return null;
    }

    public static String getApkMd5(String apkPath){
        return MD5.md5File(apkPath);
    }
}
