package com.example.criminalintent.dialogFragment

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

@Suppress("DEPRECATION")
class TimePickerFragment: DialogFragment() {

    private val args: TimePickerFragmentArgs by navArgs()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val calendar = Calendar.getInstance()
        calendar.time = args.crimeDateForTime

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

            setFragmentResult(REQUEST_KEY_TIME, bundleOf(REQUEST_BUNDLE_TIME to dateRes))

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
        const val REQUEST_KEY_TIME = "request_key_time"
        const val REQUEST_BUNDLE_TIME = "request_bundle_time"
    }

}