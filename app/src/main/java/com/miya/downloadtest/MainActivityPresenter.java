package com.miya.downloadtest;


import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.miya.downloadtest.global.App;
import com.miya.downloadtest.global.SpManager;
import com.miya.downloadtest.global.romote.ApiManager;
import com.miya.downloadtest.global.romote.download.FileCallBack;

import okhttp3.ResponseBody;

/**
 * 主页页面的presenter 用于处理服务器返回的逻辑
 * Created by air on 2016/11/1.
 */
public class MainActivityPresenter {
    private static final String TAG = "MainActivityPresenter";
    private SpManager spManager;
    private ApiManager apiManager;
    private IHomeView iHomeView;

    public MainActivityPresenter(SpManager spManager, ApiManager apiManager, IHomeView iHomeView) {
        this.spManager = spManager;
        this.apiManager = apiManager;
        this.iHomeView = iHomeView;
    }

    public void load(String url){
        String fileName = "app.apk";
        String fileStoreDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.e(TAG, "load: "+fileStoreDir.toString() );
        FileCallBack<ResponseBody> callBack = new FileCallBack<ResponseBody>(fileStoreDir,fileName) {

            @Override
            public void onSuccess(final ResponseBody responseBody) {
                Toast.makeText(App.getInstance(),"下载文件成功",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void progress(long progress, long total) {
                iHomeView.update(total,progress);
            }

            @Override
            public void onStart() {
                iHomeView.showLoading();
            }

            @Override
            public void onCompleted() {
                iHomeView.hideLoading();
            }

            @Override
            public void onError(Throwable e) {
                //TODO: 对异常的一些处理
                e.printStackTrace();
            }
        };
        apiManager.load(url, callBack);
    }
}
