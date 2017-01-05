package com.wanpg.yauld;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.os.Build;

import com.wanpg.yauld.utils.Utils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Created by wangjinpeng on 2016/12/10.
 */

public class YauldDexApplication extends Application {

    private Application realApplication;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // 初始化，加载appinfo
        YauldDex.install(this);
        // 创建真正的Application
        realApplication = createRealApplication();
        if (realApplication == null) {
            throw new IllegalArgumentException("找不到真正的Application");
        }

        // 调用真正Application的 attachBaseContext 方法
        Utils.invokeMethod(ContextWrapper.class, "attachBaseContext", Context.class, realApplication, base);
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
        monkeyPatchApplication(this, this.realApplication, YauldDex.externalResourcePath);
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

    /**
     * 替换所有ActivityThread中的Application为realApplication
     * 替换资源路径为resource.zip
     * @param bootstrap
     * @param realApplication
     * @param externalResourceFile
     */
    private static void monkeyPatchApplication(Application bootstrap, Application realApplication, String externalResourceFile) {
        try {
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Method m = activityThread.getMethod("currentActivityThread");
            m.setAccessible(true);
            Object currentActivityThread = m.invoke(activityThread);
            if (realApplication != null) {
                if (currentActivityThread != null) {
                    Field mInitialApplication = activityThread.getDeclaredField("mInitialApplication");
                    mInitialApplication.setAccessible(true);
                    Application initialApplication = (Application) mInitialApplication.get(currentActivityThread);
                    if (initialApplication == bootstrap) {
                        mInitialApplication.set(currentActivityThread, realApplication);
                    }
                }
                Field mAllApplications = activityThread.getDeclaredField("mAllApplications");
                mAllApplications.setAccessible(true);
                List<Application> allApplications = (List<Application>) mAllApplications.get(currentActivityThread);
                for (int i = 0; i < allApplications.size(); i++) {
                    if (allApplications.get(i) == bootstrap) {
                        allApplications.set(i, realApplication);
                    }
                }
            }
            Class<?> loadedApkClass;
            try {
                loadedApkClass = Class.forName("android.app.LoadedApk");
            } catch (ClassNotFoundException e) {
                loadedApkClass = Class.forName("android.app.ActivityThread$PackageInfo");
            }
            Field mApplication = loadedApkClass.getDeclaredField("mApplication");
            mApplication.setAccessible(true);
            Field mResDir = loadedApkClass.getDeclaredField("mResDir");
            mResDir.setAccessible(true);

            Field mLoadedApk = null;
            try {
                mLoadedApk = Application.class.getDeclaredField("mLoadedApk");
            } catch (NoSuchFieldException e) {
            }
            for (String fieldName : new String[]{"mPackages", "mResourcePackages"}) {
                Field field = activityThread.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(currentActivityThread);
                for (Map.Entry<String, WeakReference<?>> entry : ((Map<String, WeakReference<?>>) value).entrySet()) {
                    Object loadedApk = ((WeakReference) entry.getValue()).get();
                    if (loadedApk != null) {
                        if (mApplication.get(loadedApk) == bootstrap) {
                            if (realApplication != null) {
                                mApplication.set(loadedApk, realApplication);
                            }
                            if (externalResourceFile != null) {
                                mResDir.set(loadedApk, externalResourceFile);
                            }
                            if ((realApplication != null)
                                    && (mLoadedApk != null)) {
                                mLoadedApk.set(realApplication, loadedApk);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
