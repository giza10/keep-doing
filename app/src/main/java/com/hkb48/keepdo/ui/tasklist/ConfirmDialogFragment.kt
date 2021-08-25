package com.hkb48.keepdo.ui.tasklist

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.hkb48.keepdo.R

class ConfirmDialogFragment : DialogFragment() {
    private val args: ConfirmDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireActivity()).apply {
            setMessage(R.string.delete_confirmation)
            setPositiveButton(android.R.string.ok, listener)
            setNegativeButton(android.R.string.cancel, null)
        }.create()
    }

    private val listener = DialogInterface.OnClickListener { _, _ ->
        val data = bundleOf("id" to args.selectedItemId)
        parentFragmentManager.setFragmentResult("confirm", data)
    }
}