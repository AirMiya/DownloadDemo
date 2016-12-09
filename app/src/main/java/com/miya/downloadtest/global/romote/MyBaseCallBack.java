package com.miya.downloadtest.global.romote;


public interface MyBaseCallBack<T> {
    /**
     * 开始请求网络时
     */
    void onStart();

    /**
     * 请求网络状态成功时做什么
     * @param t
     */
    void onNext(T t);

    /**
     * 不管请求网络成功还是失败都代表当前请求网络结束
     */
    void onComplete();
}
