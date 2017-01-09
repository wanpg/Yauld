package com.wanpg.yauld.utils;

import java.lang.reflect.Method;

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
}
