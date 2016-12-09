package com.miya.downloadtest;

/**
 * Created by miya95 on 2016/12/5.
 */
public interface IHomeView {
    void showLoading();
    void hideLoading();
    void update(long total,long loaded);
}
