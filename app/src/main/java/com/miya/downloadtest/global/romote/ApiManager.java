package com.miya.downloadtest.global.romote;


import android.app.Application;

import com.miya.downloadtest.global.romote.download.FileCallBack;
import com.miya.downloadtest.global.romote.download.FileSubscriber;
import com.miya.downloadtest.model.net.api.ApiInfo;

import okhttp3.ResponseBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class ApiManager {
    private Application application;
    private ApiInfo apiInfo;

    public ApiManager(Application application, ApiInfo apiInfo) {
        this.application = application;
        this.apiInfo = apiInfo;
    }

    public void load(String url, final FileCallBack<ResponseBody> callBack){
        apiInfo.download(url)
                .subscribeOn(Schedulers.io())//请求网络 在调度者的io线程
                .observeOn(Schedulers.io()) //指定线程保存文件
                .doOnNext(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody body) {
                        callBack.saveFile(body);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread()) //在主线程中更新ui
                .subscribe(new FileSubscriber<ResponseBody>(application,callBack));
    }

}
