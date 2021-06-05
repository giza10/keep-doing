package com.hkb48.keepdo.settings

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference

internal class DoneIconPreference(context: Context?, attrs: AttributeSet?) :
    DialogPreference(context, attrs) {
    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getString(index)!!
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        persistString(defaultValue as String?)
    }

    fun updatePreference(newValue: String?) {
        if (callChangeListener(newValue)) {
            persistString(newValue)
        }
    }
}