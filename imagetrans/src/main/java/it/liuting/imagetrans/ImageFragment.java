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

public class ImageFragment extends Fragment {

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        uniqueStr = UUID.randomUUID().toString();
        return inflater.inflate(R.layout.image_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar = build.inflateProgress(getContext(), (FrameLayout) view);
        final boolean need = build.needTransOpen(pos, true);
        imageView = (TransImageView) getView().findViewById(R.id.imageView);
        imageView.settingConfig(build.itConfig, new ThumbConfig(build.sourceImageViewGet.getImageView(pos), getResources(), build.scaleType));
        imageView.setImageTransAdapter(build.imageTransAdapter);
        imageView.setTransStateChangeListener(new TransformAttacher.TransStateChangeListener() {
            @Override
            public void onChange(TransformAttacher.TransState state) {
                switch (state) {
                    case OPEN_TO_THUMB:
                    case OPEN_TO_ORI:
                        if (transformOpenListener != null) transformOpenListener.transformStart();
                        break;
                    case THUMB:
                    case ORI:
                        if (transformOpenListener != null) transformOpenListener.transformEnd();
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
        });
        final boolean needShowThumb = !build.itConfig.noThumb && !(build.itConfig.noThumbWhenCached && build.imageLoad.isCached(url));
        if (needShowThumb) {
            imageView.showThumb(need);
        }
        build.imageLoad.loadImage(url, new ImageLoad.LoadCallback() {
            @Override
            public void progress(float progress) {
                if (progressBar != null)
                    build.imageTransAdapter.onProgressChange(progressBar, progress);
            }

            @Override
            public void loadFinish(Drawable drawable) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                imageView.showImage(drawable, (need || needShowThumb) && getUserVisibleHint());
            }
        }, imageView, uniqueStr);
    }

    public void runClose() {
        imageView.showCloseTransform();
    }

    public void bindTransOpenListener(OnTransformListener listener) {
        this.transformOpenListener = listener;
    }

    @Override
    public void onDestroy() {
        build.imageLoad.cancel(uniqueStr);
        super.onDestroy();
    }
}
