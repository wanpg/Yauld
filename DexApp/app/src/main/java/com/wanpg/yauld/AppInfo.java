package com.wanpg.yauld;

import android.content.Context;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by wangjinpeng on 2016/12/11.
 */

public class AppInfo {

    public String application_id = "";
    public String application_name = "";

    public static AppInfo load(Context context){
        AppInfo appInfo = new AppInfo();
        try {
            Properties properties = new Properties();
            properties.load(context.getClass().getResourceAsStream("/" + YauldDex.APP_INFO_NAME));
            appInfo.application_id = properties.getProperty("application_id");
            appInfo.application_name = properties.getProperty("application_name");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(TextUtils.isEmpty(appInfo.application_id)){
            appInfo.application_id = context.getPackageName();
        }
        return appInfo;
    }

    public static AppInfo load(Context context, InputStream inputStream){
        AppInfo appInfo = new AppInfo();
        try {
            Properties properties = new Properties();
            properties.load(inputStream);
            appInfo.application_id = properties.getProperty("application_id");
            appInfo.application_name = properties.getProperty("application_name");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(TextUtils.isEmpty(appInfo.application_id)){
            appInfo.application_id = context.getPackageName();
        }
        return appInfo;
    }
}
