package it.liuting.imagetrans;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import it.liuting.imagetrans.listener.ImageLoad;


/**
 * Created by liuting on 17/6/1.
 * image 页面的渲染类
 */

public class ImageRenderAdapter implements ImageLoad.LoadCallback {
    private String imageUrl;
    private TransImageView imageView;
    private RingLoadingView loadingView;
    private int position;
    private boolean hasPreview = false;

    public ImageRenderAdapter(String image) {
        this.imageUrl = image;
    }

    public void renderView(FrameLayout parent, int position, TransImageView.OnCloseListener listener) {
        this.position = position;
        imageView = new TransImageView(parent.getContext());
        loadingView = new RingLoadingView(parent.getContext());
        parent.addView(imageView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(Util.dpToPx(48, parent.getContext()), Util.dpToPx(48, parent.getContext()));
        layoutParams.gravity = Gravity.CENTER;
        parent.addView(loadingView, layoutParams);
        imageView.setImageConfig(StaticParam.getImageConfig(position));
        loadingView.setVisibility(View.GONE);
        if (!StaticParam.isCached(imageUrl)) {
            //有预览图
            hasPreview = true;
            loadingView.setVisibility(View.VISIBLE);
            if (StaticParam.isClickIndex(position) && StaticParam.isFirstCheckClickIndex()) {
                imageView.startPreviewWithTransform();
            } else {
                imageView.startPreView();
            }
        } else {
            if (StaticParam.isClickIndex(position) && StaticParam.isFirstCheckClickIndex()) {
                hasPreview = true;
            } else {
                imageView.noAlpha();
            }
        }
        imageView.setOnCloseListener(listener);
        StaticParam.loadImage(imageUrl, ImageRenderAdapter.this, imageView);
    }

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
        if (StaticParam.isNowIndex(position) && hasPreview)
            imageView.setImageWithTransform(drawable);
        else imageView.setImageWithOutTransform(drawable);
    }

    public void destroy() {

    }
}
