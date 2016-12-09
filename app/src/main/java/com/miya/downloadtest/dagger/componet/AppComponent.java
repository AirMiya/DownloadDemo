package com.miya.downloadtest.dagger.componet;


import com.miya.downloadtest.dagger.module.ApiModule;
import com.miya.downloadtest.dagger.module.AppModule;
import com.miya.downloadtest.dagger.module.MainActivityModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, ApiModule.class})
public interface AppComponent {
    MainActivityComponent connect(MainActivityModule mainActivityModule);
}
