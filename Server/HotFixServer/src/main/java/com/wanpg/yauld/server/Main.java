package com.wanpg.yauld.server;

import com.wanpg.yauld.server.dao.ApkDao;
import com.wanpg.yauld.server.dao.DaoManager;
import com.wanpg.yauld.server.model.ApkItem;
import com.wanpg.yauld.server.model.HotItem;
import com.wanpg.yauld.server.transformer.JsonTransformer;
import com.wanpg.yauld.server.utils.Command;
import com.wanpg.yauld.server.utils.FileUtils;
import com.wanpg.yauld.server.utils.LogUtils;
import com.wanpg.yauld.server.utils.VersionUtils;
import org.apache.velocity.app.VelocityEngine;
import spark.ModelAndView;
import spark.Spark;
import spark.template.velocity.VelocityTemplateEngine;

import javax.servlet.MultipartConfigElement;
import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * Created by wangjinpeng on 2017/2/6.
 */
public class Main {

    public static VelocityEngine getVelocityEngine() {
        Properties properties = new Properties();
        properties.setProperty("resource.loader", "class");
        properties.setProperty(
                "class.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        properties.setProperty("encoding.default", "utf-8");
        properties.setProperty(VelocityEngine.INPUT_ENCODING, "utf-8");
        properties.setProperty(VelocityEngine.OUTPUT_ENCODING, "utf-8");
        properties.setProperty("default.contentType", "text/html");
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.init(properties);
        return velocityEngine;
    }

    public static void main(String[] args) {
        DaoManager.getInstance().init();

        Spark.staticFiles.registerMimeType("apk", "application/vnd.android.package-archive");
        Spark.staticFiles.registerMimeType("zip", "application/zip,application/x-compressed-zip");

        File apkFolder = new File("HotFixServer/src/main/resources/public/files");
        apkFolder.mkdirs();

        Spark.staticFileLocation("/public");
        Spark.staticFiles.expireTime(10); // ten minutes


        Spark.get("/stop", (request, response) -> {
            Spark.stop();
            return null;
        });

        Spark.get("/", (request, response) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("entries", DaoManager.getInstance().getApkDao().queryAllApkInfo());
            // The wm files are located under the resources directory
            return new ModelAndView(map, "template/index.vm");
        }, new VelocityTemplateEngine(getVelocityEngine()));

        Spark.get("/apk/create", (request, response) -> {
            Map map = new HashMap();

            // The wm files are located under the resources directory
            return new ModelAndView(map, "template/create_apk.vm");
        }, new VelocityTemplateEngine(getVelocityEngine()));

        Spark.post("/apk/create", (request, response) -> {
            DaoManager.getInstance().getApkDao().addApkInfo(request.queryParams("name"), request.queryParams("package"));
            response.redirect("/");
            return "";
        });

        Spark.get("/apk/show/:package", (request, response) -> {
            String packageName = request.params("package");
            ApkDao apkDao = DaoManager.getInstance().getApkDao();
            Map<String, Object> map = new HashMap<>();
            List<ApkItem> apkItems = apkDao.queryAllApkItem(packageName);
            ArrayList<Object> items = new ArrayList<>();
            for(ApkItem apkItem : apkItems){
                items.add(apkItem);
                items.addAll(apkDao.queryHotItems(apkItem.getPackageName(), apkItem.getVersionName()));
            }
            map.put("items", items);
            map.put("apk", apkDao.queryApkInfo(packageName));

            // The wm files are located under the resources directory
            return new ModelAndView(map, "template/apk_detail.vm");
        }, new VelocityTemplateEngine(getVelocityEngine()));

        Spark.get("/apk/delete", (request, response) -> {
            String packageName = request.queryParams("package");
            DaoManager.getInstance().getApkDao().deleteApk(packageName);
            // 此处还要删除文件夹
            FileUtils.delete(new File(apkFolder, packageName), true);
            response.redirect("/");
            return "";
        });

        Spark.get("/apk/upload", (request, response) -> {
            return new ModelAndView(new HashMap(), "template/apk_upload.vm");
        }, new VelocityTemplateEngine(getVelocityEngine()));

        Spark.post("/apk/upload", (request, response) -> {
            request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
            try (InputStream is = request.raw().getPart("file").getInputStream()) {
                // Use the input stream to create a file
                File tempFolder = new File(apkFolder, "temp");
                if (!tempFolder.exists()) {
                    tempFolder.mkdirs();
                }
                File tempApkFile = new File(tempFolder, "update.apk");
                FileUtils.delete(tempApkFile, true);

                FileUtils.copyStream(is, tempFolder.getPath(), "update.apk");

                //此处解析文件的内容
                List<String> infoList = Command.executeWithResult("java", "-jar",
                        "HotFixServer/diff-tool.jar",
                        "i", tempApkFile.getAbsolutePath());
                ApkItem apkItem = new ApkItem();
                for (String str : infoList) {
                    if (str.startsWith("package=")) {
                        apkItem.setPackageName(str.replace("package=", ""));
                    } else if (str.startsWith("version=")) {
                        apkItem.setVersionName(str.replace("version=", ""));
                    } else if (str.startsWith("md5=")) {
                        apkItem.setMd5(str.replace("md5=", ""));
                    }
                }

                ApkDao apkDao = DaoManager.getInstance().getApkDao();
                ApkItem apkItemOld = apkDao.queryApkItem(apkItem.getPackageName(), apkItem.getVersionName());
                File apkTargetFile = new File(apkFolder , apkItem.getPackageName() + File.separator + apkItem.getVersionName() + File.separator + "update.apk");
                if(apkItemOld != null && apkTargetFile.exists()){
                    // 进行差分
                    File hotPatchTempFile  = new File(apkFolder, apkItem.getPackageName() + File.separator + apkItem.getVersionName() + File.separator + "hot_update_temp.zip");
                    List<String> infoArray = Command.executeWithResult("java", "-jar",
                            "HotFixServer/diff-tool.jar",
                            "d", apkTargetFile.getAbsolutePath(), tempApkFile.getAbsolutePath(),
                            hotPatchTempFile.getAbsolutePath());
                    String hot_version = null;
                    String hot_md5 = null;
                    for(String info : infoArray){
                        if(info.startsWith("hot_version=")){
                            hot_version = info.replace("hot_version=", "");
                        }else if(info.startsWith("hot_md5=")){
                            hot_md5 = info.replace("hot_md5=", "");
                        }
                    }
                    if(hot_version != null && !hot_version.equals("")){
                        // 保存hotitem
                        FileUtils.copyFile(hotPatchTempFile, new File(apkFolder, apkItem.getPackageName() + File.separator + apkItem.getVersionName() + File.separator + "hot" +File.separator + hot_version + File.separator + "update.zip"));
                        FileUtils.delete(hotPatchTempFile, true);
                        HotItem hotItem = new HotItem();
                        hotItem.setMd5(hot_md5);
                        hotItem.setVersionName(apkItem.getVersionName());
                        hotItem.setHotVersion(hot_version);
                        hotItem.setPackageName(apkItem.getPackageName());
                        apkDao.deleteHotItem(hotItem);
                        apkDao.addHotItem(hotItem);
                    }
                }else {
                    FileUtils.copyFile(tempApkFile, apkTargetFile);
                    apkDao.deleteApkItem(apkItem.getPackageName(), apkItem.getVersionName());
                    apkDao.addApkItem(apkItem);
                }
                response.redirect("/apk/show/" + apkItem.getPackageName());
            }
            return "upload failed";
        });

        // 检测更新
        Spark.get("/apk/update/:package/:version/:hot_version", "application/json", (request, response) -> {
            String packageName = request.params(":package");
            String versionName = request.params(":version");
            String hotVersion = request.params(":hot_version");

            ApkDao apkDao = DaoManager.getInstance().getApkDao();

            List<ApkItem> apkItems = apkDao.queryAllApkItem(packageName);
            Collections.sort(apkItems, (o1, o2) -> VersionUtils.compareVersion(o1.getVersionName(), o2.getVersionName()));
            ApkItem apkItemUpdate = null;
            for(ApkItem apkItem : apkItems){
                if(VersionUtils.compareVersion(apkItem.getVersionName(), versionName) < 0){
                    apkItemUpdate = apkItem;
                }
                break;
            }
            if(apkItemUpdate != null){
                Map<String, Object> map = new HashMap<>();
                map.put("type", "apk");
                map.put("apk", apkItemUpdate);
                return map;
            }
            ApkItem apkItem = apkDao.queryApkItem(packageName, versionName);
            if(apkItem == null){
                return new HashMap<>();
            }
            List<HotItem> hotItems = apkDao.queryHotItems(packageName, versionName);
            Collections.sort(hotItems, (o1, o2) -> VersionUtils.compareVersion(o1.getVersionName(), o2.getVersionName()));
            for(HotItem item : hotItems){
                if(VersionUtils.compareVersion(item.getHotVersion(), hotVersion) < 0){
                    Map<String, Object> map = new HashMap<>();
                    map.put("type", "hot");
                    map.put("hot", item);
                    return map;
                }
                break;
            }
            return new HashMap<>();
        }, new JsonTransformer());

        Spark.get("/apk/download/:package/:version", (request, response) -> {
            String packageName = request.params(":package");
            String versionName = request.params(":version");

            response.redirect("/files/" + packageName + "/" + versionName + "/update.apk", 301);
            return "";
        });

        Spark.get("/apk/download/:package/:version/:hot_version", (request, response) -> {
            String packageName = request.params(":package");
            String versionName = request.params(":version");
            String hotVersion = request.params(":hot_version");

            response.redirect("/files/" + packageName + "/" + versionName + "/hot/" + hotVersion + "/update.zip", 301);
            return "";
        });
    }
}
