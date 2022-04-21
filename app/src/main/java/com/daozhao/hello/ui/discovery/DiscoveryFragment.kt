package com.daozhao.hello.ui.discovery

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.loader.app.LoaderManager
import com.daozhao.hello.*
import com.daozhao.hello.databinding.FragmentDiscoveryBinding


// The column index for the _ID column
private const val CONTACT_ID_INDEX: Int = 0
// The column index for the CONTACT_KEY column
private const val CONTACT_KEY_INDEX: Int = 1
// The column index for the CONTACT_KEY column
private const val CONTACT_NAME_INDEX: Int = 2

// Defines a constant that identifies the loader
private const val DETAILS_QUERY_ID: Int = 3
/*
 * Defines a string that specifies a sort order of MIME type
 */

class DiscoveryFragment : Fragment(), View.OnClickListener {

    private lateinit var discoveryViewModel: DiscoveryViewModel
    private var _binding: FragmentDiscoveryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var textView: TextView? = null

    // Define variables for the contact the user selects
    // The contact's _ID value
    var contactId: Long = 0
    // The contact's LOOKUP_KEY
    var contactKey: String? = null
    // The contact's NAME
    var contactName: String? = null
    // A content URI for the selected contact
    var contactUri: Uri? = null

    private var mContext: Context?= null
    /*
  * Defines a variable to contain the selection value. Once you
  * have the Cursor from the Contacts table, and you've selected
  * the desired row, move the row's LOOKUP_KEY value into this
  * variable.
  */

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

        root.findViewById<Button>(R.id.showContactList).setOnClickListener(this)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showContactList() {
        //child fragment
        val childFragMan: FragmentManager = childFragmentManager
        val childFragTrans: FragmentTransaction = childFragMan.beginTransaction()
        val fragB = ContactListFragment()
        childFragTrans.replace(R.id.FRAGMENT_PLACEHOLDER, fragB)
        childFragTrans.addToBackStack("B")
        childFragTrans.commit()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context;
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.showContactList -> showContactList()
        }
    }
}