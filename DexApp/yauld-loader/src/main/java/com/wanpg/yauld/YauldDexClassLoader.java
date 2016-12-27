package com.wanpg.yauld;

import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import dalvik.system.DexClassLoader;

public class YauldDexClassLoader extends ClassLoader {

    private final DelegateClassLoader delegateClassLoader;

    private Thread otherDexesThread;

    private boolean isLoadFinish;

    private YauldDex.OnLoadListener mOnLoadListener;

    public YauldDexClassLoader(ClassLoader original, String nativeLibraryPath, final String codeCacheDir, String mainDex, final List<String> otherDexes, YauldDex.OnLoadListener onLoadListener) {
        super(original.getParent());
        mOnLoadListener = onLoadListener;
        isLoadFinish = false;
        if(TextUtils.isEmpty(mainDex)){
            String pathBuilder = createDexPath(otherDexes);
            delegateClassLoader = new DelegateClassLoader(pathBuilder, codeCacheDir, nativeLibraryPath, original);
            onLoadComplete();
        }else {
            delegateClassLoader = new DelegateClassLoader(mainDex, codeCacheDir, nativeLibraryPath, original);
            YauldDex.debugWithTimeMillis("主Dex加载完成");
            //此处开启新线程来做
            otherDexesThread = new Thread() {
                @Override
                public void run() {
                    YauldDex.debugWithTimeMillis("开始加载其他的dex");
                    try {
                        YauldDex.installOtherDexes(delegateClassLoader, new File(codeCacheDir), otherDexes);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    YauldDex.debugWithTimeMillis("其他的dex加载完成");
                    onLoadComplete();
                }
            };
            otherDexesThread.start();
        }
    }

    private void onLoadComplete(){
        isLoadFinish = true;
        if(mOnLoadListener != null) {
            mOnLoadListener.onComplete();
        }
    }

    public Class<?> findClass(String className) throws ClassNotFoundException {
        try {
            return delegateClassLoader.findClass(className);
        } catch (ClassNotFoundException e) {
            throw e;
        }
    }

    public boolean isLoadFinish() {
        return isLoadFinish;
    }

    private static class DelegateClassLoader extends DexClassLoader {
        private DelegateClassLoader(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent) {
            super(dexPath, optimizedDirectory, libraryPath, parent);
        }

        public Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                return super.findClass(name);
            } catch (ClassNotFoundException e) {
                throw e;
            }
        }
    }

    private static String createDexPath(List<String> dexes) {
        StringBuilder pathBuilder = new StringBuilder();
        boolean first = true;
        for (String dex : dexes) {
            if (first) {
                first = false;
            } else {
                pathBuilder.append(File.pathSeparator);
            }
            pathBuilder.append(dex);
        }
        return pathBuilder.toString();
    }

    private static void setParent(ClassLoader classLoader, ClassLoader newParent) {
        try {
            Field parent = ClassLoader.class.getDeclaredField("parent");
            parent.setAccessible(true);
            parent.set(classLoader, newParent);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static YauldDexClassLoader inject(ClassLoader classLoader, String nativeLibraryPath, String codeCacheDir, List<String> dexes) {
        YauldDexClassLoader yauldDexClassLoader = new YauldDexClassLoader(classLoader, nativeLibraryPath, codeCacheDir, null, dexes, null);
        setParent(classLoader, yauldDexClassLoader);
        return yauldDexClassLoader;
    }

    public static YauldDexClassLoader inject(ClassLoader classLoader, String nativeLibraryPath, String codeCacheDir, String mainDex, List<String> otherDexes, YauldDex.OnLoadListener onLoadListener) {
        YauldDexClassLoader yauldDexClassLoader = new YauldDexClassLoader(classLoader, nativeLibraryPath, codeCacheDir, mainDex, otherDexes, onLoadListener);
        setParent(classLoader, yauldDexClassLoader);
        return yauldDexClassLoader;
    }
}
