package com.wanpg.yauld

import org.gradle.api.Project

/**
 * Created by wangjinpeng on 2016/12/12.
 */
class HotFix {

    static String getTempFolder(Project project, String flavor, String buildType){

        return project.getBuildDir().getPath() + "/intermediates/yauld-temp" +  (flavor ? File.separator + flavor : "") + (buildType ? File.separator + buildType : "")
    }
}
