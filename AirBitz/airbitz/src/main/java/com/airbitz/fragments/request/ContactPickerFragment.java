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

import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import java.util.ArrayList;
import java.util.List;

public class ContactPickerFragment
        extends BaseFragment
        implements NavigationActivity.OnBackPress {
    public final static String TYPE = "com.airbitz.contact_picker_fragment.type";
    public final static String EMAIL = "com.airbitz.contact_picker_fragment.email";
    public final static String SMS = "com.airbitz.contact_picker_fragment.sms";
    private final String TAG = getClass().getSimpleName();
    private EditText mContactName;
    private ListView mSearchListView;
    private Bundle mBundle;
    private List<Contact> mContacts = new ArrayList<Contact>();
    private List<Contact> mFilteredContacts = new ArrayList<Contact>();
    private ContactSearchAdapter mSearchAdapter;
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
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_contact_picker, container, false);

        Toolbar toolbar = (Toolbar) mView.findViewById(R.id.toolbar);
        toolbar.setTitle("");
        getBaseActivity().setSupportActionBar(toolbar);
        getBaseActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getBaseActivity().getSupportActionBar().setDisplayShowHomeEnabled(true);

        mContactName = (EditText) mView.findViewById(R.id.search);
        if (mBundle.getString(TYPE).equals(EMAIL)) {
            mContactName.setHint(getString(R.string.fragment_contact_picker_title_email));
        } else {
            mContactName.setHint(getString(R.string.fragment_contact_picker_title_sms));
        }

        mSearchListView = (ListView) mView.findViewById(R.id.fragment_contact_picker_listview_search);
        mSearchAdapter = new ContactSearchAdapter(getActivity(), mFilteredContacts);
        mSearchListView.setAdapter(mSearchAdapter);

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
                    ContactPickerFragment.popFragment(mActivity);
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
                    ContactPickerFragment.popFragment(mActivity);
                    mContactSelection.onContactSelection(contact);
                }
            }
        });
        return mView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_closeable, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_close:
            mContactName.setText("");
            return true;
        case R.id.action_help:
            mActivity.pushFragment(
                new HelpFragment(HelpFragment.RECIPIENT),
                    NavigationActivity.Tabs.REQUEST.ordinal());
            return true;
        case android.R.id.home:
            ContactPickerFragment.popFragment(mActivity);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void updateAutoCompleteArray(String strTerm) {
        mAutoCompleteContactsTask = new AutoCompleteContactsTask();
        mAutoCompleteContactsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, strTerm);
    }

    @Override
    public boolean onBackPress() {
        ContactPickerFragment.popFragment(mActivity);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        mContactName.requestFocus();
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

    private List<Contact> getMatchedContacts(String term, boolean emailSearch) {
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

        if (emailSearch) {
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
        } else {
            Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PHONE_PROJECTION, buffer == null ? null : buffer.toString(), args,
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
                        contacts.add(new Contact(name, null, PhoneNumberUtils.formatNumber(number), thumbnail));
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
            mFilteredContacts.addAll(getMatchedContacts(params[0], mBundle.getString(TYPE).equals(EMAIL)));
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

    public static void pushFragment(NavigationActivity mActivity, Bundle bundle, ContactSelection listener) {
        FragmentTransaction transaction = mActivity.getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);

        ContactPickerFragment fragment = new ContactPickerFragment();
        fragment.setContactSelectionListener(listener);
        fragment.setArguments(bundle);
        mActivity.pushFragment(fragment, transaction);
    }

    public static void popFragment(NavigationActivity mActivity) {
        FragmentTransaction transaction = mActivity.getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);

        mActivity.popFragment(transaction);
        mActivity.getFragmentManager().executePendingTransactions();
    }
}
