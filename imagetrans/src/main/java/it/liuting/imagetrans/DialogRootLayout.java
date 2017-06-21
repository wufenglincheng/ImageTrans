package it.liuting.imagetrans;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuting on 17/6/14.
 */

public class DialogRootLayout extends FrameLayout {
    private ViewPager viewPager;
    private ImagePagerAdapter mAdapter;
    private TransImageView.OnCloseListener onCloseListener;
    private ImageTransParam mImageTransParam;
    private DialogInterface dialogInterface;

    public DialogRootLayout(@NonNull Context context, ImageTransParam imageTransParam, DialogInterface dialogInterface) {
        super(context);
        this.mImageTransParam = imageTransParam;
        this.dialogInterface = dialogInterface;
        init();
    }

    private void init() {
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        viewPager = new ViewPager(getContext());
        addView(viewPager, new LayoutParams(width, height));
        mAdapter = new ImagePagerAdapter(mImageTransParam.imageList);
        viewPager.setAdapter(mAdapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setCurrentItem(mImageTransParam.clickIndex);
        if (mImageTransParam.getAdapter() != null) {
            View view = mImageTransParam.getAdapter().getView(this,viewPager,dialogInterface);
            if (view != null) {
                addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }
        }
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mImageTransParam.setNowIndex(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    public void runClose() {
        mAdapter.runClose(viewPager.getCurrentItem());
    }

    public void setOnCloseListener(TransImageView.OnCloseListener onCloseListener) {
        this.onCloseListener = onCloseListener;
    }

    class ImagePagerAdapter extends PagerAdapter {
        private SparseArray<FrameLayout> parentLayouts;
        private SparseArray<ImageRenderAdapter> imageRenders;
        private List<String> imageList = new ArrayList<>();

        ImagePagerAdapter(List<String> images) {
            parentLayouts = new SparseArray<>();
            imageRenders = new SparseArray<>();
            imageList.addAll(images);
        }

        @Override
        public int getCount() {
            return imageList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            parentLayouts.remove(position);
            ImageRenderAdapter imageRenderAdapter = imageRenders.get(position);
            imageRenderAdapter.destroy();
            imageRenders.remove(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            FrameLayout parent = parentLayouts.get(position);
            if (parent == null) {
                parent = new FrameLayout(container.getContext());
                parentLayouts.put(position, parent);
                ImageRenderAdapter renderAdapter = new ImageRenderAdapter(imageList.get(position), mImageTransParam);
                renderAdapter.renderView(parent, position, onCloseListener);
                imageRenders.put(position, renderAdapter);
            }
            container.addView(parent);
            return parent;
        }

        public void runClose(int currentItem) {
            ImageRenderAdapter item = imageRenders.get(currentItem);
            if (item == null) {
                if (onCloseListener != null) onCloseListener.close();
                return;
            }
            item.runClose();
        }
    }
}
