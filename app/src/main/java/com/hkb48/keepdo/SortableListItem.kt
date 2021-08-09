package com.hkb48.keepdo

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.hkb48.keepdo.databinding.TaskSortingListItemBinding

class SortableListItem(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    fun setText(string: String?) {
        TaskSortingListItemBinding.bind(this).textView1.text = string
    }

    val grabberView: View
        get() = TaskSortingListItemBinding.bind(this).imageViewSort
}