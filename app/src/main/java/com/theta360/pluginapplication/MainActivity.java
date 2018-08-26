/**
 * Copyright 2018 Ricoh Company, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.theta360.pluginapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.theta360.pluginapplication.network.HttpConnector;
import com.theta360.pluginapplication.task.GetOptionsTask;
import com.theta360.pluginapplication.task.ModeChangeTask;
import com.theta360.pluginapplication.task.MovieCaptureTask;
import com.theta360.pluginapplication.task.TakePictureTask;
import com.theta360.pluginapplication.task.TakePictureTask.Callback;
import com.theta360.pluginlibrary.activity.PluginActivity;
import com.theta360.pluginlibrary.callback.KeyCallback;
import com.theta360.pluginlibrary.receiver.KeyReceiver;
import com.theta360.pluginlibrary.values.LedColor;
import com.theta360.pluginlibrary.values.LedTarget;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;


public class MainActivity extends PluginActivity {
    private enum WlanStatus {
        OFF,
        AP,
        CL;
    };

    private TakePictureTask.Callback mTakePictureTaskCallback = new TakePictureTask.Callback() {
        @Override
        public void onTakePicture(String fileUrl) {
            Log.d("debug","TakePictureTask.Callback::onTakePicture(): "+fileUrl);
        }
    };
    private ModeChangeTask.Callback mModeChangeTaskCallback = new ModeChangeTask.Callback() {
        @Override
        public void onModeChange(String mode) {
            Log.d("debug","ModeChangeTask.Callback::onModeChange(): "+ mode);
            mCurrentCaptureMode = mode;
        }
    };
    private MovieCaptureTask.Callback mMovieCaptureTaskCallback = new MovieCaptureTask.Callback() {
        @Override
        public void onMovieCapture(String status) {
            Log.d("debug","MovieCaptureTask.Callback::onMovieCapture(): "+ status);
        }
    };
    private GetOptionsTask.Callback mGetCaptureModeCallback = new GetOptionsTask.Callback() {
        @Override
        public void onGetOptionsTask(JSONObject options) {
            try{
                String mode = options.getString("captureMode");
                Log.d("debug","MovieCaptureTask.Callback::onGetOptionsTask(): "+ mode);
                mCurrentCaptureMode = mode;
                if(mode.equals("video")){
                    notificationLedShow(LedTarget.LED5);
                }else if(mode.equals("image")){
                    notificationLedShow(LedTarget.LED4);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
            }
            Log.d("debug","mGetCaptureModeCallback, mCurrentCaptureMode= "+mCurrentCaptureMode);
        }
    };

    WlanStatus mWLANStatus=WlanStatus.OFF;

    String mCurrentCaptureMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("debug","onCreate()");

        // Set a callback when a button operation event is acquired.
        setKeyCallback(new KeyCallback() {

            @Override
            public void onKeyDown(int keyCode, KeyEvent event) {
                Log.d("debug","onKeyDown( keyCode="+keyCode+", event.getDeviceId="+event.getDeviceId()+" )");

                if (keyCode == KeyReceiver.KEYCODE_CAMERA) {
                    /*
                     * To take a static picture, use the takePicture method.
                     * You can receive a fileUrl of the static picture in the callback.
                     */
                    if(mCurrentCaptureMode.equals("image")) {
                        new TakePictureTask(mTakePictureTaskCallback).execute();
                    }else if(mCurrentCaptureMode.equals("video")){
                        new MovieCaptureTask(mMovieCaptureTaskCallback).execute();
                    }
                }
                if (keyCode == KeyReceiver.KEYCODE_WLAN_ON_OFF) {
                    switch(mWLANStatus){
                        case OFF:
                            notificationWlanAp();
                            mWLANStatus=WlanStatus.AP;
                            Log.d("debug","notificationWlanAp() called.");
                            break;
                        case AP:
                            notificationWlanCl();
                            mWLANStatus=WlanStatus.CL;
                            Log.d("debug","notificationWlanCl() called.");
                            break;
                        case CL:
                            notificationWlanOff();
                            mWLANStatus=WlanStatus.OFF;
                            Log.d("debug","notificationWlanOff() called.");
                            break;
                    }
                }
            }

            @Override
            public void onKeyUp(int keyCode, KeyEvent event) {
                Log.d("debug","onKeyUp( keyCode="+keyCode+", event.getDeviceId="+event.getDeviceId()+" )");
                /**
                 * You can control the LED of the camera.
                 * It is possible to change the way of lighting, the cycle of blinking, the color of light emission.
                 * Light emitting color can be changed only LED3.
                 */
                if (keyCode == KeyReceiver.KEYCODE_MEDIA_RECORD) {
                    new ModeChangeTask("",mModeChangeTaskCallback).execute();
                }
            }

            @Override
            public void onKeyLongPress(int keyCode, KeyEvent event) {
                Log.d("debug","onKeyLongPress( keyCode="+keyCode+", event.getDeviceId="+event.getDeviceId()+" )");

                if (keyCode == KeyReceiver.KEYCODE_MEDIA_RECORD) {
                    close();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("debug","onStart()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("debug","onRestart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("debug","onResume()");

        new GetOptionsTask(Arrays.asList("captureMode"), mGetCaptureModeCallback).execute();

        notificationWlanOff();
        mWLANStatus = WlanStatus.OFF;

        new ModeChangeTask("image",mModeChangeTaskCallback).execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("debug","onPause()");

        close();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("debug","onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("debug","onDestroy()");
    }
}
