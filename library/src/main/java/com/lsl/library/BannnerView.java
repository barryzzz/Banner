
package com.lsl.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : lsl 408930131@qq.com
 * @version : v1.0
 * @description : 定制修改版本banner组件，移除部分不需要的功能,扩展其他功能可以参考https://github.com/youth5201314/banner 开源库
 * @date : 2018-02-24
 */
public class BannnerView extends FrameLayout implements ViewPager.OnPageChangeListener {
    /**
     * 显示imageview集合
     */
    private List<ImageView> imageViews;
    /**
     * 图片数据集
     */
    private List datas;
    /**
     * indicator imageview
     */
    private List<ImageView> indicatorImages;
    /**
     * 本地图片数据集，在网络请求没有数据情况下使用
     */
    private List localDatas;

    /**
     * 数据count
     */
    private int count;
    /**
     * 当前item
     */
    private int currentItem;

    /**
     * 是否自动轮播
     */
    private boolean isAutoPlay = true;
    /**
     * 轮播时间间隔
     */
    private int delayTime = 2000;
    /**
     * 是否显示指示器
     */
    private boolean isIndicatorShow = true;
    /**
     * 指示器宽度
     */
    private int indicatorWidth;
    /**
     * 指示器高度
     */
    private int indicatorHeight;
    /**
     * 指示器间隔
     */
    private int indicatorMargin = 5;
    /**
     * 指示器选中样式
     */
    private int indicatorSelected = R.drawable.gray_radius;
    /**
     * 指示器未选中样式
     */
    private int indicatorUnselected = R.drawable.white_radius;
    /**
     * 轮播图片缩放状态，默认 ScaleType.CENTER_CROP
     */
    private int scaleType = 1;
    /**
     * 默认指示器尺寸
     */
    private int indicatorSize;
    /**
     * 滑动周期
     */
    private int scrollerTime = 800;
    /**
     * 图片加载委托
     */
    private ImageLoaderInterface imageLoader;

    private OnBannerClickListener bannerClickListener;


    private int lastPosition = 1;

    private ViewPager viewPager;
    private LinearLayout circleIndicatorLayout;


    private Context context;


    private Handler handler = new Handler();
    /**
     * 周期任务
     */
    private Runnable task = new Runnable() {
        @Override
        public void run() {
            if (count > 1 && isAutoPlay) {
                currentItem = currentItem % (count + 1) + 1;
                Log.e(this.getClass().getName(), "currentItem:" + currentItem);
                if (currentItem == 1) {
                    viewPager.setCurrentItem(currentItem, false);
                    handler.post(task);
                } else {
                    viewPager.setCurrentItem(currentItem);
                    handler.postDelayed(task, delayTime);
                }
            }
        }
    };


    public BannnerView(@NonNull Context context) {
        this(context, null, 0);
    }

