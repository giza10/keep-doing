package com.hkb48.keepdo.ui.addedit

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.hkb48.keepdo.R

class RecurrenceDialogFragment : DialogFragment() {
    private lateinit var recurrenceFlags: BooleanArray

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        recurrenceFlags = arguments?.getBooleanArray("recurrence") ?: BooleanArray(7)
        val weekNames = resources.getStringArray(R.array.week_names)
        return AlertDialog.Builder(requireActivity()).apply {
            setTitle(getString(R.string.recurrence))
            setMultiChoiceItems(
                weekNames,
                recurrenceFlags
            ) { _: DialogInterface?, which: Int, isChecked: Boolean ->
                recurrenceFlags[which] = isChecked
            }
            setPositiveButton(android.R.string.ok, listener)
            setNegativeButton(android.R.string.cancel, null)
        }.create()
    }

    private val listener = DialogInterface.OnClickListener { _, _ ->
        val data = bundleOf("recurrence" to recurrenceFlags)
        parentFragmentManager.setFragmentResult("key-recurrence", data)
    }
}