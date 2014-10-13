
package com.airbitz.shared.utils;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

/**
 * Utility method for {@link android.text.Spannable} stuff. Created by chris on 1/14/14.
 */
public class SpannableUtil {

    /**
     * Finds the first occurrence of <code>text</code> and applies <code>color</code> to
     * the span.
     * 
     * @param span the span
     * @param text the text to apply color to
     * @param color the color
     */
    public static void setColorSpan(Spannable span, String text, int color) {

        if (TextUtils.isEmpty(text)) {
            return;
        }

        // Find index where to apply color
        final int start = span.toString().indexOf(text);
        final int end = start + text.length();

        if (start != -1) {
            final ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
            span.setSpan(colorSpan, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * Sets the background color for <code>span</code>.
     * 
     * @param span the span.
     * @param color the background color
     */
    public static void setBackgroundColorSpan(Spannable span, int color) {
        final BackgroundColorSpan bgColorSpan = new BackgroundColorSpan(color);
        span.setSpan(bgColorSpan, 0, span.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    /**
     * Calls {@link #setStyleSpan(android.text.Spannable, String, int)} with
     * {@link android.graphics.Typeface#BOLD} as the third param.
     * 
     * @param span the span
     * @param text the text to bold
     */
    public static void setBoldSpan(Spannable span, String text) {
        setStyleSpan(span, text, Typeface.BOLD);
    }

    /**
     * Sets a {@link android.text.style.StyleSpan} to <code>text</code>.
     * 
     * @param span the span
     * @param text the text to style
     * @param style the {@link android.graphics.Typeface} style
     */
    public static void setStyleSpan(Spannable span, String text, int style) {

        final int start = span.toString().indexOf(text);
        final int end = start + text.length();

        if (start != -1) {
            final StyleSpan styleSpan = new StyleSpan(style);
            span.setSpan(styleSpan, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
    }
}
