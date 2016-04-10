package com.example.nilanjandaw.snaap;

import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Aishwarya Sarkar on 4/10/2016.
 */
public class TimeStamp {
    public String timestamp;
    private final Context mContext;

    public TimeStamp(Context mContext) {
        this.mContext = mContext;
        getTimestamp();
    }

    public String getTimestamp() {
        Calendar c1 = Calendar.getInstance();
        SimpleDateFormat simpledateformat = new SimpleDateFormat("d/M/yy h:m:s a");
        timestamp = simpledateformat.format(c1.getTime());
        Log.d("Time Stamp..........", timestamp);
        return timestamp;
    }

    public void setTimestamp(String timestamp){

        this.timestamp = timestamp;
    }


}
