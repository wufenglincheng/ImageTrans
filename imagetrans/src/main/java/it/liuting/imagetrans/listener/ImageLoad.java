package it.liuting.imagetrans.listener;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Created by liuting on 17/6/1.
 * 加载图片的接口 为了自定义自己的图片加载器
 */

public interface ImageLoad {

    /**
     * 加载图片
     *
     * @param url       图片的url
     * @param callback  回调 {@link LoadCallback}
     * @param imageView 加载图片的imageView
     */
    void loadImage(String url, LoadCallback callback, ImageView imageView);

    /**
     * 判断当前图片是否有本地缓存，用来判断是否显示缩略图
     *
     * @param url 图片地址
     * @return
     */
    boolean isCache(String url);

    /**
     * 用来通知图片预览界面销毁
     */
    void destroy();

    void cancel(String url);

    /**
     * 图片加载器中用来回传下载好的图片
     */
    interface LoadCallback {
        /**
         * 进度
         *
         * @param progress
         */
        void progress(float progress);

        void loadFinish(Bitmap bitmap);

        void loadFinish(Drawable drawable);
    }
}
