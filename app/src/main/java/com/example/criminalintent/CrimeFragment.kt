package com.example.criminalintent

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.text.SimpleDateFormat
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.doOnLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.example.criminalintent.constance.Constance
import com.example.criminalintent.databinding.FragmentCrimeBinding
import com.example.criminalintent.dialogFragment.DatePickerFragment
import com.example.criminalintent.dialogFragment.TimePickerFragment
import com.example.criminalintent.utils.getScaledBitmap
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

class CrimeFragment: Fragment(){

    private var _binding : FragmentCrimeBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val args: CrimeFragmentArgs by navArgs()

    private val crimeDetailViewModel: CrimeDetailViewModel by viewModels {
        CrimeDetailViewModelFactory(args.myArg)
    }

    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private val selectSuspect = registerForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let {
            parseContactSelection(it)
        }
    }

    private var photoName: String? = null

    private val takePhotoLaunch = registerForActivityResult(ActivityResultContracts.TakePicture())
    { didTakePhoto: Boolean ->
        if(didTakePhoto && photoName != null) {
            crimeDetailViewModel.updateCrime { oldCrime ->
                oldCrime.copy(photoFileName = photoName)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerPermissionListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrimeBinding.inflate(inflater, container, false)
        binding.crimeSuspect.setOnClickListener {
            checkPermissionReadContacts()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            crimeDetailViewModel.crime.collect {crime ->
                crime?.let { updateUi(it) }
            }
        }
    }

    binding.apply {
        crimeTitle.doOnTextChanged { text, _, _, _ ->
            crimeDetailViewModel.updateCrime { oldCrime ->
                oldCrime.copy(title = text.toString())
            }
        }

        crimeSolved.setOnCheckedChangeListener { _, isChecked ->
            crimeDetailViewModel.updateCrime { oldCrime ->
                oldCrime.copy(isSolved = isChecked)
            }
        }

        crimeCamera.setOnClickListener{
            photoName = "IMG_${Date()}.JPG"
            val photoFile = File(requireContext().applicationContext.filesDir, photoName.toString())

            val photoUri = FileProvider.getUriForFile(
                requireContext(),
                "com.example.criminalintent.fileprovider",
                photoFile
            )
            takePhotoLaunch.launch(photoUri)
        }
    }

    setFragmentResultListener(
        DatePickerFragment.REQUEST_KEY_DATE
    ) { _, bundle ->
        resultFromPickerFragment(DatePickerFragment.REQUEST_BUNDLE_DATE, bundle)
    }
    setFragmentResultListener(
        TimePickerFragment.REQUEST_KEY_TIME
    ) { _, bundle ->
        resultFromPickerFragment(TimePickerFragment.REQUEST_BUNDLE_TIME, bundle)
        }
    }
    private fun updateUi(crime: Crime) {
        binding.apply {
            if(crimeTitle.text.toString() != crime.title) {
                crimeTitle.setText(crime.title)
            }

            val simpleDateFormat = SimpleDateFormat("EE, MMM, dd, yyyy", Locale("ru"))
            val date : String = simpleDateFormat.format(crime.date).toString()
            crimeDate.text = date
            crimeDate.setOnClickListener {
                val action = CrimeFragmentDirections.actionCrimeFragmentToDatePickerFragment(crime.date)
                findNavController().navigate(action)
            }

            val simpleTimeFormat = SimpleDateFormat("HH:mm:ss", Locale("ru"))
            val time : String = simpleTimeFormat.format(crime.date).toString()
            crimeTime.text = time
            crimeTime.setOnClickListener {
            val action = CrimeFragmentDirections.actionCrimeFragmentToTimePickerFragment(crime.date)
            findNavController().navigate(action)
            }

            buttonDeleteThisClasses.setOnClickListener {
                redirectionToListClasses()
                viewLifecycleOwner.lifecycleScope.launch{
                    crimeDetailViewModel.deleteCrime(crime)
                }
            }

            crimeReport.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport(crime))
                putExtra(Intent.EXTRA_SUBJECT, R.string.crime_report_subject)
                }.also {intent ->
                    val chooserIntent = Intent.createChooser(intent, getString(R.string.crime_report))
                    startActivity(chooserIntent)
                }
            }

            crimePhoto.setOnClickListener{
                if(crime.photoFileName != null) {
                    val action = CrimeFragmentDirections.actionCrimeFragmentToPictureDialogFragment(crime.photoFileName.toString())
                    findNavController().navigate(action)
                }
            }

            crimeSuspectPhone.setOnClickListener {
                if(crime.phone.isNotEmpty()) {
                    val callContactIntent =
                        Intent(Intent.ACTION_DIAL).apply {
                            val phone = crime.phone
                            data = Uri.parse("tel:$phone")
                        }
                    startActivity(callContactIntent)
                }
            }

            crimeSolved.isChecked = crime.isSolved

            if(crime.suspect.isNotEmpty()) {
                crimeSuspect.text = crime.suspect
            }

            updatePhoto(crime.photoFileName)
        }
    }

    private fun getCrimeReport(crime:Crime): String {
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


    private fun parseContactSelection(contactUri: Uri) {
        val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID)
        val queryCursor = requireActivity().contentResolver
            .query(contactUri, queryFields, null, null, null)
        queryCursor?.use {cursor ->
            if(cursor.moveToFirst()) {
                val suspect = cursor.getString(0)
                binding.crimeSuspect.text = suspect
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(suspect = suspect)
                }

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
                    crimeDetailViewModel.updateCrime { oldCrime ->
                        oldCrime.copy(phone = phoneNumValue)
                    }
                }
            }
        }
    }

    private fun updatePhoto(photoFileName: String?) {
        if(binding.crimePhoto.tag != photoFileName) {
            val photoFile = photoFileName?.let {
                File(requireContext().applicationContext.filesDir, it)
            }
            if(photoFile?.exists() == true) {
                binding.crimePhoto.doOnLayout { measuredView ->
                    val scaledBitmap = getScaledBitmap(
                        photoFile.path,
                        measuredView.width,
                        measuredView.height
                    )
                    binding.crimePhoto.setImageBitmap(scaledBitmap)
                    binding.crimePhoto.tag = photoFileName
                }
            } else {
                binding.crimePhoto.setImageBitmap(null)
                binding.crimePhoto.tag = null
            }
        }
    }

    private fun resultFromPickerFragment(requestBundle: String, bundle: Bundle) {
        val newDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getSerializable(requestBundle, Date::class.java)
        } else {
            @Suppress("DEPRECATION")
            bundle.getSerializable(requestBundle) as Date
        }
        crimeDetailViewModel.updateCrime {
            it.copy(date = newDate!!)
        }
    }

}