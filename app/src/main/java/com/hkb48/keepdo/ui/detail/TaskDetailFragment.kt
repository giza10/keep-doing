package com.hkb48.keepdo.ui.detail

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.hkb48.keepdo.R
import com.hkb48.keepdo.Recurrence
import com.hkb48.keepdo.Reminder
import com.hkb48.keepdo.databinding.FragmentTaskDetailBinding
import com.hkb48.keepdo.db.entity.Task
import com.hkb48.keepdo.ui.addedit.AddEditTaskFragmentArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class TaskDetailFragment : Fragment() {
    private val viewModel: TaskDetailViewModel by viewModels()
    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!
    private val args: AddEditTaskFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_detail, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_edit -> {
                val action =
                    TaskDetailFragmentDirections.actionTaskDetailFragmentToAddEditTaskFragment(
                        args.taskId,
                        getString(R.string.edit_task)
                    )
                findNavController().navigate(action)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun subscribeToModel() {
        viewModel.getObservableTask(args.taskId).observe(viewLifecycleOwner, { task ->
            (requireActivity() as AppCompatActivity).supportActionBar?.title = task.name
            updateDetails(task)
        })
    }

    private fun updateDetails(task: Task) = lifecycleScope.launch {
        // Recurrence
        binding.recurrenceView.update(Recurrence.getFromTask(task))

        // Reminder
        val reminderTextView = binding.taskDetailReminderValue
        val reminder = Reminder(task.reminderEnabled, task.reminderTime ?: 0)
        if (reminder.enabled) {
            reminderTextView.text =
                getString(R.string.remind_at, reminder.hourOfDay, reminder.minute)
        } else {
            reminderTextView.setText(R.string.no_reminder)
        }

        // Description
        val descriptionTitleTextView = binding.taskDetailDescription
        val descriptionTextView = binding.taskDetailDescriptionValue
        val descriptionStr = task.description
        if (descriptionStr == null || descriptionStr.isEmpty()) {
            val descriptionLayout = binding.taskDetailDescriptionContainer
            descriptionLayout.visibility = View.GONE
            descriptionTitleTextView.visibility = View.INVISIBLE
            descriptionTextView.visibility = View.INVISIBLE
        } else {
            descriptionTitleTextView.visibility = View.VISIBLE
            descriptionTextView.visibility = View.VISIBLE
            descriptionTextView.text = descriptionStr
        }

        // Total number of done
        val numOfDone = viewModel.getNumberOfDone(task._id!!)
        binding.taskDetailNumOfDoneValue.text = getString(R.string.number_of_times, numOfDone)

        // Current combo / Max combo
        val combo = viewModel.getComboCount(task)
        val maxCombo = viewModel.getMaxComboCount(task)
        binding.taskDetailComboValue.text =
            getString(R.string.current_and_max_combo_num, combo, maxCombo)

        // First date that done is set
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
        val firstDoneDate = viewModel.getFirstDoneDate(task._id)
        if (firstDoneDate != null) {
            binding.taskDetailFirstDoneDateValue.text = dateFormat.format(firstDoneDate)
        } else {
            binding.taskDetailFirstDoneDateContainer.visibility = View.GONE
        }

        // Last date that done is set
        val lastDoneDate = viewModel.getLastDoneDate(task._id)
        if (lastDoneDate != null) {
            binding.taskDetailLastDoneDateValue.text = dateFormat.format(lastDoneDate)
        } else {
            binding.taskDetailLastDoneDateContainer.visibility = View.GONE
        }
    }
}