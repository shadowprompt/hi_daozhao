package com.daozhao.hello.ui.crime

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.daozhao.hello.CONST
import com.daozhao.hello.model.Crime
import com.daozhao.hello.R
import com.daozhao.hello.model.CrimeDetailViewModel
import java.util.*

class CrimeFragment: Fragment(), DatePickerFragment.Callback {
    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var listButton: Button

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this).get(CrimeDetailViewModel::class.java)
    }

    private var callback: Callback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as Callback
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        crime = Crime();

        val crimeId: UUID = arguments?.getSerializable(CONST.ARG_CRIME_ID) as UUID

        Log.d("Crime_Fragment", "args bundle crime ID; $crimeId")

        crimeDetailViewModel.loadCrime(crimeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)

        titleField = view.findViewById(R.id.crime_title) as EditText

        dateButton = view.findViewById(R.id.crime_date) as Button

        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox

        listButton = view.findViewById(R.id.crime_list) as Button

//        dateButton.apply {
//            text = crime.date.toString()
//
//            isEnabled = false
//        }
        listButton.setOnClickListener {
            // 通过activity方法切换fragment
            callback?.onOpenList()
        }


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { crime ->
                crime?.let {
                    this.crime = crime
                    updateUI()
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object :  TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //                TODO("Not yet implemented")
//                Log.d("CCX", "改变前 ${s.toString()}")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
//                TODO("Not yet implemented")
            }
        }

        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener {
                    _, isChecked ->
                crime.isSolved = isChecked
            }
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(this@CrimeFragment.requireFragmentManager(), CONST.DIALOG_DATE)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = crime.date.toString()

        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
    }

    interface Callback {
        fun onOpenList()
    }


    companion object {
        val REQUEST_DATE = 0
        // 现在创建CrimeFragment就必须要传入crimeId
        fun newInstance(crimeId: UUID): CrimeFragment {
            var args = Bundle().apply {
                putSerializable(CONST.ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }
}