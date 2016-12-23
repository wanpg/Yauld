package com.wanpg.yauld;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by wangjinpeng on 2016/12/10.
 */

public class YauldDex {

    public static boolean isLoadFinished() {
        return true;
    }

    static void debug(String info) {
        Log.d("yauld", info);
    }

    static final String YAULD_DEX_NAME = "yauld-dex.zip";
    private static final String YAULD_SP_NAME = "yauld_sp";

    static void init(Context context) {
    }

    private static String getDexFolder(Context context) {
        return getYauldFolder(context) + File.separator + "dex";
    }

    private static String getDexOptFolder(Context context) {

        return context.getDir("odex", Context.MODE_PRIVATE) + File.separator + "dex_opt";
    }

    private static String getYauldFolder(Context context) {
        return context.getExternalFilesDir(null).getPath() + File.separator + "yauld";
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

    static Application createRealApplication() {
        Application realApplication = null;
        try {
            Class<?> aClass = Class.forName(AppInfo.APPLICATION_NAME);
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

    static void unZipDex(Context context) {
        try {
            YauldDex.debug("----------------unZipDex---A");
            String md5Save = getYauldDexZipMd5(context);
            File dexFile = new File(getYauldFolder(context), YAULD_DEX_NAME);

            if (TextUtils.isEmpty(md5Save) || !dexFile.exists() || md5Save.equals(Utils.md5sum(dexFile.getAbsolutePath()))) {
                // 进行解压操作
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
                setYauldDexZipMd5(context, Utils.md5sum(dexFile.getAbsolutePath()));
            }

            YauldDex.debug("----------------unZipDex---B");
            String dexFolderPath = getDexFolder(context);
            if (!FileUtils.exists(dexFolderPath)) {
                FileUtils.mkdirs(dexFolderPath);
            }
            FileUtils.unZipFiles(dexFile, dexFolderPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String getYauldDexZipMd5(Context context) {
        return getSharePreferences(context).getString("yauld-dex-zip-md5", null);
    }

    static boolean setYauldDexZipMd5(Context context, String md5) {
        SharedPreferences.Editor editor = getSharePreferences(context).edit();
        editor.putString("yauld-dex-zip-md5", md5);
        return editor.commit();
    }

    static SharedPreferences getSharePreferences(Context context) {
        return context.getSharedPreferences(YAULD_SP_NAME, Context.MODE_PRIVATE);
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

        String mainDexFilePath = null;
        ArrayList<String> dexList = new ArrayList<>();
        File[] files = new File(dexFolder).listFiles();
        for (File dexFile : files) {
            String dexFileName = dexFile.getName();
            String dexFilePath = dexFile.getAbsolutePath();
            if ("classes.dex".equals(dexFileName)) {
                mainDexFilePath = dexFilePath;
            } else {
                dexList.add(dexFilePath);
            }
        }

        if (!FileUtils.exists(dexOptFolderPath)) {
            FileUtils.mkdirs(dexOptFolderPath);
        }

        YauldDex.debug("----------------setupClassLoader---A---" + System.currentTimeMillis());
//        ClassLoader inject = YauldDexClassLoader.inject(classLoader, nativeLibraryPath, dexOptFolderPath, dexList);
        ClassLoader inject = YauldDexClassLoader.inject(classLoader, nativeLibraryPath, dexOptFolderPath, mainDexFilePath, dexList);
        // 结束后
        YauldDex.debug("----------------setupClassLoader---B---" + System.currentTimeMillis());
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

    static void installOtherDexes(ClassLoader loader, File dexDir, List<String> filePaths) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IOException {
        List<File> files = new ArrayList<>();
        if (filePaths != null) {
            for (String path : filePaths) {
                files.add(new File(path));
            }
        }
        if (!files.isEmpty()) {
            if (Build.VERSION.SDK_INT >= 24) {
                YauldDex.V24.install(loader, files, dexDir);
            } else if (Build.VERSION.SDK_INT >= 23) {
                YauldDex.V23.install(loader, files, dexDir);
            } else if (Build.VERSION.SDK_INT >= 19) {
                YauldDex.V19.install(loader, files, dexDir);
            } else if (Build.VERSION.SDK_INT >= 14) {
                YauldDex.V14.install(loader, files, dexDir);
            }
        }
    }

    private static final class V14 {
        private V14() {
        }

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries, File optimizedDirectory) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
            Field pathListField = YauldDex.findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            YauldDex.expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList, new ArrayList(additionalClassPathEntries), optimizedDirectory));
        }

        private static Object[] makeDexElements(Object dexPathList, ArrayList<File> files, File optimizedDirectory) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
            Method makeDexElements = YauldDex.findMethod(dexPathList, "makeDexElements", new Class[]{ArrayList.class, File.class});
            return (Object[]) makeDexElements.invoke(dexPathList, new Object[]{files, optimizedDirectory});
        }
    }

    private static final class V19 {
        private V19() {
        }

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries, File optimizedDirectory) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
            Field pathListField = YauldDex.findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            ArrayList suppressedExceptions = new ArrayList();
            YauldDex.expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList, new ArrayList(additionalClassPathEntries), optimizedDirectory, suppressedExceptions));
            YauldDex.dexElementsSuppressedExceptions(loader, suppressedExceptions);
        }

        private static Object[] makeDexElements(Object dexPathList, ArrayList<File> files, File optimizedDirectory, ArrayList<IOException> suppressedExceptions) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
            Method makeDexElements = YauldDex.findMethod(dexPathList, "makeDexElements", new Class[]{ArrayList.class, File.class, ArrayList.class});
            return (Object[]) makeDexElements.invoke(dexPathList, new Object[]{files, optimizedDirectory, suppressedExceptions});
        }
    }

    private static final class V23 {
        private V23() {
        }

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries, File optimizedDirectory) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
            Field pathListField = YauldDex.findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            ArrayList suppressedExceptions = new ArrayList();
            YauldDex.expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList, new ArrayList(additionalClassPathEntries), optimizedDirectory, suppressedExceptions));
            YauldDex.dexElementsSuppressedExceptions(loader, suppressedExceptions);
        }

        private static Object[] makeDexElements(Object dexPathList, ArrayList<File> files, File optimizedDirectory, ArrayList<IOException> suppressedExceptions) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
            Method makeDexElements = YauldDex.findMethod(dexPathList, "makePathElements", new Class[]{List.class, File.class, List.class});
            return (Object[]) makeDexElements.invoke(dexPathList, new Object[]{files, optimizedDirectory, suppressedExceptions});
        }
    }

    private static final class V24 {
        private V24() {
        }

        private static void install(ClassLoader loader, List<File> additionalClassPathEntries, File optimizedDirectory) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
            Field pathListField = YauldDex.findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            ArrayList suppressedExceptions = new ArrayList();
            YauldDex.expandFieldArray(dexPathList, "dexElements", makeDexElements(dexPathList, new ArrayList(additionalClassPathEntries), optimizedDirectory, suppressedExceptions, loader));
            YauldDex.dexElementsSuppressedExceptions(loader, suppressedExceptions);
        }

        private static Object[] makeDexElements(Object dexPathList, ArrayList<File> files, File optimizedDirectory, ArrayList<IOException> suppressedExceptions, ClassLoader classLoader) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
            Method makeDexElements = YauldDex.findMethod(dexPathList, "makeDexElements", new Class[]{List.class, File.class, List.class, ClassLoader.class});
            return (Object[]) makeDexElements.invoke(dexPathList, new Object[]{files, optimizedDirectory, suppressedExceptions, classLoader});
        }
    }

    private static void dexElementsSuppressedExceptions(ClassLoader loader, ArrayList suppressedExceptions) throws NoSuchFieldException, IllegalAccessException {
        if (suppressedExceptions.size() > 0) {
            Iterator suppressedExceptionsField = suppressedExceptions.iterator();

            while (suppressedExceptionsField.hasNext()) {
                IOException dexElementsSuppressedExceptions = (IOException) suppressedExceptionsField.next();
                Log.w("MultiDex", "Exception in makeDexElement", dexElementsSuppressedExceptions);
            }

            Field suppressedExceptionsField1 = YauldDex.findField(loader, "dexElementsSuppressedExceptions");
            IOException[] dexElementsSuppressedExceptions1 = (IOException[]) suppressedExceptionsField1.get(loader);
            if (dexElementsSuppressedExceptions1 == null) {
                dexElementsSuppressedExceptions1 = (IOException[]) suppressedExceptions.toArray(new IOException[suppressedExceptions.size()]);
            } else {
                IOException[] combined = new IOException[suppressedExceptions.size() + dexElementsSuppressedExceptions1.length];
                suppressedExceptions.toArray(combined);
                System.arraycopy(dexElementsSuppressedExceptions1, 0, combined, suppressedExceptions.size(), dexElementsSuppressedExceptions1.length);
                dexElementsSuppressedExceptions1 = combined;
            }

            suppressedExceptionsField1.set(loader, dexElementsSuppressedExceptions1);
        }
    }

    private static void expandFieldArray(Object instance, String fieldName, Object[] extraElements) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field jlrField = findField(instance, fieldName);
        Object[] original = (Object[]) jlrField.get(instance);
        Object[] combined = (Object[]) Array.newInstance(original.getClass().getComponentType(), original.length + extraElements.length);
        System.arraycopy(original, 0, combined, 0, original.length);
        System.arraycopy(extraElements, 0, combined, original.length, extraElements.length);
        jlrField.set(instance, combined);
    }

    private static Field findField(Object instance, String name) throws NoSuchFieldException {
        Class clazz = instance.getClass();

        while (clazz != null) {
            try {
                Field e = clazz.getDeclaredField(name);
                if (!e.isAccessible()) {
                    e.setAccessible(true);
                }

                return e;
            } catch (NoSuchFieldException var4) {
                clazz = clazz.getSuperclass();
            }
        }

        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }

    private static Method findMethod(Object instance, String name, Class... parameterTypes) throws NoSuchMethodException {
        Class clazz = instance.getClass();

        while (clazz != null) {
            try {
                Method e = clazz.getDeclaredMethod(name, parameterTypes);
                if (!e.isAccessible()) {
                    e.setAccessible(true);
                }

                return e;
            } catch (NoSuchMethodException var5) {
                clazz = clazz.getSuperclass();
            }
        }

        throw new NoSuchMethodException("Method " + name + " with parameters " + Arrays.asList(parameterTypes) + " not found in " + instance.getClass());
    }
}
