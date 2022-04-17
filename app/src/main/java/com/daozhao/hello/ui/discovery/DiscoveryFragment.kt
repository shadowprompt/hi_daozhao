package com.daozhao.hello.ui.discovery

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.daozhao.hello.R
import com.daozhao.hello.databinding.FragmentDiscoveryBinding
import com.daozhao.hello.User
import com.daozhao.hello.UserEvent
import com.daozhao.hello.UserPhone


// The column index for the _ID column
private const val CONTACT_ID_INDEX: Int = 0
// The column index for the CONTACT_KEY column
private const val CONTACT_KEY_INDEX: Int = 1
// The column index for the CONTACT_KEY column
private const val CONTACT_NAME_INDEX: Int = 2

private const val LIST_QUERY_ID: Int = 2
// Defines a constant that identifies the loader
private const val DETAILS_QUERY_ID: Int = 3
/*
 * Defines a string that specifies a sort order of MIME type
 */
private const val SORT_ORDER = ContactsContract.Data.MIMETYPE

@SuppressLint("InlinedApi")
private val FROM_COLUMNS: Array<String> = arrayOf(
    if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)) {
        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
    } else {
        ContactsContract.Contacts.DISPLAY_NAME
    }
)

private var DETAIL_SELECTION: String =
        ContactsContract.Data.LOOKUP_KEY + " = ? AND "  +
        ContactsContract.Data.MIMETYPE + " IN ('" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "', '" + ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE + "', '" + ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE + "', '" + ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE + "')"


private val DETAIL_PROJECTION: Array<out String> = arrayOf(
    ContactsContract.Contacts.Data._ID,
    ContactsContract.Contacts.DISPLAY_NAME,
    ContactsContract.Contacts.Data.MIMETYPE,
    ContactsContract.Contacts.Data.DATA1,
    ContactsContract.Contacts.Data.DATA2,
    ContactsContract.Contacts.Data.DATA3,
    )

@SuppressLint("InlinedApi")
private val PROJECTION: Array<out String> = arrayOf(
    ContactsContract.Contacts._ID,
    ContactsContract.Contacts.LOOKUP_KEY,
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
    else
        ContactsContract.Contacts.DISPLAY_NAME
    ,
    ContactsContract.CommonDataKinds.Phone.NUMBER
)

@SuppressLint("InlinedApi")
private val SELECTION: String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE ?"
    else
        "${ContactsContract.Contacts.DISPLAY_NAME} LIKE ?"

// Defines a variable for the search string
private val searchString: String = ""
// Defines the array to hold values that replace the ?
private val selectionArgs = arrayOf<String>(searchString)

private val detailSelectionArgs = arrayOf<String>("")

/*
 * Defines an array that contains resource ids for the layout views
 * that get the Cursor column contents. The id is pre-defined in
 * the Android framework, so it is prefaced with "android.R.id"
 */
private val TO_IDS: IntArray = intArrayOf(R.id.msgBody)

class DiscoveryFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor>,
    AdapterView.OnItemClickListener {

    private lateinit var discoveryViewModel: DiscoveryViewModel
    private var _binding: FragmentDiscoveryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var textView: TextView? = null

    private var loaderFlag: Int? = null

    // Define global mutable variables
    // Define a ListView object
    lateinit var contactsList: ListView
    // Define variables for the contact the user selects
    // The contact's _ID value
    var contactId: Long = 0
    // The contact's LOOKUP_KEY
    var contactKey: String? = null
    // The contact's NAME
    var contactName: String? = null
    // A content URI for the selected contact
    var contactUri: Uri? = null
    /*
  * Defines a variable to contain the selection value. Once you
  * have the Cursor from the Contacts table, and you've selected
  * the desired row, move the row's LOOKUP_KEY value into this
  * variable.
  */
    private var lookupKey: String = ""
    // An adapter that binds the result Cursor to the ListView
    private var cursorAdapter: SimpleCursorAdapter? = null
    // 保留this for contact detail
    private var self: LoaderManager.LoaderCallbacks<Cursor>?  = null

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PackageManager.PERMISSION_GRANTED){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //用户同意了权限申请
            }else{
                //用户拒绝了权限申请，建议向用户解释权限用途
            }
        }
        Toast.makeText(requireContext(), requestCode.toString(), Toast.LENGTH_LONG).show()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val hasPermission: Int = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_CONTACTS
        )
        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            //已获取权限
        } else {
            //未获取权限
            requestPermissions(arrayOf(Manifest.permission.WRITE_CONTACTS), 0)
        }

        discoveryViewModel =
            ViewModelProvider(this).get(DiscoveryViewModel::class.java)

        _binding = FragmentDiscoveryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        textView = binding.textDiscovery
        discoveryViewModel.text.observe(viewLifecycleOwner, Observer {
            textView!!.text = it
        })

        contactsList = (root as ConstraintLayout).findViewById<ListView>(R.id.contact_list_view)

        // Gets the ListView from the View list of the parent activity
        activity?.also {
            // Gets a CursorAdapter
            cursorAdapter = SimpleCursorAdapter(
                it,
                R.layout.msg_item,
                null,
                FROM_COLUMNS,
                TO_IDS,
                0
            )
            // Sets the adapter for the ListView
            contactsList.adapter = cursorAdapter
        }

        contactsList.onItemClickListener = this

        // Initializes the loader
        loaderManager.initLoader(LIST_QUERY_ID, null, this)

        self = this

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        /*
         * Makes search string into pattern and
         * stores it in the selection array
         */
        selectionArgs[0] = "%$searchString%"
        loaderFlag = id;
        val mLoader = when(id) {
            LIST_QUERY_ID -> {
                // Starts the query
                activity?.let {
                    CursorLoader(
                        it,
                        ContactsContract.Data.CONTENT_URI,
                        PROJECTION,
                        SELECTION,
                        selectionArgs,
                        SORT_ORDER
                    )
                }
            }
            DETAILS_QUERY_ID -> {
                // Assigns the selection parameter
                detailSelectionArgs[0] = contactKey!!
//                detailSelectionArgs[1] = ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
                // Starts the query
                activity?.let {
                    CursorLoader(
                        it,
                        ContactsContract.Data.CONTENT_URI,
                        DETAIL_PROJECTION,
                        DETAIL_SELECTION,
                        detailSelectionArgs,
                        null
                    )
                }
            }
            else -> {
                null
            }
        }
        // Starts the query
        return mLoader ?: throw IllegalStateException()
    }

    override fun onItemClick(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        // Get the Cursor
        val cursor: Cursor? = (parent.adapter as? CursorAdapter)?.cursor?.apply {
            // Move to the selected contact
            moveToPosition(position)
            // Get the _ID value
            contactId = getLong(CONTACT_ID_INDEX)
            // Get the selected LOOKUP KEY
            contactKey = getString(CONTACT_KEY_INDEX)
            // Get the selected NAME
            contactName = getString(CONTACT_NAME_INDEX)
            // Create the contact's content Uri
            contactUri = ContactsContract.Contacts.getLookupUri(contactId, contactKey)
            /*
             * You can use contactUri as the content URI for retrieving
             * the details for a contact.
             */
            Toast.makeText(activity, contactUri.toString() + '_' + contactKey, Toast.LENGTH_LONG).show()

            loaderManager.initLoader(DETAILS_QUERY_ID, null, self!!)
        }
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        // Put the result Cursor in the adapter for the ListView
        cursorAdapter?.swapCursor(cursor)

        if (loaderFlag == DETAILS_QUERY_ID && cursor?.count!! > 0) {
            val contacts: HashMap<Long, MutableList<String?>> = HashMap()

            var infos: MutableList<String?> = ArrayList()

            infos.add("name = $contactName")

            while (cursor.moveToNext()) {
                val id: Long = cursor.getLong(0)
                val name: String = cursor.getString(1) // full name

                val mime: String = cursor.getString(2) // type of data (phone / birthday / email)

                val data: String? = cursor.getString(3) // the actual info, e.g. +1-212-555-1234

                var type: String? = cursor.getString(4);

                var label: String? = cursor.getString(5);

                var kind = "unknown"

                when (mime) {
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> kind = "phone"
                    ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE -> kind = "event"
                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> kind = "email"
                    ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE -> kind = "note"
                }



                infos!!.add("$kind  = $data/$type/$label")
            }
            textView?.let {
                it.text = it.text.toString() + infos.toString();
            }
        } else if (loaderFlag == LIST_QUERY_ID && cursor?.count!! > 0) {
            // 用进程打印列表数据
            Thread {
                val userList: ArrayList<User> = arrayListOf();
                var userHashMap: HashMap<String, User> = HashMap();
                try {
                    while (cursor.moveToNext()) {
                        val contactId = cursor.getString(0)
                        val contactKey = cursor.getString(1)
                        val contactName = cursor.getString(2)

                        var infos: MutableList<String?> = ArrayList()
                        infos.add("contactName = $contactName")

                        val bd: ContentResolver = requireActivity().contentResolver
                        detailSelectionArgs[0] = contactKey!!
                        val curs: Cursor? = bd.query(ContactsContract.Data.CONTENT_URI,
                            DETAIL_PROJECTION,
                            DETAIL_SELECTION,
                            detailSelectionArgs,
                            null)
                        var userPhoneList : ArrayList<UserPhone> = arrayListOf();
                        var userEventList : ArrayList<UserEvent> = arrayListOf();
                        if (curs != null) {
                            while (curs.moveToNext()) {
                                val id: Long = cursor.getLong(0)
                                val name: String = curs.getString(1) // full name

                                val mime: String = curs.getString(2) // type of data (phone / birthday / email)

                                val data: String? = curs.getString(3) // the actual info, e.g. +1-212-555-1234

                                var type: String? = curs.getString(4);

                                var label: String? = curs.getString(5);

                                var kind = "unknown"

                                when (mime) {
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> {
                                        kind = "phone"
                                        userPhoneList.add(UserPhone(contactKey, data, type, label))
                                    }
                                    ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE -> {
                                        kind = "event"
                                        userEventList.add(UserEvent(contactKey, data, type, label))
                                    }
                                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> kind = "email"
                                    ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE -> kind = "note"
                                }



                                infos!!.add("$kind  = $data/$type/$label")
                            }
//                            Log.i("ABC", infos.toString())
                        }
                        val user = User(contactKey, contactId, contactName, userPhoneList, userEventList, null);
                        if (!userHashMap.containsKey(user.key) && userEventList.size > 0) {
                            userHashMap.put(user.key, user);
                            userList.add(User(contactKey, contactId, contactName, userPhoneList, userEventList, null))
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ABC", "while error $e")
                }
                Log.i("EFG", userList.toString())
            }.start()
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        // Delete the reference to the existing Cursor
        cursorAdapter?.swapCursor(null)
    }
}