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
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.airbitz.R;

/**
 * Created by tom on 10/14/14.
 */
public class Numberpad extends LinearLayout {
    // operator types
    private static final String DIGITS = "0123456789";
    View mView;
    private EditText mEditText;
    ImageButton b1, b2, b3, b4, b5, b6, b7, b8, b9, b0, bBack;

    public Numberpad(Context context) {
        super(context);
        initialize();
    }

    public Numberpad(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public Numberpad(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    // setup listeners
    private void initialize() {
        final View.OnClickListener listener = new View.OnClickListener() {
            public void onClick(View v) {
                handleClick(v);
            }
        };

        mView = inflate(getContext(), R.layout.numberpad, this);
        b0 = (ImageButton) mView.findViewById(R.id.numberpadButton0);
        b1 = (ImageButton) mView.findViewById(R.id.numberpadButton1);
        b2 = (ImageButton) mView.findViewById(R.id.numberpadButton2);
        b3 = (ImageButton) mView.findViewById(R.id.numberpadButton3);
        b4 = (ImageButton) mView.findViewById(R.id.numberpadButton4);
        b5 = (ImageButton) mView.findViewById(R.id.numberpadButton5);
        b6 = (ImageButton) mView.findViewById(R.id.numberpadButton6);
        b7 = (ImageButton) mView.findViewById(R.id.numberpadButton7);
        b8 = (ImageButton) mView.findViewById(R.id.numberpadButton8);
        b9 = (ImageButton) mView.findViewById(R.id.numberpadButton9);
        bBack = (ImageButton) mView.findViewById(R.id.numberpadButtonBack);

        b0.setOnClickListener(listener);
        b1.setOnClickListener(listener);
        b2.setOnClickListener(listener);
        b3.setOnClickListener(listener);
        b4.setOnClickListener(listener);
        b5.setOnClickListener(listener);
        b6.setOnClickListener(listener);
        b7.setOnClickListener(listener);
        b8.setOnClickListener(listener);
        b9.setOnClickListener(listener);
        bBack.setOnClickListener(listener);
    }



    // This is where the text enters and the results return
    public void setEditText(EditText editText) {
        mEditText = editText;
    }

    public String getResult() {
        return mEditText.getText().toString();
    }

    public void handleClick(View v) {
        if (mEditText == null)
            return;

        Editable editable = mEditText.getText();
        int start = mEditText.getSelectionStart();
        // delete the selection, if chars are selected:
        int end = mEditText.getSelectionEnd();
        if (end > start) {
            editable.delete(start, end);
        }
        String buttonTag = v.getTag().toString();

        if (buttonTag.equals("back")) {
            String s = mEditText.getText().toString();
            if (s.length() == 1) { // 1 character, just set to 0
                mEditText.setText("");
            } else if (s.length() > 1) {
                mEditText.setText(s.substring(0, s.length() - 1));
            }
        } else if (DIGITS.contains(buttonTag)) {
            mEditText.append(buttonTag);
        }
    }

}
