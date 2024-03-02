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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import android.Manifest
import android.content.pm.PackageManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.doOnLayout
import com.example.criminalintent.constance.Constance
import com.example.criminalintent.dialogFragment.DatePickerFragment
import com.example.criminalintent.dialogFragment.TimePickerFragment
import com.example.criminalintent.utils.getScaledBitmap
import java.io.File
import java.util.Date

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

    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this)[CrimeDetailViewModel::class.java]
    }

    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    //result from Contacts: name and number of contacts
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
                    crime.phone = phoneNumValue
                    crimeDetailViewModel.saveCrime(crime)
                }
            }
        }
    }

    private val selectSuspect = registerForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let {
            parseContactSelection(it)
        }
    }

    private val takePhotoLaunch = registerForActivityResult(ActivityResultContracts.TakePicture())
    { didTakePhoto: Boolean ->
        if(didTakePhoto && photoName != null) {
            crime.photoFileName = photoName
            crimeDetailViewModel.saveCrime(crime)
        }
    }

    private var photoName: String? = null

    //result from DatePickerFragment(Request_date) and TimePickerFragment(Request_date_1)
    //**result from Camera**
    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when(requestKey) {
            Constance.REQUEST_DATE -> {
                crime.date = DatePickerFragment.getSelectedDate(result)
                updateUI()
            }

            Constance.REQUEST_DATE_1 -> {
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
        registerPermissionListener()
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        titleField = view.findViewById<EditText>(R.id.crime_title)
        dateButton = view.findViewById<Button>(R.id.crime_date)
        timeButton = view.findViewById<Button>(R.id.crime_time)
        sendResultButton = view.findViewById<Button>(R.id.create_button)
        returnWithoutSaving = view.findViewById<Button>(R.id.button_return_without_saving)
        buttonDelete = view.findViewById<Button>(R.id.button_delete_this_classes)

        buttonReport = view.findViewById<Button>(R.id.crime_report)
        suspectButton = view.findViewById<Button>(R.id.crime_suspect)
        suspectPhoneButton = view.findViewById<Button>(R.id.crime_suspect_phone)

        photoButton = view.findViewById<ImageButton>(R.id.crime_camera)
        photoView = view.findViewById<ImageView>(R.id.crime_photo)

        dateButton.setOnClickListener {
            DatePickerFragment
                .newInstance(Constance.REQUEST_DATE, crime.date)
                .show(childFragmentManager, Constance.REQUEST_DATE)

        }

        timeButton.setOnClickListener {
            TimePickerFragment
                .newInstance(crime.date, Constance.REQUEST_DATE_1)
                .show(childFragmentManager, Constance.REQUEST_DATE_1)
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

        suspectButton.setOnClickListener {
            checkPermissionReadContacts()
        }

        suspectPhoneButton.setOnClickListener {
            val callContactIntent =
                Intent(Intent.ACTION_DIAL).apply {
                    val phone = crime.phone
                    data = Uri.parse("tel:$phone")
                }
            startActivity(callContactIntent)
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

        photoButton.setOnClickListener{
            photoName = "IMG_${Date()}.JPG"
            val photoFile = File(requireContext().applicationContext.filesDir, photoName.toString())

            val photoUri = FileProvider.getUriForFile(
                requireContext(),
                "com.example.criminalintent.fileprovider",
                photoFile
            )
            takePhotoLaunch.launch(photoUri)
        }

        photoView.setOnClickListener{
            val action = CrimeFragmentDirections.actionCrimeFragmentToPictureDialogFragment(crime.photoFileName!!)
            findNavController().navigate(action)
        }

        solvedCheckBox = view.findViewById<CheckBox>(R.id.crime_solved)

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
        createChildFM(Constance.REQUEST_DATE)
        createChildFM(Constance.REQUEST_DATE_1)
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
        suspectPhoneButton.text = crime.phone

        updatePhoto(crime.photoFileName)
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

        val dateString: String = DateFormat.format(Constance.DATE_FORMAT, crime.date).toString()

        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspectString)

    }

    private fun redirectionToListClasses() {
        val action = CrimeFragmentDirections.actionCrimeFragmentToCrimeListFragment()
        findNavController().navigate(action)
    }

    private fun redirectionToContactsPhone(){
        selectSuspect.launch(null)
    }


    private fun checkPermissionReadContacts() {
        when{
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED -> {
                redirectionToContactsPhone()
                    }
            else -> {
                permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    private fun registerPermissionListener() {
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if(it) {
                redirectionToContactsPhone()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun createChildFM(requestDate: String) {
        childFragmentManager.setFragmentResultListener(requestDate, viewLifecycleOwner, this)
    }

    private fun parseContactSelection(contactUri: Uri) {
        val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID)
        val queryCursor = requireActivity().contentResolver
            .query(contactUri, queryFields, null, null, null)
        queryCursor?.use {cursor ->
            if(cursor.moveToFirst()) {
                val suspect = cursor.getString(0)
                crime.suspect = suspect
                suspectButton.text = suspect
                crimeDetailViewModel.saveCrime(crime)

                val contactId = cursor.getString(1)

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
                    crime.phone = phoneNumValue
                    crimeDetailViewModel.saveCrime(crime)
                }
            }
        }
    }

    private fun updatePhoto(photoFileName: String?) {
        if(photoView.tag != photoFileName) {
            val photoFile = photoFileName?.let {
                File(requireContext().applicationContext.filesDir, it)
            }
            if(photoFile?.exists() == true) {
                photoView.doOnLayout { measuredView ->
                    val scaledBitmap = getScaledBitmap(
                        photoFile.path,
                        measuredView.width,
                        measuredView.height
                    )
                    photoView.setImageBitmap(scaledBitmap)
                    photoView.tag = photoFileName
                }
            } else {
                photoView.setImageBitmap(null)
                photoView.tag = null
            }
        }
    }

}