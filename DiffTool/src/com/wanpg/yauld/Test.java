package com.wanpg.yauld;

/**
 * Created by wangjinpeng on 2016/12/30.
 */
public class Test {

    public static void main(String[] args){

    }

    public static void test(Runnable runnable){
        long start = System.currentTimeMillis();
        runnable.run();
        System.out.println("用时：" + (System.currentTimeMillis() - start) + "ms");
    }
}
