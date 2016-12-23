package com.wanpg.yauld

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState

/**
 * Created by wangjinpeng on 2016/12/22.
 *
 * 用来测试 资源生成与task执行的关系
 */
class HotfixBuildListener implements TaskExecutionListener, BuildListener{
    @Override
    void buildStarted(Gradle gradle) {

    }

    @Override
    void settingsEvaluated(Settings settings) {

    }

    @Override
    void projectsLoaded(Gradle gradle) {

    }

    @Override
    void projectsEvaluated(Gradle gradle) {

    }

    @Override
    void buildFinished(BuildResult result) {

    }

    @Override
    void beforeExecute(Task task) {

    }

    @Override
    void afterExecute(Task task, TaskState state) {

    }
}
