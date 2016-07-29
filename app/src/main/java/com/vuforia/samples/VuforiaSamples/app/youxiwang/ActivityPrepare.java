package com.vuforia.samples.VuforiaSamples.app.youxiwang;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.vuforia.samples.VuforiaSamples.R;
import com.vuforia.samples.VuforiaSamples.ui.ActivityList.ActivityLauncher;

/**
 * Created by Administrator on 2016/7/19.
 */
public class ActivityPrepare extends Activity
{
    //    启动页延迟显示时间
    private static long SPLASH_MILLIS = 1950;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
//        无标题并全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        inflate就相当于将一个xml中定义的布局找出来，对于一个没有被载入或者想要动态载入的界面，都需要使用LayoutInflater.inflate()来载入；
        LayoutInflater inflater = LayoutInflater.from(this);
        RelativeLayout layout = (RelativeLayout) inflater.inflate(
                R.layout.activity_buffer, null, false);

        addContentView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
//        作用：接收子线程发送来的消息并配合主线程来更新UI
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {

            @Override
            public void run()
            {

                Intent intent = new Intent(ActivityPrepare.this,
                        ActivityLauncher.class);
                startActivity(intent);

            }

        }, SPLASH_MILLIS);
    }

}