    public BannnerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannnerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;
        initTypedArray(context, attrs);
        inidatas();
        iniviews(context);
    }

    /**
     * 初始化参数
     */
    private void inidatas() {
        imageViews = new ArrayList<>();
        datas = new ArrayList<>();
        indicatorImages = new ArrayList<>();
        localDatas = new ArrayList();
    }

    /**
     * 初始化view
     *
     * @param context
     */
    private void iniviews(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.banner, this, true);
        viewPager = (ViewPager) view.findViewById(R.id.bannerViewPager);
        circleIndicatorLayout = (LinearLayout) view.findViewById(R.id.circleIndicator);
        try {
            Field field = ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
            field.set(viewPager, new CustomScroller(context, scrollerTime)); //设置滑动过程时间
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化属性
     *
     * @param context
     * @param attrs
     */
    private void initTypedArray(Context context, AttributeSet attrs) {

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        indicatorSize = dm.widthPixels / 80;

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BannnerView);
        isAutoPlay = array.getBoolean(R.styleable.BannnerView_autoplay, isAutoPlay);
        delayTime = array.getInt(R.styleable.BannnerView_delaytime, delayTime);
        isIndicatorShow = array.getBoolean(R.styleable.BannnerView_indicator_show, isIndicatorShow);
        indicatorWidth = array.getInt(R.styleable.BannnerView_indicator_width, indicatorSize);
        indicatorHeight = array.getInt(R.styleable.BannnerView_indicator_height, indicatorSize);
        indicatorMargin = array.getInt(R.styleable.BannnerView_indicator_margin, indicatorMargin);
        indicatorSelected = array.getInt(R.styleable.BannnerView_indicator_drawable_selected, indicatorSelected);
        indicatorUnselected = array.getInt(R.styleable.BannnerView_indicator_drawable_unselected, indicatorUnselected);
        scaleType = array.getInt(R.styleable.BannnerView_image_scale_type, scaleType);
        array.recycle();
    }

    /**
     * 设置数据源
     *
     * @param datas 数据集合
     * @return XfBannner
     */
    public BannnerView setImages(List<?> datas) {
        this.datas = datas;
        count = this.datas.size();
        return this;
    }

    /**
     * 设置网络数据无情况下显示数据集
     *
     * @param localdatas 本地数据集
     * @return
     */
    public BannnerView setLocalImages(List<?> localdatas) {
        this.localDatas = localdatas;
        count = this.localDatas.size();
        return this;
    }

    /**
     * @param imageLoader 设置图片加载方式
     * @return XfBannner
     */
    public BannnerView setImageLoader(ImageLoaderInterface imageLoader) {
        this.imageLoader = imageLoader;
        return this;
    }

    public BannnerView setBannerClick(OnBannerClickListener listener) {
        this.bannerClickListener = listener;
        return this;
    }

    /**
     * 设置滑动周期
     *
     * @param scrollerTime 滑动时间
     * @return XfBannner
     */
    public BannnerView setScrollerTime(int scrollerTime) {
        this.scrollerTime = scrollerTime;
        return this;
    }

    /**
     * 设置轮播周期
     *
     * @param delayTime 轮播时间
     * @return XfBannner
     */
    public BannnerView setDelayTime(int delayTime) {
        this.delayTime = delayTime;
        return this;
    }

    /**
     * 设置是否自动轮播
     *
     * @param isAutoPlay 是否轮播
     * @return XfBannner
     */
    public BannnerView setAutoPlay(boolean isAutoPlay) {
        this.isAutoPlay = isAutoPlay;
        return this;
    }

    /**
     * 设置是否显示指示器
     *
     * @param isIndicatorShow
     * @return
     */
    public BannnerView setIndicatorShow(boolean isIndicatorShow) {
        this.isIndicatorShow = isIndicatorShow;
        return this;
    }

    /**
     * 设置图片缩放模式
     *
     * @param scaleType 缩放模式
     * @return
     */
    public BannnerView setScaleType(int scaleType) {
        this.scaleType = scaleType;
        return this;
    }

    /**
     * build,启动banner
     */
    public void build() {
        if (datas == null || datas.size() <= 0) {
            datas = localDatas;
        }
        setImageList(datas);
        if (isAutoPlay)
            startAutoPlay();
    }

    /**
     * 更新数据集
     *
     * @param datas 新数据
     */
    public void update(List<?> datas) {
        this.datas.clear();
        this.imageViews.clear();
        this.datas.addAll(datas);
        this.count = this.datas.size();
        build();
    }

    /**
     * 切换动画拓展
     *
     * @param reverseDrawingOrder
     * @param transformer
     * @return
     */
    public BannnerView setPageTransformer(boolean reverseDrawingOrder, @Nullable ViewPager.PageTransformer transformer) {
        viewPager.setPageTransformer(reverseDrawingOrder, transformer);
        return this;
    }

    /**
     * 启动轮播
     */
    public void startAutoPlay() {
        handler.removeCallbacks(task);
        handler.postDelayed(task, delayTime);
    }

    /**
     * 结束轮播，ps:在界面stop的时候记得停止
     */
    public void stopAutoPlay() {
        handler.removeCallbacks(task);
    }

    /**
     * 初始化轮播图片，初始化指示器
     *
     * @param d
     */
    private void setImageList(List<?> d) {
        if (d == null || d.size() <= 0) {
            throw new RuntimeException("image datas is null");
        }
        if (isIndicatorShow) {
            initIndicator();
        }
        for (int i = 0; i <= count + 1; i++) {
            ImageView imageView = new ImageView(context);
            setScaleType(imageView);

            Object obj;
            if (i == 0) {
                obj = d.get(count - 1);
            } else if (i == count + 1) {
                obj = d.get(0);
            } else {
                obj = d.get(i - 1);
            }
            imageViews.add(imageView);
            if (imageLoader != null) {
                imageLoader.displayImage(context, obj, imageView);
            } else {
                throw new RuntimeException("please set imageloader");
            }

        }
        currentItem = 1;
        viewPager.setAdapter(new BannerAdapter());
        viewPager.setFocusable(true);
        viewPager.setCurrentItem(currentItem);
        viewPager.addOnPageChangeListener(this);

    }

    /**
     * 创建Indicator指示器
     */
    private void initIndicator() {
        indicatorImages.clear();
        circleIndicatorLayout.removeAllViews();
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(indicatorWidth, indicatorHeight);
            params.leftMargin = indicatorMargin;
            params.rightMargin = indicatorMargin;
            if (i == 0) {
                imageView.setImageResource(indicatorSelected);
            } else {
                imageView.setImageResource(indicatorUnselected);
            }
            indicatorImages.add(imageView);
            circleIndicatorLayout.addView(imageView, params);
        }
    }

    /**
     * 设置样式
     *
     * @param imageView
     */
    private void setScaleType(ImageView imageView) {
        switch (scaleType) {
            case 0:
                imageView.setScaleType(ImageView.ScaleType.CENTER);
                break;
            case 1:
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                break;
            case 2:
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                break;
            case 3:
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                break;
            case 4:
                imageView.setScaleType(ImageView.ScaleType.FIT_END);
                break;
            case 5:
                imageView.setScaleType(ImageView.ScaleType.FIT_START);
                break;
            case 6:
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                break;
            case 7:
                imageView.setScaleType(ImageView.ScaleType.MATRIX);
                break;
        }
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        currentItem = position;
        if (isIndicatorShow) {
            indicatorImages.get((lastPosition - 1 + count) % count).setImageResource(indicatorUnselected);
            indicatorImages.get((position - 1 + count) % count).setImageResource(indicatorSelected);
            lastPosition = position;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        switch (state) {
            case ViewPager.SCROLL_STATE_IDLE:
            case ViewPager.SCROLL_STATE_DRAGGING:
                if (currentItem == 0) {
                    viewPager.setCurrentItem(count, false);
                } else if (currentItem == count + 1) {
                    viewPager.setCurrentItem(1, false);
                }
                break;
            case ViewPager.SCROLL_STATE_SETTLING:
                break;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_OUTSIDE) {
            startAutoPlay();
        } else if (action == MotionEvent.ACTION_DOWN) {
            stopAutoPlay();
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 加载图片实现接口
     */
    public interface ImageLoaderInterface {
        void displayImage(Context context, Object obj, ImageView imageView);
    }

    /**
     * 点击事件接口
     */
    public interface OnBannerClickListener {
        void onBannerClick(int position);
    }

    /**
     * 返回真实的位置
     *
     * @param position p
     * @return 返回下标
     */
    public int toRealPosition(int position) {
        int realPosition = (position - 1) % count;
        if (realPosition < 0)
            realPosition += count;
        return realPosition;
    }

    class BannerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return imageViews.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            View view = imageViews.get(position);
            container.addView(view);
            if (bannerClickListener != null) {
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bannerClickListener.onBannerClick(toRealPosition(position));
                    }
                });
            }
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            View view = (View) object;
            view.setOnClickListener(null);
            container.removeView(view);
        }
    }


    private class CustomScroller extends Scroller {

        private int scrollerDuration = 800;

        private CustomScroller(Context context, int ScrollerDuration) {
            super(context);
            this.scrollerDuration = ScrollerDuration;
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            super.startScroll(startX, startY, dx, dy, scrollerDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, scrollerDuration);
        }
    }
}
