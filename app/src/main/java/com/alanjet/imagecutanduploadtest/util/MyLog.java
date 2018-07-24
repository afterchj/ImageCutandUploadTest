package com.alanjet.imagecutanduploadtest.util;

import android.util.Log;

/**
 * Created by hongjian.chen on 2018/6/13.
 */

public class MyLog {
    private static boolean debug =true;
    private static String TAG= "MESSAGE";
    private MyLog() {
        super();
    }


    public static void d(String tag,String msg){
        if(debug){
            Log.d(tag,msg);
        }
    }

    public static void d(String msg){
        if(debug){
            Log.d(TAG,msg);
        }
    }
}
