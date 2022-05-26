package com.hfad.criminalintent

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_TIME = "time"
class TimePickerFragment : DialogFragment() {

    interface CallBbacks {
        fun onTimeSelected(date: Date)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val date = arguments?.getSerializable(ARG_TIME) as Date
        val clock = TimePicker(requireContext())

        clock.hour = date.hours
        clock.minute = date.minutes

        val initial_hour = clock.hour
        val initial_minute = clock.minute

        val timeListener = TimePickerDialog.OnTimeSetListener {
                _:TimePicker, hour : Int, minute : Int ->
            val resultTime : Date = GregorianCalendar(date.year + 1900,
                date.month, date.day, hour, minute, 0 ).time
            targetFragment?.let {  fragment ->
                (fragment as CallBbacks).onTimeSelected(resultTime)
            }
        }
        return TimePickerDialog(
            requireContext(),
            timeListener,
            initial_hour,
            initial_minute,
            true
        )
    }

    companion object {
        fun newInstance(date : Date): TimePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_TIME, date)
            }
            return TimePickerFragment().apply {
                arguments = args
            }
        }
    }

}