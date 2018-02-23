package com.lsl.banner.banner;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private int[] imgs = {R.mipmap.ic_launcher, R.mipmap.ic_launcher_round, R.mipmap.ic_launcher};
    private List<ImageView> mImageViews;

    private int count;

    private int currentItem;

    private Handler mHandler = new Handler();

    private Runnable task = new Runnable() {
        @Override
        public void run() {
            if (count > 1) {
                currentItem = currentItem % (count + 1) + 1;
                Log.e(this.getClass().getName(), "currentItem:" + currentItem);
                if (currentItem == 1) {  //欺骗眼睛错觉以为是无限循环
                    mViewPager.setCurrentItem(currentItem, false);
                    mHandler.post(task);
                } else {
                    mViewPager.setCurrentItem(currentItem);
                    mHandler.postDelayed(task, 2000);
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iniviews();

        iniImages();

        iniAdapter();

        mHandler.removeCallbacks(task);
        mHandler.postDelayed(task, 2000);

    }

    private void iniAdapter() {

        iniScro();

        mViewPager.setAdapter(new BannerAdapter());
        mViewPager.setFocusable(true);
        mViewPager.setCurrentItem(1);
    }

    private void iniScro() {
        try {
            Field field = ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
            field.set(mViewPager, new BannerSco(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void iniImages() {
        mImageViews = new ArrayList<>();
        count = imgs.length;

        for (int i = 0; i <= count + 1; i++) { //欺骗眼睛错觉以为是无限循环 增加两个
            int resId;
            ImageView imageView = new ImageView(this);
            if (i == 0) {
                resId = imgs[count - 1];
            } else if (i == count + 1) {
                resId = imgs[0];
            } else {
                resId = imgs[i - 1];
            }
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mImageViews.add(imageView);
            imageView.setImageResource(resId);

        }
    }

    private void iniviews() {
        mViewPager = findViewById(R.id.viewpager);

    }


    class BannerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mImageViews.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }


        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = mImageViews.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }
}
