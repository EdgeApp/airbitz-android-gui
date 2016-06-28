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

package com.airbitz.objects;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.RectF;
import android.os.Build;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.widget.TextView;

// Source refactor from:
// http://stackoverflow.com/questions/5033012/auto-scale-textview-text-to-fit-within-bounds/17782522#17782522


public class AutoResizeMixin {
    private interface SizeTester {
        /**
        *
        * @param suggestedSize
        *            Size of text to be tested
        * @param availableSpace
        *            available space in which text must fit
        * @return an integer < 0 if after applying {@code suggestedSize} to
        *         text, it takes less space than {@code availableSpace}, > 0
        *         otherwise
        */
        public int onTestSize(int suggestedSize, RectF availableSpace);
    }

    private RectF mTextRect = new RectF();
    private RectF mAvailableSpaceRect;
    private SparseIntArray mTextCachedSizes;
    private TextPaint mPaint;
    private float mMaxTextSize;
    private float mSpacingMult = 1.0f;
    private float mSpacingAdd = 0.0f;
    private float mMinTextSize = 20;
    private int mWidthLimit;

    private static final int NO_LINE_LIMIT = -1;
    private int mMaxLines;

    private boolean mEnableSizeCache = true;
    private boolean mInitialized;

    private Context mContext;
    private TextView mTextView;

    public void init(Context context, TextView textview) {
        if (mInitialized) {
            return;
        }
        mContext = context;
        mTextView = textview;
        mPaint = new TextPaint(mTextView.getPaint());
        mMaxTextSize = mTextView.getTextSize();
        mAvailableSpaceRect = new RectF();
        mTextCachedSizes = new SparseIntArray();
        if (mMaxLines == 0) {
            mMaxLines = NO_LINE_LIMIT;
        }
        mInitialized = true;
    }

    public void setText(final CharSequence text, TextView.BufferType type) {
        adjustTextSize(text.toString());
    }

    public void setMaxLines(int maxlines) {
        mMaxLines = maxlines;
        reAdjust();
    }

    public int getMaxLines() {
        return mMaxLines;
    }

    public void setSingleLine() {
        mMaxLines = 1;
        reAdjust();
    }

    public void setSingleLine(boolean singleLine) {
        if (singleLine) {
            mMaxLines = 1;
        } else {
            mMaxLines = NO_LINE_LIMIT;
        }
        reAdjust();
    }

    public void setLines(int lines) {
        mMaxLines = lines;
        reAdjust();
    }

    private String getText() {
        String text = mTextView.getText().toString();
        if (TextUtils.isEmpty(text)) {
            return mTextView.getHint().toString();
        } else {
            return text;
        }
    }

    private boolean mAdjusting = false;
    public boolean setTextSize(float size) {

        if (mAdjusting) {
            return false;
        }
        mMaxTextSize = size;
        clearTextCache();
        adjustTextSize(getText());
        return true;
    }

    public boolean setTextSize(int unit, float size) {
        if (mAdjusting) {
            return false;
        }
        Context c = mContext;
        Resources r;
        if (c == null) {
            r = Resources.getSystem();
        } else {
            r = c.getResources();
        }
        mMaxTextSize = TypedValue.applyDimension(unit, size,
                r.getDisplayMetrics());
        clearTextCache();
        adjustTextSize(getText());
        return true;
    }

    public void setLineSpacing(float add, float mult) {
        mSpacingMult = mult;
        mSpacingAdd = add;
    }

    /**
    * Set the lower text size limit and invalidate the view
    *
    * @param minTextSize
    */
    public void setMinTextSize(float minTextSize) {
        mMinTextSize = minTextSize;
        reAdjust();
    }

    public void reAdjust() {
        adjustTextSize(getText());
    }

    public boolean adjustTextSize(String string) {
        if (!mInitialized || mAdjusting) {
            return false;
        }
        mAdjusting = true;

        int startSize = (int) mMinTextSize;
        int heightLimit = mTextView.getMeasuredHeight() - mTextView.getCompoundPaddingBottom()
            - mTextView.getCompoundPaddingTop();
        mWidthLimit = Math.max(0, mTextView.getMeasuredWidth() - mTextView.getCompoundPaddingLeft()
            - mTextView.getCompoundPaddingRight());
        mAvailableSpaceRect.right = mWidthLimit;
        mAvailableSpaceRect.bottom = heightLimit;
        int result = efficientTextSizeSearch(startSize, (int) mMaxTextSize,
                        mSizeTester, mAvailableSpaceRect);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, result);

        mAdjusting = false;
        return true;
    }

    private final SizeTester mSizeTester = new SizeTester() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public int onTestSize(int suggestedSize, RectF availableSPace) {
            mPaint.setTextSize(suggestedSize);
            String text = getText();
            boolean singleline = getMaxLines() == 1;
            if (singleline) {
                mTextRect.bottom = mPaint.getFontSpacing();
                mTextRect.right = mPaint.measureText(text);
            } else {
                StaticLayout layout = new StaticLayout(text, mPaint,
                        mWidthLimit, Alignment.ALIGN_NORMAL, mSpacingMult,
                        mSpacingAdd, true);
                // return early if we have more lines
                if (getMaxLines() != NO_LINE_LIMIT
                        && layout.getLineCount() > getMaxLines()) {
                    return 1;
                }
                mTextRect.bottom = layout.getHeight();
                int maxWidth = -1;
                for (int i = 0; i < layout.getLineCount(); i++) {
                    if (maxWidth < layout.getLineWidth(i)) {
                        maxWidth = (int) layout.getLineWidth(i);
                    }
                }
                mTextRect.right = maxWidth;
            }

            mTextRect.offsetTo(0, 0);
            if (availableSPace.contains(mTextRect)) {
                // may be too small, don't worry we will find the best match
                return -1;
            } else {
                // too big
                return 1;
            }
        }
    };

    public void enableSizeCache(boolean enable) {
        mEnableSizeCache = enable;
        clearTextCache();
        adjustTextSize(getText());
    }

    private int efficientTextSizeSearch(int start, int end,
            SizeTester sizeTester, RectF availableSpace) {
        if (!mEnableSizeCache) {
            return binarySearch(start, end, sizeTester, availableSpace);
        }
        String text = getText();
        int key = text == null ? 0 : text.length();
        int size = mTextCachedSizes.get(key);
        if (size != 0) {
            return size;
        }
        size = binarySearch(start, end, sizeTester, availableSpace);
        mTextCachedSizes.put(key, size);
        return size;
    }

    private static int binarySearch(int start, int end, SizeTester sizeTester,
            RectF availableSpace) {
        int lastBest = start;
        int lo = start;
        int hi = end - 1;
        int mid = 0;
        while (lo <= hi) {
            mid = (lo + hi) >>> 1;
            int midValCmp = sizeTester.onTestSize(mid, availableSpace);
            if (midValCmp < 0) {
                lastBest = lo;
                lo = mid + 1;
            } else if (midValCmp > 0) {
                hi = mid - 1;
                lastBest = hi;
            } else {
                return mid;
            }
        }
        // make sure to return last best
        // this is what should always be returned
        return lastBest;
    }

    public void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        reAdjust();
    }

    public void onSizeChanged(int width, int height, int oldwidth, int oldheight) {
        if (width != oldwidth || height != oldheight) {
            reAdjust();
        }
    }

    public void clearTextCache() {
        mTextCachedSizes.clear();
    }
}
