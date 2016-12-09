package com.miya.downloadtest.dagger.module;


import com.miya.downloadtest.IHomeView;
import com.miya.downloadtest.MainActivityPresenter;
import com.miya.downloadtest.global.SpManager;
import com.miya.downloadtest.global.romote.ApiManager;

import dagger.Module;
import dagger.Provides;


/**
 * 测试主页网络请求的类的Module类，用来提供Presenter实例的调用
 * Created by air on 2016/11/1.
 */
@Module
public class MainActivityModule {
    private IHomeView iHomeView;

    public MainActivityModule(IHomeView iHomeView) {
        this.iHomeView = iHomeView;
    }

    /**
     * 提供presenter实例
     * @param spManager sp的管理器，可以存储一些轻量级的数据
     * @param apiManager api的管理器，用来处理网络请求及其回调
     * @return
     */
    @Provides
    public MainActivityPresenter provideMainActivityPresenter(SpManager spManager, ApiManager apiManager){
        return new MainActivityPresenter(spManager,apiManager,iHomeView);
    }
}
