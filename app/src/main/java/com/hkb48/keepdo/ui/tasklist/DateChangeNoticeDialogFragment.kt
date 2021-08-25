package com.hkb48.keepdo.ui.tasklist

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.hkb48.keepdo.R

class DateChangeNoticeDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity()).apply {
            setMessage(R.string.date_changed)
            setPositiveButton(android.R.string.ok, null)
            setCancelable(false)
        }.create()
    }
}