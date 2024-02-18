package com.example.criminalintent

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

private const val REQUEST_DATE = "requestDate"
private const val REQUEST_DATE_1 = "requestDate1"

class CrimeFragment: Fragment(), FragmentResultListener {

    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var sendResultButton: Button
    private lateinit var returnWithoutSaving: Button
    private lateinit var buttonDelete: Button

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this)[CrimeDetailViewModel::class.java]
    }

    //result from DatePickerFragment(Request_date) and TimePickerFragment(Request_date_1)
    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when(requestKey) {
            REQUEST_DATE -> {
                crime.date = DatePickerFragment.getSelectedDate(result)
                updateUI()
            }

            REQUEST_DATE_1 -> {
                crime.date = TimePickerFragment.getSelectedDate(result)
                updateUI()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()

        // from CrimeListFragment using Navigation get ID crime
        val crimeID: UUID = UUID.fromString(arguments?.getString("myArg"))

        //create new crime with crimeLiveData crimeID
        crimeDetailViewModel.loadCrime(crimeID)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        timeButton = view.findViewById(R.id.crime_time) as Button
        sendResultButton = view.findViewById(R.id.create_button) as Button
        returnWithoutSaving = view.findViewById(R.id.button_return_without_saving) as Button
        buttonDelete = view.findViewById(R.id.button_delete_this_classes) as Button

        dateButton.setOnClickListener {
            DatePickerFragment
                .newInstance(REQUEST_DATE, crime.date)
                .show(childFragmentManager, REQUEST_DATE)

        }

        timeButton.setOnClickListener {
            TimePickerFragment
                .newInstance(crime.date, REQUEST_DATE_1)
                .show(childFragmentManager, REQUEST_DATE_1)
        }

        sendResultButton.setOnClickListener {
            crimeDetailViewModel.saveCrime(crime)
            redirectionToListClasses()
        }

        returnWithoutSaving.setOnClickListener {
            redirectionToListClasses()
        }

        buttonDelete.setOnClickListener {
            redirectionToListClasses()
            crimeDetailViewModel.deleteCrime(crime)
        }

        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //load from crimeLiveData crime
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            Observer {crime->
                crime?.let {
                    this.crime = crime
                    updateUI()
                }
            }
        )
        childFragmentManager.setFragmentResultListener(REQUEST_DATE, viewLifecycleOwner, this)
        childFragmentManager.setFragmentResultListener(REQUEST_DATE_1, viewLifecycleOwner, this)
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        }
        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }

    }

    private fun updateUI() {
        titleField.setText(crime.title)

        val simpleDateFormat = SimpleDateFormat("EE, MMM, dd, yyyy", Locale("ru"))
        val date : String = simpleDateFormat.format(this.crime.date).toString()
        dateButton.text = date

        val simpleTimeFormat = SimpleDateFormat("HH:mm:ss", Locale("ru"))
        val time : String = simpleTimeFormat.format(this.crime.date).toString()
        timeButton.text = time

        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
    }

    private fun redirectionToListClasses() {
        val action = CrimeFragmentDirections.actionCrimeFragmentToCrimeListFragment()
        findNavController().navigate(action)
    }


}