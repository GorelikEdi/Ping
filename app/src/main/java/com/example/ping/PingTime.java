package com.example.ping;

import android.annotation.SuppressLint;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PingTime {

    public static long getEpoch(){
        return System.currentTimeMillis() / 1000L;
    }

    @SuppressLint("SimpleDateFormat")
    public static String getTimeFromEpoch(long epoch){
        return new SimpleDateFormat("HH:mm").format(new Date(epoch*1000));
    }

}
