package com.example.todoapp.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.todoapp.databinding.FragmentAddTodoPopupBinding
import com.example.todoapp.utils.ToDoData
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class AddTodoPopupFragment : DialogFragment() {

    private lateinit var binding: FragmentAddTodoPopupBinding
    private var listener: DialogNextBtnClickListener? = null
    private var toDoData: ToDoData? = null

    private var selectedDate: String? = null
    private var selectedTime: String? = null



    companion object {
        const val TAG = "AddTodoPopupFragment"

        @JvmStatic
        fun newInstance(taskId: String, task: String) = AddTodoPopupFragment().apply {
            arguments = Bundle().apply {
                putString("taskId", taskId)
                putString("task", task)
            }
        }
    }

    fun setListener(listener: DialogNextBtnClickListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddTodoPopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val taskId = it.getString("taskId")
            val task = it.getString("task")

            if (taskId != null && task != null) {
                toDoData = ToDoData(taskId, task)
                binding.todoEt.setText(task)
            }
        }

        registerEvents()
    }

    private fun registerEvents() {

        val calendar = Calendar.getInstance()

        binding.dateCard.setOnClickListener {
            val dp = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    // Format selected date
                    val selectedCal = Calendar.getInstance()
                    selectedCal.set(year, month, day, 0, 0, 0)

                    val today = Calendar.getInstance()
                    today.set(Calendar.HOUR_OF_DAY, 0)
                    today.set(Calendar.MINUTE, 0)
                    today.set(Calendar.SECOND, 0)
                    today.set(Calendar.MILLISECOND, 0)

                    // Check if selected date is in the past
                    if (selectedCal.before(today)) {
                        Toast.makeText(context, "Cannot select past date", Toast.LENGTH_SHORT).show()
                    } else {
                        selectedDate = "$day/${month + 1}/$year"
                        binding.todoDate.text = selectedDate
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // Prevent past dates from being selectable
            dp.datePicker.minDate = System.currentTimeMillis() - 1000  // today

            dp.show()
        }


        binding.timeCard.setOnClickListener {
            val tp = TimePickerDialog(
                requireContext(),
                { _, hour, min ->
                    selectedTime = String.format("%02d:%02d", hour, min)
                    binding.todoTime.text = selectedTime
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            tp.show()
        }

        binding.todoNextBtn.setOnClickListener {
            val todoTask = binding.todoEt.text.toString().trim()

            if (todoTask.isEmpty()) {
                Toast.makeText(context, "Please Enter Task", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedDate.isNullOrEmpty() || selectedTime.isNullOrEmpty()) {
                Toast.makeText(context, "Please select Date and Time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (toDoData == null) {
                listener?.onSaveTask(todoTask,  selectedDate!!, selectedTime!!,binding.todoEt)

            } else {
                toDoData!!.task = todoTask
                listener?.onUpdateTask(toDoData!!,  selectedDate!!, selectedTime!!, binding.todoEt)
            }

            dismiss()
        }

        binding.todoClose.setOnClickListener {
            dismiss()
        }
    }



    interface DialogNextBtnClickListener {
        fun onSaveTask(todo: String, date: String, time: String,  todoEt: TextInputEditText )
        fun onUpdateTask(toDoData: ToDoData, date: String, time: String, todoEt: TextInputEditText)
    }
}
