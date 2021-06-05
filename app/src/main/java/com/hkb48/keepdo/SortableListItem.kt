package com.hkb48.keepdo

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

class SortableListItem(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    fun setText(string: String?) {
        val textView = findViewById<TextView>(R.id.textView1)
        textView.text = string
    }

    val grabberView: View
        get() = findViewById(R.id.imageViewSort)
}