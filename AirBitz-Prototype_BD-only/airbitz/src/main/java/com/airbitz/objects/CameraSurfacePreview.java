/* ======================================================================
 *  Copyright � 2013 Qualcomm Technologies, Inc. All Rights Reserved.
 *  QTI Proprietary and Confidential.
 *  =====================================================================
 *
 *  Licensed by Qualcomm, Inc. under the Snapdragon SDK for Android license.
 *
 *  You may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    https://developer.qualcomm.com/snapdragon-sdk-license
 *
 *  This Qualcomm software is supplied to you by Qualcomm Inc. in consideration
 *  of your agreement to the licensing terms.  Your use, installation, modification
 *  or redistribution of this Qualcomm software constitutes acceptance of these terms.
 *  If you do not agree with these terms, please do not use, install, modify or
 *  redistribute this Qualcomm software.
 *
 *  Qualcomm grants you a personal, non-exclusive license, under Qualcomm's
 *  copyrights in this original Qualcomm software, to use, reproduce, modify
 *  and redistribute the Qualcomm Software, with or without modifications, in
 *  source and/or binary forms; provided that if you redistribute the Qualcomm
 *  Software in its entirety and without modifications, you must retain this
 *  notice and the following text and disclaimers in all such redistributions
 *  of the Qualcomm Software.
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * @file:   CameraSurfacePreview.java
 *
 */


package com.airbitz.objects;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.IOException;

public class CameraSurfacePreview extends SurfaceView implements SurfaceHolder.Callback{
	
	private SurfaceHolder mHolder;
    private Camera mCamera;
    private FrameLayout mPreview;
    private TextView mNumFaceText;
    Context mContext;
    
    //Facial Values
    int numFaces = 0;
    int smileValue = 0;
    
    //Supported Preview Sizes for the camera
    int mSupportedWidth;
    int mSupportedHeight;

    

	public CameraSurfacePreview(Context context, Camera camera) {
		super(context);
		mCamera = camera;
		mContext = context;
		// Install a SurfaceHolder.Callback so we get notified when the underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
    
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, now tell the camera where to draw the preview.
        try 
        {
        	mCamera.setPreviewDisplay(holder);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();                
        } 
        catch (IOException e)
        {
            Log.d("TAG", "Error setting camera preview: " + e.getMessage());
        }
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		
		
	}


}
