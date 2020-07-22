/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.mlkit.vision.demo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.UiThread;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import android.widget.ToggleButton;

import com.google.android.gms.common.annotation.KeepName;

import com.google.mlkit.vision.demo.facedetector.FaceDetectorProcessor;

import com.google.mlkit.vision.demo.preference.PreferenceUtils;
import com.google.mlkit.vision.demo.preference.SettingsActivity;
import com.google.mlkit.vision.demo.preference.SettingsActivity.LaunchSource;
import com.google.mlkit.vision.demo.tflite.AgeClassifier;
import com.google.mlkit.vision.demo.tflite.Classifier;
import com.google.mlkit.vision.demo.tflite.EmotionClassifier;
import com.google.mlkit.vision.demo.tflite.GenderClassifier;
import com.google.mlkit.vision.face.FaceDetectorOptions;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
*/

/**
 * Live preview demo for ML Kit APIs.
 */
@KeepName
public final class LivePreviewActivity extends AppCompatActivity
        implements OnRequestPermissionsResultCallback,
        OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener


{

    private static final Logger LOGGER = new Logger();



    private LinearLayout gestureLayout;
    protected TextView recognitionTextView,
            recognition1TextView,
            recognition2TextView,
            recognitionValueTextView,
            recognition1ValueTextView,
            recognition2ValueTextView;
    protected TextView recognitionIDView;

    private static final String FACE_DETECTION = "Facial Dectection";  /***/
    private static final String FACE_RECOGNITION = "Facial Recognition";  /***/
    private static final String FACE_EXPRESSION = "Facial Expression Detection";  /***/
    private static final String FACE_GENDER = "Gender Detection";  /***/
    private static final String FACE_AGE = "AGE Prediction";  /***/

    private static final String TEST_JSON = "TEST API";

    private static final String OBJECT_DETECTION = "Object Detection";
    private static final String OBJECT_DETECTION_CUSTOM = "Custom Object Detection (Birds)";




   // private static final String TEXT_RECOGNITION = "Text Recognition";
   // private static final String BARCODE_SCANNING = "Barcode Scanning";
    private static final String IMAGE_LABELING = "Image Labeling";
    private static final String IMAGE_LABELING_CUSTOM = "Custom Image Labeling (Birds)";
   // private static final String AUTOML_LABELING = "AutoML Image Labeling";
    private static final String TAG = "LivePreviewActivity";
    private static final int PERMISSION_REQUESTS = 1;

    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private String selectedModel = FACE_GENDER;

    private Bitmap cropFace;

    private Integer sensorOrientation, rotation;

    private Classifier classifier;



   // ExecutorService executorService = Executors.newFixedThreadPool(4);
    public static DetectionMode mode = new DetectionMode();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_live_preview);

        preview = findViewById(R.id.preview);
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        graphicOverlay = findViewById(R.id.graphic_overlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        Spinner spinner = findViewById(R.id.spinner);
        List<String> options = new ArrayList<>();
        //options.add(OBJECT_DETECTION);
        //options.add(OBJECT_DETECTION_CUSTOM);
        //options.add(FACE_DETECTION);
        options.add(FACE_AGE);
        options.add(FACE_GENDER);
        options.add(FACE_EXPRESSION);
        /*options.add(FACE_RECOGNITION);*/

        //options.add(TEST_JSON);

       // options.add(TEXT_RECOGNITION);
        //options.add(BARCODE_SCANNING);
       // options.add(IMAGE_LABELING);
        //options.add(IMAGE_LABELING_CUSTOM);
       // options.add(AUTOML_LABELING);
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_style, options);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(this);

        ToggleButton facingSwitch = findViewById(R.id.facing_switch);
        facingSwitch.setOnCheckedChangeListener(this);

        ImageView settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(
                v -> {
                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    intent.putExtra(SettingsActivity.EXTRA_LAUNCH_SOURCE,
                            SettingsActivity.LaunchSource.LIVE_PREVIEW);
                    startActivity(intent);
                });

        gestureLayout = findViewById(R.id.gesture_layout);
        ViewTreeObserver vto = gestureLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            gestureLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            gestureLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        //                int width = bottomSheetLayout.getMeasuredWidth();
                        int height = gestureLayout.getMeasuredHeight();


                    }
                });
        recognitionTextView = findViewById(R.id.detected_item0);
        recognitionValueTextView = findViewById(R.id.detected_item0_value);

        recognition1TextView = findViewById(R.id.detected_item1);
        recognition1ValueTextView = findViewById(R.id.detected_item1_value);

        recognition2TextView = findViewById(R.id.detected_item2);
        recognition2ValueTextView = findViewById(R.id.detected_item2_value);

        recognitionIDView = findViewById(R.id.detect_id);
        //sensorOrientation = rotation - getScreenOrientation();
       // LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);



        mode.setMode(Constant.GENDER_OPTION);

        if (allPermissionsGranted()) {


            if (cameraSource == null) {

                cameraSource  = new CameraSource(this, graphicOverlay);
                setClassifier();
                Log.i(TAG, "Using Face Detector Processor");
                FaceDetectorOptions faceDetectorOptions =
                        PreferenceUtils.getFaceDetectorOptionsForLivePreview(this);
                cameraSource.setMachineLearningFrameProcessor(
                        new FaceDetectorProcessor(this,faceDetectorOptions, mode.getMode(),  classifier));

            }
           // createCameraSource(selectedModel);

        } else {
            getRuntimePermissions();
        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.live_preview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra(SettingsActivity.EXTRA_LAUNCH_SOURCE, LaunchSource.LIVE_PREVIEW);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public synchronized void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        selectedModel = parent.getItemAtPosition(pos).toString();


        Log.d(TAG, "Selected model: " + selectedModel);
        setClassifier();
        preview.stop();
        if (allPermissionsGranted()) {
            //createCameraSource(selectedModel);
            startCameraSource();

            FaceDetectorOptions faceDetectorOptions =
                    PreferenceUtils.getFaceDetectorOptionsForLivePreview(this);
            cameraSource.setMachineLearningFrameProcessor(
                    new FaceDetectorProcessor(this ,faceDetectorOptions, mode.getMode(),   classifier ));
        } else {
            getRuntimePermissions();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing.
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "Set facing");
        if (cameraSource != null) {
            if (isChecked) {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
            } else {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
            }
        }
        preview.stop();
        startCameraSource();
    }
 private void setClassifier ()
 {
     if (selectedModel == FACE_GENDER) {
         mode.setMode(Constant.GENDER_OPTION);
         try {
             classifier = new GenderClassifier(this);
         } catch ( IOException e) {
             LOGGER.e("Loading gender classifier");
         }
     }

     else   if (selectedModel == FACE_AGE) {
         mode.setMode(Constant.AGE_OPTION);
         try {
             classifier = new AgeClassifier(this);
         } catch ( IOException e) {
             LOGGER.e("Loading Age classifier");
         }
     }
     else   if (selectedModel == FACE_EXPRESSION) {
         mode.setMode(Constant.EXP_OPTION);

         try {
             classifier = new EmotionClassifier(this);
         } catch ( IOException e) {
             LOGGER.e("Loading Emotion classifier");
         }
     }
     else   if (selectedModel == FACE_RECOGNITION)
         mode.setMode(Constant.FACE_OPTION);

 }


    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }


    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        //createCameraSource(selectedModel);
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {

            FaceDetectorOptions faceDetectorOptions =
                    PreferenceUtils.getFaceDetectorOptionsForLivePreview(this);

            cameraSource.setMachineLearningFrameProcessor(
                    new FaceDetectorProcessor(this ,faceDetectorOptions, mode.getMode(), classifier ));

            //createCameraSource(selectedModel);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }


    @UiThread
    public void showObjectID(int id) {
        recognitionIDView.setText(String.format("Face : %2d", id ));
    }

    @UiThread
    public void showResultsInBottomSheet(List<Classifier.Recognition> results) {

        // limit only three results at most

        if (results != null && results.size() >= 2) {
            Classifier.Recognition recognition = results.get(0);
            if (recognition != null) {
                if (recognition.getTitle() != null) recognitionTextView.setText(recognition.getTitle());
                if (recognition.getConfidence() != null) {
                    recognitionValueTextView.setText(
                            String.format("%.2f", (100 * recognition.getConfidence())) + "%");
                    System.out.println(recognition.getTitle()+" "+String.format("%.2f", (100 * recognition.getConfidence())) + "%");
                }
            }

            Classifier.Recognition recognition1 = results.get(1);
            if (recognition1 != null) {
                if (recognition1.getTitle() != null) recognition1TextView.setText(recognition1.getTitle());
                if (recognition1.getConfidence() != null) {
                    recognition1ValueTextView.setText(
                            String.format("%.2f", (100 * recognition1.getConfidence())) + "%");
                    System.out.println(recognition1.getTitle()+" "+String.format("%.2f", (100 * recognition1.getConfidence())) + "%");
                }
            }
            if (results.size() > 2) {
                Classifier.Recognition recognition2 = results.get(2);
                if (recognition2 != null) {
                    if (recognition2.getTitle() != null)
                        recognition2TextView.setText(recognition2.getTitle());
                    if (recognition2.getConfidence() != null)
                        recognition2ValueTextView.setText(
                                String.format("%.2f", (100 * recognition2.getConfidence())) + "%");
                }
            }
            else {
                recognition2TextView.setText("                                    ");
                recognition2ValueTextView.setText("     ");


            }
        }
    }

}
