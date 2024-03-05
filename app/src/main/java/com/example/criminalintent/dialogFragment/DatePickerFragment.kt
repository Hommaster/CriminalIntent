package com.example.criminalintent.dialogFragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.example.criminalintent.constance.Constance
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

class DatePickerFragment: DialogFragment() {

    private val args: DatePickerFragmentArgs by navArgs()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        calendar.time = args.crimeDate

        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)

        val initialHour = calendar.get(Calendar.HOUR)
        val initialMinute = calendar.get(Calendar.MINUTE)
        val initialSecond = calendar.get(Calendar.SECOND)

        val dateListener = DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val current = LocalDateTime.of(
                year,
                month + 1,
                dayOfMonth,
                initialHour,
                initialMinute,
                initialSecond
            )

            val instant = Timestamp.valueOf(current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).toInstant()
            val dateRes = Date.from(instant)

            setFragmentResult(REQUEST_KEY_DATE, bundleOf(REQUEST_BUNDLE_DATE to dateRes))
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
        const val REQUEST_KEY_DATE = "request_key_date"
        const val REQUEST_BUNDLE_DATE = "request_bundle_date"
    }


}