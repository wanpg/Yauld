package com.wanpg.yauld;

import com.wanpg.yauld.utils.SmaliUtils;

/**
 * Created by wangjinpeng on 2016/12/30.
 */
public class Test {

    public static void main(String[] args){
//        System.out.println(SmaliUtils.getFieldValue("/Users/wangjinpeng/WorkSpace/github/wanpg/Yauld/DiffTool/temp/.yauld-temp/patch/dex/temp/com/wanpg/yauld/AppInfo.smali",
//                "com.wanpg.yauld.AppInfo", "VERSION"));

        System.out.println("MainActivity1$123$3.smali".replaceAll("\\$[0-9]+", ""));
    }

    public static void test(Runnable runnable){
        long start = System.currentTimeMillis();
        runnable.run();
        System.out.println("用时：" + (System.currentTimeMillis() - start) + "ms");
    }
}
