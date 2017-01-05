package com.wanpg.yauld;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.wanpg.yauld.patcher.DexPatcher;
import com.wanpg.yauld.patcher.ResourcePatcher;
import com.wanpg.yauld.utils.FileUtils;
import com.wanpg.yauld.utils.Utils;
import com.wanpg.yauld.utils.ZipUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangjinpeng on 2016/12/10.
 */
public class YauldDex {

    public static final String TAG = YauldDex.class.getSimpleName();
    private static final String YAULD_UPDATE_ZIP_NAME = "update.zip";

    public static void debug(String info) {
        Log.d(TAG, info);
    }

    public static void debugWithTimeMillis(String info) {
        debug(System.currentTimeMillis() + "--:--" + info);
    }

    static String externalResourcePath = null;

    /**
     * 获取 odex 的路径，此路径最好在system的app目录下，这样只用转换一次
     *
     * @param context
     * @return
     */
    private static String getDexOptFolder(Context context) {
        return context.getDir("odex", Context.MODE_PRIVATE) + File.separator + "dex_opt";
    }

    /**
     * 获取Yauld整个更新操作的目录
     *
     * @param context
     * @return
     */
    private static String getYauldFolder(Context context) {
//        return context.getFilesDir().getPath() + File.separator + "yauld";
        return context.getExternalFilesDir(null).getPath() + File.separator + "yauld";
    }

    /**
     * 获取更新文件的临时目录
     *
     * @param context
     * @return
     */
    private static String getYauldUpdateTempFolder(Context context) {
        return getYauldFolder(context) + File.separator + "update_temp";
    }

    /**
     * 获取更新文件（dex，resource.zip所在的目录）
     *
     * @param context
     * @return
     */
    public static String getYauldUpdateFolder(Context context) {
        return getYauldFolder(context) + File.separator + "update";
    }

    /**
     * 获取更新资源最终打成的zip包的路径
     *
     * @param context
     * @return
     */
    public static String getYauldUpdateResourceZipPath(Context context) {
        return getYauldUpdateFolder(context) + File.separator + "resources.zip";
    }

