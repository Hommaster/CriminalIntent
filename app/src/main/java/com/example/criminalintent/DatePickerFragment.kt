package com.example.criminalintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

private const val ARG_DATE = "arg_date"
private const val ARG_REQUEST_CODE = "requestCode"
private const val RESULT_DATE_KEY = "resultKey"

class DatePickerFragment: DialogFragment() {

    private lateinit var crime: Crime

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

        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)

        val dateListener = DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val resultDate: Date = GregorianCalendar(year, month, dayOfMonth).time

            val result = Bundle().apply {
                putSerializable(RESULT_DATE_KEY, resultDate)
            }

            val resultRequestCode = requireArguments().getString(ARG_REQUEST_CODE, "")
            setFragmentResult(resultRequestCode, result)
        }

        return DatePickerDialog(
            requireContext(),
            dateListener,
            initialYear,
            initialMonth,
            initialDay
        )
    }


    companion object {

        fun newInstance(requestCode: String, date: Date): DatePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
                putString(ARG_REQUEST_CODE, requestCode)
            }

            return DatePickerFragment().apply {
                arguments = args
            }
        }

        fun getSelectedDate(result: Bundle) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            result.getSerializable(RESULT_DATE_KEY, Date::class.java) as Date
        } else {
            result.getSerializable(RESULT_DATE_KEY) as Date
        }
    }


}