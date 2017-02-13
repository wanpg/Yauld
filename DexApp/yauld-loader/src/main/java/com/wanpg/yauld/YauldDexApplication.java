package com.wanpg.yauld;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.wanpg.yauld.utils.Utils;

/**
 * Created by wangjinpeng on 2016/12/10.
 */

public class YauldDexApplication extends Application {

    private Application realApplication;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        printLoader(getClassLoader());

        // 初始化，加载appinfo
        YauldDex.install(this);

        printLoader(getClassLoader());
        // 创建真正的Application
        realApplication = createRealApplication();
        if (realApplication == null) {
            throw new IllegalArgumentException("找不到真正的Application");
        }

        // 调用真正Application的 attachBaseContext 方法
        Utils.invokeMethod(ContextWrapper.class, "attachBaseContext", Context.class, realApplication, base);
    }

    private void printLoader(ClassLoader loader){
        Log.d("YauldDexApplication", loader.getClass().getSimpleName());
        if(loader.getParent() != null) {
            printLoader(loader.getParent());
        }
    }

    /**
     * 创建真正的Application
     * @return
     */
    private Application createRealApplication() {
        Application realApplication = null;
        try {
            Class<?> aClass = Class.forName(AppInfo.APPLICATION_NAME);
            if (aClass != null) {
                realApplication = (Application) aClass.getConstructor().newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (realApplication == null) {
            realApplication = new Application();
        }
        return realApplication;
    }

    @Override
    public void onCreate() {
        YauldPatcher.monkeyPatchApplication(this, this, this.realApplication, YauldDex.externalResourcePath);
        YauldPatcher.monkeyPatchExistingResources(this, YauldDex.externalResourcePath, null);
        super.onCreate();
        this.realApplication.onCreate();
        YauldDex.register(this);
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
