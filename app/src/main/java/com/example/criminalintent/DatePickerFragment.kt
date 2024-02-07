package com.example.criminalintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.UUID

private const val ARG_DATE = "arg_date"

class DatePickerFragment: DialogFragment() {

    private lateinit var crime: Crime

    interface Callbacks {
        fun onDateSelected(date: Date)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val date = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(ARG_DATE, Date::class.java) as Date
        } else {
            arguments?.getSerializable(ARG_DATE) as Date
        }
        val calendar = Calendar.getInstance()
        calendar.time = date

//        val argument: Long? = arguments?.getLong("ArgDate")
//
//        calendar.time = argument?.let { Date(it) }!!

        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)

        val dateListener = DatePickerDialog.OnDateSetListener { _:
                                                                DatePicker, year: Int, month: Int, day: Int ->
            val resultDate: Date = GregorianCalendar(year, month, day).time
            targetFragment?.let { fragment ->
                (fragment as Callbacks).onDateSelected(resultDate)
            }
        }

        return DatePickerDialog(
            requireContext(),
            dateListener,
            initialYear,
            initialMonth,
            initialDay
        )
    }

    private fun updateUI(date: Date) {
        crime.date = date
    }

    override fun onStop() {
        super.onStop()
    }

    companion object {
        fun newInstance(date: Date): DatePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
            }

            return DatePickerFragment().apply {
                arguments = args
            }
        }
    }


}