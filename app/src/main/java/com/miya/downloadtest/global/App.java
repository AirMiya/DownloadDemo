package com.miya.downloadtest.global;

import android.app.Application;

import com.miya.downloadtest.dagger.componet.AppComponent;
import com.miya.downloadtest.dagger.componet.DaggerAppComponent;
import com.miya.downloadtest.dagger.module.AppModule;


public class App extends Application {
    /**
     * 全局AppComponent，用来连接module和presenter和需要注入的application，activity，fragment等
     */
    private AppComponent appComponent;

    /**
     * 自己的单例对象
     */
    private static App instance;

    /**
     * 得到自己的单例对象
     * @return
     */
    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
       appComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
    }

    /**
     * 得到全局appComponent
     * @return
     */
    public AppComponent getAppComponent(){
        return appComponent;
    }
}
