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

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.UriMatcher;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.airbitz.api.DirectoryWrapper;
import com.airbitz.api.directory.DirectoryApi;
import com.airbitz.api.directory.BusinessDetail;
import com.airbitz.utils.Common;

import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.UrlConnectionDownloader;
import static com.squareup.picasso.Downloader.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

public class AirbitzRequestHandler extends RequestHandler {

    private final String TAG = getClass().getSimpleName();
    private static final UriMatcher matcher;
    private static final String SCHEME = "airbitz";
    private static final String AUTHORITY = "airbitz_authority";
    private static final int ID_CONTACT = 1;
    private static final int ID_BIZ = 2;
    private ConcurrentMap<Integer, String> mBizIds;

    static {
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI("person", "*", ID_CONTACT);
        matcher.addURI("business", "#", ID_BIZ);
    }

    private final Context mContext;
    private DirectoryApi mDirectoryApi;
    private Downloader mDownloader;

    public AirbitzRequestHandler(Context context) {
        this.mContext = context;
        this.mDirectoryApi = DirectoryWrapper.getApi();
        this.mDownloader = new UrlConnectionDownloader(mContext);
        this.mBizIds = new ConcurrentHashMap<Integer, String>();
    }

    @Override
    public boolean canHandleRequest(Request request) {
        final Uri uri = request.uri;
        return (SCHEME.equals(uri.getScheme()) && matcher.match(request.uri) != UriMatcher.NO_MATCH);
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        Uri uri = request.uri;
        Log.d(TAG, request.uri.toString());
        switch (matcher.match(uri)) {
        case ID_CONTACT:
            Log.d(TAG, "ID_CONTACT");
            InputStream is = getContactStream(uri);
            return is != null ? new Result(is, LoadedFrom.DISK) : null;
        case ID_BIZ:
            Log.d(TAG, "ID_BIZ");
            return fetchBizLink(request, networkPolicy);
        default:
            throw new IllegalStateException("Invalid uri: " + uri);
        }
    }

    private InputStream getContactStream(Uri uri) throws IOException {
        ContentResolver contentResolver = mContext.getContentResolver();
        List<String> pathSegments = uri.getPathSegments();
        String key = pathSegments.get(0);
        Map<String, Uri> map = Common.GetMatchedContactsList(mContext, key);
        if (map == null) {
            return null;
        }
        return contentResolver.openInputStream(map.get(key));
    }

    private Result fetchBizLink(Request request, int networkPolicy) {
        try {
            List<String> pathSegments = request.uri.getPathSegments();
            Integer bizId = Integer.parseInt(pathSegments.get(0));

            Uri thumbnail = null;
            if (mBizIds.get(bizId) == null) {
                BusinessDetail biz = mDirectoryApi.getHttpBusiness(bizId);
                if (biz == null || biz.getSquareImageLink() == null) {
                    return null;
                }
                mBizIds.put(bizId, biz.getSquareImageLink());
                thumbnail = Uri.parse(biz.getSquareImageLink());
                Log.d(TAG, "Fetch..." + thumbnail.toString());
            } else {
                thumbnail = Uri.parse(mBizIds.get(bizId));
            }
            return networkFetch(request, thumbnail, networkPolicy);
        } catch (Exception e) {
            Log.d(TAG, "exception");
            Log.e(TAG, request.uri.getPath(), e);
        }
        return null;
    }

    private Result networkFetch(Request request, Uri uri, int networkPolicy) throws IOException {
        Response response = mDownloader.load(uri, networkPolicy);
        if (response == null) {
            return null;
        }

        Picasso.LoadedFrom loadedFrom = LoadedFrom.NETWORK; // response.cached ? LoadedFrom.DISK : LoadedFrom.NETWORK;

        Bitmap bitmap = response.getBitmap();
        if (bitmap != null) {
            return new Result(bitmap, loadedFrom);
        }

        InputStream is = response.getInputStream();
        if (is == null) {
            return null;
        }
        if (response.getContentLength() == 0) {
            if (is == null) {
                return null;
            }
            try {
                is.close();
            } catch (IOException ignored) {
            }
            throw new ContentLengthException("Received response with 0 content-length header.");
        }
        return new Result(is, loadedFrom);
    }

    static class ContentLengthException extends IOException {
        public ContentLengthException(String message) {
            super(message);
        }
    }
}
