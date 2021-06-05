package com.hkb48.keepdo.settings

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.Preference

internal class RingtonePreference(context: Context?, attrs: AttributeSet?) :
    Preference(context, attrs) {
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