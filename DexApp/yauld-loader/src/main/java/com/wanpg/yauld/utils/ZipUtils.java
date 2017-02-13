package com.wanpg.yauld.utils;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    private static final String BASE_DIR = "";

    // 符号"/"用来作为目录标识判断符
    private static final String PATH = "/";
    private static final int BUFFER = 1024;

    /**
     * 文件压缩
     *
     * @param srcPath  源文件路径
     * @param destPath 目标文件路径
     */
    public static boolean compress(String srcPath, String destPath) {
        return compress(new File(srcPath), new File(destPath));
    }

    /**
     * 压缩
     *
     * @param srcFile  源路径
     * @param destFile 目标路径
     * @throws Exception
     */
    public static boolean compress(File srcFile, File destFile) {
        // 对输出文件做CRC32校验
        CheckedOutputStream cos = null;
        ZipOutputStream zos = null;
        try {
            cos = new CheckedOutputStream(new FileOutputStream(destFile), new CRC32());
            zos = new ZipOutputStream(cos);
            compress(srcFile, zos, BASE_DIR);
            zos.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeStream(zos);
            closeStream(cos);
        }
    }

    private static void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 压缩
     *
     * @param srcFile  源路径
     * @param zos      ZipOutputStream
     * @param basePath 压缩包内相对路径
     * @throws Exception
     */
    private static void compress(File srcFile, ZipOutputStream zos, String basePath)
            throws IOException {
        if (srcFile.isDirectory()) {
            File[] files = srcFile.listFiles();
            if (files != null) {
                // 构建空目录
                if (files.length < 1) {
                    ZipEntry entry = new ZipEntry(basePath + srcFile.getName() + PATH);
                    zos.putNextEntry(entry);
                    zos.closeEntry();
                }
                for (File file : files) {
                    if (file.isDirectory()) {
                        // 递归压缩
                        compress(file, zos, basePath + file.getName() + PATH);
                    } else {
                        compressFile(file, zos, basePath);
                    }
                }
            }
        } else {
            throw new IOException("src file is not a directory");
        }
    }

    /**
     * 文件压缩
     *
     * @param file 待压缩文件
     * @param zos  ZipOutputStream
     * @param dir  压缩文件中的当前路径
     * @throws Exception
     */
    private static void compressFile(File file, ZipOutputStream zos, String dir)
            throws IOException {
        /**
         * 压缩包内文件名定义
         *
         * <pre>
         * 如果有多级目录，那么这里就需要给出包含目录的文件名
         * 如果用WinRAR打开压缩包，中文名将显示为乱码
         * </pre>
         */
        ZipEntry entry = new ZipEntry(dir + file.getName());

        zos.putNextEntry(entry);

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

        int count;
        byte data[] = new byte[BUFFER];
        while ((count = bis.read(data, 0, BUFFER)) != -1) {
            zos.write(data, 0, count);
        }
        bis.close();

        zos.closeEntry();
    }


    /**
     * 解压到指定目录
     *
     * @param zipPath
     * @param descDir
     * @author isea533
     */
    public static boolean unZipFiles(String zipPath, String descDir) {
        return unZipFiles(new File(zipPath), descDir);
    }

    /**
     * 解压文件到指定目录
     *
     * @param zipFile
     * @param destDir
     * @author isea533
     */
    public static boolean unZipFiles(File zipFile, String destDir) {
        File pathFile = new File(destDir);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        ZipFile zip = null;
        try {
            zip = new ZipFile(zipFile);
            for (Enumeration entries = zip.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String zipEntryName = entry.getName();
                InputStream in = zip.getInputStream(entry);
                String outPath = ((destDir.endsWith(File.separator) ? destDir : (destDir + File.separator)) + zipEntryName).replaceAll("\\*", "/");
                //判断路径是否存在,不存在则创建文件路径
                File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
                if (!file.exists()) {
                    file.mkdirs();
                }
                //判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
                if (new File(outPath).isDirectory()) {
                    continue;
                }
                //输出文件路径信息
                OutputStream out = new FileOutputStream(outPath);
                byte[] buf1 = new byte[1024];
                int len;
                while ((len = in.read(buf1)) > 0) {
                    out.write(buf1, 0, len);
                }
                in.close();
                out.close();
            }
            System.out.println("******************解压完毕********************");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 解压apk下面的res、assets和resources.arsc
     * 解压 android manifest 6.0以下版本，要求压缩包中存在AndroidManifest.xml
     */
    public static boolean unZipApkResources(String path, String destDir) {
        File pathFile = new File(destDir);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        ZipFile zip = null;
        try {
            zip = new ZipFile(path);
            for (Enumeration entries = zip.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String zipEntryName = entry.getName();
                if ("resources.arsc".equals(zipEntryName)
                        || zipEntryName.startsWith("res")
                        || zipEntryName.startsWith("assets")
                        || "AndroidManifest.xml".equals(zipEntryName)) {
                    File outFile = new File(destDir, zipEntryName);
                    //判断路径是否存在,不存在则创建文件路径
                    File file = outFile.getParentFile();
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    //判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
                    if (outFile.isDirectory()) {
                        continue;
                    }
                    //输出文件路径信息
                    InputStream in = zip.getInputStream(entry);
                    OutputStream out = new FileOutputStream(outFile);
                    byte[] buf1 = new byte[1024];
                    int len;
                    while ((len = in.read(buf1)) > 0) {
                        out.write(buf1, 0, len);
                    }
                    in.close();
                    out.close();
                }
            }
            zip.close();
            System.out.println("******************解压完毕********************");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}  