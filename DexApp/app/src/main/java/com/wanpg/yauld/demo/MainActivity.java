package com.wanpg.yauld.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.wanpg.yauld.YauldDex;
import com.wanpg.yauld.demo.model.Result;
import com.wanpg.yauld.utils.FileUtils;
import com.wanpg.yauld.utils.MD5;

import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private TextView updateInfo;
    private Button updateButton;
    private Result result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updateInfo = (TextView) findViewById(R.id.update_info);
        updateButton = (Button) findViewById(R.id.update);
        updateButton.setText("更新");
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string = updateButton.getText().toString();
                if("更新".equals(string)) {
                    checkUpdate();
                }else if("下载".equals(string)){
                    download();
                }else if("重启".equals(string)){
                    YauldDex.restartApplication(MainActivity.this);
                }
            }
        });

        TextView info = (TextView) findViewById(R.id.info);

        info.setText("Hello World! /n I'm a old Demo with old Image");
    }


    String download_hot = "http://192.168.1.37:4567/apk/download/%s/%s/%s";
    String download_apk = "http://192.168.1.37:4567/apk/download/%s/%s";
    private void download(){
        if(result != null){
            if(result.hot != null){
                String url = String.format(download_hot, result.hot.getPackageName(), result.hot.getVersionName(), result.hot.getHotVersion());
                Request.Builder builder = new Request.Builder()
                        .tag(url)
                        .url(url)
                        .get();
                Call call = getOkHttpInstance().newCall(builder.build());
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateButton.setText("更新");
                                updateInfo.setText("下载失败");
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        InputStream inputStream = response.body().byteStream();
                        File folder = new File(YauldDex.getYauldFolder(MainActivity.this));
                        folder.mkdirs();

                        FileUtils.copyStream(inputStream, folder.getAbsolutePath(), YauldDex.YAULD_UPDATE_ZIP_NAME);
                        if(result.hot.getMd5().equals(MD5.md5File(new File(folder, YauldDex.YAULD_UPDATE_ZIP_NAME)))){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateButton.setText("重启");
                                    updateInfo.setText("下载成功，请重启");
                                }
                            });
                        }else{
                            FileUtils.delete(folder, false);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateButton.setText("更新");
                                    updateInfo.setText("下载成功，MD5不一致");
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    String update_url = "http://192.168.1.37:4567/apk/update/%s/%s/%s";

    private void checkUpdate() {
        updateInfo.setText("正在检查更新...");
        String requestUrl = String.format(update_url, YauldDex.getPackageName(this), YauldDex.getVersionName(this), YauldDex.getHotVersion(this));
        Request.Builder builder = new Request.Builder()
                .tag(requestUrl)
                .url(requestUrl)
                .get();
        Call call = getOkHttpInstance().newCall(builder.build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateButton.setText("更新");
                        updateInfo.setText("更新检查失败");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                final Gson gson = new Gson();
                result = gson.fromJson(string, Result.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(result != null) {
                            if (result.hot != null) {
                                updateButton.setText("下载");
                                updateInfo.setText("热更新" + gson.toJson(result.hot));
                                return;
                            } else if (result.apk != null) {
                                updateButton.setText("下载");
                                updateInfo.setText("apk更新" + gson.toJson(result.apk));
                                return;
                            }
                        }else{
                            updateButton.setText("更新");
                            updateInfo.setText("暂无更新");
                        }
                    }
                });
            }
        });
    }


    private static final int CONNECT_TIMEOUT = 60 * 1000;
    private static final int READ_TIMEOUT = 5 * 60 * 1000;
    private static final int WRITE_TIMEOUT = 3 * 60 * 1000;

    private static class OkHttpClientBuilder {
        private static OkHttpClient instance;

        static {
            instance = newHttpClientBuilder()
                    .proxySelector(ProxySelector.getDefault())
                    .build();
        }
    }

    private static OkHttpClient.Builder newHttpClientBuilder() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.retryOnConnectionFailure(false)
                .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS);
        return builder;
    }

    private static OkHttpClient getOkHttpInstance() {
        return OkHttpClientBuilder.instance;
    }
}
