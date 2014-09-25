package com.airbitz.fragments;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.ContactSearchAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.objects.Contact;
import com.airbitz.utils.Common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tom on 9/25/14.
 */
public class ContactPickerFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();

    private EditText mContactName;
    private ListView mSearchListView;

    private Bundle bundle;

    private List<String> mContactNames;
    private List<Contact> mContacts;
    private HashMap<String, Uri> mContactPhotos;
    private ContactSearchAdapter mSearchAdapter;

    private CoreAPI mCoreAPI;
    private NavigationActivity mActivity;
    private View mView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = getArguments();
        if (bundle != null) {
            if (bundle.getString(WalletsFragment.FROM_SOURCE) != null && bundle.getString(WalletsFragment.FROM_SOURCE).equals(SuccessFragment.TYPE_SEND)) {
                Common.LogD(TAG, "SEND");

            } else if (bundle.getString(WalletsFragment.FROM_SOURCE) != null && bundle.getString(WalletsFragment.FROM_SOURCE).equals(SuccessFragment.TYPE_REQUEST)) {

                Common.LogD(TAG, "REQUEST");
            }
        }

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mActivity = (NavigationActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_contact_picker, container, false);

        mContactName = (EditText) mView.findViewById(R.id.fragment_contact_picker_edittext_name);
        mSearchListView = (ListView) mView.findViewById(R.id.fragment_contact_picker_listview_search);
        mContacts = new ArrayList<Contact>();
        mContactPhotos = new LinkedHashMap<String, Uri>();
        mSearchAdapter = new ContactSearchAdapter(getActivity(), mContacts);
        mSearchListView.setAdapter(mSearchAdapter);

        mContactName.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        mContactName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus)
                    updateAutoCompleteArray(mContactName.getText().toString());
            }
        });

        mContactName.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    ((NavigationActivity) getActivity()).hideSoftKeyboard(mContactName);

                    //TODO send info back to RequestQRCodeFragment

                    return true;
                }
                return false;
            }
        });

        mContactName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateAutoCompleteArray(mContactName.getText().toString());
            }
        });

        mSearchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String name = (String) mSearchAdapter.getItem(i);
                mContactName.setText(name);

                //TODO send info back to RequestQRCodeFragment

            }
        });

        mContactName.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mContactName.requestFocus();

        return mView;
    }

    private void updateAutoCompleteArray(String strTerm) {
        mContacts.clear();
        // go through all the contacts
        mContacts.addAll(GetMatchedContactsByEmail(getActivity(), strTerm));
        mSearchAdapter.notifyDataSetChanged();

//                    mArrayAutoComplete = [arrayAutoComplete sortedArrayUsingSelector:@selector(localizedCaseInsensitiveCompare:)];
    }

    @Override public void onPause() {
        super.onPause();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }


    public static LinkedHashMap<String, Uri> GetMatchedContactsByName(Context context, String searchTerm) {
        LinkedHashMap<String, Uri> contactList = new LinkedHashMap<String, Uri>();
        ContentResolver cr = context.getContentResolver();
        String columns[] = {ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.Contacts.PHOTO_THUMBNAIL_URI, ContactsContract.CommonDataKinds.Email.DATA, ContactsContract.Contacts.PHOTO_THUMBNAIL_URI};
        Cursor cur;
        if (searchTerm == null) {
            cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    columns, null, null, null);
        } else {
            cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    columns, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE " + DatabaseUtils.sqlEscapeString("%" + searchTerm + "%"), null, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC");
        }
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String photoURI = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                if (photoURI != null) {
                    Uri thumbUri = Uri.parse(photoURI);
                    contactList.put(name, thumbUri);
                }
            }
        }
        cur.close();
        return contactList;
    }

    private List<Contact> GetMatchedContactsByEmail(Context context, String term) {
        List<Contact> contacts = new ArrayList<Contact>();
        ContentResolver cr = context .getContentResolver();
        Cursor cur = cr .query(ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null);

        if (cur.getCount() > 0) {

            while (cur.moveToNext()) {

                String contactId = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));

                // Create query to use CommonDataKinds classes to fetch emails
                Cursor emails = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId, null, null);

                            /*
                            //You can use all columns defined for ContactsContract.Data
                            // Query to get phone numbers by directly call data table column

                            Cursor c = getContentResolver().query(Data.CONTENT_URI,
                                      new String[] {Data._ID, Phone.NUMBER, Phone.TYPE, Phone.LABEL},
                                      Data.CONTACT_ID + "=?" + " AND "
                                              + Data.MIMETYPE + "= + Phone.CONTENT_ITEM_TYPE + ",
                                      new String[] {String.valueOf(contactId)}, null);
                            */

                while (emails.moveToNext()) {
                    // This gets several email addresses
                    String emailAddress = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    String name = emails.getString(emails.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
                    String thumbnail = emails.getString(emails.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                    contacts.add(new Contact(name, emailAddress, null, thumbnail));
                }
                emails.close();
            }
        }
        cur.close();
        return contacts;
    }

}
