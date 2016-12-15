package com.wanpg.yauld.task

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.google.common.collect.ImmutableSet
import com.wanpg.yauld.Utils

/**
 * Created by wangjinpeng on 2016/12/15.
 */
class DexModifyTransform extends Transform{

    @Override
    String getName() {
        return "YauldModifyTransform"
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
        return true
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        Utils.print("这是我自己的Transform")
        transformInvocation.inputs.each { input ->
            input.directoryInputs.each { DirectoryInput directoryInput ->
                Utils.print("输入的directoryInputs---->${directoryInput.name}")
            }
            input.jarInputs.each { JarInput jarInput ->
                Utils.print("输入的jarInputs---->${jarInput.name}")
            }
        }
    }
}
