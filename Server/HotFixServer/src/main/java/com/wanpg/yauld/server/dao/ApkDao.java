package com.wanpg.yauld.server.dao;

import com.wanpg.yauld.server.model.Apk;
import com.wanpg.yauld.server.model.ApkItem;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangjinpeng on 2017/2/6.
 */
public class ApkDao extends Dao {

    public static String TABLE1 = "create table if not exists apk (" +
            "  id integer primary key autoincrement," +
            "  name string not null," +
            "  package string not null" +
            ");";

    public static String TABLE2 = "create table if not exists apk_update (" +
            "  package string not null," +
            "  version_name string not null primary key," +
            "  md5 string not null" +
            ");";
    public static String TABLE3 = "create table if not exists hot_update (" +
            "  package string not null," +
            "  version_name string not null," +
            "  hot_version string not null," +
            "  md5 string not null" +
            ");";

    @Override
    protected void init() {
        super.init();
        try {
            ct = getConnection();
            ct.setAutoCommit(false);
            sm = ct.createStatement();
            sm.addBatch(TABLE1);
            sm.addBatch(TABLE2);
            sm.addBatch(TABLE3);
            sm.executeBatch();
            ct.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    public List<Apk> queryAllApkInfo(){
        List<Apk> result = new ArrayList<>();
        try {
            ct = getConnection();
            sm = ct.createStatement();
            rs = sm.executeQuery("select name, package from apk");
            while (rs.next()){
                Apk apk = new Apk();
                apk.name = rs.getString("name");
                apk.packageName = rs.getString("package");
                result.add(apk);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
        return result;
    }

    public Apk queryApkInfo(String packageName){
        try {
            ct = getConnection();
            sm = ct.createStatement();
            rs = sm.executeQuery("select name, package from apk WHERE package='" + packageName + "'");
            if (rs.next()){
                Apk apk = new Apk();
                apk.name = rs.getString("name");
                apk.packageName = rs.getString("package");
                return apk;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
        return null;
    }

    public void addApkInfo(String name, String packageName){
        try {
            ct = getConnection();
            ct.setAutoCommit(false);
            PreparedStatement statement = ct.prepareStatement("insert into apk (name, package) values (?, ?)");
            statement.setString(1, name);
            statement.setString(2, packageName);
            statement.addBatch();
            statement.executeBatch();
            sm = statement;
            ct.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    public void deleteApk(String packageName){
        try {
            ct = getConnection();
            ct.setAutoCommit(false);
            sm = ct.createStatement();
            sm.addBatch("delete from apk where package='" + packageName + "'");
            sm.addBatch("delete from apk_update where package='" + packageName + "'");
            sm.addBatch("delete from hot_update where package='" + packageName + "'");
            sm.executeBatch();
            ct.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    public List<ApkItem> queryAllApkItem(String packageName){
        List<ApkItem> result = new ArrayList<>();
        try {
            ct = getConnection();
            sm = ct.createStatement();
            rs = sm.executeQuery("select * from apk_update where package='" + packageName + "'");
            while (rs.next()){
                ApkItem apk = new ApkItem();
                apk.setPackageName(rs.getString("package"));
                apk.setVersionName(rs.getString("version_name"));
                apk.setMd5(rs.getString("md5"));
                result.add(apk);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
        return result;
    }

    public void addApkItem(ApkItem apkItem){
        try {
            ct = getConnection();
            ct.setAutoCommit(false);
            PreparedStatement statement = ct.prepareStatement("insert into apk_update (package, version_name, md5) values (?, ?, ?)");
            statement.setString(1, apkItem.getPackageName());
            statement.setString(2, apkItem.getVersionName());
            statement.setString(3, apkItem.getMd5());
            statement.addBatch();
            statement.executeBatch();
            sm = statement;
            ct.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    public void deleteApkItem(String packageName, String versionName){
        try {
            ct = getConnection();
            sm = ct.createStatement();
            sm.execute("delete from apk_update where package='" + packageName + "' and version_name='"+ versionName +"'");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    public ApkItem queryApkItem(String packageName, String versionName){
        try {
            ct = getConnection();
            sm = ct.createStatement();
            rs = sm.executeQuery("select name, package from apk_update WHERE package='" + packageName + "' and version_name='"+ versionName +"'" );
            if (rs.next()){
                ApkItem apkItem = new ApkItem();
                apkItem.setPackageName(rs.getString("package"));
                apkItem.setVersionName(rs.getString("version_name"));
                apkItem.setMd5(rs.getString("md5"));
                return apkItem;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
        return null;
    }
}
