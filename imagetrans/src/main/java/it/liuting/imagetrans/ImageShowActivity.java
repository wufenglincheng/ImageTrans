package it.liuting.imagetrans;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuting on 17/5/27.
 */

public class ImageShowActivity extends Activity implements TransImageView.OnCloseListener {
    private ViewPager viewPager;
    private ImageTransAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_image_show);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        adapter = new ImageTransAdapter(StaticParam.imageList);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setCurrentItem(StaticParam.clickIndex);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                StaticParam.setNowIndex(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void close() {
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        adapter.runClose(viewPager.getCurrentItem());
    }

    class ImageTransAdapter extends PagerAdapter {
        private SparseArray<FrameLayout> parentLayouts;
        private SparseArray<ImageRenderAdapter> imageRenders;
        private List<String> imageList = new ArrayList<>();

        ImageTransAdapter(List<String> images) {
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
                ImageRenderAdapter renderAdapter = new ImageRenderAdapter(imageList.get(position));
                renderAdapter.renderView(parent, position, ImageShowActivity.this);
                imageRenders.put(position, renderAdapter);
            }
            container.addView(parent);
            return parent;
        }

        public void runClose(int currentItem) {
            imageRenders.get(currentItem).runClose();
        }
    }

    @Override
    public void finish() {
        StaticParam.destroy();
        super.finish();
    }
}
