/**
 * Copyright (c) 2014, Airbitz Inc
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms are permitted provided that 
 * the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Redistribution or use of modified source code requires the express written
 *    permission of Airbitz Inc.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the Airbitz Project.
 */

package com.airbitz.fragments.request;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.models.Contact;
import com.airbitz.objects.HighlightOnPressImageButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tom on 9/25/14.
 */
public class ContactPickerFragment extends BaseFragment {
    public final static String TYPE = "com.airbitz.contact_picker_fragment.type";
    public final static String EMAIL = "com.airbitz.contact_picker_fragment.email";
    public final static String SMS = "com.airbitz.contact_picker_fragment.sms";
    private final String TAG = getClass().getSimpleName();
    private EditText mContactName;
    private TextView mFragmentTitle;
    private ListView mSearchListView;
    private HighlightOnPressImageButton mBackButton;
    private HighlightOnPressImageButton mHelpButton;
    private Bundle mBundle;
    private List<Contact> mContacts = new ArrayList<Contact>();
    private List<Contact> mFilteredContacts = new ArrayList<Contact>();
    private ContactSearchAdapter mSearchAdapter;
    private NavigationActivity mActivity;
    private View mView;
    // Callback interface for a selection
    private ContactSelection mContactSelection;

    public void setContactSelectionListener(ContactSelection listener) {
        mContactSelection = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = getArguments();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mActivity = (NavigationActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_contact_picker, container, false);

        mFragmentTitle = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        if (mBundle.getString(TYPE).equals(EMAIL)) {
            mFragmentTitle.setText(getString(R.string.fragment_contact_picker_title_email));
        } else {

            mFragmentTitle.setText(getString(R.string.fragment_contact_picker_title_sms));
        }

        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.layout_title_header_button_help);
        mHelpButton.setVisibility(View.VISIBLE);
        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity) getActivity()).pushFragment(new HelpFragment(HelpFragment.RECIPIENT), NavigationActivity.Tabs.REQUEST.ordinal());
            }
        });


        mContactName = (EditText) mView.findViewById(R.id.fragment_contact_picker_edittext_name);
        mSearchListView = (ListView) mView.findViewById(R.id.fragment_contact_picker_listview_search);
        mSearchAdapter = new ContactSearchAdapter(getActivity(), mFilteredContacts);
        mSearchListView.setAdapter(mSearchAdapter);
        mBackButton = (HighlightOnPressImageButton) mView.findViewById(R.id.layout_title_header_button_back);
        mBackButton.setVisibility(View.VISIBLE);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
                ((NavigationActivity) getActivity()).showNavBar();
            }
        });

        mContactName.setTypeface(NavigationActivity.latoRegularTypeFace);

        mContactName.setText("");
        mContactName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    updateAutoCompleteArray(mContactName.getText().toString());
                    mActivity.showSoftKeyboard(mContactName);
                }
            }
        });

        mContactName.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE && mActivity != null) {
                    mActivity.hideSoftKeyboard(mContactName);
                    mActivity.popFragment();
                    mActivity.getFragmentManager().executePendingTransactions();
                    String name = mContactName.getText().toString();
                    if (mBundle.getString(TYPE).equals(EMAIL)) {
                        mContactSelection.onContactSelection(new Contact(name, name, null, null));
                    } else {
                        mContactSelection.onContactSelection(new Contact(name, null, name, null));
                    }
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
                Contact contact = (Contact) mSearchAdapter.getItem(i);
                mContactName.setText(contact.getName());

                if (mContactSelection != null) {
                    ((NavigationActivity) getActivity()).popFragment();
                    mActivity.getFragmentManager().executePendingTransactions();
                    mContactSelection.onContactSelection(contact);
                }
            }
        });

        return mView;
    }

    private void updateAutoCompleteArray(String strTerm) {
        mAutoCompleteContactsTask = new AutoCompleteContactsTask();
        mAutoCompleteContactsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, strTerm);
    }

    @Override
    public void onResume() {
        super.onResume();
        mContactName.requestFocus();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    private static final String[] EMAIL_PROJECTION = new String[] {
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Email.DATA,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
    };

    private static final String[] PHONE_PROJECTION = new String[] {
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
    };

    private List<Contact> GetMatchedContacts(String term, boolean emailSearch) {
        List<Contact> contacts = new ArrayList<Contact>();

        long startMillis = System.currentTimeMillis();

        ContentResolver contentResolver = mActivity.getContentResolver();

        StringBuilder buffer = null;
        String[] args = null;
        if (term != null) {
            buffer = new StringBuilder();
            buffer.append("UPPER(");
            buffer.append(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);
            buffer.append(") GLOB ?");
            args = new String[]{term.toUpperCase() + "*"};
        }

        if(emailSearch) {
            Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, EMAIL_PROJECTION, buffer == null ? null : buffer.toString(), args,
                    ContactsContract.Contacts.SORT_KEY_PRIMARY + " ASC");
            if (cursor != null) {
                try {
                    final int displayNameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                    final int emailIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
                    int indexThumbnail = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);
                    String name, email;
                    while (cursor.moveToNext()) {
                        name = cursor.getString(displayNameIndex);
                        email = cursor.getString(emailIndex);
                        String thumbnail = cursor.getString(indexThumbnail);
                        contacts.add(new Contact(name, email, null, thumbnail));
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        else {
            Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PHONE_PROJECTION, buffer == null ? null : buffer.toString(), args,
                    ContactsContract.Contacts.SORT_KEY_PRIMARY + " ASC");
            if (cursor != null) {
                try {
                    final int displayNameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                    int indexNumber = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    int indexThumbnail = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);
                    String name, number;
                    while (cursor.moveToNext()) {
                        name = cursor.getString(displayNameIndex);
                        number = cursor.getString(indexNumber);
                        String thumbnail = cursor.getString(indexThumbnail);
                        contacts.add(new Contact(name, null, number, thumbnail));
                    }
                } finally {
                    cursor.close();
                }
            }        }
        Log.d(TAG, "total search time: " + (System.currentTimeMillis() - startMillis));
        return contacts;
    }

    public interface ContactSelection {
        public void onContactSelection(Contact contact);
    }


    AutoCompleteContactsTask mAutoCompleteContactsTask = null;
    public class AutoCompleteContactsTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            mFilteredContacts.clear();
        }

        @Override
        protected Void doInBackground(String... params) {
            mFilteredContacts.addAll(GetMatchedContacts(params[0], mBundle.getString(TYPE).equals(EMAIL)));
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            mSearchAdapter.notifyDataSetChanged();
            mContactName.requestFocus();
            mAutoCompleteContactsTask = null;
            mActivity.showModalProgress(false);
        }

        @Override
        protected void onCancelled() {
            mAutoCompleteContactsTask = null;
        }
    }

}
