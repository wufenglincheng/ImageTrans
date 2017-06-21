package com.demo.imagetransdemo.adapter;

import android.content.DialogInterface;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.demo.imagetransdemo.MyApplication;
import com.demo.imagetransdemo.R;
import com.demo.imagetransdemo.view.RoundPageIndicator;

import it.liuting.imagetrans.listener.ImageTransAdapter;

/**
 * Created by liuting on 17/6/15.
 */

public class MyImageTransAdapter extends ImageTransAdapter {
    private View view;
    private View topPanel;
    private RoundPageIndicator bottomPanel;
    private boolean isShow = true;

    @Override
    public View getView(ViewGroup parent, ViewPager viewPager, final DialogInterface dialogInterface) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_trans_adapter, null);
        topPanel = view.findViewById(R.id.top_panel);
        bottomPanel = (RoundPageIndicator) view.findViewById(R.id.page_indicator);
        view.findViewById(R.id.top_panel_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogInterface.dismiss();
            }
        });
        topPanel.setTranslationY(-MyApplication.dpToPx(56));
        bottomPanel.setTranslationY(MyApplication.dpToPx(80));
        bottomPanel.setViewPager(viewPager);
        return view;
    }

    @Override
    public void pullRange(float range) {
        topPanel.setTranslationY(-MyApplication.dpToPx(56) * range * 4);
        bottomPanel.setTranslationY(MyApplication.dpToPx(80) * range * 4);
    }

    @Override
    public void pullCancel() {
        showPanel();
    }

    @Override
    public void onShow() {
        showPanel();
    }

    @Override
    public void onDismiss() {
        hiddenPanel();
    }

    @Override
    public boolean onImageClick() {
        if (isShow) {
            showPanel();
        } else {
           hiddenPanel();
        }
        isShow = !isShow;

        return true;
    }

    @Override
    public void onImageLongClick() {
        Toast.makeText(view.getContext(), "long click", Toast.LENGTH_SHORT).show();
    }

    public void hiddenPanel(){
        topPanel.animate().translationY(-MyApplication.dpToPx(56)).setDuration(200).start();
        bottomPanel.animate().translationY(MyApplication.dpToPx(80)).setDuration(200).start();
    }

    public void showPanel(){
        topPanel.animate().translationY(0).setDuration(200).start();
        bottomPanel.animate().translationY(0).setDuration(200).start();
    }
}
