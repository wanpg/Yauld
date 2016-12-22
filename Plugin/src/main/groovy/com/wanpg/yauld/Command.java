package com.wanpg.yauld;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by wangjinpeng on 16/7/20.
 */
public class Command {

    /**
     * 执行命令
     *
     * @param command 命令
     */
    public static synchronized boolean execute(String... command) {
        Process process = null;
        BufferedReader reader = null;
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            process = builder.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utils.close(reader);
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }

    public static synchronized boolean execute(List<String> command) {
        Process process = null;
        BufferedReader reader = null;
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            process = builder.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utils.close(reader);
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }
}
