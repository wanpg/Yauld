package com.wanpg.yauld.patcher;

import android.content.Context;

import com.wanpg.yauld.YauldDex;
import com.wanpg.yauld.bs.JBPatch;
import com.wanpg.yauld.utils.FileUtils;
import com.wanpg.yauld.utils.ZipUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by wangjinpeng on 2016/12/30.
 */

public class ResourcePatcher {

    /**
     *
     * @param context
     * @param rootFolder
     * @param updateTempFolder
     * @param resZipPath
     * @return
     */
    public static boolean patch(Context context, String rootFolder, String updateTempFolder, String resZipPath){
        YauldDex.debugWithTimeMillis("ResourcePatcher---patch---A");
        File resourceTempFolder = new File(rootFolder, "resource");
        if(!ZipUtils.unZipApkResources(context.getApplicationInfo().sourceDir, resourceTempFolder.getAbsolutePath())){
            return false;
        }
        YauldDex.debugWithTimeMillis("ResourcePatcher---patch---B");

        File oldResArsc = new File(resourceTempFolder, "resources.arsc");
        File newResArsc = new File(resourceTempFolder, "resources_new.arsc");
        File resourcesArscPatchFile = new File(updateTempFolder, "resources.arsc.patch");
        try {
            JBPatch.bspatch(oldResArsc, newResArsc, resourcesArscPatchFile);
            YauldDex.debugWithTimeMillis("ResourcePatcher---patch---C");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        if(!newResArsc.exists()){
            return false;
        }
        oldResArsc.delete();
        newResArsc.renameTo(oldResArsc);
        YauldDex.debugWithTimeMillis("ResourcePatcher---patch---D");

        // 移除不需要的资源

        // 复制变化的和新增的资源
        FileUtils.copy(new File(updateTempFolder, "resource"), resourceTempFolder.getParentFile());
        // 压缩资源到 resources.ap_
        YauldDex.debugWithTimeMillis("ResourcePatcher---patch---E");
        if(!ZipUtils.compress(resourceTempFolder, new File(resZipPath))){
            YauldDex.debugWithTimeMillis("ResourcePatcher---patch---F");
            return false;
        }
        FileUtils.delete(resourceTempFolder, true);
        YauldDex.debugWithTimeMillis("ResourcePatcher---patch---G");
        return true;
    }
}
