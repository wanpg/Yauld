package com.wanpg.yauld.server.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by wangjinpeng on 2017/2/7.
 */

public class VersionUtils {
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
            } else if (oldInt < newInt) {
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
