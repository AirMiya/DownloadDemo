package com.miya.downloadtest.global;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * 自定义的sp管理器。
 * Created by air on 2016/11/1.
 */
public class SpManager {
    private static final String PREFERENCES_NAME = "air";
    private SharedPreferences sharedPreferences;

    public SpManager(Application application){
        sharedPreferences = application.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    //TODO 在这里可以做一些持久化数据的方法。

}
