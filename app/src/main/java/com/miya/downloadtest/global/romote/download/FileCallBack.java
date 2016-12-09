package com.miya.downloadtest.global.romote.download;

import android.util.Log;

import com.miya.downloadtest.global.romote.RxBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;
import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by miya95 on 2016/12/5.
 */
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
