package com.wanpg.yauld;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.wanpg.yauld.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by wangjinpeng on 2016/12/10.
 */

public class YauldDex {

    private static final String TAG = YauldDex.class.getSimpleName();
    private static final String YAULD_DEX_ZIP_NAME = "yauld-dex.zip";
    private static final String YAULD_SP_NAME = "yauld_sp";

    public interface OnLoadListener {
        void onComplete();
    }

    private static YauldDexClassLoader yauldDexClassLoader;

    private static final ArrayList<OnLoadListener> loadListenerArray = new ArrayList<>();

    public static boolean isLoadFinished() {
        if (yauldDexClassLoader != null && yauldDexClassLoader.isLoadFinish()) {
            return true;
        }
        return false;
    }

    public static void isLoadFinished(OnLoadListener onLoadListener) {
        if(onLoadListener == null){
            return;
        }
        if(isLoadFinished()){
            onLoadListener.onComplete();
        }else{
            if (!loadListenerArray.contains(onLoadListener)) {
                loadListenerArray.add(onLoadListener);
            }
        }
    }

    static void debug(String info) {
        Log.d(TAG, info);
    }

    static void debugWithTimeMillis(String info) {
        debug(System.currentTimeMillis() + "--:--" + info);
    }

    FileChannel fileChannel = null;
    FileLock fileLock = null;
    /**
     * 动态加载所有的dex
     * @param context
     */
    synchronized void install(final Application context) {
        // 获得文件锁，保证多进程时解压部分代码只进行一次操作
        FileUtils.mkdirs(getYauldFolder(context));
        File file = new File(getYauldFolder(context), "lock.txt");
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(!getFileLocked(file.getAbsolutePath())){
            return;
        }

        // 解压yuald-dex.zip
        boolean isUnZiped = unZipDex(context);
        // 设置classLoader
        setupClassLoader(context, context.getClassLoader(), isUnZiped, new OnLoadListener() {
            @Override
            public void onComplete() {
                releaseLock();
                for (OnLoadListener onLoadListener : loadListenerArray) {
                    if (onLoadListener != null) {
                        onLoadListener.onComplete();
                    }
                }
                loadListenerArray.clear();
            }
        });
    }

    private synchronized boolean getFileLocked(String filePath){
        boolean isLockSuccess;
        try {
            fileChannel = new FileOutputStream(filePath).getChannel();
            while (fileLock == null){
                fileLock = fileChannel.tryLock();
            }
            isLockSuccess = true;
        } catch (IOException e) {
            e.printStackTrace();
            isLockSuccess = false;
        }
        return isLockSuccess;
    }

