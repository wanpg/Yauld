package com.wanpg.yauld.server.model;

/**
 * Created by wangjinpeng on 2017/2/7.
 */
public class HotItem {
    String packageName;
    String versionName;
    String hotVersion;
    String md5;


    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getHotVersion() {
        return hotVersion;
    }

    public void setHotVersion(String hotVersion) {
        this.hotVersion = hotVersion;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
