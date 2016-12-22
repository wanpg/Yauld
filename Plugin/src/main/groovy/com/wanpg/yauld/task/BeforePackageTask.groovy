package com.wanpg.yauld.task

import com.wanpg.yauld.HotFix
import com.wanpg.yauld.Utils
import org.zeroturnaround.zip.FileSource
import org.zeroturnaround.zip.ZipEntrySource
import org.zeroturnaround.zip.ZipUtil

/**
 * Created by wangjinpeng on 2016/12/15.
 */
class BeforePackageTask extends BaseTask{

    String resourcesPackOutPath

    @Override
    void onTaskExecute() {
        super.onTaskExecute()
        Utils.print("此处执行BeforePackageTask")
        Utils.print("resourcesPackOutPath 的路径 ${resourcesPackOutPath}")

        File resourceZip = new File(resourcesPackOutPath)
        if(!resourceZip.exists()){
            throw new IllegalStateException("找不到打包前的资源文件")
        }

        String tmpFolder = HotFix.getTempFolder(project, flavor, buildType)
        File dexZip = new File("${tmpFolder}/yauld-dex.zip")
        if(dexZip.exists()) {
            def sources = new ZipEntrySource[1]
            sources[0] = new FileSource("yauld-dex.zip", dexZip)
            ZipUtil.addOrReplaceEntries(resourceZip, sources)
        }
    }
}
