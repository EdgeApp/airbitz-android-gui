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

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.airbitz.R;

import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DessertView extends RelativeLayout {

    final String TAG = getClass().getSimpleName();

    private View mView;
    private ImageView mIcon;
    private TextView mLine1;
    private TextView mLine2;
    private TextView mLine3;

    public DessertView(Context context) {
        super(context);
        init();
    }

    public DessertView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DessertView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mView = inflate(getContext(), R.layout.dessert_view, this);
        mIcon = (ImageView) mView.findViewById(R.id.icon);
        mLine1 = (TextView) mView.findViewById(R.id.line1);
        mLine2 = (TextView) mView.findViewById(R.id.line2);
        mLine3 = (TextView) mView.findViewById(R.id.line3);
        setVisibility(View.INVISIBLE);
    }

    public void setWarningIcon() {
        mIcon.setImageResource(R.drawable.warning);
    }

    public void setOkIcon() {
        mIcon.setImageResource(R.drawable.icon_bitcoin_symbol);
    }

    public TextView getLine1() {
        return mLine1;
    }

    public TextView getLine2() {
        return mLine2;
    }

    public TextView getLine3() {
        return mLine3;
    }

    static final int HIDE_TIMEOUT = 10000;

    public void show() {
        if (getVisibility() == View.VISIBLE) {
            return;
        }
        ObjectAnimator key = ObjectAnimator.ofFloat(this, "translationX", -this.getWidth(), 0f);
        key.setDuration(250);
        key.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                DessertView.this.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationStart(Animator animator) {
                DessertView.this.setVisibility(View.VISIBLE);
            }
        });
        key.start();
        this.postDelayed(new Runnable() {
            public void run() {
                hide();
            }
        }, HIDE_TIMEOUT);
    }

    public void hide() {
        if (this.getVisibility() == View.INVISIBLE) {
            return;
        }
        ObjectAnimator key = ObjectAnimator.ofFloat(this, "translationX", 0f, this.getWidth());
        key.setDuration(250);
        key.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                DessertView.this.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationStart(Animator animator) {
                DessertView.this.setVisibility(View.VISIBLE);
            }
        });
        key.start();
    }
}
