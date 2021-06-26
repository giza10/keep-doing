package com.hkb48.keepdo.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceDialogFragmentCompat
import com.hkb48.keepdo.R

class DoneIconDialogFragment : PreferenceDialogFragmentCompat() {
    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        super.onPrepareDialogBuilder(builder)
    }

    override fun onCreateDialogView(context: Context): View {
        val listener = OnClickListener()
        return View.inflate(requireContext(), R.layout.done_icon_selector, null).apply {
            findViewById<View>(R.id.done_icon_1).setOnClickListener(listener)
            findViewById<View>(R.id.done_icon_2).setOnClickListener(listener)
            findViewById<View>(R.id.done_icon_3).setOnClickListener(listener)
            findViewById<View>(R.id.done_icon_4).setOnClickListener(listener)
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        // Do nothing
    }

    private inner class OnClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            requireDialog().dismiss()
            val newValue: String = when (v.id) {
                R.id.done_icon_2 -> "type2"
                R.id.done_icon_3 -> "type3"
                R.id.done_icon_4 -> "type4"
                else -> "type1"
            }
            (preference as DoneIconPreference).updatePreference(newValue)
        }
    }

    companion object {
        fun newInstance(key: String?): DoneIconDialogFragment {
            val fragment = DoneIconDialogFragment()
            val bundle = Bundle(1)
            bundle.putString(ARG_KEY, key)
            fragment.arguments = bundle
            return fragment
        }
    }
}