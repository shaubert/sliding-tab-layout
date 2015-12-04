package com.shaubert.ui.slidingtab;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;

public class SlidingTabLayoutStyle {

    private static final int[] STYLE_ATTR = { R.attr.stl_slidingTabLayoutStyle };

    public static int getSlidingTabTextAppearance(Context context) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                getSlidingTabLayoutTheme(context), R.styleable.STL_SlidingTabLayoutTheme);
        int resId = typedArray.getResourceId(
                R.styleable.STL_SlidingTabLayoutTheme_stl_slidingTabTextAppearance, R.style.STL_SlidingTabTextAppearance);
        typedArray.recycle();
        return resId;
    }

    public static int getSlidingTabStripColor(Context context) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                getSlidingTabLayoutTheme(context), R.styleable.STL_SlidingTabLayoutTheme);
        int color = typedArray.getColor(R.styleable.STL_SlidingTabLayoutTheme_stl_slidingTabStripColor, Color.BLUE);
        typedArray.recycle();
        return color;
    }

    public static int getSlidingTabLayoutTheme(Context context) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(STYLE_ATTR);
        int resId = typedArray.getResourceId(0, R.style.STL_DefaultStyle);
        typedArray.recycle();

        return resId;
    }

}
