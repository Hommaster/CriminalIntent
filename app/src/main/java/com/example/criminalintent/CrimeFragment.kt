package com.example.criminalintent

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import java.util.jar.Manifest

private const val REQUEST_DATE = "requestDate"
private const val REQUEST_DATE_1 = "requestDate1"

private const val DATE_FORMAT = "EEE, MMM, dd"

class CrimeFragment: Fragment(), FragmentResultListener {

    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var sendResultButton: Button

    private lateinit var returnWithoutSaving: Button
    private lateinit var buttonDelete: Button

    private lateinit var buttonReport: Button
    private lateinit var suspectButton: Button
    private lateinit var suspectPhoneButton: Button

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this)[CrimeDetailViewModel::class.java]
    }

    private val resultLaunch = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result: ActivityResult ->
        if(result.resultCode == Activity.RESULT_OK) {
            val contactUri: Uri = result.data?.data!!
            val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID)
            val cursor = contactUri.let {
                requireActivity().contentResolver
                    .query(it, queryFields, null, null, null)
            }!!
            cursor.use {
                if(it.count == 0) {
                    return@registerForActivityResult
                }
                it.moveToFirst()
                val suspect = it.getString(0)
                crime.suspect = suspect.toString()
                suspectButton.text = suspect
                crimeDetailViewModel.saveCrime(crime)

                val contactId = it.getString(1)

                // This is the Uri to get a Phone number
                val phoneURI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI

                // phoneNumberQueryFields: a List to return the PhoneNumber Column Only
                val phoneNumberQueryFields = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)

                // phoneWhereClause: A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself)
                val phoneWhereSanya = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?"

                val phoneQueryParameters = arrayOf(contactId)

                val phoneCursor = phoneURI.let {
                    requireActivity().contentResolver
                        .query(it, phoneNumberQueryFields, phoneWhereSanya, phoneQueryParameters, null)
                }

                phoneCursor.use {
                    it?.moveToFirst()
                    val phoneNumValue = it!!.getString(0)
                    suspectPhoneButton.text = phoneNumValue
                }
            }
            val queryNumber = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val cursorPhone = contactUri.let {
                requireActivity().contentResolver
                    .query(it, queryNumber, null, null, null)
            }!!
            cursorPhone.use {
                if(it.count == 0) {
                    return@registerForActivityResult
                }
                it.moveToFirst()
                val number = it.getString(0)
                suspectPhoneButton.text = number
            }
        }
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

        buttonReport = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button
        suspectPhoneButton = view.findViewById(R.id.crime_suspect_phone)

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

        suspectButton.apply {
            com.example.criminalintent.Manifest.permission.
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)

            setOnClickListener {
                resultLaunch.launch(pickContactIntent)
            }

//            val packageManager: PackageManager = requireActivity().packageManager
//            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)
//            if(resolvedActivity == null) {
//                isEnabled = false
//            }
        }

        buttonReport.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, R.string.crime_report_subject)
            }.also {intent ->
                val chooserIntent = Intent.createChooser(intent, getString(R.string.crime_report))
                startActivity(chooserIntent)
            }
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

        if(crime.suspect.isNotEmpty()) {
            suspectButton.text = crime.suspect
        }
    }

    private fun getCrimeReport(): String {
        val solvedString = if(crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val suspectString: String = if(crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        val dateString: String = DateFormat.format(DATE_FORMAT, crime.date).toString()

        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspectString)

    }

    private fun redirectionToListClasses() {
        val action = CrimeFragmentDirections.actionCrimeFragmentToCrimeListFragment()
        findNavController().navigate(action)
    }


}