package com.lsl.banner.banner;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class BannerSco extends Scroller {
    public BannerSco(Context context) {
        super(context);
    }

    public BannerSco(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    public BannerSco(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        super.startScroll(startX, startY, dx, dy,800);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, 800);
    }
}
