package com.hkb48.keepdo.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.hkb48.keepdo.R

class BackupRestoreFragment : DialogFragment() {
    interface DialogFragmentResultListener {
        fun onDialogFragmentResult(selectedIndex: Int)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity()).apply {
            setTitle(getString(R.string.backup_restore))
            setPositiveButton(R.string.dialog_start, listener)
            setNegativeButton(android.R.string.cancel, null)
            setSingleChoiceItems(
                R.array.dialog_choice_backup_restore, -1
            ) { dialog: DialogInterface, _: Int ->
                (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
            }

        }.create()
    }

    override fun onResume() {
        super.onResume()
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
    }

    private val listener = DialogInterface.OnClickListener { dialog, _ ->
        val selectedIndex = (dialog as AlertDialog).listView.checkedItemPosition
        val listener = activity as DialogFragmentResultListener
        listener.onDialogFragmentResult(selectedIndex)
    }
}