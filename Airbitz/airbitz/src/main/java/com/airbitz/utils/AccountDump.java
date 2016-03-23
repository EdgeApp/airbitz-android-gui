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

package com.airbitz.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.airbitz.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AccountDump {

    public static final String TAG = Common.class.getSimpleName();

    public static final String BASE_NAME = "airbitz-account";

    public static void cleanUp(Context context) {
        File zipFile = new File(context.getExternalFilesDir(null), BASE_NAME + ".zip");
        if (zipFile.exists()) {
            zipFile.delete();
        }
    }

    public static void shareAccountData(Activity activity) {
        File file = AccountDump.extractAccount(activity);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("application/zip");

        Uri uri = Uri.fromFile(file);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        activity.startActivity(Intent.createChooser(intent, "Share?"));
    }

    public static File extractAccount(Context context) {
        File root = context.getFilesDir();
        List <File> files = traverse(root);

        File zipFile = new File(context.getExternalFilesDir(null), BASE_NAME + ".zip");
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new FileOutputStream(zipFile));
            for (File file : files) {
                String entryName = createEntryName(root, file);
                zipAttachFile(out, entryName, file);
            }
        } catch (IOException e) {
            Log.e(TAG, "", e);
            if (zipFile.exists()) {
                zipFile.delete();
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignore) {
                    // Nothing to see here
                }
            }
        }
        return zipFile;
    }

    private static String createEntryName(File root, File file) {
        return BASE_NAME + "/" + file.toString().replaceAll(root.toString(), "");
    }

    private static void zipAttachFile(ZipOutputStream out, String entryName, File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        out.putNextEntry(new ZipEntry(entryName));

        byte[] b = new byte[1024];
        int count;
        while ((count = in.read(b)) > 0) {
            out.write(b, 0, count);
        }
        in.close();
    }

    private static List<File> traverse(File root) {
        List <File> files = new LinkedList<File>();
        traverse(root, files);
        return files;
    }

    private static void traverse(File root, List<File> files) {
        for (File file : root.listFiles()) {
            if (file.isDirectory()) {
                traverse(file, files);
            }
            String n = file.getName();
            if (n.equals("UserName.json")
                    || n.equals("CarePackage.json")
                    || n.equals("LoginPackage.json")) {
                files.add(file);
            }
        }
    }
}
