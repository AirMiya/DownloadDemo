package com.miya.downloadtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import com.miya.downloadtest.dagger.module.MainActivityModule;
import com.miya.downloadtest.global.App;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity implements IHomeView{
    @Inject
    MainActivityPresenter presenter;
    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pb = (ProgressBar) findViewById(R.id.pb_test);
        App.getInstance().getAppComponent().connect(new MainActivityModule(this)).in(this);
        presenter.load("http://download.fir.im/v2/app/install/5818acbcca87a836f50014af?download_token=a01301d7f6f8f4957643c3fcfe5ba6ff");
    }

    @Override
    public void showLoading() {
        pb.setVisibility(View.VISIBLE);
        pb.setMax(100);
    }

    @Override
    public void hideLoading() {
        pb.setVisibility(View.GONE);
    }

    @Override
    public void update(long total, long loaded) {
        pb.setProgress((int) ((double)loaded/total*100));
    }

}
