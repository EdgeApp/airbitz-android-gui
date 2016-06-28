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

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

public class AutoResizeEditText extends EditText {
    private AutoResizeMixin mResize;
    public AutoResizeEditText(Context context) {
        super(context);
        init(context);
    }

    public AutoResizeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AutoResizeEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mResize =  new AutoResizeMixin();
        mResize.init(context, (android.widget.TextView) this);
    }

    @Override
    public void setText(final CharSequence text, BufferType type) {
        super.setText(text, type);
        if (mResize != null) {
            mResize.setText(text, type);
        }
    }

    @Override
    public void setTextSize(float size) {
        if (mResize != null && !mResize.setTextSize(size)) {
            super.setTextSize(size);
        }
    }

    @Override
    public void setTextSize(int unit, float size) {
        if (mResize != null && !mResize.setTextSize(unit, size)) {
            super.setTextSize(unit, size);
        }
    }

    @Override
    public void setMaxLines(int maxlines) {
        super.setMaxLines(maxlines);
        if (mResize != null) {
            mResize.setMaxLines(maxlines);
        }
    }

    @Override
    public void setSingleLine() {
        super.setSingleLine();
        if (mResize != null) {
            mResize.setSingleLine();
        }
    }

    @Override
    public void setSingleLine(boolean singleLine) {
        super.setSingleLine(singleLine);
        if (mResize != null) {
            mResize.setSingleLine(singleLine);
        }
    }

    @Override
    public void setLines(int lines) {
        super.setLines(lines);
        if (mResize != null) {
            mResize.setLines(lines);
        }
    }

    @Override
    public void setLineSpacing(float add, float mult) {
        super.setLineSpacing(add, mult);
        if (mResize != null) {
            mResize.setLineSpacing(add, mult);
        }
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start,
            final int before, final int after) {
        super.onTextChanged(text, start, before, after);
        if (mResize != null) {
            mResize.onTextChanged(text, start, before, after);
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldwidth,
            int oldheight) {
        if (mResize != null) {
            mResize.clearTextCache();
        }
        super.onSizeChanged(width, height, oldwidth, oldheight);
        if (mResize != null) {
            mResize.onSizeChanged(width, height, oldwidth, oldheight);
        }
    }
}
