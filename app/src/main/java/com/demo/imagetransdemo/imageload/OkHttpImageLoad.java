package com.demo.imagetransdemo.imageload;

import android.text.TextUtils;

import com.demo.imagetransdemo.BuildConfig;
import com.demo.imagetransdemo.MyApplication;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Executor;


/**
 * Created by liuting on 16/8/26.
 */
public class OkHttpImageLoad {

    private Platform mPlatform;

    private OkHttpClient mOkHttpClient;

    private volatile static OkHttpImageLoad mInstance;
    private HashMap<String, Builder> map = new LinkedHashMap<>();

    public static Builder get(String url) {
        if (mInstance == null) {
            mInstance = new OkHttpImageLoad();
        }
        String key = url;
        Builder builder = mInstance.map.get(key);
        if (builder == null) {
            builder = new Builder(key);
            mInstance.map.put(key, builder);
        }
        return builder;
    }

    private OkHttpImageLoad() {
        mOkHttpClient = new OkHttpClient();
        mPlatform = Platform.get();
    }

    private Executor getDelivery() {
        return mPlatform.defaultCallbackExecutor();
    }

    public static void cancel(String key) {
        if (null == mInstance) {
            return;
        }
        Builder builder = mInstance.map.get(key);
        if (null != builder) {
            builder.cancel();
            builder.removeAllListener();
            try {
                mInstance.map.remove(key);
            } catch (Throwable e) {

            }
        }
    }

    public static class Builder {
        protected Request.Builder builder = new Request.Builder();
        protected String url;
        private Request request;
        private Call call;
        private List<ImageDownLoadListener> imageDownLoadListener = new ArrayList<>();
        private boolean isSucess = false;
        private String key;

        public Builder(String key) {
            this.key = key;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder listener(ImageDownLoadListener listener) {
            imageDownLoadListener.add(listener);
            return this;
        }

        public boolean build() {
            if (request != null && call != null) return false;
            request = builder.url(url).get().build();
            call = mInstance.mOkHttpClient.newCall(request);
            return true;
        }

        public void cancel() {
            if (null == call) {
                throw new NullPointerException(" cancel() must be called before calling build() ");
            }
            if (!isSucess) {
                //切换到非UI线程，进行网络的取消工作
                MyApplication.cThreadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        call.cancel();
                    }
                });

                if (null != imageDownLoadListener) {
                    for (ImageDownLoadListener listener : imageDownLoadListener)
                        listener.onCancel();
                }
            }

        }

        public void execute() {
            if (!build()) {
                return;
            }
            if (!TextUtils.isEmpty(url)) {
                if (isCached(url)) {
                    sendSuccessResultCallback();
                    return;
                }
            }
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, final IOException e) {
                    sendFailResultCallback(e);
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        if (call.isCanceled()) {
                            sendFailResultCallback(new IOException("Canceled!"));
                            return;
                        }
                        if (!response.isSuccessful()) {
                            sendFailResultCallback(new IOException("request failed , reponse's code is : " + response.code()));
                            return;
                        }
                        saveFile(response);
                        sendSuccessResultCallback();
                    } catch (Exception e) {
                        sendFailResultCallback(e);
                    } finally {
                        if (response.body() != null)
                            response.body().close();
                    }

                }
            });
        }


        private void saveFile(Response response) throws IOException {
            InputStream is = null;
            byte[] buf = new byte[2048];
            int len;
            FileOutputStream fos = null;
            try {
                is = response.body().byteStream();
                final long total = response.body().contentLength();

                long sum = 0;

                File dir = new File(MyApplication.getImageCachePath());
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                String destUrl = getCacheFileName(url);
                File file = new File(destUrl);
                fos = new FileOutputStream(file);
                while ((len = is.read(buf)) != -1) {
                    sum += len;
                    fos.write(buf, 0, len);
                    final long finalSum = sum;
                    mInstance.getDelivery().execute(new Runnable() {
                        @Override
                        public void run() {
                            if (null != imageDownLoadListener) {
                                for (ImageDownLoadListener listener : imageDownLoadListener)
                                    listener.inProgress(finalSum * 1.0f / total, total);
                            }
                        }
                    });
                }
                fos.flush();
            } finally {
                try {
                    response.body().close();
                    if (is != null) is.close();
                } catch (IOException e) {
                }
                try {
                    if (fos != null) fos.close();
                } catch (IOException e) {
                }

            }
        }


        public void sendFailResultCallback(final Exception e) {
            mInstance.mPlatform.execute(new Runnable() {
                @Override
                public void run() {
                    if (imageDownLoadListener == null) return;
                    for (ImageDownLoadListener listener : imageDownLoadListener)
                        listener.onError(e);
                }
            });
        }

        public void sendSuccessResultCallback() {
            isSucess = true;
            mInstance.map.remove(key);
            if (imageDownLoadListener == null) return;
            mInstance.mPlatform.execute(new Runnable() {
                @Override
                public void run() {
                    if (null != imageDownLoadListener) {
                        for (ImageDownLoadListener listener : imageDownLoadListener)
                            listener.onSuccess(getCacheFileName(url));
                    }
                }
            });
        }

        public void removeAllListener() {
            imageDownLoadListener = null;
        }
    }


    public interface ImageDownLoadListener {
        void inProgress(float progress, long total);

        void onError(Exception e);

        void onSuccess(String path);

        void onCancel();
    }

    public static String getCacheFileName(String url) {
        String key = MyApplication.generate(url);
        String destUrl = MyApplication.getImageCachePath() + "/" + key;
        return destUrl;
    }

    public static boolean isCached(String url) {
        String key = MyApplication.generate(url);
        String destUrl = MyApplication.getImageCachePath() + "/" + key;
        File file = new File(destUrl);
        if (file.exists()) {
            int size = MyApplication.getMaxSizeOfBitMap(destUrl);
            if (size > 0) {
                return true;
            } else {
                file.delete();
            }
        }
        return false;
    }
}
