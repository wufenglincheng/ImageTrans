package it.liuting.imagetrans;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import it.liuting.imagetrans.listener.ImageLoad;
import it.liuting.imagetrans.listener.SimpleTransformListener;


/**
 * Created by liuting on 17/6/1.
 * image 页面的渲染类
 */

public class ImageRenderAdapter extends SimpleTransformListener implements ImageLoad.LoadCallback {
    private String imageUrl;
    private TransImageView imageView;
    private RingLoadingView loadingView;
    private int position;
    private boolean isRunImageTrans = false;
    private ImageTransParam mImageTransParam;

    public ImageRenderAdapter(String image, ImageTransParam imageTransParam) {
        this.imageUrl = image;
        this.mImageTransParam = imageTransParam;
    }

    public void renderView(FrameLayout parent, int position, TransImageView.OnCloseListener listener) {
        this.position = position;
        //添加imageView
        imageView = new TransImageView(parent.getContext());
        parent.addView(imageView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        //添加 loading view
        loadingView = new RingLoadingView(parent.getContext());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(Util.dpToPx(48, parent.getContext()), Util.dpToPx(48, parent.getContext()));
        layoutParams.gravity = Gravity.CENTER;
        loadingView.setVisibility(View.GONE);
        parent.addView(loadingView, layoutParams);
        //初始化imageview参数
        imageView.setOnCloseListener(listener);
        imageView.setOpenTransformListener(this);
        imageView.setCloseTransformListener(this);
        if (mImageTransParam.hasAdapter()) {
            imageView.setImageTransAdapter(mImageTransParam.getAdapter());
            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mImageTransParam.getAdapter().onImageLongClick();
                    return false;
                }
            });
        }

        imageView.setImageConfig(mImageTransParam.getImageConfig(position));
        if (!mImageTransParam.isCached(imageUrl)) {
            //有预览图
            isRunImageTrans = true;
            loadingView.setVisibility(View.VISIBLE);
            if (mImageTransParam.isFirstStartOfIndex(position)) {
                imageView.showThumbWithTransform();
            } else {
                imageView.showThumb();
            }
        } else {
            if (mImageTransParam.isFirstStartOfIndex(position)) {
                isRunImageTrans = true;
            } else {
                imageView.setBackgroundAlpha(255);
            }
        }
        mImageTransParam.loadImage(imageUrl, ImageRenderAdapter.this, imageView);
    }

    /**
     * 执行关闭动画
     */
    public void runClose() {
        imageView.onClose();
    }

    @Override
    public void progress(float progress) {
        if (loadingView.getVisibility() == View.GONE) {
            loadingView.setVisibility(View.VISIBLE);
        }
        loadingView.setProgress(progress);
    }

    @Override
    public void loadFinish(Bitmap bitmap) {
        Drawable drawable = new BitmapDrawable(imageView.getResources(), bitmap);
        loadFinish(drawable);
    }

    @Override
    public void loadFinish(final Drawable drawable) {
        loadingView.setVisibility(View.GONE);
        if (mImageTransParam.isNowIndex(position) && isRunImageTrans)
            imageView.setImageWithTransform(drawable);
        else imageView.setImage(drawable);
    }

    public void destroy() {
        mImageTransParam.cancel(imageUrl);
    }

    @Override
    public void transformEnd() {
        mImageTransParam.showAttachView(position);
    }

    @Override
    public void transformStart() {
        mImageTransParam.startDismiss();
    }
}
