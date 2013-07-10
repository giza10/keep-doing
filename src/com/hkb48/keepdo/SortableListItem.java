package com.hkb48.keepdo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

class SortableListItem extends LinearLayout {
    public SortableListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setText(String string) {
        TextView textView = (TextView) findViewById(R.id.textView1);
        textView.setText(string);
    }

    public View getGrabberView() {
        return findViewById(R.id.imageViewSort);
    }
}
