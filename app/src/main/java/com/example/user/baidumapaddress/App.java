package com.example.user.baidumapaddress;

import android.app.Application;
import android.content.Context;
import android.content.MutableContextWrapper;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by zitan on 2018/4/13.
 */

public class App extends Application {
    private static Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext=this;
    }
    public static Context getContext(){
        return mContext;
    }
}
