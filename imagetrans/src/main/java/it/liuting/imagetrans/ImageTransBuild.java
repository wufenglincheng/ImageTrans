package it.liuting.imagetrans;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.lang.reflect.Constructor;
import java.util.List;

import it.liuting.imagetrans.listener.SourceImageViewGet;

/**
 * Created by liuting on 18/3/14.
 */

class ImageTransBuild {
    protected int clickIndex;
    protected int nowIndex;
    protected List<String> imageList;
    protected SourceImageViewGet sourceImageViewGet;
    protected ITConfig itConfig;
    protected ImageTransAdapter imageTransAdapter;
    protected ImageLoad imageLoad;
    protected ScaleType scaleType = ScaleType.CENTER_CROP;
    protected DialogInterface dialogInterface;
    protected Class progressClass;
    protected int progressWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
    protected int progressHeight = ViewGroup.LayoutParams.WRAP_CONTENT;

    void checkParam() {
        if (itConfig == null)
            itConfig = new ITConfig();
        if (imageTransAdapter == null) {
            imageTransAdapter = new ImageTransAdapter() {
                @Override
                protected View onCreateView(View parent, ViewPager viewPager, DialogInterface dialogInterface) {
                    return null;
                }
            };
        }
        if (sourceImageViewGet == null)
            throw new NullPointerException("not set SourceImageViewGet");
        if (imageLoad == null)
            throw new NullPointerException("not set ImageLoad");
        if (imageList == null)
            throw new NullPointerException("not set ImageList");
    }

    boolean needTransOpen(int pos, boolean change) {
        boolean need = pos == clickIndex;
        if (need && change) {
            clickIndex = -1;
        }
        return need;
    }

    View inflateProgress(Context context, FrameLayout rootView) {
        if (progressClass != null) {
            try {
                Class[] parameterType = {Context.class};
                Constructor constructor = progressClass.getConstructor(parameterType);
                Object[] parameter = {context};
                View progressBar = (View) constructor.newInstance(parameter);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(progressWidth, progressHeight);
                lp.gravity = Gravity.CENTER;
                rootView.addView(progressBar, lp);
                return progressBar;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
