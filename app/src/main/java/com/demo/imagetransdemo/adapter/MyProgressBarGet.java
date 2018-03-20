package com.demo.imagetransdemo.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.demo.imagetransdemo.MyApplication;
import com.demo.imagetransdemo.view.RingLoadingView;

import it.liuting.imagetrans.listener.ProgressViewGet;

/**
 * Created by liuting on 18/3/19.
 */

public class MyProgressBarGet implements ProgressViewGet<RingLoadingView> {
    @Override
    public RingLoadingView getProgress(Context context) {
        RingLoadingView view = new RingLoadingView(context);
        view.setLayoutParams(new ViewGroup.LayoutParams(MyApplication.dpToPx(50), MyApplication.dpToPx(50)));
        return view;
    }

    @Override
    public void onProgressChange(RingLoadingView view, float progress) {
        view.setProgress(progress);
    }
}
