package com.hkb48.keepdo.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceDialogFragmentCompat
import com.hkb48.keepdo.R
import com.hkb48.keepdo.databinding.SettingsDoneIconSelectorBinding

class DoneIconDialogFragment : PreferenceDialogFragmentCompat() {
    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        super.onPrepareDialogBuilder(builder)
    }

    override fun onCreateDialogView(context: Context): View {
        val listener = OnClickListener()
        val binding = SettingsDoneIconSelectorBinding.inflate(layoutInflater).apply {
            doneIcon1.setOnClickListener(listener)
            doneIcon2.setOnClickListener(listener)
            doneIcon3.setOnClickListener(listener)
            doneIcon4.setOnClickListener(listener)
        }
        return binding.root
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