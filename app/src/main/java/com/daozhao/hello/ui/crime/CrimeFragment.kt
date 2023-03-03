package com.daozhao.hello.ui.crime

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.daozhao.hello.CONST
import com.daozhao.hello.R
import com.daozhao.hello.getScaledBitmap
import com.daozhao.hello.model.Crime
import com.daozhao.hello.model.CrimeDetailViewModel
import java.io.File
import java.util.*


private const val REQUEST_DATE = 0
private const val REQUEST_CONTACT = 1
private const val REQUEST_PHOTO = 2
private const val REQUEST_CONTACT_NUMBER = 2
private const val DATE_FORMAT = "EEE,  MMM, dd"


class CrimeFragment: Fragment(), DatePickerFragment.Callback {
    private lateinit var crime: Crime

    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var callButton: Button
    private lateinit var listButton: Button

    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView

    private lateinit var lookupKey: String

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

        requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
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

        suspectButton = view.findViewById(R.id.crime_suspect) as Button

        reportButton = view.findViewById(R.id.crime_report) as Button

        callButton = view.findViewById(R.id.make_call) as Button

        listButton = view.findViewById(R.id.crime_list) as Button

        photoButton = view.findViewById(R.id.crime_camera) as ImageButton

        photoView = view.findViewById(R.id.crime_photo) as ImageView

//        dateButton.apply {
//            text = crime.date.toString()
//
//            isEnabled = false
//        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also {
                intent ->
                val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        suspectButton.apply {
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener{
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }

//            pickContactIntent.addCategory(Intent.CATEGORY_HOME) // 阻止任何联系人应用与pickContactIntent匹配，模拟无联系人应用

            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false // 找不到联系人应用则禁用按钮
            }
        }

        callButton.apply {
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener{
                startActivityForResult(pickContactIntent, REQUEST_CONTACT_NUMBER)
            }

        }

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
                    // 保存图片存储位置
                    photoFile = crimeDetailViewModel.getPhotoFile(crime)
                    // 把本地问自己路径转换成相机能使用的Uri形式
                    photoUri = FileProvider.getUriForFile(requireActivity(), "com.daozhao.hello.fileProvider", photoFile)
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

        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager

            val captureImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(captureImageIntent, PackageManager.MATCH_DEFAULT_ONLY)

            if (resolvedActivity == null) {
//                isEnabled = false
            }

            setOnClickListener {
                captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                val cameraActivities: List<ResolveInfo> = packageManager.queryIntentActivities(captureImageIntent, PackageManager.MATCH_DEFAULT_ONLY)
                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(cameraActivity.activityInfo.packageName, photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
                startActivityForResult(captureImageIntent, REQUEST_PHOTO)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return
            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                val cursor = contactUri?.let { requireActivity().contentResolver.query(it, queryFields, null, null, null) }
                cursor?.use {
                    if(it.count ==0) {
                        return
                    }
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    crime.suspect = suspect
                    crimeDetailViewModel.saveCrime(crime)
                    suspectButton.text = "Suspect: $suspect"
                }
            }
            requestCode == REQUEST_CONTACT_NUMBER && data != null -> {
                val contactUri: Uri? = data.data
                val queryFields = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY)
                val cursor = contactUri?.let { requireActivity().contentResolver.query(it, queryFields, null, null, null) }
                cursor?.use { it ->
                    if(it.count ==0) {
                        return
                    }
                    it.moveToFirst()
                    // 通过key查询对应的电话号码，如果没有则直接return
                    lookupKey = it.getString(1) ?: return

                    // 检查是否还有READ_CONTACTS权限
                    if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)) {
                        makeCall()
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                requireActivity(),
                                Manifest.permission.READ_CONTACTS
                            )
                        ) {

                            // Show an explanation to the user *asynchronously* -- don't block
                            // this thread waiting for the user's response! After the user
                            // sees the explanation, try again to request the permission.
                            Log.i(CONST.PERMISSIONS_TAG, "we should explain why we need this permission!");

                            requestReadContactPermission()
                        } else {

                            // No explanation needed, we can request the permission.
                            Log.i(CONST.PERMISSIONS_TAG, "==request the permission==");

                            requestReadContactPermission()
                        }

                    }
                }
            }
            requestCode == REQUEST_PHOTO -> {
                updatePhotoView()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CONST.MY_PERMISSIONS_REQUEST_READ_CONTACTS -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeCall()
                    Log.i(CONST.PERMISSIONS_TAG, "user granted the permission!")
                } else {
                    Log.i(CONST.PERMISSIONS_TAG, "user denied the permission!")
                }
                return
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

    private fun makeCall() {
        val selection = ContactsContract.Data.LOOKUP_KEY + " = ? AND "  + ContactsContract.Data.MIMETYPE + " IN ('" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "')"
        val queryArgs = arrayOf(lookupKey)
        val phoneCursor = requireActivity().contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.Contacts.Data.DATA1),
            selection,
            queryArgs,
            null
        )
        lookupKey = ""

        phoneCursor?.use {
            if(it.count ==0) {
                return
            }
            it.moveToFirst()
            var phoneNumber = it.getString(0)
            val phoneNumberUri = Uri.parse("tel:$phoneNumber")

            Log.i("PHONE_NUMBER", phoneNumber)
            while (it.moveToNext()) {
                phoneNumber = it.getString(0)
                Log.i("PHONE_NUMBER", phoneNumber)
            }
            phoneNumberUri?.let{
                val callIntent = Intent(Intent.ACTION_CALL, phoneNumberUri)
                startActivity(callIntent)
            }

        }
    }

    private fun requestReadContactPermission() {
        // Fragment中使用ActivityCompat.requestPermissions，只会去调用宿主Activity的onRequestPermissionsResult，而没有调用Fragment的该方法。
        // Fragment中直接使用requestPermissions即可
        requestPermissions(
            arrayOf(Manifest.permission.READ_CONTACTS),
            CONST.MY_PERMISSIONS_REQUEST_READ_CONTACTS
        )
    }

    fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = crime.date.toString()

        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }

        if(crime.suspect.isNotEmpty()) {
            suspectButton.text = "Suspect: ${crime.suspect}"
        }

        updatePhotoView()
    }

    fun updatePhotoView() {
        // 使用估算值
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap)
        } else {
            photoView.setImageDrawable(null)
        }
        // 使用精准值
        val vto2: ViewTreeObserver = photoView.getViewTreeObserver()
        vto2.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                photoView.viewTreeObserver.removeGlobalOnLayoutListener(this)
                Log.i("OK", "${photoView.height},${photoView.width}")
                if (photoFile.exists()) {
                    val bitmap = getScaledBitmap(photoFile.path, photoView.height, photoView.width)
                    photoView.setImageBitmap(bitmap)
                } else {
                    photoView.setImageDrawable(null)
                }
            }
        })
    }

    private fun getCrimeReport(): String {
        val solvedString = if(crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()

        val suspect = if(crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }

    interface Callback {
        fun onOpenList()
    }


    companion object {
        val REQUEST_DATE = 0
        // 现在创建CrimeFragment就必须要传入crimeId
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(CONST.ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }
}