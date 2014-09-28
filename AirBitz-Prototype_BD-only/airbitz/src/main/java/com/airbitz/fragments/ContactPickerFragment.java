package com.airbitz.fragments;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import com.airbitz.objects.Contact;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.utils.Common;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tom on 9/25/14.
 */
public class ContactPickerFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();

    public final static String TYPE = "com.airbitz.contact_picker_fragment.type";
    public final static String EMAIL = "com.airbitz.contact_picker_fragment.email";
    public final static String SMS = "com.airbitz.contact_picker_fragment.sms";

    private EditText mContactName;
    private TextView mFragmentTitle;
    private ListView mSearchListView;
    private HighlightOnPressButton mCancelButton;

    private boolean mEmailSearch = false;

    private Bundle mBundle;

    private List<Contact> mContacts = new ArrayList<Contact>();
    private List<Contact> mFilteredContacts = new ArrayList<Contact>();
    private ContactSearchAdapter mSearchAdapter;

    private NavigationActivity mActivity;
    private View mView;

    // Callback interface for a selection
    private ContactSelection mContactSelection;
    public interface ContactSelection {
        public void onContactSelection(Contact contact);
    }
    public void setContactSelectionListener(ContactSelection listener) {
        mContactSelection = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBundle = getArguments();
        if (mBundle != null) {
            if (mBundle.getString(TYPE) != null && mBundle.getString(TYPE).equals(EMAIL)) {
                Common.LogD(TAG, "Email");
                mEmailSearch = true;
            } else if (mBundle.getString(TYPE) != null && mBundle.getString(TYPE).equals(SMS)) {
                Common.LogD(TAG, "SMS");
                mEmailSearch = false;
            }
        }

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mActivity = (NavigationActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_contact_picker, container, false);

        mFragmentTitle = (TextView) mView.findViewById(R.id.fragment_contact_picker_title);
        if(mEmailSearch) {
            mFragmentTitle.setText("Email Search");
        } else {

            mFragmentTitle.setText("SMS Search");
        }

        mContactName = (EditText) mView.findViewById(R.id.fragment_contact_picker_edittext_name);
        mSearchListView = (ListView) mView.findViewById(R.id.fragment_contact_picker_listview_search);
        mSearchAdapter = new ContactSearchAdapter(getActivity(), mFilteredContacts);
        mSearchListView.setAdapter(mSearchAdapter);
        mCancelButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_qrcode_cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
                ((NavigationActivity)getActivity()).showNavBar();
            }
        });

        mContactName.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        mContactName.setText("");
        mContactName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
                    updateAutoCompleteArray(mContactName.getText().toString());
                    mActivity.showSoftKeyboard(mContactName);
                }
            }
        });

        mContactName.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    ((NavigationActivity) getActivity()).hideSoftKeyboard(mContactName);

                    //TODO send info back to RequestQRCodeFragment ?

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

                if(mContactSelection!=null) {
                    ((NavigationActivity)getActivity()).popFragment();
                    mActivity.getFragmentManager().executePendingTransactions();
                    mContactSelection.onContactSelection(contact);
                }
            }
        });

        mContactName.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mContactName.requestFocus();

        return mView;
    }

    private void updateAutoCompleteArray(String strTerm) {
        mFilteredContacts.clear();
        mFilteredContacts.addAll(GetMatchedContacts(strTerm, mEmailSearch));
        mSearchAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        mGenerateContactsTask = new GenerateContactsTask();
        mGenerateContactsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override public void onPause() {
        super.onPause();
        if(mGenerateContactsTask!=null)
            mGenerateContactsTask.cancel(true);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }


    private List<Contact> GetMatchedContacts(String term, boolean emailSearch) {
        List<Contact> contacts = new ArrayList<Contact>();

        for(Contact contact : mContacts) {
            if(emailSearch && contact.getName()!=null && contact.getEmail()!=null) {
                if(contact.getName().contains(term) || contact.getEmail().contains(term))
                    contacts.add(contact);
            } else if(!emailSearch && contact.getName()!=null && contact.getPhone()!=null) { // phone search
                if(contact.getName().contains(term) || contact.getPhone().contains(term))
                    contacts.add(contact);
            }
        }
        return contacts;
    }

    GenerateContactsTask mGenerateContactsTask = null;
    public class GenerateContactsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            ((NavigationActivity)getActivity()).showModalProgress(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            mContacts = GenerateListOfContacts(mActivity);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            updateAutoCompleteArray(mContactName.getText().toString());
            mGenerateContactsTask = null;
            mActivity.showModalProgress(false);
        }

        @Override
        protected void onCancelled() {
            mGenerateContactsTask = null;
            mActivity.showModalProgress(false);
        }
    }


    private List<Contact> GenerateListOfContacts(Context context) {
        List<Contact> contacts = new ArrayList<Contact>();
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
                String thumbnail = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));

//                if(name!=null && thumbnail!=null)
//                    mContactPhotos.put(name, Uri.parse(thumbnail));

                // name and emails
                Cursor emails = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId, null, null);

                while (emails.moveToNext()) {
                    String emailAddress = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    contacts.add(new Contact(name, emailAddress, null, thumbnail));
                }
                emails.close();

                // name and phones
                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{contactId}, null);
                    while (phones.moveToNext()) {
                        String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contacts.add(new Contact(name, null, phoneNumber, thumbnail));
                    }
                    phones.close();
                }
            }
        }
        cursor.close();
        return contacts;
    }

}
