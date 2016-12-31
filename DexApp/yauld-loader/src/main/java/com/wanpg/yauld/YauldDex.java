package com.wanpg.yauld;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.wanpg.yauld.patcher.DexPatcher;
import com.wanpg.yauld.patcher.ResourcePatcher;
import com.wanpg.yauld.utils.FileUtils;
import com.wanpg.yauld.utils.Utils;
import com.wanpg.yauld.utils.ZipUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wangjinpeng on 2016/12/10.
 */

public class YauldDex {

    public static final String TAG = YauldDex.class.getSimpleName();
    private static final String YAULD_UPDATE_ZIP_NAME = "update.zip";

    static void debug(String info) {
        Log.d(TAG, info);
    }

    static void debugWithTimeMillis(String info) {
        debug(System.currentTimeMillis() + "--:--" + info);
    }

    private FileChannel fileChannel = null;
    private FileLock fileLock = null;

    public String externalResourcePath = null;

    private static String getDexOptFolder(Context context) {
        return context.getDir("odex", Context.MODE_PRIVATE) + File.separator + "dex_opt";
    }

    private static String getYauldFolder(Context context) {
//        return context.getFilesDir().getPath() + File.separator + "yauld";
        return context.getExternalFilesDir(null).getPath() + File.separator + "yauld";
    }

    private static String getYauldUpdateTempFolder(Context context){
        return getYauldFolder(context) + File.separator + "update_temp";
    }

    public static String getYauldUpdateFolder(Context context){
        return getYauldFolder(context) + File.separator + "update";
    }

    public static String getYauldUpdateResourceZipPath(Context context){
        return getYauldUpdateFolder(context) + File.separator + "resources.zip";
    }

    private static SharedPreferences getSharePreferences(Context context) {
        return context.getSharedPreferences("yauld_sp", Context.MODE_PRIVATE);
    }

    /**
     * 动态加载所有的dex
     *
     * @param context
     */
    synchronized void install(final Application context) {
        // 获得文件锁，保证多进程时解压部分代码只进行一次操作
        String yauldFolder = getYauldFolder(context);
        FileUtils.mkdirs(yauldFolder);
        File file = new File(yauldFolder, "lock.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!getFileLocked(file.getAbsolutePath())) {
            return;
        }
        //检查是否有更新文件
        if (checkUpdateAndUnZip(context)) {
            if(DexPatcher.patch(context, getYauldUpdateTempFolder(context), getYauldUpdateFolder(context))) {
                // 设置classLoader
                if (setupClassLoader(context, context.getClassLoader())) {
                    externalResourcePath = getYauldUpdateResourceZipPath(context);
                    if (!ResourcePatcher.patch(context, yauldFolder, getYauldUpdateTempFolder(context), externalResourcePath)) {
                        externalResourcePath = null;
                    }
                }
            }
        }
        releaseLock();
    }

    private synchronized boolean getFileLocked(String filePath) {
        boolean isLockSuccess;
        try {
            fileChannel = new FileOutputStream(filePath).getChannel();
            while (fileLock == null) {
                fileLock = fileChannel.tryLock();
            }
            isLockSuccess = true;
        } catch (IOException e) {
            e.printStackTrace();
            isLockSuccess = false;
        }
        return isLockSuccess;
    }

    private synchronized void releaseLock() {
        if (fileLock != null) {
            try {
                fileLock.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (fileChannel != null) {
            try {
                fileChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkUpdateAndUnZip(Context context) {
        String yauldFolder = getYauldFolder(context);
        if (!FileUtils.exists(yauldFolder)) {
            return false;
        }
        File file = new File(yauldFolder, YAULD_UPDATE_ZIP_NAME);
        if (!file.exists()) {
            return false;
        }
        try {
            File updateFolder = new File(getYauldUpdateTempFolder(context));
            FileUtils.delete(updateFolder, true);
            ZipUtils.unZipFiles(file, updateFolder.getAbsolutePath());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean setupClassLoader(Context context, ClassLoader classLoader) {
        File updateDexFile = new File(getYauldUpdateFolder(context), "patch.dex");
        if (!updateDexFile.exists()) {
            return false;
        }

        ArrayList<String> dexList = new ArrayList<>();
        dexList.add(updateDexFile.getAbsolutePath());

        if (dexList.isEmpty()) {
            return false;
        }

        String nativeLibraryPath = "";
        try {
            nativeLibraryPath = (String) classLoader.getClass().getMethod("getLdLibraryPath", new Class[0]).invoke(classLoader);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String dexOptFolderPath = getDexOptFolder(context);
        if (!FileUtils.exists(dexOptFolderPath)) {
            FileUtils.mkdirs(dexOptFolderPath);
        }

        YauldDex.debugWithTimeMillis("----------------setupClassLoader---A---");
        YauldDexClassLoader yauldDexClassLoader = new YauldDexClassLoader(classLoader, nativeLibraryPath, dexOptFolderPath, dexList);
        try {
            Class<?> aClass = yauldDexClassLoader.loadClass(AppInfo.class.getName());
            if (aClass != null) {
                Field declaredField = aClass.getDeclaredField("VERSION");
                if (declaredField != null) {
                    Object versionValue = declaredField.get(aClass);
                    if (versionValue != null && Utils.compareVersion(AppInfo.VERSION, versionValue.toString()) > 0) {
                        YauldDexClassLoader.setParent(classLoader, yauldDexClassLoader);
                        AppInfo.VERSION = versionValue.toString();
                        YauldDex.debugWithTimeMillis("----------------setupClassLoader---B---");
                        return true;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        yauldDexClassLoader = null;
        YauldDex.debugWithTimeMillis("----------------setupClassLoader---C---");
        return false;
    }

    static void monkeyPatchApplication(Application bootstrap, Application realApplication, String externalResourceFile) {
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
