/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shaubert.ui.slidingtab;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

/**
 * To be used with ViewPager to provide a tab indicator component which give constant feedback as to
 * the user's scroll progress.
 * <p>
 * To use the component, simply add it to your view hierarchy. Then in your
 * {@link android.app.Activity} or {@link android.support.v4.app.Fragment} call
 * {@link #setViewPager(ViewPager)} providing it the ViewPager this layout is being used for.
 * <p>
 * The colors can be customized in two ways. The first and simplest is to provide an array of colors
 * via {@link #setSelectedIndicatorColors(int...)} and {@link #setDividerColors(int...)}. The
 * alternative is via the {@link TabColorizer} interface which provides you complete control over
 * which color is used for any individual position.
 * <p>
 * The views used as tabs can be customized by calling {@link #setCustomTabView(int, int)},
 * providing the layout ID of your custom layout.
 */
public class SlidingTabLayout extends HorizontalScrollView {

    /**
     * Allows complete control over the colors drawn in the tab layout. Set with
     * {@link #setCustomTabColorizer(TabColorizer)}.
     */
    public interface TabColorizer {

        /**
         * @return return the color of the indicator used when {@code position} is selected.
         */
        int getIndicatorColor(int position);

        /**
         * @return return the color of the divider drawn to the right of {@code position}.
         */
        int getDividerColor(int position);

    }

    public enum StretchOption {
        IF_LESS_THAN_3,
        IF_POSSIBLE,
        ALWAYS,
        NONE,
    }

    private static final int TITLE_OFFSET_DIPS = 24;
    private static final int TAB_VIEW_PADDING_HORIZ_DIPS = 12;
    private static final int TAB_VIEW_PADDING_VERT_DIPS = 8;

    private static final int DEFAULT_HEIGHT_DP = 12;

    private int mTitleOffset;

    private int mTabViewLayoutId;
    private int mTabViewTextViewId;

    private ViewPager mViewPager;
    private InternalViewPagerListener viewPagerListener;

    private StretchOption stretchOption = StretchOption.IF_LESS_THAN_3;

