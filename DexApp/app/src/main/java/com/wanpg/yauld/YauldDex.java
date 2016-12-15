package com.wanpg.yauld;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by wangjinpeng on 2016/12/10.
 */

public class YauldDex {

    static void debug(String info){
        Log.d("yauld", info);
    }

    static final String YAULD_DEX_NAME = "yauld-dex.zip";
    static final String APP_INFO_NAME = "AppInfo.properties";

    public static AppInfo appInfo;

    public static void init(Context context) {
        try {
            YauldDex.debug("----------------init---C");
            ZipFile apkFile = new ZipFile(context.getApplicationInfo().sourceDir);
            Enumeration<? extends ZipEntry> entries = apkFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String zipEntryName = zipEntry.getName();
                if (APP_INFO_NAME.equals(zipEntryName)) {
                    InputStream inputStream = apkFile.getInputStream(zipEntry);
                    if (inputStream != null) {
                        YauldDex.debug("----------------init---D");
                        appInfo = AppInfo.load(context, inputStream);
                        inputStream.close();
                    }
                    break;
                }
            }
            apkFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getDexFolder(Context context) {
        return getYauldFolder(context) + File.separator + "dex";
    }

    public static String getDexOptFolder(Context context) {

        return context.getDir("odex", Context.MODE_PRIVATE) + File.separator + "dex_opt";
    }

    public static String getYauldFolder(Context context) {
        return context.getExternalFilesDir(null).getPath() + File.separator + "yauld";
    }

    static Application createRealApplication() {
        Application realApplication = null;
        try {
            Class<?> aClass = Class.forName(appInfo.application_name);
            if (aClass != null) {
                realApplication = (Application) aClass.getConstructor(new Class[0]).newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (realApplication == null) {
            realApplication = new Application();
        }
        return realApplication;
    }

    public static void monkeyPatchApplication(Context context, Application bootstrap, Application realApplication, String externalResourceFile) {
        try {
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Method m = activityThread.getMethod("currentActivityThread");
            m.setAccessible(true);
            Object currentActivityThread = m.invoke(activityThread);
            if (realApplication != null) {
                if(currentActivityThread != null) {
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

    static void invokeMethod(Class<?> clazz, String methodName, Class<?> paramType, Object object, Object value) {
        try {
            Object localObject = clazz.getDeclaredMethod(methodName, paramType);
            ((Method) localObject).setAccessible(true);
            ((Method) localObject).invoke(object, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unZipDex(Context context) {
        try {
            YauldDex.debug("----------------unZipDex---A");
            Set<String> md5Set = getDexMd5s(context);

            String dexFolderPath = getDexFolder(context);
            File dexFolder = new File(dexFolderPath);
            if(dexFolder.exists() && md5Set != null && md5Set.size() > 0){
                YauldDex.debug("----------------unZipDex---B");
                boolean isNeedUnZip = false;
                File[] files = dexFolder.listFiles();
                if(files != null && files.length > 0){
                    YauldDex.debug("----------------unZipDex---C");
                    for(File dexFile : files){
                        if(!md5Set.contains(Utils.md5sum(dexFile.getPath()))){
                            YauldDex.debug("----------------unZipDex---D");
                            isNeedUnZip = true;
                            break;
                        }
                    }
                }else {
                    YauldDex.debug("----------------unZipDex---E");
                    isNeedUnZip = true;
                }
                if(!isNeedUnZip) {
                    YauldDex.debug("----------------unZipDex---F");
                    return;
                }
            }

            if(!FileUtils.exists(dexFolderPath)){
                FileUtils.mkdirs(dexFolderPath);
            }

            YauldDex.debug("----------------unZipDex---G");
            ZipFile apkFile = new ZipFile(context.getApplicationInfo().sourceDir);
            Enumeration<? extends ZipEntry> entries = apkFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String zipEntryName = zipEntry.getName();
                if (YAULD_DEX_NAME.equals(zipEntryName)) {
                    InputStream inputStream = apkFile.getInputStream(zipEntry);
                    FileUtils.copyStream(inputStream, getYauldFolder(context), YAULD_DEX_NAME);
                    break;
                }
            }
            apkFile.close();

            FileUtils.unZipFiles(new File(getYauldFolder(context), YAULD_DEX_NAME), dexFolderPath);
            FileUtils.delete(getYauldFolder(context) + File.separator + YAULD_DEX_NAME);

            Set<String> md5SetNew = new HashSet<>();
            File dexFolderNew = new File(dexFolderPath);
            if(dexFolderNew.exists()){
                File[] dexFilesNew = dexFolderNew.listFiles();
                if(dexFilesNew != null){
                    for(File dexNew : dexFilesNew){
                        md5SetNew.add(Utils.md5sum(dexNew.getPath()));
                    }
                }
            }
            saveDexMd5s(context, md5SetNew);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void setupClassLoader(Context contextBase, ClassLoader classLoader) {
        String nativeLibraryPath = "";

        try {
            nativeLibraryPath = (String) classLoader.getClass().getMethod("getLdLibraryPath", new Class[0]).invoke(classLoader);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String dexOptFolderPath = getDexOptFolder(contextBase);
        String dexFolder = getDexFolder(contextBase);

        ArrayList<String> dexList = new ArrayList<>();
        File[] files = new File(dexFolder).listFiles();
        for (File dexFile : files) {
            dexList.add(dexFile.getPath());
        }

        if(!FileUtils.exists(dexOptFolderPath)){
            FileUtils.mkdirs(dexOptFolderPath);
        }

        YauldDex.debug("----------------setupClassLoader---A---" + System.currentTimeMillis());
        YauldDexLoader.inject(classLoader, nativeLibraryPath, dexOptFolderPath, dexList);
        // 结束后
        YauldDex.debug("----------------setupClassLoader---B---" + System.currentTimeMillis());
    }

    public static void saveDexMd5s(Context context, Set<String> modify){
        SharedPreferences yauld = context.getSharedPreferences("yauld", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = yauld.edit();
        editor.putStringSet("dex_md5", modify);
        editor.commit();
    }
    public static Set<String> getDexMd5s(Context context){
        SharedPreferences yauld = context.getSharedPreferences("yauld", Context.MODE_PRIVATE);
        return yauld.getStringSet("dex_md5", null);
    }
}
