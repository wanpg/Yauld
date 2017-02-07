package com.wanpg.yauld.server;

import com.wanpg.yauld.server.dao.ApkDao;
import com.wanpg.yauld.server.dao.DaoManager;
import com.wanpg.yauld.server.model.ApkItem;
import com.wanpg.yauld.server.transformer.JsonTransformer;
import com.wanpg.yauld.server.utils.Command;
import com.wanpg.yauld.server.utils.FileUtils;
import org.apache.velocity.app.VelocityEngine;
import spark.*;
import spark.template.velocity.VelocityTemplateEngine;

import javax.servlet.MultipartConfigElement;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by wangjinpeng on 2017/2/6.
 */
public class Main {

    public static void main(String[] args) {
        DaoManager.getInstance().init();

        Spark.staticFiles.registerMimeType("apk", "application/vnd.android.package-archive");
        Spark.staticFiles.registerMimeType("zip", "application/octet-stream");

        File apkFolder = new File("HotFixServer/src/main/resources/public/files");
        apkFolder.mkdirs();

        Spark.staticFileLocation("/public");

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
        Spark.get("/apk/show", (request, response) -> {
            String packageName = request.queryParams("package");
            ApkDao apkDao = DaoManager.getInstance().getApkDao();
            Map<String, Object> map = new HashMap<>();
            map.put("items", apkDao.queryAllApkItem(packageName));
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
                        "HotFixServer/src/main/resources/tools/diff_tool/diff-tool.jar",
                        "i", tempApkFile.getAbsolutePath());
                String s = infoList.get(0);
                String[] strings = s.split(";");
                ApkItem apkItem = new ApkItem();
                for (String str : strings) {
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
                            "HotFixServer/src/main/resources/tools/diff_tool/diff-tool.jar",
                            "d", apkTargetFile.getAbsolutePath(), tempApkFile.getAbsolutePath(),
                            hotPatchTempFile.getAbsolutePath());
                }else {
                    FileUtils.copyFile(tempApkFile, apkTargetFile);
                    apkDao.deleteApkItem(apkItem.getPackageName(), apkItem.getVersionName());
                    apkDao.addApkItem(apkItem);
                }
                response.redirect("/apk/show?package=" + apkItem.getPackageName());
            }
            return "upload failed";
        });

        // 检测更新
        Spark.get("/apk/update/:package/:version/:hotversion", new Route() {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                String packageName = request.params(":package");
                ApkDao apkDao = DaoManager.getInstance().getApkDao();
                apkDao.queryAllApkItem(packageName);
                return null;
            }
        }, new JsonTransformer());
    }

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
}
