package com.wanpg.yauld;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.wanpg.yauld.utils.Utils;

import java.util.List;
import java.util.Set;

/**
 * Created by wangjinpeng on 2016/12/10.
 */

public class YauldDexApplication extends Application {

    private Application realApplication;
    private String mainThreadName;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        YauldDex.debug("----------------A");

        // 初始化，加载appinfo
        new YauldDex().install(this);

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
        YauldDex.monkeyPatchApplication(this, this, this.realApplication, null);
        super.onCreate();
        mainThreadName = Thread.currentThread().getName();
        String processName = getProcessName(this, android.os.Process.myPid());

        this.realApplication.onCreate();
        if(processName != null){
            boolean defaultProcess = processName.equals(getPackageName());
            if(defaultProcess){
                //当前应用的初始化
                // 此处做interface的处理
                YauldDex.isLoadFinished(new YauldDex.OnLoadListener() {
                    @Override
                    public void onComplete() {
                        invokeInterfaceForApp();
                    }
                });
            }
        }
    }

    private void invokeInterfaceForApp(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ApplicationInfo appInfo = null;
                try {
                    appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                if(appInfo != null) {
                    Bundle metaData = appInfo.metaData;
                    if(metaData != null) {
                        Set<String> strings = metaData.keySet();
                        if(strings != null && !strings.isEmpty()) {
                            for (String name : strings) {
                                Object value = metaData.get(name);
                                if ("YauldInterface".equals(value)) {
                                    try {
                                        Class<?> aClass = Class.forName(name);
                                        if (aClass != null) {
                                            Object o = aClass.getConstructor().newInstance();
                                            if (o instanceof YauldInterface) {
                                                ((YauldInterface) o).onInitialize(realApplication);
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
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
