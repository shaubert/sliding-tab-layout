package com.shaubert.ui.slidingtab;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

class Utils {

    static int dpToPx(Context context, float dp) {
        Resources r = context.getResources();
        return (int) (TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()) + 0.5f);
    }

    static int spToPx(Context context, float sp) {
        Resources r = context.getResources();
        return (int) (TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, sp, r.getDisplayMetrics()) + 0.5f);
    }

}
