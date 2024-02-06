package com.example.criminalintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

class DatePickerFragment: DialogFragment() {

    private lateinit var crime: Crime
    private lateinit var datePastUpdate: Date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val calendar = Calendar.getInstance()

        val argument: Long? = arguments?.getLong("ArgDate")

        calendar.time = argument?.let { Date(it) }!!

        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)

        val dateListener = DatePickerDialog.OnDateSetListener { _:
                                                                DatePicker, year: Int, month: Int, day: Int ->
            val resultDate: Date = GregorianCalendar(year, month, day).time
            datePastUpdate = resultDate
            updateUI(resultDate)
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
        Log.d("MyTag", "$datePastUpdate")
        super.onStop()
    }


}