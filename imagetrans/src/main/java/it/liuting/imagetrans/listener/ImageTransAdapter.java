package it.liuting.imagetrans.listener;

import android.content.DialogInterface;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by liuting on 17/6/15.
 */

public class ImageTransAdapter {

    /**
     * 得到用户自定义的VIEW 覆盖在 最上层
     *
     * @param parent
     * @return
     */
    public View getView(ViewGroup parent, ViewPager viewPager, DialogInterface dialogInterface) {
        return null;
    }

    /**
     * 下拉手势的拉动范围值
     *
     * @param range 范围0-1
     */
    public void pullRange(float range) {

    }

    /**
     * 下拉手势取消回到原位
     *
     */
    public void pullCancel() {

    }

    /**
     * imageView 完全显示，开始动画的结束
     */
    public void onShow() {

    }

    /**
     * 开始关闭，是关闭动画的开始
     */
    public void onDismiss() {

    }

    public boolean onImageClick() {
        return false;
    }

    public void onImageLongClick() {

    }

}
