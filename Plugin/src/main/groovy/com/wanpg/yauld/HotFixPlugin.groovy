package com.wanpg.yauld

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import com.wanpg.yauld.task.BaseTask
import com.wanpg.yauld.task.BeforePackageTask
import com.wanpg.yauld.task.DexModifyTask
import com.wanpg.yauld.task.ManifestModifyTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by wangjinpeng on 2016/12/9.
 */
class HotFixPlugin implements Plugin<Project> {

    void apply(Project project) {

        project.extensions.create("yauld_hotfix_params", ConfigParams)
//        project.gradle.addListener(new HotFixListener())

        def android = project.extensions.android //AppExtension

        if (!(android instanceof AppExtension)) {
            project.logger.error("Your Yauld config must add to build.gradle of project which applied 'com.android.application'")
        }

        AppExtension androidExtension = android
//        android.registerTransform(new DexModifyTransform())

        project.afterEvaluate {
            ConfigParams configParams = project.extensions.findByType(ConfigParams)

            Utils.print("Yauld 打包 开关 ${configParams.enable}")
            Utils.print("Yauld 打包 主工程名称 ${configParams.mainProjectName}")

            androidExtension.applicationVariants.all { ApplicationVariant variant ->

                BaseVariantOutput variantOutput = variant.outputs.first()

                if (variantOutput instanceof ApkVariantOutputImpl) {

                    def instantRunMode = variantOutput.processResources.instantRunMode
                    Utils.print("variantOutput.processResources.instantRunMode 是否开启 ${instantRunMode}")
                    if (instantRunMode) {
                        Utils.print("Yauld Hotfix Plug-in can not run under instant run mode")
                        return
                    }

                    List<BaseTask> myTasks = new ArrayList<>()

                    String variantName = variant.name.capitalize()
                    String buildTypeName = variant.buildType.name
                    String flavorName = variant.flavorName

                    // 设置Manifest修改的
                    def manifestModifyTask = project.tasks.create("yauldProcess${variantName}Manifest", ManifestModifyTask)
                    myTasks.add(manifestModifyTask)
                    manifestModifyTask.manifestOutPath = variantOutput.processManifest.manifestOutputFile
                    manifestModifyTask.mustRunAfter variantOutput.processManifest

                    variantOutput.processResources.dependsOn manifestModifyTask

                    // 配置dex修改备份的task
                    def dexModifyTask = project.tasks.create("yauldReplaceAndBackup${variantName}Dex", DexModifyTask)
                    myTasks.add(dexModifyTask)
                    variantOutput.packageApplication.dependsOn dexModifyTask

                    // 修改资源，将AppInfo.properties 和 打包的dex 打入 resource.zip
                    def beforePackageTask = project.tasks.create("yauldModify${variantName}ResBeforePackage", BeforePackageTask)
                    myTasks.add(beforePackageTask)
                    beforePackageTask.resourcesPackOutPath = variantOutput.packageApplication.resourceFile.path
                    beforePackageTask.dependsOn dexModifyTask
                    variantOutput.packageApplication.dependsOn beforePackageTask

                    // 为自己的task赋值
                    myTasks.each { baseTask ->
                        baseTask.buildType = buildTypeName
                        baseTask.flavor = flavorName
                    }
                }
            }
        }
    }
}
