package com.wanpg.yauld.demo.model;

/**
 * Created by wangjinpeng on 2017/2/6.
 */
public class ApkItem {
    String packageName;

    String versionName;
    String md5;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
