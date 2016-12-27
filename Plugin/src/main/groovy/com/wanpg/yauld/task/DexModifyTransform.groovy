package com.wanpg.yauld.task

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.google.common.collect.ImmutableSet
import com.wanpg.yauld.Command
import com.wanpg.yauld.ConfigParams
import com.wanpg.yauld.HotFix
import com.wanpg.yauld.utils.FileUtils
import com.wanpg.yauld.utils.ReferenceParser
import com.wanpg.yauld.utils.Utils
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

        // 是否按照更新的打包
        if (configParams.build_enable) {
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
                            // 解压开发者自有代码到临时目录
                            ZipUtil.unpack(jarInput.file, oldClassesTmpFolder)
                        }
                    }
                }
            }

            // 替换自己的AppInfo.class
            String tempFolder = HotFix.getTempFolder(project, flavor, buildType)
//            Command.execute("javac", "${tempFolder}${File.separator}AppInfo.java", "-d", outDir.path)
            Command.execute("javac", "-d", classesFolder.path, "-source", "1.7", "-target", "1.7", "${tempFolder}${File.separator}AppInfo.java")
            boolean isMulitDex = false
            // 编译原APP的代码为main.dex
            if (configParams.main_dex_list) {
                // 读取maindexlist的list
                List<String> mainDexList = FileUtils.readFileByLines(project.file(configParams.main_dex_list))
                if(mainDexList != null && !mainDexList.isEmpty()){
                    isMulitDex = true
                    ArrayList<String> classFolderArray = new ArrayList<>()
                    classFolderArray.add(oldClassesTmpFolder.path)
                    ReferenceParser referenceParser = new ReferenceParser(mainDexList, classFolderArray)

                    List<String> referenceList = referenceParser.parse()
                    String mainDexListFilePath = "${tmpFolder.path}/mainDexList.txt"
                    def formatPackageList2PathList = ReferenceParser.formatPackageList2PathList(referenceList, ".class")
                    FileUtils.writeFileLines(mainDexListFilePath, formatPackageList2PathList)
                    Command.execute(dxPath, "--dex", "--output", dexFolder.path,
                            "--multi-dex",
                                "--set-max-idx-number=40000",
                            "--main-dex-list=${mainDexListFilePath}".toString(), "--minimal-main-dex",
                            oldClassesTmpFolder.path)
                }
            }

            if(!isMulitDex){
                Command.execute(dxPath, "--dex", "--output", dexFolder.path,
                        "--multi-dex",
                            "--set-max-idx-number=40000",
                        oldClassesTmpFolder.path)
            }

            // 压缩移动系统编译的Dex 到自己的临时目录
            ZipUtil.pack(dexFolder, new File("${HotFix.getTempFolder(project, flavor, buildType)}/yauld-dex.zip"))
//            tmpFolder.deleteDir()
            oldClassesTmpFolder.deleteDir()
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
                    if (jarInput.file.isFile()) {
                        ZipUtil.unpack(jarInput.file, classesFolder)
                    }
                }
            }
        }
    }
}
