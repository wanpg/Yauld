package com.wanpg.yauld.task

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.google.common.collect.ImmutableSet
import com.wanpg.yauld.Command
import com.wanpg.yauld.ConfigParams
import com.wanpg.yauld.HotFix
import org.gradle.api.Project
import org.zeroturnaround.zip.ZipUtil

/**
 * Created by wangjinpeng on 2016/12/15.
 */
class DexModifyTransform extends Transform {

    String buildType = ""
    String flavor = ""
    ConfigParams configParams

    static final String NAME = "YauldModify"

    Project project
    AppExtension appExtension

    DexModifyTransform(Project project, AppExtension appExtension) {
        this.project = project
        this.appExtension = appExtension
    }

    @Override
    String getName() {
        return NAME
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return ImmutableSet.of(QualifiedContent.DefaultContentType.CLASSES)
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return ImmutableSet.of(
                QualifiedContent.Scope.PROJECT,
                QualifiedContent.Scope.SUB_PROJECTS,
                QualifiedContent.Scope.PROJECT_LOCAL_DEPS,
                QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS,
                QualifiedContent.Scope.EXTERNAL_LIBRARIES
        )
    }

    @Override
    boolean isIncremental() {
        return false
    }

    String getTaskNamePrefix() {
        return "transformClassesWith${NAME}For"
    }

    @Override
    void transform(TransformInvocation transformInvocation)
            throws TransformException, InterruptedException, IOException {

        configParams = project.extensions.findByType(ConfigParams.class)
        def classesFolder =
                transformInvocation.outputProvider.getContentLocation("classes", outputTypes, scopes, Format.DIRECTORY)

        classesFolder.deleteDir()
        classesFolder.mkdirs()

        // 这里也要原封不动的拷贝
        transformInvocation.inputs.each { inputs ->
            inputs.directoryInputs.each { directoryInput ->
                int pathBitLen = directoryInput.file.toString().length()
                directoryInput.file.traverse { fileInput ->
                    def path = fileInput.toString().substring(pathBitLen)
                    if (fileInput.isDirectory()) {
                        new File(classesFolder, path).mkdirs()
                    } else {
                        new File(classesFolder, path).bytes = fileInput.bytes
                    }
                }
            }

            inputs.jarInputs.each { jarInput ->
                if (jarInput.file.isFile()) {
//                    if (configParams.build_enable && jarInput.file.path.contains("com.wanpg.yauld") && jarInput.file.path.contains("hotfix-loader")) {
//                        ZipUtil.unpack(jarInput.file, classesFolder)
//                    }else{
//                        new File(classesFolder, "${jarInput.name}.jar").bytes = jarInput.file.bytes
//                    }
                    ZipUtil.unpack(jarInput.file, classesFolder)
                }
            }
        }

        if(configParams.build_enable){
            def taskSuffix = transformInvocation.context.path.toLowerCase().split(getTaskNamePrefix().toLowerCase())[1]

            if (taskSuffix.endsWith("release")) {
                buildType = "release"
            } else if (taskSuffix.endsWith("debug")) {
                buildType = "debug"
            }
            flavor = taskSuffix.replace(buildType, "")
            // 替换自己的AppInfo.class
            String tempFolder = HotFix.getTempFolder(project, flavor, buildType)
//            Command.execute("javac", "${tempFolder}${File.separator}AppInfo.java", "-d", outDir.path)
            Command.execute("javac", "-d", classesFolder.path,
                    "-source", "1.7", "-target", "1.7",
                    "${tempFolder}${File.separator}AppInfo.java")
        }
    }
}
