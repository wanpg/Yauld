package com.wanpg.yauld.task

import com.wanpg.yauld.ConfigParams
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by wangjinpeng on 2016/12/14.
 */
class BaseTask extends DefaultTask {

    String buildType = ""
    String flavor = ""
    ConfigParams configParams

    @TaskAction
    void onTaskExecute() {
        configParams = project.extensions.findByType(ConfigParams.class)
    }
}
