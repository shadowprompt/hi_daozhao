package com.daozhao.hello

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.daozhao.hello.databinding.FragmentContactListBinding
import com.daozhao.hello.ui.ListAdapter
import com.google.gson.Gson


// The column index for the _ID column
private const val CONTACT_ID_INDEX: Int = 0
// The column index for the CONTACT_KEY column
private const val CONTACT_KEY_INDEX: Int = 1
// The column index for the CONTACT_KEY column
private const val CONTACT_NAME_INDEX: Int = 2

private const val LIST_QUERY_ID: Int = 2
// Defines a constant that identifies the loader
private const val DETAILS_QUERY_ID: Int = 3

private var loaderFlag: Int? = null

private const val SORT_ORDER = ContactsContract.Data.MIMETYPE

// Define global mutable variables
// Define a ListView object
lateinit var contactsList: ListView

// Defines a variable for the search string
private val searchString: String = ""

private var lookupKey: String = ""
// An adapter that binds the result Cursor to the ListView
private var cursorAdapter: SimpleCursorAdapter? = null


private val detailSelectionArgs = arrayOf<String>("")

// Defines the array to hold values that replace the ?
private val selectionArgs = arrayOf<String>(searchString)

private val DISPLAY_NAME: String = if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)) {
    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
} else {
    ContactsContract.Contacts.DISPLAY_NAME
}

private val PROJECTION: Array<out String> = arrayOf(
    ContactsContract.Contacts._ID,
    ContactsContract.Contacts.LOOKUP_KEY,
    DISPLAY_NAME,
    ContactsContract.CommonDataKinds.Phone.NUMBER
)

private val SELECTION: String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE ?"
    else
        "${ContactsContract.Contacts.DISPLAY_NAME} LIKE ?"

// cursor数据对应列
private val FROM_COLUMNS: Array<String> = arrayOf(
    DISPLAY_NAME,
    ContactsContract.CommonDataKinds.Phone.NUMBER,
    ContactsContract.Contacts._ID
)
// cursor数据对应列对应的展示位置ID
private val TO_IDS: IntArray = intArrayOf(R.id.msgTime, R.id.msgTitle, R.id.msgBody)


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

// Define variables for the contact the user selects
// The contact's _ID value
var contactId: Long = 0
// The contact's LOOKUP_KEY
var contactKey: String? = null
// The contact's NAME
var contactName: String? = null
// A content URI for the selected contact
var contactUri: Uri? = null

/**
 * A simple [Fragment] subclass.
 * Use the [ContactListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContactListFragment : Fragment(), AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private var _binding: FragmentContactListBinding? = null

    private val binding get() = _binding!!

    private var textView: TextView? = null
    // 保留this for contact detail
    private var self: LoaderManager.LoaderCallbacks<Cursor>?  = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentContactListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        contactsList = root.findViewById(R.id.contact_list)

        textView = root.findViewById(R.id.contact_list_text)

        textView?.text = "loading..."

        self = this

//        initDataFromMock();

        initData()

        // Initializes the loader
        LoaderManager.getInstance(this).initLoader(LIST_QUERY_ID, null, this)

        return root
    }

    fun initDataFromMock() {
        val adapter = ListAdapter(requireContext(), arrayListOf(Msg("test", "body", "timeee")))
        contactsList!!.adapter = adapter
    }

    fun initData() {
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
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        /*
         * Makes search string into pattern and
         * stores it in the selection array
         */
        loaderFlag = id

        selectionArgs[0] = "%$searchString%"
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
            } else -> {
                null
            }
        }
        // Starts the query
        return mLoader ?: throw IllegalStateException()
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        // Put the result Cursor in the adapter for the ListView
        cursorAdapter?.swapCursor(cursor)

        if (loaderFlag == DETAILS_QUERY_ID && cursor?.count!! > 0) {
            val contacts: HashMap<Long, MutableList<String?>> = HashMap()

            var currentInfos: MutableList<String?> = ArrayList()

            currentInfos.add("name = $contactName")

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
                currentInfos!!.add("$kind  = $data/$type/$label")
            }
            textView?.text = "detail: " + currentInfos.toString()

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

                        var currentInfos: MutableList<String?> = ArrayList()
                        currentInfos.add("contactName = $contactName")

                        var userPhoneList : ArrayList<UserPhone> = arrayListOf();
                        var userEventList : ArrayList<UserEvent> = arrayListOf();

                        val cr: ContentResolver = requireActivity().contentResolver
                        detailSelectionArgs[0] = contactKey!!
                        val curs: Cursor? = cr.query(ContactsContract.Data.CONTENT_URI,
                            DETAIL_PROJECTION,
                            DETAIL_SELECTION,
                            detailSelectionArgs,
                            null)

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



                                currentInfos!!.add("$kind  = $data/$type/$label")
                            }
                            Log.i("CURRENT", currentInfos.toString())
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
                if (userList.size > 0) {
                    val userListStr = Gson().toJson(userList);
                    Utils.saveData(requireContext(), "test", "userList", userListStr)
//                    textView?.text = "list: " + userList.toString()
                    Log.i("LIST", userList.toString())
                }
            }.start()
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        // Delete the reference to the existing Cursor
        cursorAdapter?.swapCursor(null)
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

            loaderFlag = DETAILS_QUERY_ID

            LoaderManager.getInstance(requireActivity()).initLoader(DETAILS_QUERY_ID, null, self!!)
        }
    }
}