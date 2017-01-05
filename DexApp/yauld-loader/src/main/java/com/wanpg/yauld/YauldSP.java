package com.wanpg.yauld;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by wangjinpeng on 2017/1/5.
 */

@SuppressLint("CommitPrefEdits")
public class YauldSP {

    public static final String CONTENT_DEX = "content_type_dex_only";
    public static final String CONTENT_DEX_RES = "content_type_dex_and_resource";

    private static SharedPreferences getSharePreferences(Context context) {
        return context.getSharedPreferences("yauld_sp", Context.MODE_PRIVATE);
    }

    static long getUpdateZipModifyTime(Context context){
        return getSharePreferences(context).getLong("update_zip_modify_time", System.currentTimeMillis());
    }

    static void setUpdateZipModifyTime(Context context, long time){
        getSharePreferences(context).edit().putLong("update_zip_modify_time", time).commit();
    }

    static long getUpdateFileModifyTime(Context context){
        return getSharePreferences(context).getLong("update_file_modify_time", System.currentTimeMillis());
    }

    static void setUpdateFileModifyTime(Context context, long time){
        getSharePreferences(context).edit().putLong("update_file_modify_time", time).commit();
    }

    static String getUpdateContentType(Context context){
        return getSharePreferences(context).getString("update_content_type", null);
    }
    static void setUpdateContentType(Context context, String type){
        getSharePreferences(context).edit().putString("update_content_type", type).commit();
    }
}
