package com.example.nicolefsy.project_timer;

/**
 * Created by NicoleFSY on 11/22/2018.
 */

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.os.ResultReceiver;
import android.util.Log;


import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.MobileAds;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class BackgroundService extends Service {


    public static String str_receiver = "com.nicolefsy.project_timer.receiver";

    private Handler mHandler = new Handler();
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;
    String strDate;
    Date date_current, date_diff;
    SharedPreferences mpref;
    SharedPreferences.Editor mEditor;

    protected Timer mTimer = null;
    public static final long NOTIFY_INTERVAL = 1000;
    Intent intent;

    boolean work;
    Boolean stop;
    boolean sound;
    Boolean vibrate;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mpref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mEditor = mpref.edit();
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 5, NOTIFY_INTERVAL);
        intent = new Intent(str_receiver);

    }


    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {

            mHandler.post(new Runnable() {

                @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
                @Override
                public void run() {
                    calendar = Calendar.getInstance();
                    stop = mpref.getBoolean("stop", false);
                    work = mpref.getBoolean("stat", true);
                    simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
                    strDate = simpleDateFormat.format(calendar.getTime());

                    vibrate= mpref.getBoolean("vi", false);
                    sound= mpref.getBoolean("sound", true);

                    Log.e("strDate", strDate);

                    if(stop == false){
                        if (work == true) {
                            mEditor.putString("Wmins", twoDatesBetweenTime(
                                    Integer.valueOf(mpref.getString("Wmins", ""))));
                        } else {
                            mEditor.putString("Rmins", twoDatesBetweenTime(
                                    Integer.valueOf(mpref.getString("Rmins", "9"))));
                        }
                        return;
                    }else{
                        return;
                    }

                }
            });

       }

    }


    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public String twoDatesBetweenTime(int int_mins) {

        stop = mpref.getBoolean("stop", false);
        try {
            date_current = simpleDateFormat.parse(strDate);
        } catch (Exception e) {

        }

        try {
            date_diff = simpleDateFormat.parse(mpref.getString("data", ""));
        } catch (Exception e) {

        }


        long diff= date_current.getTime()- date_diff.getTime();

        long int_timer= int_timer = TimeUnit.MINUTES.toMillis(int_mins);
        long long_mins= int_timer - diff;
        long diffSeconds2= long_mins / (1000 / 60) % 60;
        long diffHours2= long_mins / (60 * 1000) % 24;
        long diffMinutes2= long_mins/1000;

        if(long_mins >= 0) {
            int time= (int) diffMinutes2;
            long_mins= time;
            Log.v("ihyo", String.valueOf(long_mins));
            String str_testing = String.valueOf(time);

            Log.v("TIME",str_testing);

            fn_update(str_testing);
        }else {
            mEditor.putBoolean("finish", true).commit();
            mEditor.putBoolean("stop", true).commit();
            mTimer.cancel();

            //  find sound on/off, vibrate on/off
            sound= mpref.getBoolean("sound", true);
            vibrate= mpref.getBoolean("vi", false);



            Intent dialogIntent = new Intent(this, PopUpWindow.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(dialogIntent);

            doNotify();

            if (vibrate == true) {
                Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(900);
            }



            work = !work;
            mEditor.putBoolean("stat", work).commit();

        }
        return String.valueOf(long_mins);

    }

    public void doNotify() {

        //  build notification content
        Context context = getApplicationContext();
        android.support.v7.app.NotificationCompat.Builder soundBuilder =
                (android.support.v7.app.NotificationCompat.Builder) new android.support.v7.app.NotificationCompat.Builder(context);

        Intent intent = new Intent(this, com.example.nicolefsy.project_timer.Timer.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (sound == true) {
            Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            soundBuilder.setSound(sound);
        }

        int unicode_coffee = 0x2615;
        int unicode_work=  0x0001F4BC ;
        int unicode_goodjob= 0x0001F44D;

        //  call manager
        if (work == true) {
            soundBuilder.setSmallIcon(R.drawable.ic_stat_name);
            soundBuilder.setContentTitle("You have done well"+ getEmojiByUnicode(unicode_goodjob));
            soundBuilder.setContentText("why don't get some coffee and relax for a while"+
                    getEmojiByUnicode(unicode_coffee)+ "\n Click to set a new time.");
        } else {
            soundBuilder.setSmallIcon(R.drawable.ic_stat_business);
            soundBuilder.setContentTitle("Break time over");
            soundBuilder.setContentText("time to get back to work!" +
                    getEmojiByUnicode(unicode_work)+ "\n Click to set a new time.");
        }
        soundBuilder.setAutoCancel(true);
        soundBuilder.setContentIntent(pendingIntent);


        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(100, soundBuilder.build());

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        mTimer.cancel();
        Log.e("Service finish", "Finish");
    }

    private void fn_update(String str_time) {

        intent.putExtra("time", str_time);
        sendBroadcast(intent);
    }

    //  convert unicode to emoji
    public String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }
}