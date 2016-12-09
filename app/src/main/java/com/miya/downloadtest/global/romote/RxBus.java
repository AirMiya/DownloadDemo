package com.miya.downloadtest.global.romote;

import java.util.HashMap;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subscriptions.CompositeSubscription;

public class RxBus {

    private static volatile RxBus mInstance;
    private SerializedSubject<Object, Object> mSubject;
    private HashMap<String, CompositeSubscription> mSubscriptionMap;

    /**
     *  PublishSubject只会把在订阅发生的时间点之后来自原始Observable的数据发射给观察者
     *  Subject同时充当了Observer和Observable的角色，Subject是非线程安全的，要避免该问题，
     *  需要将 Subject转换为一个 SerializedSubject ，上述RxBus类中把线程非安全的PublishSubject包装成线程安全的Subject。
     */
    private RxBus() {
        mSubject = new SerializedSubject<>(PublishSubject.create());
    }

    /**
     * 单例 双重锁
     * @return
     */
    public static RxBus getInstance() {
        if (mInstance == null) {
            synchronized (RxBus.class) {
                if (mInstance == null) {
                    mInstance = new RxBus();
                }
            }
        }
        return mInstance;
    }

    /**
     * 发送一个新的事件
     * @param o
     */
    public void post(Object o) {
        mSubject.onNext(o);
    }

    /**
     * 根据传递的 eventType 类型返回特定类型(eventType)的 被观察者
     * @param type
     * @param <T>
     * @return
     */
    public <T> Observable<T> tObservable(final Class<T> type) {
        //ofType操作符只发射指定类型的数据，其内部就是filter+cast
        return mSubject.ofType(type);
    }

    public <T> Subscription doSubscribe(Class<T> type, Action1<T> next, Action1<Throwable> error) {
        return tObservable(type)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(next, error);
    }

    public void addSubscription(Object o, Subscription subscription) {

        if (mSubscriptionMap == null) {
            mSubscriptionMap = new HashMap<>();
        }
        String key = o.getClass().getName();
        if (mSubscriptionMap.get(key) != null) {
            mSubscriptionMap.get(key).add(subscription);
        } else {
            CompositeSubscription compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(subscription);
            mSubscriptionMap.put(key, compositeSubscription);
          //  Log.e("air", "addSubscription:订阅成功 " );
        }
    }

    public void unSubscribe(Object o) {
        if (mSubscriptionMap == null) {
            return;
        }
        String key = o.getClass().getName();
        if (!mSubscriptionMap.containsKey(key)) {
            return;
        }
        if (mSubscriptionMap.get(key) != null) {
            mSubscriptionMap.get(key).unsubscribe();
        }
        mSubscriptionMap.remove(key);
        //Log.e("air", "unSubscribe: 取消订阅" );
    }

}
