package it.liuting.imagetrans;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import java.util.List;

import it.liuting.imagetrans.listener.ImageLoad;
import it.liuting.imagetrans.listener.ImageTransAdapter;
import it.liuting.imagetrans.listener.SourceImageViewParam;

/**
 * Created by liuting on 17/6/14.
 */

class ImageTransParam {

    protected int screenWidth;
    protected int screenHeight;
    protected int clickIndex;
    protected int nowIndex;
    protected List<String> imageList;
    protected ImageLoad imageLoad;
    protected SourceImageViewParam sourceImageViewParam;
    protected ImageTransAdapter imageTransAdapter;
    protected boolean firstCheckClickIndex;
    protected boolean alreadyShowAttachView;

    protected ImageTransParam(Context context) {
        firstCheckClickIndex = true;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
    }

    /**
     * 检查参数是否完全
     */
    protected void checkParam() {
        if (sourceImageViewParam == null)
            throw new NullPointerException("not set SourceImageViewParam");
        if (imageLoad == null)
            throw new NullPointerException("not set ImageLoad");
        if (imageList == null)
            throw new NullPointerException("not set ImageList");
    }

    /**
     * 是否是第一次打开动画的index
     *
     * @param position
     * @return
     */
    protected boolean isFirstStartOfIndex(int position) {
        if (clickIndex == position) {
            boolean temp = firstCheckClickIndex;
            if (firstCheckClickIndex) firstCheckClickIndex = false;
            return true && temp;
        }
        return false;
    }

    protected boolean isNowIndex(int position) {
        return nowIndex == position;
    }

    protected void setNowIndex(int pos) {
        nowIndex = pos;
    }

    /**
     * {@link ImageLoad#loadImage(String, ImageLoad.LoadCallback, ImageView)}
     *
     * @param url
     * @param callback
     * @param imageView
     */
    protected void loadImage(String url, ImageLoad.LoadCallback callback, ImageView imageView) {
        imageLoad.loadImage(url, callback, imageView);
    }

    /**
     * {@link ImageLoad#isCache(String)}
     *
     * @param url
     * @return
     */
    protected boolean isCached(String url) {
        return imageLoad.isCache(url);
    }

    protected void cancel(String url) {
        imageLoad.cancel(url);
    }

    /**
     * {@link ImageLoad#destroy()}
     */
    protected void destroy() {
        imageLoad.destroy();
    }

    /**
     * 得到当前图片的原始 view 的相关参数
     *
     * @param position
     * @return
     */
    protected ImageConfig getImageConfig(int position) {
        View view = sourceImageViewParam.getSourceView(position);
        ImageConfig imageConfig = new ImageConfig();
        imageConfig.setView(view, this);
        imageConfig.setScaleType(sourceImageViewParam.getScaleType(position));
        return imageConfig;
    }

    protected ImageTransAdapter getAdapter() {
        return imageTransAdapter;
    }

    public void showAttachView(int position) {
        if (clickIndex == position) {
            if (!alreadyShowAttachView && hasAdapter()) {
                alreadyShowAttachView = true;
                imageTransAdapter.onShow();
            }
        }
    }

    public void startDismiss() {
        if (alreadyShowAttachView && hasAdapter()) {
            alreadyShowAttachView = false;
            imageTransAdapter.onDismiss();
        }
    }

    public boolean hasAdapter() {
        return imageTransAdapter != null;
    }
}
