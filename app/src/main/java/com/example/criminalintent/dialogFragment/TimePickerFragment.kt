package com.example.criminalintent.dialogFragment

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.criminalintent.constance.Constance
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

@Suppress("DEPRECATION")
class TimePickerFragment: DialogFragment() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val date: Date = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(Constance.ARG_TIME, Date::class.java) as Date
        } else {
            arguments?.getSerializable(Constance.ARG_TIME) as Date
        }

        val calendar = Calendar.getInstance()
        calendar.time = date

        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH) + 1
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)

        val initialHour = calendar.get(Calendar.HOUR)
        val initialMinute = calendar.get(Calendar.MINUTE)
        val initialSecond = calendar.get(Calendar.SECOND)


        val timeListener = TimePickerDialog.OnTimeSetListener { _: TimePicker, hourOfDay, minute ->

            val current = LocalDateTime.of(
                initialYear,
                initialMonth,
                initialDay,
                hourOfDay,
                minute,
                initialSecond
            )

            val instant = Timestamp.valueOf(current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).toInstant()
            val dateRes = Date.from(instant)

            Log.d("TPFdate", "$dateRes")

            val result = Bundle().apply {
                putSerializable(Constance.RESULT_TIME_KEY, dateRes)
            }

            val resultRequestCode = requireArguments().getString(Constance.ARG_REQUEST_CODE_TIME, "")
            setFragmentResult(resultRequestCode, result)

        }

        return TimePickerDialog(
            requireContext(),
            timeListener,
            initialHour,
            initialMinute,
            false
        )
    }

    companion object {
        fun newInstance(date: Date, requestCode: String): TimePickerFragment {
            val args = Bundle().apply {
                putSerializable(Constance.ARG_TIME, date)
                putString(Constance.ARG_REQUEST_CODE_TIME, requestCode)
            }

            return TimePickerFragment().apply {
                arguments = args
            }
        }

        fun getSelectedDate(result: Bundle) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            result.getSerializable(Constance.RESULT_TIME_KEY, Date::class.java) as Date
        } else {
            result.getSerializable(Constance.RESULT_TIME_KEY) as Date
        }
    }

}