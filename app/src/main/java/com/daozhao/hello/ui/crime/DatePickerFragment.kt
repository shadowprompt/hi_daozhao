package com.daozhao.hello.ui.crime

import android.app.DatePickerDialog
import android.app.Dialog
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.daozhao.hello.CONST
import java.util.*

class DatePickerFragment: DialogFragment() {
    // 初始化日期值
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val date = arguments?.getSerializable(CONST.ARG_DATE) as Date

        val calendar = Calendar.getInstance()
        calendar.time = date

        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDate = calendar.get(Calendar.DAY_OF_MONTH)

        // 给DatePickerDialog添加一个监听器，把日期发回给CrimeFragment
        val dateListener = DatePickerDialog.OnDateSetListener {
            _: DatePicker, year: Int, month: Int, day: Int ->
            val resultDate: Date = GregorianCalendar(year, month, day).time

            targetFragment?.let { fragment ->
                (fragment as Callback).onDateSelected(resultDate)
            }
        }

        return DatePickerDialog(requireContext(), dateListener, initialYear, initialMonth, initialDate)
    }

    interface Callback {
        fun onDateSelected(date: Date)
    }
    
    companion object {
        fun newInstance(date: Date): DatePickerFragment {
            val args = Bundle().apply {
                putSerializable(CONST.ARG_DATE, date)
            }

            return DatePickerFragment().apply {
                arguments = args
            }
        }
    }
}