    /**
     * 动态加载所有的dex
     *
     * @param context
     */
    static synchronized void install(final Application context) {
        debugWithTimeMillis("YauldDex----install---A");
        String yauldFolder = getYauldFolder(context);
        if (!FileUtils.exists(yauldFolder)) {
            FileUtils.mkdirs(yauldFolder);
        }
        // 获取文件锁
        // 获得文件锁，保证多进程时解压部分代码只进行一次操作
        File file = new File(getYauldFolder(context), "lock.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        externalResourcePath = null;
        FileChannel fileChannel = null;
        FileLock fileLock = null;
        try {
            fileChannel = new FileOutputStream(file).getChannel();
            while (fileLock == null) {
                fileLock = fileChannel.tryLock();
            }
            debugWithTimeMillis("YauldDex----install---B");
            // 检测 update.zip 的修改时间是否与保存的保持一致，如果不一致，则删除并返回
            boolean needReUnZip = true;
            if(checkLastUpdateFile(context)){
                if(setupClassLoader(context, context.getClassLoader())){
                    needReUnZip = false;
                    externalResourcePath = getYauldUpdateResourceZipPath(context);
                    if(!FileUtils.exists(externalResourcePath)){
                        externalResourcePath = null;
                    }
                }
            }
            if(needReUnZip){
                FileUtils.delete(getYauldUpdateFolder(context));
                FileUtils.delete(getYauldUpdateTempFolder(context));
                long currentModifyTime = currentFileModifyTimeMillis();
                //检查是否有更新文件
                if (checkUpdateAndUnZip(context)) {
                    debugWithTimeMillis("YauldDex----install---C");
                    if (DexPatcher.patch(context, getYauldUpdateTempFolder(context), getYauldUpdateFolder(context))) {
                        debugWithTimeMillis("YauldDex----install---D");
                        // 设置classLoader
                        if (setupClassLoader(context, context.getClassLoader())) {
                            debugWithTimeMillis("YauldDex----install---E");
                            externalResourcePath = getYauldUpdateResourceZipPath(context);
                            if (ResourcePatcher.patch(context, yauldFolder, getYauldUpdateTempFolder(context), externalResourcePath)) {
                                debugWithTimeMillis("YauldDex----install---F");
                                YauldSP.setUpdateContentType(context, YauldSP.CONTENT_DEX_RES);
                                FileUtils.setFileLastModify(getYauldUpdateResourceZipPath(context), currentModifyTime);
                            }else{
                                debugWithTimeMillis("YauldDex----install---G");
                                externalResourcePath = null;
                                YauldSP.setUpdateContentType(context, YauldSP.CONTENT_DEX);
                            }
                            FileUtils.setFileLastModify(new File(getYauldUpdateFolder(context), "patch.dex"), currentModifyTime);
                            YauldSP.setUpdateFileModifyTime(context, currentModifyTime);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            debugWithTimeMillis("YauldDex----install---H");
        } finally {
            FileUtils.delete(getYauldUpdateTempFolder(context));
            // 释放锁
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
        debugWithTimeMillis("YauldDex----install---I");
    }

    private static long currentFileModifyTimeMillis() {
        long l = System.currentTimeMillis() / 1000;
        return l * 1000;
    }

    /**
     * 判断 patch.zip 和 resource.zip 的修改时间是否一致，如果不一致，则删除更新的update 和 update_temp 重新解压
     * @param context
     * @return
     */
    private static boolean checkLastUpdateFile(Context context){
        File dexPatchFile = new File(getYauldUpdateFolder(context), "patch.dex");
        File resourceZipFile = new File(getYauldUpdateResourceZipPath(context));

        String updateContentType = YauldSP.getUpdateContentType(context);
        if(YauldSP.CONTENT_DEX.equals(updateContentType)){
            if(!dexPatchFile.exists() || resourceZipFile.exists()){
                return false;
            }
        }else if(YauldSP.CONTENT_DEX_RES.equals(updateContentType)){
            if(!dexPatchFile.exists() || !resourceZipFile.exists()){
                return false;
            }
        }else{
            return false;
        }

        if (dexPatchFile.exists()) {
            long updateFileModifyTime = YauldSP.getUpdateFileModifyTime(context);
            if (dexPatchFile.lastModified() == updateFileModifyTime) {
                if (resourceZipFile.exists()) {
                    if(resourceZipFile.lastModified() == updateFileModifyTime){
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检测更新文件是否存在并且解压
     *
     * @param context
     * @return
     */
    private static boolean checkUpdateAndUnZip(Context context) {
        String yauldFolder = getYauldFolder(context);
        if (!FileUtils.exists(yauldFolder)) {
            return false;
        }
        File file = new File(yauldFolder, YAULD_UPDATE_ZIP_NAME);
        if (!file.exists()) {
            return false;
        }
        File updateFolder = new File(getYauldUpdateTempFolder(context));
        FileUtils.delete(updateFolder, true);
        return ZipUtils.unZipFiles(file, updateFolder.getAbsolutePath());
    }

    private static boolean setupClassLoader(Context context, ClassLoader classLoader) {
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

    /**
     * 重启应用，延时
     *
     * @param context
     */
    public static void restartApplication(Context context) {
        for (Activity activity : activityRecords) {
            try {
                activity.finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            //check if the context is given
            if (context != null) {
                //fetch the packageManager so we can get the default launch activity
                // (you can replace this intent with any other activity if you want
                PackageManager pm = context.getPackageManager();
                //check if we got the PackageManager
                if (pm != null) {
                    //create the intent with the default start activity for your application
                    Intent mStartActivity = pm.getLaunchIntentForPackage(context.getPackageName());
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //create a pending intent so the application is restarted after System.exit(0) was called.
                        // We use an AlarmManager to call this intent in 100ms
                        int mPendingIntentId = 223344;
                        PendingIntent mPendingIntent = PendingIntent
                                .getActivity(context, mPendingIntentId, mStartActivity,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 50, mPendingIntent);
                        //kill the application
                        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                        List<ActivityManager.RunningAppProcessInfo> services = manager.getRunningAppProcesses();
                        for (ActivityManager.RunningAppProcessInfo info : services) {
                            int service1name = info.pid;
                            android.os.Process.killProcess(service1name);
                        }
                    } else {
                        Log.e(TAG, "Was not able to restart application, mStartActivity null");
                    }
                } else {
                    Log.e(TAG, "Was not able to restart application, PM null");
                }
            } else {
                Log.e(TAG, "Was not able to restart application, Context null");
            }
        } catch (Exception ex) {
            Log.e(TAG, "Was not able to restart application");
        }
    }

    private final static List<Activity> activityRecords = new ArrayList<>();

    static void register(Application application) {
        activityRecords.clear();
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                activityRecords.add(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                activityRecords.remove(activity);
            }
        });
    }
}
