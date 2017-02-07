package com.wanpg.yauld.server.dao;

/**
 * Created by wangjinpeng on 16/4/19.
 */
public class DaoManager {

    private static DaoManager instance = new DaoManager();

    public static DaoManager getInstance() {
        return instance;
    }

    public void init() {
        apkDao.init();
    }

    ApkDao apkDao = new ApkDao();

    public ApkDao getApkDao() {
        return apkDao;
    }
}
