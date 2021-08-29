package com.hkb48.keepdo.ui.addedit

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs

class ReminderTimePickerDialogFragment : DialogFragment() {
    private val args: ReminderTimePickerDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return TimePickerDialog(requireContext(), listener, args.hour, args.minute, true)
    }

    private val listener =
        TimePickerDialog.OnTimeSetListener { _: TimePicker, hourOfDay: Int, minute: Int ->
            val data = bundleOf(
                "hour" to hourOfDay,
                "minute" to minute
            )
            parentFragmentManager.setFragmentResult("key-reminder", data)
        }
}