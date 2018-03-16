package it.liuting.imagetrans;

import android.content.DialogInterface;
import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by liuting on 18/3/15.
 */

public abstract class ImageTransAdapter {

    protected abstract View onCreateView(View parent, ViewPager viewPager, final DialogInterface dialogInterface);

    protected void onPullRange(float range) {

    }

    protected void onPullCancel() {

    }

    protected void onOpenTransStart() {

    }

    protected void onOpenTransEnd() {

    }

    protected void onCloseTransStart() {

    }

    protected void onCloseTransEnd() {

    }

    protected void onProgressChange(View progressBar, float progress) {

    }

    protected boolean onClick(View v) {
        return false;
    }

    protected void onLongClick(View v) {

    }
}
