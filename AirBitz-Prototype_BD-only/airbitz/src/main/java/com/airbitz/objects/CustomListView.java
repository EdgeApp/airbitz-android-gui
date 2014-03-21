package com.airbitz.objects;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created on 2/21/14.
 */
public class CustomListView extends ListView{

    public CustomListView(Context context){
        super(context);
    }


    public CustomListView(Context context,AttributeSet attributeSet){
        super(context, attributeSet);
    }

    public CustomListView(Context context, AttributeSet attributeSet, int defStyle){
        super(context, attributeSet, defStyle);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }
}
