package com.wanpg.yauld.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wangjinpeng on 2017/2/7.
 */
public class SmaliUtils {

    public static String getFieldValue(String smaliFilePath, String className, String fieldName) {

        FileInputStream fileInputStream = null;
        String smaliContent = null;
        try {
            fileInputStream = new FileInputStream(smaliFilePath);
            byte[] buffer = new byte[fileInputStream.available()];
            int read = fileInputStream.read(buffer);
            smaliContent = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(TextUtils.isEmpty(smaliContent)){
            return null;
        }

        String classNamePath = className.replace(".", "/");
        String pattern1 = "(const-string v[0-9]+,(\\s)?\".*\"(\\s)*)?sput-object\\sv[0-9]+,(\\s)?L" + classNamePath + ";->" + fieldName + ":Ljava/lang/String;";
        Matcher m1 = Pattern.compile(pattern1).matcher(smaliContent);
        String result1 = "";
        if (m1.find()) {
            result1 = m1.group();
        }
        if (TextUtils.isEmpty(result1)) {
            return null;
        }
        String substring = result1.substring(result1.indexOf("\"") + 1);
        return substring.substring(0, substring.indexOf("\""));
    }
}
