package com.wanpg.yauld;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import dalvik.system.DexClassLoader;

public class YauldDexLoader extends ClassLoader {

    private final DelegateClassLoader delegateClassLoader;

    public YauldDexLoader(ClassLoader original, String nativeLibraryPath, String codeCacheDir, List<String> dexes) {
        super(original.getParent());
        String pathBuilder = createDexPath(dexes);
        delegateClassLoader = new DelegateClassLoader(pathBuilder, codeCacheDir, nativeLibraryPath, original);
    }

    public Class<?> findClass(String className) throws ClassNotFoundException {
        try {
            return delegateClassLoader.findClass(className);
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

    public static ClassLoader inject(ClassLoader classLoader, String nativeLibraryPath, String codeCacheDir, List<String> dexes) {
        YauldDexLoader yauldDexLoader = new YauldDexLoader(classLoader, nativeLibraryPath, codeCacheDir, dexes);
        setParent(classLoader, yauldDexLoader);
        return yauldDexLoader;
    }
}
