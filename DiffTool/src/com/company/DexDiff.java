package com.company;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangjinpeng on 2016/12/28.
 */
public class DexDiff {

    public static void diff(String oldDex, String newDex){
        String tmpFolder = "/Users/wangjinpeng/WorkSpace/github/wanpg/Yauld/untitled/temp";
        File file = new File(tmpFolder);
        FileUtils.delete(file,false);

        try {
            File oldDexSmaliFolder = new File(tmpFolder, "old");
            File newDexSmaloFolder = new File(tmpFolder, "new");
            dex2smali(oldDex, oldDexSmaliFolder.getAbsolutePath());
            dex2smali(newDex, newDexSmaloFolder.getAbsolutePath());
            List<String> oldSmaliList = getFileList(oldDexSmaliFolder.getAbsolutePath(), oldDexSmaliFolder.getAbsolutePath());
            for(String oldSmali : oldSmaliList){
                File oldSmaliFile = new File(oldDexSmaliFolder, oldSmali);
                File newSmaliFile = new File(newDexSmaloFolder, oldSmali);
                if(oldSmaliFile.exists() && newSmaliFile.exists() && Utils.md5File(oldSmaliFile).equals(Utils.md5File(newSmaliFile))){
                    FileUtils.delete(newSmaliFile, true);
                }
            }
            smali2dex(newDexSmaloFolder.getAbsolutePath(), new File(tmpFolder, "patch.dex").getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void dex2smali(String dex, String outPut) throws IOException {
        String jar = "/Users/wangjinpeng/WorkSpace/github/wanpg/Yauld/untitled/tools/baksmali-2.2b4.jar";
        Command.execute("java", "-jar", jar, "disassemble", "-o", outPut, dex);
    }

    public static void smali2dex(String smalifolder, String outDex){
        String[] args = new String[]{
                "assemble", "-o", outDex, smalifolder
        };
        String jar = "/Users/wangjinpeng/WorkSpace/github/wanpg/Yauld/untitled/tools/smali-2.2b4.jar";
        Command.execute("java", "-jar", jar, "assemble", "-o", outDex, smalifolder);
//        org.jf.smali.main.main(args);
    }

    public static List<String> getFileList(String rootFolder, String folderPath){
        ArrayList<String> result = new ArrayList<>();
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        for(File file : files){
            if(file.isDirectory()){
                result.addAll(getFileList(rootFolder, file.getAbsolutePath()));
            }else if(file.isFile() && file.getName().endsWith(".smali")){
                result.add(file.getAbsolutePath().replace(rootFolder, ""));
            }
        }
        return result;
    }
}
