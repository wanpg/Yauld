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


    static String getUpdateZipMd5(Context context){
        return getSharePreferences(context).getString("update_zip_md5", null);
    }

    static void setUpdateZipMd5(Context context, String md5){
        SharedPreferences.Editor edit = getSharePreferences(context).edit();
        if(md5 == null){
            edit.remove("update_zip_md5");
        }else{
            edit.putString("update_zip_md5", md5);
        }
        edit.commit();
    }

    static String getUpdateDexPatchMd5(Context context){
        return getSharePreferences(context).getString("update_dex_patch_md5", null);
    }

    static void setUpdateDexPatchMd5(Context context, String md5){
        SharedPreferences.Editor edit = getSharePreferences(context).edit();
        if(md5 == null){
            edit.remove("update_dex_patch_md5");
        }else{
            edit.putString("update_dex_patch_md5", md5);
        }
        edit.commit();
    }

    static String getUpdateResZipMd5(Context context){
        return getSharePreferences(context).getString("update_res_zip_md5", null);
    }

    static void setUpdateResZipMd5(Context context, String md5){
        SharedPreferences.Editor edit = getSharePreferences(context).edit();
        if(md5 == null){
            edit.remove("update_res_zip_md5");
        }else{
            edit.putString("update_res_zip_md5", md5);
        }
        edit.commit();
    }

    static String getUpdateContentType(Context context){
        return getSharePreferences(context).getString("update_content_type", null);
    }
    static void setUpdateContentType(Context context, String type){
        getSharePreferences(context).edit().putString("update_content_type", type).commit();
    }
}
