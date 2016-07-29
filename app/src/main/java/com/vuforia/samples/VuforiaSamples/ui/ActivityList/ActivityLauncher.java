/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2015 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/


package com.vuforia.samples.VuforiaSamples.ui.ActivityList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.vuforia.samples.VuforiaSamples.R;


// This activity starts activities which demonstrate the Vuforia features
public class  ActivityLauncher extends ListActivity
{
    
    private String mActivities[] = { "AR识图","游戏王","AR购物","AR游戏","AR导航","AR餐饮","AR交流","AR云识","AR测试"};

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
            R.layout.activities_list_text_view, mActivities);

//        全屏No Title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activities_list);
        setListAdapter(adapter);
    }
    
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id)//Item事件
    {
        
        Intent intent = new Intent(this, AboutScreen.class);
        intent.putExtra("ABOUT_TEXT_TITLE", mActivities[position]);
        
        switch (position)//点击事件分发
        {
            case 0:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                    "app.ImageTargets.ImageTargets");
                intent.putExtra("ABOUT_TEXT", "ImageTargets/IT_about.html");
                break;

            //游戏王
            case 1:
                intent.putExtra("ACTIVITY_TO_LAUNCH",


                    "app.youxiwang.ActivityPrepare");
                intent.putExtra("ABOUT_TEXT", "Yu_Gi_Oh/ARYu_Gi_Oh.html");
                intent.putExtra("ABOUT_TEXT", "CylinderTargets/CY_about.html");

                break;
            case 2:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                    "app.MultiTargets.MultiTargets");
                intent.putExtra("ABOUT_TEXT", "MultiTargets/MT_about.html");
                break;
            case 3:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                    "app.UserDefinedTargets.UserDefinedTargets");
                intent.putExtra("ABOUT_TEXT",
                    "UserDefinedTargets/UD_about.html");
                break;
//            AR导航
            case 4:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                    "app.ARNavigation.MainNavigation");
                intent.putExtra("ABOUT_TEXT", "ARNavigation/ARNavigation.html");
                break;
            case 5:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                    "app.CloudRecognition.CloudReco");
                intent.putExtra("ABOUT_TEXT", "CloudReco/CR_about.html");
                break;
            case 6:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                    "app.TextRecognition.TextReco");
                intent.putExtra("ABOUT_TEXT", "TextReco/TR_about.html");
                break;
            case 7:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                    "app.FrameMarkers.FrameMarkers");
                intent.putExtra("ABOUT_TEXT", "FrameMarkers/FM_about.html");
                break;
            case 8:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                    "app.VirtualButtons.VirtualButtons");
                intent.putExtra("ABOUT_TEXT", "VirtualButtons/VB_about.html");
                break;
            default:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                        "app.DragonTest.DragonTest");
//                intent.putExtra("ABOUT_TEXT","VirtualButtons/VB_about.html");
                break;
        }
        
        startActivity(intent);
        
    }
}
