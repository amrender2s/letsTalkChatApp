package com.example.sample;

import android.app.Application;
import android.content.Context;

public class GetTimeAgo extends Application {

    int secondsMillis=1000;
    int minuteMillis=60*secondsMillis;
    int hourMillis=60*minuteMillis;
    int dayMillis=24*hourMillis;


    public String getTimeAgo(long time, Context context){
        if(time<1000000000000L){
            time*=1000;
        }
        long now=System.currentTimeMillis();
        if(time>now || time<=0){
            return null;
        }
        long diff=now-time;
        if(diff<minuteMillis){
            return "just now";
        }else if(diff<2*minuteMillis){
            return "a minute ago";
        }else if(diff<50*minuteMillis){
            return diff/minuteMillis+" minute ago";
        }else if(diff<90*minuteMillis){
            return "an hour ago";
        }else if(diff<24*hourMillis){
            return diff/hourMillis+ " hour ago";
        }else if(diff<48*hourMillis){
            return "yesterday";
        }else {
            return diff/dayMillis+" day ago";
        }
    }
}
