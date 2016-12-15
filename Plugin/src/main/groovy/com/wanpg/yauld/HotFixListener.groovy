package com.wanpg.yauld

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.api.execution.TaskActionListener
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState

/**
 * Created by wangjinpeng on 2016/11/16.
 */

public class HotFixListener implements BuildListener, TaskActionListener, TaskExecutionListener {

    public static ConfigParams configParams;

    @Override
    public void buildStarted(Gradle gradle) {
    }

    @Override
    public void settingsEvaluated(Settings settings) {

    }

    @Override
    public void projectsLoaded(Gradle gradle) {

    }

    @Override
    public void projectsEvaluated(Gradle gradle) {
        configParams = gradle.getExtensions().findByType(ConfigParams.class);
        Utils.print("主的编译工程名称为：" + configParams.getMainProjectName());
    }

    @Override
    public void buildFinished(BuildResult result) {

    }

    @Override
    public void beforeActions(Task task) {

    }

    @Override
    public void afterActions(Task task) {

    }

    @Override
    public void beforeExecute(Task task) {
        if(!configParams.isEnable()){
            return;
        }
    }

    @Override
    public void afterExecute(Task task, TaskState state) {
        if(!configParams.isEnable()){
            return;
        }
        if(task.getProject().getName().equalsIgnoreCase(configParams.getMainProjectName())) {
            String name = task.getName();
            if (name.startsWith("transformClassesWithDex")) {
                String transformClassesWithDexFor = name.replace("transformClassesWithDexFor", "");
                String buildType = "debug";
                String flavor = "";
                if (transformClassesWithDexFor.endsWith("Debug")) {
                    buildType = "debug";
                    flavor = transformClassesWithDexFor.replace("Debug", "");
                } else if (transformClassesWithDexFor.endsWith("Release")) {
                    buildType = "release";
                    flavor = transformClassesWithDexFor.replace("Release", "");
                }
                HotFix.backupSrcDex(task.getProject(), flavor, buildType);
                HotFix.createYauldDex(task.getProject(), flavor, buildType, getClass().getResourceAsStream("/classes.dex"));

                String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
                Utils.print("当前hotfix库的路径----->>>" + path);
            } else if (name.startsWith("process") && name.endsWith("Manifest") && !name.contains("AndroidTest")) {
                // 此处修改manifest
                String transformResourcesWithMergeJavaResFor = name.replace("process", "").replace("Manifest", "");
                String buildType = "debug";
                String flavor = "";
                if (transformResourcesWithMergeJavaResFor.endsWith("Debug")) {
                    buildType = "debug";
                    flavor = transformResourcesWithMergeJavaResFor.replace("Debug", "");
                } else if (transformResourcesWithMergeJavaResFor.endsWith("Release")) {
                    buildType = "release";
                    flavor = transformResourcesWithMergeJavaResFor.replace("Release", "");
                }

//                HotFix.modifyManifest(task.getProject(), buildType, flavor);
            }
        }
    }
}
