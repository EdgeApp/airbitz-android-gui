/**
 * Copyright (c) 2014, Airbitz Inc
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms are permitted provided that 
 * the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Redistribution or use of modified source code requires the express written
 *    permission of Airbitz Inc.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the Airbitz Project.
 */


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
