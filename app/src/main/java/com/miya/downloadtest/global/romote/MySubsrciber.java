package com.miya.downloadtest.global.romote;

import android.app.Application;
import android.widget.Toast;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import rx.Subscriber;

/**
 * 自定义订阅者.观察者
 * 泛型为服务器返回的数据封装后类的类型
 * Created by air on 2016/11/1.
 */
public class MySubsrciber<T> extends Subscriber<T> {

    private Application application;
    /**
     * 简单回调接口
     */
    private MyBaseCallBack<T> myBaseCallBack;

    public MySubsrciber(Application application, MyBaseCallBack<T> myBaseCallBack) {
        this.application = application;
        this.myBaseCallBack = myBaseCallBack;
    }

    /**
     * 观察者开始时的回调。调用简单回调开始的方法
     */
    @Override
    public void onStart() {
        super.onStart();
        if(myBaseCallBack != null)
            myBaseCallBack.onStart();
    }

    /**
     * 观察者完成时的回调，调用简单回调完成时的方法
     */
    @Override
    public void onCompleted() {
        if (myBaseCallBack != null)
            myBaseCallBack.onComplete();

        // 数据完成后取消订阅。释放资源，避免内存泄漏.Subscriber内部在onNext和onError后会自动取消订阅
    }

    /**
     * 观察者错误时的回调方法，调用简单回调完成时的方法。因为不管错误还是成功，都代表当前事件已经执行完毕了
     * @param e 错误类型
     */
    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
        if (e instanceof SocketTimeoutException) {
            Toast.makeText(application, "网络中断，请检查您的网络状态", Toast.LENGTH_SHORT).show();
        } else if (e instanceof ConnectException) {
            Toast.makeText(application, "网络中断，请检查您的网络状态", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(application, "error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (myBaseCallBack != null)
            myBaseCallBack.onComplete();
    }

    /**
     * 观察者普通事件的回调方法。调用简单回调下一步的方法
     * @param t
     */
    @Override
    public void onNext(T t) {
        if (myBaseCallBack != null)
            myBaseCallBack.onNext(t);
    }
}