    private TabClickCallback tabClickCallback;
    private final OnClickListener tabClickListener = new TabClickListener();
    private DataSetObserver observer = new DataSetObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            notifyDataSetChanged();
        }
    };

    private final SlidingTabStrip mTabStrip;

    public SlidingTabLayout(Context context) {
        this(context, null);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Disable the Scroll Bar
        setHorizontalScrollBarEnabled(false);
        // Make sure that the Tab Strips fills this View
        setFillViewport(true);

        mTitleOffset = (int) (TITLE_OFFSET_DIPS * getResources().getDisplayMetrics().density);

        mTabStrip = new SlidingTabStrip(context);
        addView(mTabStrip, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    /**
     * Set the custom {@link TabColorizer} to be used.
     *
     * If you only require simple custmisation then you can use
     * {@link #setSelectedIndicatorColors(int...)} and {@link #setDividerColors(int...)} to achieve
     * similar effects.
     */
    public void setCustomTabColorizer(TabColorizer tabColorizer) {
        mTabStrip.setCustomTabColorizer(tabColorizer);
    }

    /**
     * Sets the colors to be used for indicating the selected tab. These colors are treated as a
     * circular array. Providing one color will mean that all tabs are indicated with the same color.
     */
    public void setSelectedIndicatorColors(int... colors) {
        mTabStrip.setSelectedIndicatorColors(colors);
    }

    /**
     * Sets the colors to be used for tab dividers. These colors are treated as a circular array.
     * Providing one color will mean that all tabs are indicated with the same color.
     */
    public void setDividerColors(int... colors) {
        mTabStrip.setDividerColors(colors);
    }

    public void setTabClickCallback(TabClickCallback tabClickCallback) {
        this.tabClickCallback = tabClickCallback;
    }

    public void setStretchOption(StretchOption stretchOption) {
        if (this.stretchOption != stretchOption) {
            this.stretchOption = stretchOption;
            mTabStrip.requestLayout();
        }
    }

    public StretchOption getStretchOption() {
        return stretchOption;
    }

    /**
     * Set the custom layout to be inflated for the tab views.
     *
     * @param layoutResId Layout id to be inflated
     * @param textViewId id of the {@link TextView} in the inflated view
     */
    public void setCustomTabView(int layoutResId, int textViewId) {
        mTabViewLayoutId = layoutResId;
        mTabViewTextViewId = textViewId;

        mTabStrip.removeAllViews();
        notifyDataSetChanged();
    }

    /**
     * Sets the associated view pager.
     */
    public void setViewPager(ViewPager viewPager) {
        if (mViewPager != null) {
            mViewPager.getAdapter().unregisterDataSetObserver(observer);
            mViewPager.removeOnPageChangeListener(viewPagerListener);
        }

        mViewPager = viewPager;
        if (viewPager != null) {
            viewPagerListener = new InternalViewPagerListener();
            viewPager.addOnPageChangeListener(viewPagerListener);
            viewPager.getAdapter().registerDataSetObserver(observer);
            notifyDataSetChanged();
        } else {
            trimChildrenToSize(0);
        }
    }

    /**
     * Create a default view to be used for tabs. This is called if a custom tab view is not set via
     * {@link #setCustomTabView(int, int)}.
     */
    protected TextView createDefaultTabView(Context context) {
        TextView textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView.setTextAppearance(SlidingTabLayoutStyle.getSlidingTabTextAppearance(context));
        } else {
            //noinspection deprecation
            textView.setTextAppearance(context, SlidingTabLayoutStyle.getSlidingTabTextAppearance(context));
        }
        textView.setAllCaps(true);
        textView.setMaxLines(2);
        textView.setEllipsize(TextUtils.TruncateAt.END);

        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground,
                outValue, true);
        textView.setBackgroundResource(outValue.resourceId);

        float density = getResources().getDisplayMetrics().density;
        int paddingHoriz = (int) (TAB_VIEW_PADDING_HORIZ_DIPS * density);
        int paddingVert = (int) (TAB_VIEW_PADDING_VERT_DIPS * density);
        if (getLayoutParams() != null && getLayoutParams().height > 0) {
            int maxHeight = getLayoutParams().height;
            int maxVertPadding = Math.max(0, (maxHeight - (int) textView.getTextSize()) / 2);
            paddingVert = Math.min(maxVertPadding, paddingVert);
        }
        textView.setPadding(paddingHoriz, paddingVert, paddingVert, paddingHoriz);

        return textView;
    }

    private void notifyDataSetChanged() {
        final PagerAdapter adapter = mViewPager.getAdapter();

        int count = adapter.getCount();
        trimChildrenToSize(count);
        for (int i = 0; i < count; i++) {
            View tabView = getView(i);
            TextView tabTitleView = null;

            if (mTabViewLayoutId != 0) {
                tabTitleView = (TextView) tabView.findViewById(mTabViewTextViewId);
            }
            if (tabTitleView == null && TextView.class.isInstance(tabView)) {
                tabTitleView = (TextView) tabView;
            }
            if (tabTitleView != null) {
                tabTitleView.setText(adapter.getPageTitle(i));
            }
        }

        mTabStrip.markPositionAsSelected(mViewPager.getCurrentItem());

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @SuppressLint("NewApi")
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                if (mViewPager != null) {
                    scrollToTab(mViewPager.getCurrentItem(), 0);
                }
            }
        });
    }

    private View getView(int position) {
        if (position < mTabStrip.getChildCount()) {
            return mTabStrip.getChildAt(position);
        }

        View tabView = null;
        if (mTabViewLayoutId != 0) {
            // If there is a custom tab view layout id set, try and inflate it
            tabView = LayoutInflater.from(getContext()).inflate(mTabViewLayoutId, mTabStrip,
                    false);
        }

        if (tabView == null) {
            tabView = createDefaultTabView(getContext());
        }

        tabView.setOnClickListener(tabClickListener);
        mTabStrip.addView(tabView);
        return tabView;
    }

    private void trimChildrenToSize(int newSize) {
        int size = Math.max(0, newSize);
        if (size < mTabStrip.getChildCount()) {
            mTabStrip.removeViews(size, mTabStrip.getChildCount() - size);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // If we have a MeasureSpec which allows us to decide our height, try and use the default
        // height
        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case MeasureSpec.AT_MOST:
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                        Math.min(Utils.dpToPx(getContext(), DEFAULT_HEIGHT_DP), MeasureSpec.getSize(heightMeasureSpec)),
                        MeasureSpec.EXACTLY);
                break;
            case MeasureSpec.UNSPECIFIED:
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(Utils.dpToPx(getContext(), DEFAULT_HEIGHT_DP),
                        MeasureSpec.EXACTLY);
                break;
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (mViewPager != null) {
            scrollToTab(mViewPager.getCurrentItem(), 0);
        }
    }

    private void scrollToTab(int tabIndex, int positionOffset) {
        final int tabStripChildCount = mTabStrip.getChildCount();
        if (tabStripChildCount == 0 || tabIndex < 0 || tabIndex >= tabStripChildCount) {
            return;
        }

        View selectedChild = mTabStrip.getChildAt(tabIndex);
        if (selectedChild != null) {
            int targetScrollX = selectedChild.getLeft() + positionOffset;

            if (tabIndex > 0 || positionOffset > 0) {
                // If we're not at the first child and are mid-scroll, make sure we obey the offset
                targetScrollX -= mTitleOffset;
            }

            scrollTo(targetScrollX, 0);
        }
    }

    private class InternalViewPagerListener implements ViewPager.OnPageChangeListener {
        private int mScrollState;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int tabStripChildCount = mTabStrip.getChildCount();
            if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
                return;
            }

            mTabStrip.onViewPagerPageChanged(position, positionOffset);

            View selectedTitle = mTabStrip.getChildAt(position);
            int extraOffset = (selectedTitle != null)
                    ? (int) (positionOffset * selectedTitle.getWidth())
                    : 0;
            scrollToTab(position, extraOffset);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mScrollState = state;
        }

        @Override
        public void onPageSelected(int position) {
            if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
                mTabStrip.onViewPagerPageChanged(position, 0f);
                scrollToTab(position, 0);
            }
            mTabStrip.markPositionAsSelected(position);
        }

    }

    private class TabClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                if (v == mTabStrip.getChildAt(i)) {
                    if (tabClickCallback == null
                            || !tabClickCallback.onTabClicked(i)) {
                        mViewPager.setCurrentItem(i);
                    }
                    return;
                }
            }
        }
    }

    public interface TabClickCallback {
        /**
         * @param tabIndex index of clicked tab
         * @return true if event handled, false otherwise
         */
        boolean onTabClicked(int tabIndex);
    }

}
