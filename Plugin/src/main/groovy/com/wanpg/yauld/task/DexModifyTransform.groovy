package com.wanpg.yauld.task

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.google.common.collect.ImmutableSet
import com.wanpg.yauld.Command
import com.wanpg.yauld.ConfigParams
import com.wanpg.yauld.HotFix
import com.wanpg.yauld.Utils
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
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        configParams = project.extensions.findByType(ConfigParams.class)
        def classesFolder = transformInvocation.outputProvider.getContentLocation("classes", outputTypes, scopes, Format.DIRECTORY)
        def tmpFolder = transformInvocation.outputProvider.getContentLocation("temp", outputTypes, scopes, Format.DIRECTORY)

        classesFolder.deleteDir()
        classesFolder.mkdirs()

        if (configParams.enable) {
            def taskSuffix = transformInvocation.context.path.toLowerCase().split(getTaskNamePrefix().toLowerCase())[1]

            if (taskSuffix.endsWith("release")) {
                buildType = "release"
            } else if (taskSuffix.endsWith("debug")) {
                buildType = "debug"
            }
            flavor = taskSuffix.replace(buildType, "")

            File oldClassesTmpFolder = new File("${tmpFolder.path}/classes")

            oldClassesTmpFolder.mkdirs()

            def dexFolder = transformInvocation.outputProvider.getContentLocation("dex", outputTypes, scopes, Format.DIRECTORY)
            dexFolder.mkdirs()

            String dxPath = "${appExtension.sdkDirectory.path}/build-tools/${appExtension.buildToolsVersion}/dx".toString()

            def classIndex = 1
            // 移动原来的class到临时目录
            transformInvocation.inputs.each { inputs ->
                Utils.print("开始转换directoryInputs")
                inputs.directoryInputs.each { directInput ->
                    int pathBitLen = directInput.file.toString().length()
                    Utils.print(directInput.file.path)
                    directInput.file.traverse { fileInput ->
                        def path = "${fileInput.toString().substring(pathBitLen)}"
                        if (fileInput.isDirectory()) {
                            new File(oldClassesTmpFolder, path).mkdirs()
                        } else {
                            new File(oldClassesTmpFolder, path).bytes = fileInput.bytes
                        }
                    }
                }


                Utils.print("开始转换jarInputs")
                inputs.jarInputs.each { jarInput ->
                    Utils.print("jarInput.name是${jarInput.name}---jarInput.path是${jarInput.file.path}")
                    if (jarInput.file.isFile()) {
                        if (jarInput.file.path.contains("com.wanpg.yauld") && jarInput.file.path.contains("hotfix-loader")) {
                            // 解压自己的jar 到class文件
                            ZipUtil.unpack(jarInput.file, classesFolder)
                        } else {
                            // 将第三方库编译为dex
                            if (configParams.multi_dex) {
                                Command.execute(dxPath, "--dex", "--output", "${dexFolder}/classes${classIndex}.dex".toString(), jarInput.file.path)
                            } else {
                                new File(oldClassesTmpFolder, "classes${classIndex}.jar".toString()).bytes = jarInput.file.bytes
                            }
                            classIndex++
                        }
                    }
                }
            }

            // 替换自己的AppInfo.class
            String tempFolder = HotFix.getTempFolder(project, flavor, buildType)
//            Command.execute("javac", "${tempFolder}${File.separator}AppInfo.java", "-d", outDir.path)
            Command.execute("javac", "-d", classesFolder.path, "-source", "1.7", "-target", "1.7", "${tempFolder}${File.separator}AppInfo.java")

            // 编译原APP的代码为main.dex
            if (configParams.multi_dex) {
                Command.execute(dxPath, "--dex", "--output", "${dexFolder.path}/classes.dex", oldClassesTmpFolder.path)
            } else {
                if(configParams.main_dex_list){
                    Command.execute(dxPath, "--dex", "--output", dexFolder.path,
                            "--multi-dex",
//                            "--set-max-idx-number=50000",
                            "--main-dex-list=${project.file(configParams.main_dex_list).path}".toString(), "--minimal-main-dex",
                            oldClassesTmpFolder.path)
                }else{
                    Command.execute(dxPath, "--dex", "--output", dexFolder.path, "--multi-dex", "--set-max-idx-number=50000", oldClassesTmpFolder.path)
                }
            }

            // 压缩移动系统编译的Dex 到自己的临时目录
            ZipUtil.pack(dexFolder, new File("${HotFix.getTempFolder(project, flavor, buildType)}/yauld-dex.zip"))
            tmpFolder.deleteDir()
        } else {
            // 这里也要原封不动的拷贝
            transformInvocation.inputs.each { inputs ->
                inputs.directoryInputs.each { directoryInput ->
                    int pathBitLen = directoryInput.file.toString().length()
                    directoryInput.file.traverse { fileInput ->
                        def path = "${fileInput.toString().substring(pathBitLen)}"
                        if (fileInput.isDirectory()) {
                            new File(classesFolder, path).mkdirs()
                        } else {
                            new File(classesFolder, path).bytes = fileInput.bytes
                        }
                    }
                }

                inputs.jarInputs.each { jarInput ->
                    if(jarInput.file.isFile()){
                        ZipUtil.unpack(jarInput.file, classesFolder)
                    }
                }
            }
        }
    }
}
