package com.wanpg.yauld;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Created by wangjinpeng on 2016/12/10.
 */

public class YauldDexApplication extends Application {

    private Application realApplication;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        YauldDex.debug("----------------A");

        // 初始化，加载appinfo
        YauldDex.init(base);
        // 解压yuald-dex.zip
        YauldDex.unZipDex(base);

        // 设置classLoader
        YauldDex.setupClassLoader(base, getClassLoader());

        // 创建真正的Application
        realApplication = YauldDex.createRealApplication();
        if (realApplication == null) {
            throw new IllegalArgumentException("找不到真正的Application");
        }

        // 调用真正Application的 attachBaseContext 方法
        YauldDex.invokeMethod(ContextWrapper.class, "attachBaseContext", Context.class, realApplication, base);
    }

    @Override
    public void onCreate() {
        YauldDex.monkeyPatchApplication(this, this, this.realApplication, null);
        super.onCreate();
        this.realApplication.onCreate();
    }

    @Override
    public void registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        realApplication.registerActivityLifecycleCallbacks(callback);
    }

    @Override
    public void registerComponentCallbacks(ComponentCallbacks callback) {
        realApplication.registerComponentCallbacks(callback);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void registerOnProvideAssistDataListener(OnProvideAssistDataListener callback) {
        realApplication.registerOnProvideAssistDataListener(callback);
    }

    @Override
    public void unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        realApplication.unregisterActivityLifecycleCallbacks(callback);
    }

    @Override
    public void unregisterComponentCallbacks(ComponentCallbacks callback) {
        realApplication.unregisterComponentCallbacks(callback);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void unregisterOnProvideAssistDataListener(OnProvideAssistDataListener callback) {
        realApplication.unregisterOnProvideAssistDataListener(callback);
    }

    @Override
    public Context createPackageContext(String packageName, int flags) throws PackageManager.NameNotFoundException {
        Context localContext = realApplication.createPackageContext(packageName, flags);
        if (localContext == null) {
            return realApplication;
        }
        return localContext;
    }

}
