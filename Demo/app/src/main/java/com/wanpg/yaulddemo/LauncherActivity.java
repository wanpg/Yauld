package com.wanpg.yaulddemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.wanpg.yauld.YauldDex;

/**
 * Created by wangjinpeng on 2016/12/26.
 */

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                YauldDex.isLoadFinished(new YauldDex.OnLoadListener() {
                    @Override
                    public void onComplete() {
                        toMainActivity();
                    }
                });
            }
        }, 2000);
    }


    private void toMainActivity(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setAction("com.wanpg.yaulddemo.MainActivity");
                intent.setPackage(getPackageName());
                startActivity(intent);
            }
        });
    }
}
