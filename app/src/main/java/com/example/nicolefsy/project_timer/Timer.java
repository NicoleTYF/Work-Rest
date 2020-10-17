package com.example.nicolefsy.project_timer;

/**
 * Created by NicoleFSY on 11/22/2018.
 */

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Message;

//  Used in functions outsi
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.google.android.gms.ads.MobileAds;

import static com.example.nicolefsy.project_timer.BackgroundService.*;

    public class Timer extends AppCompatActivity {

        //  time variables
        int Timep;
        int Timeq;
        int Time;
        String date_time;
        Calendar calendar;
        SimpleDateFormat simpleDateFormat;

        //  states
        boolean stop= false;
        boolean rest;
        boolean work;

        //  tools
        SharedPreferences mpref;
        SharedPreferences.Editor mEditor;

        private MessageHandler messageHandler;
        public static Handler h;


        //  layouts
        Button b;
        Button k;
        TextView displayTime;
        TextView displayState;

        EditText input_workTime;
        EditText input_restText;
        ProgressBar progressBar;

        Boolean vibrate= false;
        Boolean sound= true;
        ImageButton imageButton;
        ImageButton soundButton;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            init();

        }

        public void init() {
            input_workTime = findViewById(R.id.workEditText);
            input_restText = findViewById(R.id.restEditText);
            displayTime = findViewById(R.id.display_time);
            displayState = findViewById(R.id.display_state);
            progressBar= findViewById(R.id.linearprogressBar);
            k = findViewById(R.id.Stop);

            messageHandler = new MessageHandler();

            imageButton= findViewById(R.id.imageButton);
            soundButton= findViewById(R.id.soundBtn);

            MobileAds.initialize(this, "ca-app-pub-9018007061294017~7093281101");

            h = new Handler() {

                @RequiresApi(api = Build.VERSION_CODES.M)
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    switch(msg.what) {

                        case 0:

                            stop= true;
                            work= true;
                            rest= false;

                            mEditor.putBoolean("stop",stop).commit();
                            mEditor.clear();
                            Intent intent_service = new Intent(getApplicationContext(), BackgroundService.class);
                            stopService(intent_service);

                            CleanDisplay();
                            break;

                        case  1:
                            stop= false;
                            stop = mpref.getBoolean("stop", stop);
                            work = mpref.getBoolean("stat", work);

                            CleanDisplay();

                            setUpViBtn(mpref.getBoolean("vi", vibrate));
                            setUpSoundsBtn(mpref.getBoolean("sound", sound));

                            startTimer();
                            break;
                    }
                }
            };


            b = findViewById(R.id.Startcount);


            b.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View view) {

                            stop= false;

                            HideKeyboard();

                            //  Manage invalid input of null and 0
                            String Timep1 = input_workTime.getText().toString();
                            String Timeq1 = input_restText.getText().toString();

                            Context context= getApplicationContext();
                            if (Timep1.matches("") || Timep1.matches("0")) {
                                Toast.makeText(context, "Work time must be greater than 0", Toast.LENGTH_SHORT).show();
                            } else if (Timeq1.matches("") || Timeq1.matches("0")) {
                                Toast.makeText(context, "Rest time must be greater than 0", Toast.LENGTH_SHORT).show();

                            } else {

                                //  Input are valid numbers
                                work= true;
                                rest= false;
                                startTimer();
                            }
                        }
                    });



            k.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View view) {
                            stop= true;
                            work= true;
                            rest= false;

                                mEditor.putBoolean("stop",stop).commit();
                                mEditor.clear();
                                Intent intent_service = new Intent(getApplicationContext(), BackgroundService.class);
                                stopService(intent_service);

                            CleanDisplay();

                        }

                    });

            imageButton.setOnClickListener(
                    new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        public void onClick(View view){

                            vibrate = !vibrate;
                            mEditor.putBoolean("vi",vibrate).commit();

                            Context context = getApplicationContext();
                            if(vibrate == true) {
                                Toast.makeText(context, R.string.vibrate_on, Toast.LENGTH_SHORT).show();
                            }
                            if (vibrate == false) {
                                Toast.makeText(context, R.string.vibrate_off, Toast.LENGTH_SHORT).show();
                            }
                            setUpViBtn(mpref.getBoolean("vi", vibrate));
                        }

                    });

            soundButton.setOnClickListener(
                    new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        public void onClick(View view) {

                            sound = !sound;
                            mEditor.putBoolean("sound",sound).commit();
                            Context context = getApplicationContext();

                            //  make toast & change color
                            if (sound == true) {
                                Toast.makeText(context, "sound on", Toast.LENGTH_SHORT).show();
                            }
                            if (sound == false) {
                                Toast.makeText(context, "sound off", Toast.LENGTH_SHORT).show();
                            }

                            setUpSoundsBtn(mpref.getBoolean("sound", sound));
                        }

                    });


            mpref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            mEditor = mpref.edit();


        }


        @SuppressLint("SimpleDateFormat")
        public void startTimer(){

            stop= false;
            mEditor.putBoolean("stop",stop).commit();
            if (stop== false) {

                HideKeyboard();

                String Timep1 = input_workTime.getText().toString();
                String Timeq1 = input_restText.getText().toString();

                calendar = Calendar.getInstance();
                simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
                date_time = simpleDateFormat.format(calendar.getTime());

                //  Input received time, state into the editor
                mEditor.putString("data", date_time).commit();
                mEditor.putString("Wmins", Timep1).commit();
                mEditor.putString("Rmins", Timeq1).commit();
                mEditor.putBoolean("stat",work).commit();
                mEditor.putBoolean("stop",stop).commit();


                // Convert text string to integer
                Timep= Integer.parseInt(Timep1);
                Timeq= Integer.parseInt(Timeq1);

                if(work== true) {

                    displayState.setText("status: Working");

                    //  progrss bar showing time decreased in seconds(minute* 60), set maximum as input time in the work state
                    progressBar.setMax(Timep*60);

                }else{

                    displayState.setText("status: Resting");

                    //  progrss bar showing time decreased in seconds(minute* 60), set maximum as input time in the rest state
                    progressBar.setMax(Timeq*60);

                }// end work/rest if

                //  Logcat search for "emma", show value of input time at that state
                    //  Test case 1: In work state, input 90 in Work time ___, show "90"
                    //  Test case 2: In rest state, input 1000 in Break time ___, show "1000"
                    //  Test case 3: In work state, input 80 in Work time ___ && 34 in Break time___, show "80"
                    //  Test case 4: In rest state, input 80 in Work time ___ && 34 in Break time___, show "34"
                Log.v("emma", String.valueOf(progressBar.getMax()));

              //  start calculating time in background
                Intent intent_service = new Intent(getApplicationContext(), BackgroundService.class);
                startService(intent_service);

            }// end stop if
        }


        private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(stop== false){
                String str_time = intent.getStringExtra("time");   //  get time from BackgroundService
                int Timen= Integer.parseInt(str_time);
                String Second="";
                Time= Integer.parseInt(str_time);
                displayTime.setText(String.format("%02d",Timen/60+1));
                if(work== true) {
                    displayState.setText("status: Working");
                    progressBar.setProgress(Timep*60);
                }else {
                    displayState.setText("status: Resting");
                    progressBar.setProgress(Timeq*60);
                }
                progressBar.setProgress(Timen);

                Bundle bundle = new Bundle();
                bundle.putInt("Current Count",Integer.parseInt(str_time));
                Message message = new Message();
                message.setData(bundle);

                Log.v("emma2", String.valueOf(Second));

                messageHandler.sendMessage(message);
            }
           }
        };

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected void onResume() {
            super.onResume();
                registerReceiver(broadcastReceiver, new IntentFilter(str_receiver));

            setUpViBtn(mpref.getBoolean("vi", vibrate));
            setUpSoundsBtn(mpref.getBoolean("sound", sound));
        }

        @Override
        protected void onPause() {
            super.onPause();
            unregisterReceiver(broadcastReceiver);
        }

        class MessageHandler extends Handler {
            @Override
            public void handleMessage(Message message) {
                int currentCount = message.getData().getInt("Current Count");

                if(currentCount== 1) {
                    // mEditor.putBoolean("stat", work);
                    mEditor.putBoolean("statR", rest);

                    CleanDisplay();


                }
            }
        }

        //  Resetting display from buttons to text display, applies in finishing a state or stopping of timer
        public void CleanDisplay(){

            if(stop== true) {
                k.setClickable(false);
                k.setEnabled(false);
                b.setClickable(true);
                b.setEnabled(true);

            }else {
                k.setClickable(true);
                k.setEnabled(true);
                b.setClickable(false);
                b.setEnabled(false);

            }

            displayTime.setText("00");
            progressBar.setMax(0);
            progressBar.setProgress(0);
            displayState.setText("Status: Null");

        }

        //  Hide keyboard, applies in finishing editing by pressing "START" button
        @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
        public void HideKeyboard(){
            Context context= getApplicationContext();
            InputMethodManager inputManager= (InputMethodManager) getSystemService(context.INPUT_METHOD_SERVICE);

            //   if texts are being edited, there is a keyboard & has a window, then hide keyboard
            if (inputManager!= null) {

                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }


        @RequiresApi(api = Build.VERSION_CODES.M)
        public void setUpViBtn(boolean vibrate){

            if(vibrate == true) {
                imageButton.setColorFilter(getColor(R.color.yeledored));
            }else {
                imageButton.setColorFilter(getColor(R.color.common_google_signin_btn_text_light_disabled));
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        public void setUpSoundsBtn(boolean sound){

            //  make toast & change color
            if (sound == true) {
                soundButton.setColorFilter(getColor(R.color.yeledored));
            }else {
                soundButton.setColorFilter(getColor(R.color.common_google_signin_btn_text_light_disabled));
            }
        }

}
