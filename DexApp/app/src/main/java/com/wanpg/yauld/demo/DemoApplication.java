package com.wanpg.yauld.demo;

import android.content.Context;
import android.support.multidex.MultiDexApplication;


/**
 * Created by wangjinpeng on 2016/12/10.
 */

public class DemoApplication extends MultiDexApplication {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