    private synchronized void releaseLock(){
        if(fileLock != null){
            try {
                fileLock.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(fileChannel != null){
            try {
                fileChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getDexFolder(Context context) {
        return getYauldFolder(context) + File.separator + "dex";
    }

    private static String getDexOptFolder(Context context) {

        return context.getDir("odex", Context.MODE_PRIVATE) + File.separator + "dex_opt";
    }

    private static String getYauldFolder(Context context) {
        return context.getFilesDir().getPath() + File.separator + "yauld";
    }

    /**
     * 解压app运行的dex
     * @param context
     * @return 是否做了解压操作，做了这个操作，需要异步执行任务
     */
    private boolean unZipDex(Context context) {
        boolean isUnZiped = false;
        try {
            YauldDex.debugWithTimeMillis("----------------unZipDex---A");
            long dexZipModify = getYauldDexZipModify(context);
            YauldDex.debugWithTimeMillis("----------------unZipDex---B");
            String yauldFolder = getYauldFolder(context);

            if (!FileUtils.exists(yauldFolder)) {
                isUnZiped = true;
                FileUtils.mkdirs(yauldFolder);
            }

            File dexZipFile = new File(yauldFolder, YAULD_DEX_ZIP_NAME);
            boolean isNewZip = false;
            debug("上一次的dexzip modify：" + dexZipFile.lastModified());
            debug("上一次保存的dexzip modify：" + dexZipModify);
            if (!dexZipFile.exists() || dexZipFile.lastModified() != dexZipModify) {
                // 进行解压操作
                ZipFile apkFile = new ZipFile(context.getApplicationInfo().sourceDir);
                Enumeration<? extends ZipEntry> entries = apkFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry zipEntry = entries.nextElement();
                    String zipEntryName = zipEntry.getName();
                    if (YAULD_DEX_ZIP_NAME.equals(zipEntryName)) {
                        InputStream inputStream = apkFile.getInputStream(zipEntry);
                        YauldDex.debugWithTimeMillis("----------------unZipDex---C");
                        FileUtils.copyStream(inputStream, yauldFolder, YAULD_DEX_ZIP_NAME);
                        YauldDex.debugWithTimeMillis("----------------unZipDex---D");
                        isNewZip = true;
                        break;
                    }
                }
                apkFile.close();
                long currentTimeMillis = currentFileModifyTimeMillis();
                debug("zip设置存储时间：" + dexZipFile.setLastModified(currentTimeMillis) + "   " + currentTimeMillis);
                setYauldDexZipModify(context, currentTimeMillis);
                YauldDex.debugWithTimeMillis("----------------unZipDex---E");
            }

            File dexFolder = new File(getDexFolder(context));
            if (isNewZip || !dexFolder.exists() || isDexModify(context, dexFolder)) {
                YauldDex.debugWithTimeMillis("----------------unZipDex---F");

                if (!dexFolder.exists()) {
                    dexFolder.mkdirs();
                } else {
                    FileUtils.delete(dexFolder, false);
                }
                FileUtils.unZipFiles(dexZipFile, dexFolder.getAbsolutePath());
                isUnZiped = true;

                long currentTimeMillis = currentFileModifyTimeMillis();
                File[] listFiles = dexFolder.listFiles();
                for (File file : listFiles) {
                    debug("dexfile设置存储时间：" + file.setLastModified(currentTimeMillis) + "   " + currentTimeMillis);
                }
                setYauldDexFileModify(context, currentTimeMillis);
                YauldDex.debugWithTimeMillis("----------------unZipDex---G");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isUnZiped;
    }

    private static long currentFileModifyTimeMillis(){
        long l = System.currentTimeMillis() / 1000;
        return l * 1000;
    }

    private boolean isDexModify(Context context, File dexFolder) {
        if (dexFolder == null) {
            return true;
        }
        if (dexFolder.isFile()) {
            dexFolder.delete();
            return true;
        }
        File[] files = dexFolder.listFiles();
        if (files == null || files.length <= 0) {
            return true;
        }
        long yauldDexFileModify = getYauldDexFileModify(context);
        for (File file : files) {
            if(file.lastModified() != yauldDexFileModify){
                return true;
            }
        }
        return false;
    }

    private static long getYauldDexZipModify(Context context){
        return getSharePreferences(context).getLong("yauld-dex-zip-modify", System.currentTimeMillis());
    }

    private static boolean setYauldDexZipModify(Context context, long modify){
        SharedPreferences.Editor editor = getSharePreferences(context).edit();
        editor.putLong("yauld-dex-zip-modify", modify);
        return editor.commit();
    }

    private static long getYauldDexFileModify(Context context){
        return getSharePreferences(context).getLong("yauld-dex-file-modify", System.currentTimeMillis());
    }

    private static boolean setYauldDexFileModify(Context context, long modify){
        SharedPreferences.Editor editor = getSharePreferences(context).edit();
        editor.putLong("yauld-dex-file-modify", modify);
        return editor.commit();
    }

    private static SharedPreferences getSharePreferences(Context context) {
        return context.getSharedPreferences(YAULD_SP_NAME, Context.MODE_PRIVATE);
    }

    private void setupClassLoader(Context contextBase, ClassLoader classLoader, boolean async, OnLoadListener loadListener) {
        String nativeLibraryPath = "";

        try {
            nativeLibraryPath = (String) classLoader.getClass().getMethod("getLdLibraryPath", new Class[0]).invoke(classLoader);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String dexOptFolderPath = getDexOptFolder(contextBase);
        String dexFolder = getDexFolder(contextBase);

        String mainDexFilePath = null;
        ArrayList<String> dexList = new ArrayList<>();
        File[] files = new File(dexFolder).listFiles();
        for (File dexFile : files) {
            String dexFileName = dexFile.getName();
            String dexFilePath = dexFile.getAbsolutePath();
            if ("classes.dex".equals(dexFileName) && async) {
                mainDexFilePath = dexFilePath;
            } else {
                dexList.add(dexFilePath);
            }
        }

        if (!FileUtils.exists(dexOptFolderPath)) {
            FileUtils.mkdirs(dexOptFolderPath);
        }

        YauldDex.debugWithTimeMillis("----------------setupClassLoader---A---");
        if(async) {
            yauldDexClassLoader = YauldDexClassLoader.inject(classLoader, nativeLibraryPath, dexOptFolderPath, mainDexFilePath, dexList, loadListener);
            YauldDex.debug("----------------setupClassLoader---B---");
        }else {
            yauldDexClassLoader = YauldDexClassLoader.inject(classLoader, nativeLibraryPath, dexOptFolderPath, dexList);
            YauldDex.debug("----------------setupClassLoader---C---");
            if(loadListener != null){
                loadListener.onComplete();
            }
        }
    }

    static void monkeyPatchApplication(Context context, Application bootstrap, Application realApplication, String externalResourceFile) {
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


    static void installOtherDexes(ClassLoader loader, File dexDir, List<String> filePaths)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException,
            InvocationTargetException, NoSuchMethodException, IOException {
        List<File> files = new ArrayList<>();
        if (filePaths != null) {
            for (String path : filePaths) {
                files.add(new File(path));
            }
        }
        if (!files.isEmpty()) {
            if (Build.VERSION.SDK_INT >= 24) {
                V24.install(loader, files, dexDir);
            } else if (Build.VERSION.SDK_INT >= 23) {
                V23.install(loader, files, dexDir);
            } else if (Build.VERSION.SDK_INT >= 19) {
                V19.install(loader, files, dexDir);
            } else if (Build.VERSION.SDK_INT >= 14) {
                V14.install(loader, files, dexDir);
            }
        }
    }

    /**
     * Installer for platform versions 14, 15, 16, 17 and 18.
     */
    private static final class V14 {

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries,
                                    File optimizedDirectory)
                throws IllegalArgumentException, IllegalAccessException,
                NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
            /* The patched class loader is expected to be a descendant of
             * dalvik.system.BaseDexClassLoader. We modify its
             * dalvik.system.DexPathList pathList field to append additional DEX
             * file entries.
             */
            Field pathListField = findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList,
                    new ArrayList<>(additionalClassPathEntries), optimizedDirectory));
        }

        /**
         * A wrapper around
         * {@code private static final dalvik.system.DexPathList#makeDexElements}.
         */
        private static Object[] makeDexElements(
                Object dexPathList, ArrayList<File> files, File optimizedDirectory)
                throws IllegalAccessException, InvocationTargetException,
                NoSuchMethodException {
            Method makeDexElements =
                    findMethod(dexPathList, "makeDexElements", ArrayList.class, File.class);

            return (Object[]) makeDexElements.invoke(dexPathList, files, optimizedDirectory);
        }
    }


    /**
     * Installer for platform versions 19.
     */
    private static final class V19 {

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries,
                                    File optimizedDirectory)
                throws IllegalArgumentException, IllegalAccessException,
                NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
            /* The patched class loader is expected to be a descendant of
             * dalvik.system.BaseDexClassLoader. We modify its
             * dalvik.system.DexPathList pathList field to append additional DEX
             * file entries.
             */
            Field pathListField = findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            ArrayList<IOException> suppressedExceptions = new ArrayList<>();
            expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList,
                    new ArrayList<>(additionalClassPathEntries), optimizedDirectory,
                    suppressedExceptions));
            dexElementsSuppressedExceptions(dexPathList, suppressedExceptions);
        }

        /**
         * A wrapper around
         * {@code private static final dalvik.system.DexPathList#makeDexElements}.
         */
        private static Object[] makeDexElements(
                Object dexPathList, ArrayList<File> files, File optimizedDirectory,
                ArrayList<IOException> suppressedExceptions)
                throws IllegalAccessException, InvocationTargetException,
                NoSuchMethodException {
            Method makeDexElements =
                    findMethod(dexPathList, "makeDexElements", ArrayList.class, File.class,
                            ArrayList.class);

            return (Object[]) makeDexElements.invoke(dexPathList, files, optimizedDirectory,
                    suppressedExceptions);
        }
    }

