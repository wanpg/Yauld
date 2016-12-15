package com.wanpg.yauld.task

import com.wanpg.yauld.FileUtils
import com.wanpg.yauld.HotFix
import org.zeroturnaround.zip.ZipUtil

/**
 * Created by wangjinpeng on 2016/12/14.
 */
class DexModifyTask extends BaseTask {

    String dexFolderPath

    @Override
    void onTaskExecute() {
        super.onTaskExecute()
        dexFolderPath = "${project.getBuildDir()}/intermediates/transforms/dex/${(flavor ? flavor + File.separator : "")}${buildType}/folders/1000/1f/main"
        // 压缩移动系统编译的Dex 到自己的临时目录
        ZipUtil.pack(new File(dexFolderPath), new File("${HotFix.getTempFolder(project, flavor, buildType)}/yauld-dex.zip"))
        // 删除原来的dex
        FileUtils.delete(dexFolderPath, false)
        // 替换自己的dex到系统的dex
        FileUtils.copyStream(getClass().getResourceAsStream("/classes.dex"), dexFolderPath, "classes.dex")
    }
}
