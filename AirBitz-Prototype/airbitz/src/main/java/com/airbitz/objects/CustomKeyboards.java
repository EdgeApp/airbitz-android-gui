package com.airbitz.objects;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.view.View;
import android.widget.LinearLayout;

import com.airbitz.R;

/**
 * Created on 2/24/14.
 */
public class CustomKeyboards extends InputMethodService implements KeyboardView.OnKeyboardActionListener{

    private LinearLayout mInputView;

    @Override
    public View onCreateInputView() {
        mInputView = (LinearLayout) getLayoutInflater().inflate(R.layout.keyboard_layout, null);
        return mInputView;

    }

    @Override
    public void onPress(int i) {

    }

    @Override
    public void onRelease(int i) {

    }

    @Override
    public void onKey(int i, int[] ints) {

    }

    @Override
    public void onText(CharSequence charSequence) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }
}
