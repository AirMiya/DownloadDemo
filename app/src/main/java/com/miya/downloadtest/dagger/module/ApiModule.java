package com.miya.downloadtest.dagger.module;

import android.app.Application;

import com.miya.downloadtest.global.Constant;
import com.miya.downloadtest.global.romote.ApiManager;
import com.miya.downloadtest.global.romote.download.ProgressInterceptor;
import com.miya.downloadtest.model.net.api.ApiInfo;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


@Module
public class ApiModule {

    @Provides
    @Singleton
    public OkHttpClient provideClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new ProgressInterceptor())
                .build();
        return client;
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient client){
        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(Constant.HOST)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit;
    }

    @Provides
    @Singleton
    public ApiInfo provideApiInfo(Retrofit retrofit){
        return retrofit.create(ApiInfo.class);
    }

    @Provides
    @Singleton
    public ApiManager provideApiManager(Application application, ApiInfo apiInfo){
        return new ApiManager(application,apiInfo);
    }

}
