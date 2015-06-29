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
import android.widget.EditText;
import android.widget.LinearLayout;

import com.airbitz.R;

import java.text.DecimalFormat;

/**
 * Created by tom on 5/7/14.
 * from http://innovativenetsolutions.com/2013/01/calculator-app/
 */
public class Calculator extends LinearLayout  {
    // operator types
    public static final String ADD = "+";
    public static final String SUBTRACT = "-";
    public static final String MULTIPLY = "*";
    public static final String DIVIDE = "/";
    public static final String PERCENT = "%";
    public static final String CLEAR = "C";
    private static final String DIGITS = "0123456789.";
    DecimalFormat mDF = new DecimalFormat("@###########");
    View mView;
    // 3 + 6 = 9
    // 3 & 6 are called the operand.
    // The + is called the operator.
    // 9 is the result of the operation.
    private double mOperand;
    private double mWaitingOperand;
    private String mWaitingOperator;
    private double mCalculatorMemory;
    private EditText mEditText;
    private Boolean userIsInTheMiddleOfTypingANumber = false;
    private Button mDone;

    /**
     * Interface for calculator key responses
     */
    private OnCalculatorKey mOnCalculatorKeyListener;
    public interface OnCalculatorKey {
        public void OnCalculatorKeyPressed(String tag);
    }

    public void setCalculatorKeyListener(OnCalculatorKey listener) {
        mOnCalculatorKeyListener = listener;
    }


    // public static final String EQUALS = "=";

    public Calculator(Context context) {
        super(context);
        init();
    }

    public Calculator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Calculator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    // constructor
    private void init() {
        mView = inflate(getContext(), R.layout.calculator, this);
        mDone = (Button) mView.findViewById(R.id.imageButtonDone);
        mView.findViewById(R.id.imageButton0).setOnClickListener(mOnKeyListener);
        mView.findViewById(R.id.imageButton1).setOnClickListener(mOnKeyListener);
        mView.findViewById(R.id.imageButton2).setOnClickListener(mOnKeyListener);
        mView.findViewById(R.id.imageButton3).setOnClickListener(mOnKeyListener);
        mView.findViewById(R.id.imageButton4).setOnClickListener(mOnKeyListener);
        mView.findViewById(R.id.imageButton5).setOnClickListener(mOnKeyListener);
        mView.findViewById(R.id.imageButton6).setOnClickListener(mOnKeyListener);
        mView.findViewById(R.id.imageButton7).setOnClickListener(mOnKeyListener);
        mView.findViewById(R.id.imageButton8).setOnClickListener(mOnKeyListener);
        mView.findViewById(R.id.imageButton9).setOnClickListener(mOnKeyListener);

        mDone.setOnClickListener(mOnKeyListener);
        mView.findViewById(R.id.imageButtonClear).setOnClickListener(mOnKeyListener);
        mView.findViewById(R.id.imageButtonDot).setOnClickListener(mOnKeyListener);
        mView.findViewById(R.id.imageButtonPlus).setOnClickListener(mOnKeyListener);
        mView.findViewById(R.id.imageButtonPercent).setOnClickListener(mOnKeyListener);
        mView.findViewById(R.id.imageButtonMinus).setOnClickListener(mOnKeyListener);
        mView.findViewById(R.id.imageButtonMultiply).setOnClickListener(mOnKeyListener);
        mView.findViewById(R.id.imageButtonDivide).setOnClickListener(mOnKeyListener);
        mView.findViewById(R.id.imageButtonBack).setOnClickListener(mOnKeyListener);
        mView.findViewById(R.id.imageButtonEquals).setOnClickListener(mOnKeyListener);
    }

    private final String TAG = getClass().getSimpleName();

    // This is where the text enters and the results return
    public void setEditText(EditText editText) {
        mEditText = editText;
        mOperand = 0;
        mWaitingOperand = 0;
        mWaitingOperator = "";
        mCalculatorMemory = 0;
        mDF.setMinimumFractionDigits(0);
        mDF.setMaximumFractionDigits(6);
        mDF.setMinimumIntegerDigits(1);
        mDF.setMaximumIntegerDigits(8);
    }

    public void hideDoneButton() {
        mDone.setText(getResources().getString(R.string.request_next));
    }

    public void showDoneButton() {
        mDone.setText(getResources().getString(R.string.calculator_done));
    }

    public void setOperand(double operand) {
        mOperand = operand;
    }

    public double getResult() {
        return mOperand;
    }

    // used on screen orientation change
    public double getMemory() {
        return mCalculatorMemory;
    }

    // used on screen orientation change
    public void setMemory(double calculatorMemory) {
        mCalculatorMemory = calculatorMemory;
    }

    public String toString() {
        return Double.toString(mOperand);
    }

    public double performOperation(String operator) {
        if (operator.equals(CLEAR)) {
            mOperand = 0;
            mWaitingOperator = "";
            mWaitingOperand = 0;
            // mCalculatorMemory = 0;
        } else if (operator.equals(PERCENT)) {
            mOperand = mWaitingOperand * mOperand * 0.01;
        } else {
            performWaitingOperation();
            mWaitingOperator = operator;
            mWaitingOperand = mOperand;
        }

        return mOperand;
    }

