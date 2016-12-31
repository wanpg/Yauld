package com.wanpg.yauld;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.wanpg.yauld.utils.Utils;

import java.util.List;

/**
 * Created by wangjinpeng on 2016/12/10.
 */

public class YauldDexApplication extends Application {

    private Application realApplication;
    private String mainThreadName;
    private YauldDex yauldDex;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        YauldDex.debug("----------------A");
        yauldDex = new YauldDex();
        // 初始化，加载appinfo
        yauldDex.install(this);

        // 创建真正的Application
        realApplication = createRealApplication();
        if (realApplication == null) {
            throw new IllegalArgumentException("找不到真正的Application");
        }

        // 调用真正Application的 attachBaseContext 方法
        Utils.invokeMethod(ContextWrapper.class, "attachBaseContext", Context.class, realApplication, base);
    }

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
        YauldDex.monkeyPatchApplication(this, this.realApplication, yauldDex.externalResourcePath);
        super.onCreate();
        mainThreadName = Thread.currentThread().getName();
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


    private void runOnUiThread(Runnable runnable){
        if(Thread.currentThread().getName().equals(mainThreadName)){
            runnable.run();
        }else{
            if(Looper.myLooper() == null) {
                Looper.prepare();
            }
            new Handler(Looper.getMainLooper()).post(runnable);
        }
    }

    public static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }
}
