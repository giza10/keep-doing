package com.hkb48.keepdo.ui.sort

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.hkb48.keepdo.databinding.TaskSortListItemBinding

class SortableListItem(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    fun setText(string: String?) {
        TaskSortListItemBinding.bind(this).textView1.text = string
    }

    val grabberView: View
        get() = TaskSortListItemBinding.bind(this).imageViewSort
}