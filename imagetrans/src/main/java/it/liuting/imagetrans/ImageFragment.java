package it.liuting.imagetrans;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.UUID;

import it.liuting.imagetrans.listener.OnTransformListener;


/**
 * Created by liuting on 18/3/14.
 */

public class ImageFragment extends Fragment implements TransformAttacher.TransStateChangeListener {

    public static ImageFragment newInstance(int pos, String url, ImageTransBuild build) {
        ImageFragment fragment = new ImageFragment();
        fragment.url = url;
        fragment.build = build;
        fragment.pos = pos;
        return fragment;
    }

    private String url;

    private TransImageView imageView;
    private ImageTransBuild build;
    private int pos;
    private OnTransformListener transformOpenListener;
    private String uniqueStr;
    private View progressBar;
    private boolean transOpenEnd;
    private boolean loadFinish = false;
    private boolean isCached = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        uniqueStr = UUID.randomUUID().toString();
        View view = inflater.inflate(R.layout.image_fragment, container, false);
        progressBar = build.inflateProgress(getContext(), (FrameLayout) view);
        imageView = (TransImageView) view.findViewById(R.id.imageView);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hideProgress();
        final boolean need = build.needTransOpen(pos, true);
        isCached = build.imageLoad.isCached(url);
        imageView.settingConfig(build.itConfig, new ThumbConfig(build.sourceImageViewGet.getImageView(pos), getResources(), build.scaleType));
        imageView.setImageTransAdapter(build.imageTransAdapter);
        imageView.setTransStateChangeListener(this);
        final boolean needShowThumb = !build.itConfig.noThumb && !(build.itConfig.noThumbWhenCached && build.imageLoad.isCached(url));
        if (needShowThumb) {
            imageView.showThumb(need);
        }
        loadImage(need || needShowThumb);
    }

    void loadImage(final boolean needTrans) {
        build.imageLoad.loadImage(url, new ImageLoad.LoadCallback() {
            @Override
            public void progress(float progress) {
                if (getUserVisibleHint() && transOpenEnd) {
                    if (progressBar != null)
                        build.imageTransAdapter.onProgressChange(progressBar, progress);
                }
            }

            @Override
            public void loadFinish(Drawable drawable) {
                hideProgress();
                loadFinish = true;
                imageView.showImage(drawable, needTrans && getUserVisibleHint());
            }
        }, imageView, uniqueStr);
    }

    public void runClose() {
        imageView.showCloseTransform();
    }

    public void bindTransOpenListener(OnTransformListener listener) {
        this.transformOpenListener = listener;
    }

    private void showProgress() {
        if (progressBar != null && !loadFinish) progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onChange(TransformAttacher.TransState state) {
        switch (state) {
            case OPEN_TO_THUMB:
            case OPEN_TO_ORI:
                if (transformOpenListener != null) transformOpenListener.transformStart();
                break;
            case THUMB:
            case ORI:
                if (!transOpenEnd) {
                    transOpenEnd = true;
                    if (!isCached)
                        showProgress();
                    if (transformOpenListener != null) transformOpenListener.transformEnd();
                }
                break;
            case THUMB_TO_CLOSE:
            case ORI_TO_CLOSE:
                build.imageTransAdapter.onCloseTransStart();
                break;
            case CLOSEED:
                build.imageTransAdapter.onCloseTransEnd();
                build.dialogInterface.dismiss();
                break;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && transOpenEnd) {
            showProgress();
        } else {
            hideProgress();
        }
    }

    @Override
    public void onDestroy() {
        build.imageLoad.cancel(url, uniqueStr);
        super.onDestroy();
    }
}
