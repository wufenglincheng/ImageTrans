package it.liuting.imagetrans;

import android.view.View;
import android.widget.ImageView;

import java.util.List;

import it.liuting.imagetrans.listener.ImageLoad;
import it.liuting.imagetrans.listener.SourceImageViewParam;

/**
 * Created by liuting on 17/5/27.
 */

public class StaticParam {
    public static int clickIndex;
    public static int nowIndex;
    public static List<String> imageList;
    public static ImageLoad imageLoad;
    public static SourceImageViewParam sourceImageViewParam;
    public static boolean firstCheckClickIndex;

    public static void init(){
        firstCheckClickIndex = true;
    }

    /**
     * 是否是点击
     *
     * @param position
     * @return
     */
    public static boolean isClickIndex(int position) {
        return clickIndex == position;
    }

    public static boolean isNowIndex(int position) {
        return nowIndex == position;
    }

    public static void setNowIndex(int pos) {
        nowIndex = pos;
    }

    /**
     * {@link ImageLoad#loadImage(String, ImageLoad.LoadCallback, ImageView)}
     *
     * @param url
     * @param callback
     * @param imageView
     */
    public static void loadImage(String url, ImageLoad.LoadCallback callback, ImageView imageView) {
        if (imageLoad == null) throw new NullPointerException("not set imageLoad");
        imageLoad.loadImage(url, callback, imageView);
    }

    /**
     * {@link ImageLoad#isCache(String)}
     *
     * @param url
     * @return
     */
    public static boolean isCached(String url) {
        if (imageLoad == null) throw new NullPointerException("not set imageLoad");
        return imageLoad.isCache(url);
    }

    /**
     * {@link ImageLoad#destroy()}
     */
    public static void destroy() {
        if (imageLoad == null) throw new NullPointerException("not set imageLoad");
        imageLoad.destroy();
    }

    /**
     * 得到当前图片的原始 view 的相关参数
     *
     * @param position
     * @return
     */
    public static ImageConfig getImageConfig(int position) {
        if (sourceImageViewParam == null)
            throw new NullPointerException("not set SourceImageViewParam");
        View view = sourceImageViewParam.getSourceView(position);
        ImageConfig imageConfig = new ImageConfig();
        imageConfig.setView(view);
        imageConfig.setScaleType(sourceImageViewParam.getScaleType(position));
        return imageConfig;
    }

    public static boolean isFirstCheckClickIndex() {
        boolean temp = firstCheckClickIndex;
        if (firstCheckClickIndex) firstCheckClickIndex = false;
        return temp;
    }
}
