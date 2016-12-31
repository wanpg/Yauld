package com.wanpg.yauld.patcher;

import android.content.Context;

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
        File resourceTempFolder = new File(rootFolder, "resource");
        try {
            ZipUtils.unZipApkResources(context.getApplicationInfo().sourceDir, resourceTempFolder.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        File oldResArsc = new File(resourceTempFolder, "resources.arsc");
        File newResArsc = new File(resourceTempFolder, "resources_new.arsc");
        File resourcesArscPatchFile = new File(updateTempFolder, "resources.arsc.patch");
        try {
            JBPatch.bspatch(oldResArsc, newResArsc, resourcesArscPatchFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        if(!newResArsc.exists()){
            return false;
        }
        oldResArsc.delete();
        newResArsc.renameTo(oldResArsc);

        // 移除不需要的资源

        // 复制变化的和新增的资源
        FileUtils.copy(new File(updateTempFolder, "resource"), resourceTempFolder.getParentFile());
        // 压缩资源到 resources.ap_
        try {
            ZipUtils.compress(resourceTempFolder, new File(resZipPath));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        FileUtils.delete(resourceTempFolder, true);
        return true;
    }
}
