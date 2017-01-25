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

package com.airbitz.objects;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.airbitz.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileSaveLocationDialog implements AdapterView.OnItemClickListener {
    List<File> mFileList;
    File mCurrentDirectory;
    FileSaveLocation mCallback;
    DirectoryListingAdapter mAdapter;
    Context mContext;
    Dialog mAlertDialog;

    public interface FileSaveLocation {
        void onFileSaveLocation(File file);
    }

    public FileSaveLocationDialog (Context context, File startDirectory, FileSaveLocation callback) {
        mContext = context;
        File directory = startDirectory == null ? Environment.getExternalStorageDirectory() : startDirectory;

        mCurrentDirectory = directory;
        mCallback = callback;
        mFileList = getDirectoryListing(directory);
        mAdapter = new DirectoryListingAdapter(context, android.R.layout.simple_list_item_1, mFileList);

        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder( new ContextThemeWrapper(mContext, R.style.AlertDialogCustom) );
        builder.setTitle( directory.getAbsolutePath() );
        builder.setAdapter( mAdapter, null );

        builder.setPositiveButton(R.string.string_save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (mCallback != null )
                    mCallback.onFileSaveLocation(mCurrentDirectory);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.string_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        mAlertDialog = builder.show();
//        mAlertDialog.getListView().setOnItemClickListener(this);
        mAlertDialog.show();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(i >= 0 && i < mFileList.size()) {
            mCurrentDirectory = mFileList.get(i).getName().equals("..") ?
                mCurrentDirectory.getParentFile() : mFileList.get(i);

            mFileList = getDirectoryListing(mCurrentDirectory);
            mAdapter.notifyDataSetChanged();
            mAlertDialog.setTitle(mCurrentDirectory.getAbsolutePath());
        }
    }

    private List<File> getDirectoryListing(File directory) {
        if(mFileList == null) {
            mFileList = new ArrayList<>();
        }
        else {
            mFileList.clear();
        }
        if(directory.getParent() != null) {
            mFileList.add(new File(".."));
        }

        File[] files = directory.listFiles();
        if(files != null) {
            for(File file : files) {
                if(file.isDirectory() && file.canWrite()) {
                    mFileList.add(file);
                }
            }
        }
        return mFileList;
    }

    public class DirectoryListingAdapter extends ArrayAdapter<File> {
        List<File> files = null;
        public DirectoryListingAdapter(Context context, int resourceId, List<File> list) {
            super(context, resourceId, list);
            files = list;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv = (TextView) super.getView(position, convertView, parent);

            String text = files.get(position) == null ? ".." : files.get(position).getName();
            tv.setText(text);
            tv.setTextColor(mContext.getResources().getColor(android.R.color.black));
            return tv;
        }
    }
}
