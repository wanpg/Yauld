package com.wanpg.yauld;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import dalvik.system.DexClassLoader;

public class YauldDexClassLoader extends ClassLoader {

    private final DelegateClassLoader delegateClassLoader;

    public YauldDexClassLoader(ClassLoader original, String nativeLibraryPath, final String codeCacheDir, final String dexPath) {
        super(original.getParent());
        delegateClassLoader = new DelegateClassLoader(dexPath, codeCacheDir, nativeLibraryPath, original.getParent());
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return delegateClassLoader.findClass(name);
        } catch (ClassNotFoundException e) {
            throw e;
        }
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

    public static String createDexPath(List<String> dexes) {
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

    public static void setParent(ClassLoader classLoader, ClassLoader newParent) {
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
}
