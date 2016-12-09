package com.miya.downloadtest.model.net.bean;

import rx.Observable;
import rx.functions.Func1;


public class BaseResponseFunc1<T> implements Func1<BaseResponse<T>,Observable<T>> {

    @Override
    public Observable<T> call(BaseResponse<T> tBaseResponse) {
        //模拟服务器请求码为1和5时错误
        if (tBaseResponse.getCode() == 1){
            return Observable.error(new Throwable("请求参数有误"));
        }else if (tBaseResponse.getCode() == 5){
            return Observable.error(new Throwable("登陆失败，用户名或密码有误"));
        }else {
            return Observable.just(tBaseResponse.getData());
        }
    }

}
