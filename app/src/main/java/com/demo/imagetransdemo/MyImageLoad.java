package com.demo.imagetransdemo;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.demo.imagetransdemo.imageload.OkHttpImageLoad;

import java.util.regex.Pattern;

import it.liuting.imagetrans.ImageLoad;

/**
 * Created by liuting on 17/6/1.
 */

public class MyImageLoad implements ImageLoad {
    private static final Pattern webPattern = Pattern.compile("http[s]*://[[[^/:]&&[a-zA-Z_0-9]]\\.]+(:\\d+)?(/[a-zA-Z_0-9]+)*(/[a-zA-Z_0-9]*([a-zA-Z_0-9]+\\.[a-zA-Z_0-9]+)*)?(\\?(&?[a-zA-Z_0-9]+=[%[a-zA-Z_0-9]-]*)*)*(#[[a-zA-Z_0-9]|-]+)?(.jpg|.png|.gif|.jpeg)?");
    private static final String ASSET_PATH_SEGMENT = "android_asset";

    @Override
    public void loadImage(final String url, final LoadCallback callback, final ImageView imageView) {
        Uri uri = Uri.parse(url);
        if (isLocalUri(uri.getScheme())) {
            if (isAssetUri(uri)) {
                //是asset资源文件

                return;
            } else {
                //是本地文件
                loadImageFromLocal(uri.getPath(), callback, imageView);
                return;
            }
        } else {
            if (isNetUri(url)) {
                loadImageFromNet(url, callback, imageView);
                return;
            }
            Log.e("MyImageLoad", "未知的图片URL的类型");
        }
    }

    /**
     * 从网络加载图片
     */
    private void loadImageFromNet(String url, final LoadCallback callback, final ImageView imageView) {

        OkHttpImageLoad.get(url).url(url).listener(new OkHttpImageLoad.ImageDownLoadListener() {
            @Override
            public void inProgress(float progress, long total) {
                callback.progress(progress);
            }

            @Override
            public void onError(Exception e) {

            }

            @Override
            public void onSuccess(String path) {
                loadImageFromLocal(path, callback, imageView);
            }

            @Override
            public void onCancel() {

            }

        }).execute();
    }

    /**
     * 从本地加载图片
     */
    private void loadImageFromLocal(String url, final LoadCallback callback, final ImageView imageView) {
        TileBitmapDrawable.attachTileBitmapDrawable(imageView, url, new TileBitmapDrawable.OnLoadListener() {
            @Override
            public void onLoadFinish(TileBitmapDrawable drawable) {
                callback.loadFinish(drawable);
            }

            @Override
            public void onError(Exception ex) {

            }
        });
    }


    @Override
    public boolean isCache(String url) {
        if (isLocalUri(Uri.parse(url).getScheme())) {
            //是本地图片不用预览图
            return true;
        }
        return OkHttpImageLoad.isCached(url);
    }

    @Override
    public void destroy() {
        TileBitmapDrawable.clearCache();
    }

    private static boolean isNetUri(String url) {
        return webPattern.matcher(url).find();
    }

    private static boolean isLocalUri(String scheme) {
        return ContentResolver.SCHEME_FILE.equals(scheme)
                || ContentResolver.SCHEME_CONTENT.equals(scheme)
                || ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme);
    }

    public static boolean isAssetUri(Uri uri) {
        return ContentResolver.SCHEME_FILE.equals(uri.getScheme()) && !uri.getPathSegments().isEmpty()
                && ASSET_PATH_SEGMENT.equals(uri.getPathSegments().get(0));
    }

}
