package com.wanpg.yauld.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by wangjinpeng on 2016/12/13.
 */

public class Utils {

    private static final char HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b[i] & 0x0f]);
        }
        return sb.toString();
    }

    public static String md5File(String filename) {
        InputStream fis;
        byte[] buffer = new byte[1024];
        int numRead = 0;
        MessageDigest md5;
        try {
            fis = new FileInputStream(filename);
            md5 = MessageDigest.getInstance("MD5");
            while ((numRead = fis.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
            fis.close();
            return toHexString(md5.digest());
        } catch (Exception e) {
            System.out.println("error");
            return null;
        }
    }

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
