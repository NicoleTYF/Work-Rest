package com.example.nicolefsy.project_timer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;

import java.sql.Time;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class PopUpWindow extends AppCompatActivity {


    boolean work;
    boolean rest= false;
    boolean stop= true;
    boolean vibrate;

    SharedPreferences mpref;
    SharedPreferences.Editor mEditor;

    ImageView display_stateImg;
    Button switchBtn;
    Button cancelBtn;
    Timer timer;

    Dialog MyDialog;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // the background of the fragment work.xml & rest.xml
        setContentView(R.layout.popup);

        mpref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mEditor = mpref.edit();

        mEditor.putString("Wmins", mpref.getString("Wmins", ""));
        mEditor.putString("Rmins", mpref.getString("Rmins", ""));
        work = !mpref.getBoolean("stat",true);
        stop= mpref.getBoolean("stop",true);
        Timer.h.sendEmptyMessage(0);

        Log.v("no_click", ""+ work);

        if (work == true) {

            MyDialog = new Dialog(PopUpWindow.this);
            MyDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            MyDialog.setContentView(R.layout.rest);
            MyDialog.setTitle("My Custom Dialog");
            MyDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            MyDialog.setCanceledOnTouchOutside(false);
            MyDialog.setCancelable(false);

            display_stateImg= (ImageView) MyDialog.findViewById(R.id.stateImg);
            switchBtn = (Button) MyDialog.findViewById(R.id.switchBtn);
            cancelBtn= (Button) MyDialog.findViewById(R.id.cancelBtn);



            switchBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    work = !work;
                    rest = !rest;

                    mEditor.putBoolean("stat", work);

                    Intent intent_service = new Intent(getApplicationContext(), BackgroundService.class);
                    stopService(intent_service);
                    startService(intent_service);

                    Log.v("Button", "clicked");
                    Timer.h.sendEmptyMessage(1);
                    MyDialog.cancel();
                    MyDialog.dismiss();
                    MyDialog= null;

                    Log.v("no_clickEndRest", ""+ work);
                    PopUpWindow.this.finish();


                }
            });

            cancelBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    work= true;
                    stop= true;

                    //  work= true, stop= true
                    mEditor.putBoolean("stat", work);
                    mEditor.putBoolean("stop", stop);

                    // call Timer's CleanDisplay()
                    Timer.h.sendEmptyMessage(0);
                    MyDialog.cancel();
                    MyDialog.dismiss();
                    MyDialog= null;
                    PopUpWindow.this.finish();
                }
            });

        } else{

            MyDialog = new Dialog(PopUpWindow.this);
            MyDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            MyDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            MyDialog.setContentView(R.layout.work);
            MyDialog.setTitle("My Custom Dialog");
            MyDialog.setCanceledOnTouchOutside(false);
            MyDialog.setCancelable(false);


            display_stateImg= (ImageView) MyDialog.findViewById(R.id.stateImg);
            switchBtn = (Button) MyDialog.findViewById(R.id.switchBtn);
            cancelBtn= (Button) MyDialog.findViewById(R.id.cancelBtn);


            setSwitchBtn();
            setCancelBtn();

        }
        MyDialog.show();
    }

    public void setSwitchBtn(){
        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                work = !work;
                rest = !rest;

                mEditor.putBoolean("stat", work);

                Intent intent_service = new Intent(getApplicationContext(), BackgroundService.class);
                stopService(intent_service);
                startService(intent_service);

                Log.v("Button", "clicked");

                Timer.h.sendEmptyMessage(1);
                MyDialog.cancel();
                MyDialog.dismiss();
                MyDialog= null;
                PopUpWindow.this.finish();

            }
        });
    }

    public void setCancelBtn(){
        cancelBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                work= true;
                stop= true;

                //  work= true, stop= true
                mEditor.putBoolean("stat", work);
                mEditor.putBoolean("stop", stop);

                // call Timer's CleanDisplay()
                Timer.h.sendEmptyMessage(0);
                MyDialog.cancel();
                MyDialog.dismiss();
                MyDialog= null;
                PopUpWindow.this.finish();
            }
        });
    }



}