package com.miya.downloadtest.dagger.module;

import android.app.Application;

import com.miya.downloadtest.global.SpManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module
public class AppModule {

    private Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    /**
     * 提供application的实例
     * @return
     */
    @Provides
    @Singleton
    public Application provideApplication() {
        return application;
    }

    @Provides
    @Singleton
    public SpManager provideSpManager(){
        return new SpManager(application);
    }
}
