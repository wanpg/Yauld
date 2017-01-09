package com.wanpg.yauld.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by wangjinpeng on 2016/12/13.
 */

public class Utils {

    public static void invokeMethod(Class<?> clazz, String methodName, Class<?> paramType, Object object, Object value) {
        try {
            Object localObject = clazz.getDeclaredMethod(methodName, paramType);
            ((Method) localObject).setAccessible(true);
            ((Method) localObject).invoke(object, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 比较版本大小
     *
     * @param oldVersion
     * @param newVersion
     * @return 小于 0 说明老版本大于新版本的版本号
     * 大于 0 说明新版本版本号更大
     * 等于 0 说明版本号相等
     */
    public static int compareVersion(String oldVersion, String newVersion) {
        List<String> oldArray = formatVersion(oldVersion);
        List<String> newArray = formatVersion(newVersion);
        int size = oldArray.size() > newArray.size() ? oldArray.size() : newArray.size();
        for (int i = 0; i < size; i++) {
            int oldInt, newInt;
            try {
                oldInt = Integer.parseInt(oldArray.get(i));
            } catch (Exception e) {
                oldInt = 0;
            }
            try {
                newInt = Integer.parseInt(newArray.get(i));
            } catch (Exception e) {
                newInt = 0;
            }
            if (oldInt > newInt) {
                return -1;
            } else {
                return 1;
            }
        }
        return 0;
    }

    private static List<String> formatVersion(String version) {
        List<String> arrayList = new ArrayList<>();
        if (version.contains(".")) {
            String[] split = version.split("\\.");
            Collections.addAll(arrayList, split);
        } else {
            arrayList.add(version);
        }
        return arrayList;
    }
}
