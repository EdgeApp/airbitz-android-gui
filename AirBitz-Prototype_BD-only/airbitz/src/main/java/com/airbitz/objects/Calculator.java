package com.airbitz.objects;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.airbitz.R;

import java.text.DecimalFormat;

/**
 * Created by tom on 5/7/14.
 * from http://innovativenetsolutions.com/2013/01/calculator-app/
 */
public class Calculator extends LinearLayout {
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
    DecimalFormat mDF = new DecimalFormat("@###########");

    private static final String DIGITS = "0123456789.";

    View mView;

    // operator types
    public static final String ADD = "+";
    public static final String SUBTRACT = "-";
    public static final String MULTIPLY = "*";
    public static final String DIVIDE = "/";
    public static final String PERCENT = "%" ;

    public static final String BACK = "back" ;
    public static final String CLEAR = "C" ;
    public static final String CLEARMEMORY = "MC";
    public static final String ADDTOMEMORY = "M+";
    public static final String SUBTRACTFROMMEMORY = "M-";
    public static final String RECALLMEMORY = "MR";
    public static final String SQUAREROOT = "√";
    public static final String SQUARED = "x²";
    public static final String INVERT = "1/x";
    public static final String TOGGLESIGN = "+/-";
    public static final String SINE = "sin";
    public static final String COSINE = "cos";
    public static final String TANGENT = "tan";

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
    }

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

    public void setOperand(double operand) {
        mOperand = operand;
    }

    public double getResult() {
        return mOperand;
    }

    // used on screen orientation change
    public void setMemory(double calculatorMemory) {
        mCalculatorMemory = calculatorMemory;
    }

    // used on screen orientation change
    public double getMemory() {
        return mCalculatorMemory;
    }

    public String toString() {
        return Double.toString(mOperand);
    }

    public double performOperation(String operator) {

        /*
        * If you are using Java 7, then you can use switch in place of if statements
        *
        *     switch (operator) {
        *     case CLEARMEMORY:
        *         calculatorMemory = 0;
        *         break;
        *     case ADDTOMEMORY:
        *         calculatorMemory = calculatorMemory + operand;
        *         break;
        *     etc...
        *     }
        */

        if (operator.equals(CLEAR)) {
            mOperand = 0;
            mWaitingOperator = "";
            mWaitingOperand = 0;
            // mCalculatorMemory = 0;
        } else if (operator.equals(PERCENT)) {
            mOperand = mWaitingOperand*mOperand*0.01;
//        } else if (operator.equals(CLEARMEMORY)) {
//            mCalculatorMemory = 0;
//        } else if (operator.equals(ADDTOMEMORY)) {
//            mCalculatorMemory = mCalculatorMemory + mOperand;
//        } else if (operator.equals(SUBTRACTFROMMEMORY)) {
//            mCalculatorMemory = mCalculatorMemory - mOperand;
//        } else if (operator.equals(RECALLMEMORY)) {
//            mOperand = mCalculatorMemory;
//        } else if (operator.equals(SQUAREROOT)) {
//            mOperand = Math.sqrt(mOperand);
//
//        } else if (operator.equals(SQUARED)) {
//            mOperand = mOperand * mOperand;
//
//        } else if (operator.equals(INVERT)) {
//            if (mOperand != 0) {
//                mOperand = 1 / mOperand;
//            }
//        } else if (operator.equals(TOGGLESIGN)) {
//            mOperand = -mOperand;
//        } else if (operator.equals(SINE)) {
//            mOperand = Math.sin(Math.toRadians(mOperand)); // Math.toRadians(mOperand) converts result to degrees
//        } else if (operator.equals(COSINE)) {
//            mOperand = Math.cos(Math.toRadians(mOperand)); // Math.toRadians(mOperand) converts result to degrees
//        } else if (operator.equals(TANGENT)) {
//            mOperand = Math.tan(Math.toRadians(mOperand)); // Math.toRadians(mOperand) converts result to degrees
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
        }  else if (mWaitingOperator.equals(SUBTRACT)) {
            mOperand = mWaitingOperand - mOperand;
        } else if (mWaitingOperator.equals(MULTIPLY)) {
            mOperand = mWaitingOperand * mOperand;
        } else if (mWaitingOperator.equals(DIVIDE)) {
            if (mOperand != 0) {
                mOperand = mWaitingOperand / mOperand;
            }
        }
    }

    public void onButtonClick(View v) {
        if(mEditText==null)
            return;

        Editable editable = mEditText.getText();
        int start = mEditText.getSelectionStart();
        // delete the selection, if chars are selected:
        int end = mEditText.getSelectionEnd();
        if (end > start) {
            editable.delete(start, end);
        }
        String buttonTag = v.getTag().toString();

        if(buttonTag.equals("back")) {
            String s = mEditText.getText().toString();
            if (s.length() == 1) { // 1 character, just set to 0
                performOperation(Calculator.CLEAR);
                mEditText.setText("");
            } else if (s.length() > 1) {
                mEditText.setText(s.substring(0, s.length() - 1));
            }
        } else if(buttonTag.equals("done")) {
                mEditText.dispatchKeyEvent(new KeyEvent(EditorInfo.IME_ACTION_DONE, KeyEvent.KEYCODE_BACK));
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
                } catch(NumberFormatException e) { // ignore any non-double
                }
                userIsInTheMiddleOfTypingANumber = false;
            }

            performOperation(buttonTag);
            mEditText.setText(mDF.format(getResult()));
            if(buttonTag.equals("=")) {
            }
        }

    }

}