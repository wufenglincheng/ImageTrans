package it.liuting.imagetrans;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import java.util.List;

import it.liuting.imagetrans.listener.OnTransformListener;

/**
 * Created by liuting on 18/3/15.
 */
class DialogViewInflater implements OnTransformListener {

    private ImageTransBuild build;
    private FragmentManager fragmentManager;
    private FrameLayout parentView;
    private InterceptViewPager viewPager;
    private ImagePagerAdapter mAdapter;

    DialogViewInflater(ImageTransBuild build, FragmentManager fragmentManager) {
        this.build = build;
        this.fragmentManager = fragmentManager;
    }

    View createView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.images_viewpager, null);
    }

    void init(View view) {
        parentView = (FrameLayout) view;
        viewPager = (InterceptViewPager) parentView.findViewById(R.id.viewPager);
        mAdapter = new ImagePagerAdapter(fragmentManager, build.imageList);
        viewPager.setAdapter(mAdapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setCurrentItem(build.clickIndex);
        View maskView = build.imageTransAdapter.onCreateView(view, viewPager, build.dialogInterface);
        if (maskView != null) {
            parentView.addView(maskView);
        }
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                build.nowIndex = position;
                build.imageTransAdapter.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void runClose() {
        ImageFragment fragment = (ImageFragment) mAdapter.instantiateItem(viewPager, build.nowIndex);
        fragment.runClose();
    }


    @Override
    public void transformStart() {
        build.imageTransAdapter.onOpenTransStart();
        viewPager.setCanScroll(false);
    }

    @Override
    public void transformEnd() {
        build.imageTransAdapter.onOpenTransEnd();
        viewPager.setCanScroll(true);
    }

    class ImagePagerAdapter extends FragmentStatePagerAdapter {

        private List<String> mData;

        public ImagePagerAdapter(FragmentManager fm, @NonNull List<String> data) {
            super(fm);
            mData = data;
        }

        @Override
        public Fragment getItem(int position) {
            ImageFragment fragment = ImageFragment.newInstance(position, mData.get(position), build);
            if (build.needTransOpen(position, false)) {
                fragment.bindTransOpenListener(DialogViewInflater.this);
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }
}
