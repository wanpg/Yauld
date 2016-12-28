package com.wanpg.yauld.bs;

import java.io.File;
import java.io.IOException;

/**
 * Created by wangjinpeng on 2016/12/27.
 */
public class JBTest {

    public static void main(String[] args){
        String folder = "/Users/wangjinpeng/Desktop/动态更新/test";
        try {
            JBPatch.bspatch(new File(folder, "a.jar"),
                    new File(folder, "b_new.jar"),
                    new File(folder, "2.patch"));
//            JBDiff.bsdiff(new File(folder, "a.jar"),
//                    new File(folder, "b.jar"),
//                    new File(folder, "2.patch"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