    protected void performWaitingOperation() {
        if (mWaitingOperator.equals(ADD)) {
            mOperand = mWaitingOperand + mOperand;
        } else if (mWaitingOperator.equals(SUBTRACT)) {
            mOperand = mWaitingOperand - mOperand;
        } else if (mWaitingOperator.equals(MULTIPLY)) {
            mOperand = mWaitingOperand * mOperand;
        } else if (mWaitingOperator.equals(DIVIDE)) {
            if (mOperand != 0) {
                mOperand = mWaitingOperand / mOperand;
            }
        }
    }

    public OnClickListener mOnKeyListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mEditText == null)
                return;

            String buttonTag = view.getTag().toString();
            Editable editable = mEditText.getText();
            int start = mEditText.getSelectionStart();
            // delete the selection, if chars are selected:
            int end = mEditText.getSelectionEnd();
            if (end > start && !buttonTag.equals("done")) {
                editable.delete(start, end);
            }

            if (buttonTag.equals("back")) {
                String s = mEditText.getText().toString();
                if (s.length() == 1) { // 1 character, just set to 0
                    performOperation(Calculator.CLEAR);
                    mEditText.setText("");
                } else if (s.length() > 1) {
                    mEditText.setText(s.substring(0, s.length() - 1));
                }
            } else if (buttonTag.equals("done")) {
                mEditText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
            } else if (DIGITS.contains(buttonTag)) {

                // digit was pressed
                if (userIsInTheMiddleOfTypingANumber) {
                    if (buttonTag.equals(".") && mEditText.getText().toString().contains(".")) {
                        // ERROR PREVENTION
                        // Eliminate entering multiple decimals
                    } else {
                        mEditText.append(buttonTag);
                    }
                } else {
                    if (buttonTag.equals(".")) {
                        // ERROR PREVENTION
                        // This will avoid error if only the decimal is hit before an operator, by placing a leading zero
                        // before the decimal
                        mEditText.setText(0 + buttonTag);
                    } else {
                        mEditText.setText(buttonTag);
                    }
                    userIsInTheMiddleOfTypingANumber = true;
                }

            } else {
                // operation was pressed
                if (userIsInTheMiddleOfTypingANumber) {
                    try {
                        setOperand(Double.parseDouble(mEditText.getText().toString()));
                    } catch (NumberFormatException e) { // ignore any non-double
                    }
                    userIsInTheMiddleOfTypingANumber = false;
                }

                performOperation(buttonTag);
                mEditText.setText(mDF.format(getResult()));
                if (buttonTag.equals("=")) {
                }
            }
            if(mOnCalculatorKeyListener != null) {
                mOnCalculatorKeyListener.OnCalculatorKeyPressed(buttonTag);
            }
        }
    };

    // Keyboard animation variables
    static final int KEYBOARD_ANIM = 250;
    private boolean mAnimating = false;

    public void showCalculator() {
        showCalculator(null);
    }

    public void showCalculator(final ValueAnimator.AnimatorUpdateListener updateListener) {
        if (mAnimating || getVisibility() == View.VISIBLE) {
            return;
        }
        mAnimating = true;
        final ValueAnimator val = ValueAnimator.ofFloat(1f, 0f);
        if (null != updateListener) {
            val.addUpdateListener(updateListener);
        }
        ObjectAnimator key = ObjectAnimator.ofFloat(this, "translationY", this.getHeight(), 0f);

        AnimatorSet set = new AnimatorSet();
        set.setDuration(KEYBOARD_ANIM);
        set.playTogether(key, val);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                Calculator.this.setVisibility(View.VISIBLE);
                if (null != updateListener) {
                    updateListener.onAnimationUpdate(val);
                }
                mAnimating = false;
            }

            @Override
            public void onAnimationStart(Animator animator) {
                Calculator.this.setVisibility(View.VISIBLE);
            }
        });
        set.start();
    }

    public void hideCalculator() {
        hideCalculator(null);
    }

    public void hideCalculator(final ValueAnimator.AnimatorUpdateListener updateListener) {
        if (mAnimating || getVisibility() != View.VISIBLE) {
            return;
        }
        mAnimating = true;
        final ValueAnimator val = ValueAnimator.ofFloat(0f, 1f);
        if (null != updateListener) {
            val.addUpdateListener(updateListener);
        }
        ObjectAnimator key =
            ObjectAnimator.ofFloat(this, "translationY", 0f, this.getHeight());
        key.setDuration(KEYBOARD_ANIM);

        final AnimatorSet set = new AnimatorSet();
        set.playTogether(key, val);
        set.setDuration(KEYBOARD_ANIM);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                Calculator.this.setVisibility(View.INVISIBLE);
                if (null != updateListener) {
                    updateListener.onAnimationUpdate(val);
                }
                mAnimating = false;
            }

            @Override
            public void onAnimationStart(Animator animator) {
                Calculator.this.setVisibility(View.VISIBLE);
            }
        });
        set.start();
    }
}