    private static final class V23 {
        private V23() {
        }

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries,
                                    File optimizedDirectory)
                throws IllegalArgumentException, IllegalAccessException,
                NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
            /* The patched class loader is expected to be a descendant of
             * dalvik.system.BaseDexClassLoader. We modify its
             * dalvik.system.DexPathList pathList field to append additional DEX
             * file entries.
             */
            Field pathListField = findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            ArrayList<IOException> suppressedExceptions = new ArrayList<>();
            expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList,
                    new ArrayList<>(additionalClassPathEntries), optimizedDirectory,
                    suppressedExceptions));
            dexElementsSuppressedExceptions(dexPathList, suppressedExceptions);
        }

        /**
         * A wrapper around
         * {@code private static final dalvik.system.DexPathList#makeDexElements}.
         */
        private static Object[] makeDexElements(
                Object dexPathList, ArrayList<File> files, File optimizedDirectory,
                ArrayList<IOException> suppressedExceptions)
                throws IllegalAccessException, InvocationTargetException,
                NoSuchMethodException {
            Method makeDexElements =
                    findMethod(dexPathList, "makeDexElements", List.class, File.class, List.class);

            return (Object[]) makeDexElements.invoke(dexPathList, files, optimizedDirectory,
                    suppressedExceptions);
        }
    }

    private static final class V24 {
        private V24() {
        }

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries,
                                    File optimizedDirectory)
                throws IllegalArgumentException, IllegalAccessException,
                NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
            /* The patched class loader is expected to be a descendant of
             * dalvik.system.BaseDexClassLoader. We modify its
             * dalvik.system.DexPathList pathList field to append additional DEX
             * file entries.
             */
            Field pathListField = findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            ArrayList<IOException> suppressedExceptions = new ArrayList<>();
            expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList,
                    new ArrayList<>(additionalClassPathEntries), optimizedDirectory,
                    suppressedExceptions, loader));
            dexElementsSuppressedExceptions(dexPathList, suppressedExceptions);
        }

        /**
         * A wrapper around
         * {@code private static final dalvik.system.DexPathList#makeDexElements}.
         */
        private static Object[] makeDexElements(
                Object dexPathList, ArrayList<File> files, File optimizedDirectory,
                ArrayList<IOException> suppressedExceptions, ClassLoader classLoader)
                throws IllegalAccessException, InvocationTargetException,
                NoSuchMethodException {
            Method makeDexElements =
                    findMethod(dexPathList, "makeDexElements",
                            List.class, File.class, List.class, ClassLoader.class);

            return (Object[]) makeDexElements.invoke(dexPathList, files, optimizedDirectory,
                    suppressedExceptions, classLoader);
        }
    }

    private static void dexElementsSuppressedExceptions(Object dexPathList,
                                                        ArrayList<IOException> suppressedExceptions)
            throws NoSuchFieldException, IllegalAccessException {
        if (suppressedExceptions.size() > 0) {
            for (IOException e : suppressedExceptions) {
                Log.w(TAG, "Exception in makeDexElement", e);
            }
            Field suppressedExceptionsField =
                    findField(dexPathList, "dexElementsSuppressedExceptions");
            IOException[] dexElementsSuppressedExceptions =
                    (IOException[]) suppressedExceptionsField.get(dexPathList);

            if (dexElementsSuppressedExceptions == null) {
                dexElementsSuppressedExceptions =
                        suppressedExceptions.toArray(
                                new IOException[suppressedExceptions.size()]);
            } else {
                IOException[] combined =
                        new IOException[suppressedExceptions.size() +
                                dexElementsSuppressedExceptions.length];
                suppressedExceptions.toArray(combined);
                System.arraycopy(dexElementsSuppressedExceptions, 0, combined,
                        suppressedExceptions.size(), dexElementsSuppressedExceptions.length);
                dexElementsSuppressedExceptions = combined;
            }

            suppressedExceptionsField.set(dexPathList, dexElementsSuppressedExceptions);
        }
    }

    /**
     * Replace the value of a field containing a non null array, by a new array containing the
     * elements of the original array plus the elements of extraElements.
     *
     * @param instance      the instance whose field is to be modified.
     * @param fieldName     the field to modify.
     * @param extraElements elements to append at the end of the array.
     */
    private static void expandFieldArray(Object instance, String fieldName,
                                         Object[] extraElements)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field jlrField = findField(instance, fieldName);
        Object[] original = (Object[]) jlrField.get(instance);
        Object[] combined = (Object[]) Array.newInstance(
                original.getClass().getComponentType(), original.length + extraElements.length);
        System.arraycopy(original, 0, combined, 0, original.length);
        System.arraycopy(extraElements, 0, combined, original.length, extraElements.length);
        jlrField.set(instance, combined);
    }

    /**
     * Locates a given field anywhere in the class inheritance hierarchy.
     *
     * @param instance an object to search the field into.
     * @param name     field name
     * @return a field object
     * @throws NoSuchFieldException if the field cannot be located
     */
    private static Field findField(Object instance, String name) throws NoSuchFieldException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(name);


                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                return field;
            } catch (NoSuchFieldException e) {
                // ignore and search next
            }
        }

        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }

    /**
     * Locates a given method anywhere in the class inheritance hierarchy.
     *
     * @param instance       an object to search the method into.
     * @param name           method name
     * @param parameterTypes method parameter types
     * @return a method object
     * @throws NoSuchMethodException if the method cannot be located
     */
    private static Method findMethod(Object instance, String name, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Method method = clazz.getDeclaredMethod(name, parameterTypes);


                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }

                return method;
            } catch (NoSuchMethodException e) {
                // ignore and search next
            }
        }

        throw new NoSuchMethodException("Method " + name + " with parameters " +
                Arrays.asList(parameterTypes) + " not found in " + instance.getClass());
    }
}
