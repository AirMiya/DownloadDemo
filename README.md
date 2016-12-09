## 前言
最近在学习Retrofit，虽然Retrofit没有提供文件下载进度的回调，但是Retrofit底层依赖的是OkHttp，实际上所需要的实现OkHttp对下载进度的监听，在OkHttp的官方Demo中，有一个Progress.java的文件，顾名思义。[点我查看](http://xn--progress-z09lrnp84u.xn--java%2C-9h1h2a84miko6si1h1sx9qcwxmq00azqfuy0f1gf636ks7b/)。
## 准备工作
本文采用Dagger2，Retrofit，RxJava。
```
compile'com.squareup.retrofit2:retrofit:2.0.2'
compile'com.squareup.retrofit2:converter-gson:2.0.2'
compile'com.squareup.retrofit2:adapter-rxjava:2.0.2'
//dagger2
compile'com.google.dagger:dagger:2.6'
apt'com.google.dagger:dagger-compiler:2.6'
//RxJava
compile'io.reactivex:rxandroid:1.2.0'
compile'io.reactivex:rxjava:1.1.5'
compile'com.jakewharton.rxbinding:rxbinding:0.4.0'
```

## 改造ResponseBody 
okHttp3默认的ResponseBody因为不知道进度的相关信息，所以需要对其进行改造。可以使用接口监听进度信息。这里采用的是RxBus发送FileLoadEvent对象实现对下载进度的实时更新。这里先讲改造的ProgressResponseBody。
```
public class ProgressResponseBody extends ResponseBody {
    private ResponseBody responseBody;

    private BufferedSource bufferedSource;
    public ProgressResponseBody(ResponseBody responseBody) {
        this.responseBody = responseBody;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long bytesReaded = 0;
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                bytesReaded += bytesRead == -1 ? 0 : bytesRead;
               //实时发送当前已读取的字节和总字节
                RxBus.getInstance().post(new FileLoadEvent(contentLength(), bytesReaded));
                return bytesRead;
            }
        };
    }
}
```
呃，OKIO相关知识我也正在学，这个是从官方Demo中copy的代码，只不过中间使用了RxBus实时发送FileLoadEvent对象。
## FileLoadEvent
FileLoadEvent很简单，包含了当前已加载进度和文件总大小。
```
public class FileLoadEvent {

    long total;
    long bytesLoaded;

    public long getBytesLoaded() {
        return bytesLoaded;
    }

    public long getTotal() {
        return total;
    }

    public FileLoadEvent(long total, long bytesLoaded) {
        this.total = total;
        this.bytesLoaded = bytesLoaded;
    }
}
```
### RxBus
RxBus 名字看起来像一个库，但它并不是一个库，而是一种模式，它的思想是使用 RxJava 来实现了 EventBus ，而让你不再需要使用OTTO或者 EventBus。[点我查看详情](http://www.jianshu.com/p/ca090f6e2fe2)。
```
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

```
## FileCallBack
那么，重点来了。代码其实有5个方法需要重写，好吧，其实这些方法可以精简一下。其中progress()方法有两个参数，progress和total,分别表示文件已下载的大小和总大小，我们将这两个参数不断更新到UI上就行了。
```
public abstract class FileCallBack<T> {

    private String destFileDir;
    private String destFileName;

    public FileCallBack(String destFileDir, String destFileName) {
        this.destFileDir = destFileDir;
        this.destFileName = destFileName;
        subscribeLoadProgress();
    }

    public abstract void onSuccess(T t);

    public abstract void progress(long progress, long total);

    public abstract void onStart();

    public abstract void onCompleted();

    public abstract void onError(Throwable e);

    public void saveFile(ResponseBody body) {
        InputStream is = null;
        byte[] buf = new byte[2048];
        int len;
        FileOutputStream fos = null;
        try {
            is = body.byteStream();
            File dir = new File(destFileDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, destFileName);
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            fos.flush();
            unsubscribe();
            //onCompleted();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
                if (fos != null) fos.close();
            } catch (IOException e) {
                Log.e("saveFile", e.getMessage());
            }
        }
    }

    /**
     * 订阅加载的进度条
     */
    public void subscribeLoadProgress() {
        Subscription subscription = RxBus.getInstance().doSubscribe(FileLoadEvent.class, new Action1<FileLoadEvent>() {
            @Override
            public void call(FileLoadEvent fileLoadEvent) {
                progress(fileLoadEvent.getBytesLoaded(),fileLoadEvent.getTotal());
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                //TODO 对异常的处理
            }
        });
        RxBus.getInstance().addSubscription(this, subscription);
    }

    /**
     * 取消订阅，防止内存泄漏
     */
    public void unsubscribe() {
        RxBus.getInstance().unSubscribe(this);
    }

}
```


## 开始下载
### 使用自己的ProgressResponseBody
通过OkHttpClient的拦截器去拦截Response，并将我们的ProgressReponseBody设置进去监听进度。
```
public class ProgressInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());
        return originalResponse.newBuilder()
                .body(new ProgressResponseBody(originalResponse.body()))
                .build();
    }
}
```
### 构建Retrofit
```
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
```
### 请求接口
```
public interface ApiInfo {
    @Streaming
    @GET
    Observable<ResponseBody> download(@Url String url);
}
```
### 执行请求
```
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
```
### 在presenter层中执行网络请求。
通过V层依赖注入的presenter对象调用请求网络，请求网络后调用V层更新UI的操作。
```
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
```
## 踩到的坑。
* 依赖的Retrofit版本一定要保持一致！！！说多了都是泪啊。
* 保存文件时要使用RxJava的doOnNext操作符，后续更新UI的操作切换到UI线程。
## 总结
看似代码很多，其实过程并不复杂：

* 在保存文件时，调用ForwardingSource的read方法，通过RxBus发送实时的FileLoadEvent对象。
* FileCallBack订阅RxBus发送的FileLoadEvent。通过接收到FileLoadEvent中的下载进度和文件总大小对UI进行更新。
* 在下载保存文件完成后，取消订阅，防止内存泄漏。