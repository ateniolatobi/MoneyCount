package com.example.moneycount;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.GradientDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 0; // 0 if camera permission is not granted
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 0; // 0 if external storage permission is not granted
    private static final int STATE_PREVIEW = 0; // constant value for camera still focusing.
    private static final int STATE_WAIT_LOCK = 1; // constant value for ready to capture.
    boolean hasCap; // track if picture is taken.
    private int mCaptureState = STATE_PREVIEW; // manages when camera is in autofocus.
    MediaPlayer noimage = null; // mediaplayer for noimage detected sound byte
    MediaPlayer detecting = null; // mediaplayer for detecting soundbyte
    private TextureView  mTextureView; // mTextureView to display camera areea
    Classifier classifier; // deep learning model.

    //  TextureView.SurfaceTextureListener is used to listen on the launch screen.
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        //  The onSurfaceTextureAvailable function gets called as soon as the screen is ready for use.
        //  The listener also provides the width and height of the screen with the surface instance.
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

            setUpCamera(width, height); // setupcamera function is called to setup important fields and methods in order to connect the camera.
            connectCamera(); // connects the Camera
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
    private CameraDevice mCameraDevice; // camera device

    // mCameraDeviceStateCallBack provides an anonymous function that can check if the app is open or not and start the camera preview in the app.
    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) { // opens camera when application is opened.
            mCameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) { // closes camera when application is closed for other apps.
            camera.close();
            mCameraDevice = null;

        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };

    private HandlerThread mBackgroundHandlerThread; // BackgroundHandler Thread
    private Handler mBackgroundHandler; //Background Handler
    private String mCameraId; // Camera Sensor Id
    private Size mPreviewSize; // Camera preview dimension
    Button total;
    private CameraCaptureSession mPreviewCaptureSession;
    private CameraCaptureSession.CaptureCallback mPreviewCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        private void process(CaptureResult captureResult){
            switch (mCaptureState){ // If capturestate is 1 takes picture
                case STATE_PREVIEW:
                    //Do nothing
                    break;
                case STATE_WAIT_LOCK:
                    mCaptureState = STATE_PREVIEW; // sets captureState to preview state.
                    Integer afState = captureResult.get(CaptureResult.CONTROL_AF_STATE);
                    startStillCaptureRequest();
                    break;
            }
        }
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            process(result);
        }
    };
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private Size mImageSize; // Captured Image dimension.
    private ImageReader mImageReader; // Captured Image Reader


    //  mOnImageAvailableListener is an anonymous class that utilizes the onImageAvailable() function which is called when the identify button is clicked.
    // this processes the latest image captured, runs through the deep learning model and then returns the prediction to the user.
    private final  ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) { // onImageAvailable() is called once the user clicks the identify button.
            Image mImage = reader.acquireLatestImage();
            ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer(); // converts to bytbuffer

            byte[] bytes = new byte[byteBuffer.remaining()]; // creates byte array.
            byteBuffer.get(bytes); // converts bytebuffer to byte array.
            final Bitmap bmp= BitmapFactory.decodeByteArray(bytes,0,bytes.length); // converts bytes to bitmap.
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            final Bitmap rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true); // Rotates the bitmap needed for model.
            runOnUiThread(new Runnable() { // Creates sperate thread to run the model on the bitmap image.

                @Override
                public void run() {

                    // Stuff that updates the UI
                    String pred = classifier.predict(rotatedBitmap); // gets predicted currency
                    total_amount = total_amount + Integer.parseInt(pred); // converts to image and add to total amount
                    NumToWord obj = new NumToWord();
                    MediaPlayer detectd = MediaPlayer.create(getApplicationContext(),  getStringIdentifier(getApplicationContext(), "detected")); // Loads up the detected sound byte
                    detectd.start(); // plays the detected sound
                    convertText(obj.convert(Integer.parseInt(pred))); // loads the denomination and plays the respective soundbyte.
                    Toast.makeText(getApplicationContext(), "Detected "+obj.convert(Integer.parseInt(pred)) + " naira", Toast.LENGTH_SHORT).show();

                }

            });
            mImage.close();  // closes image

        }
    };

    private Button mStillImageButton; // identify button
    private static SparseIntArray ORIENTATIONS = new SparseIntArray(); // orientations array.
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }
    // compareSizeByArea, used as a comparator for sorting various dimensions with width and height provided.
    private static class CompareSizeByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs){
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() / (long) rhs.getWidth() * rhs.getHeight());
        }
    }



    int total_amount; // total amount
    boolean hasSummed; // check if sum had been taken.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hasCap = false; //
        hasSummed = false;
        total_amount = 0;
        final MediaPlayer welcome = MediaPlayer.create(this, R.raw.welcome); // loads welcome soundbyte
        final MediaPlayer totalaudio = MediaPlayer.create(this, R.raw.totamount); // loads totalamoun soundbyte
        noimage = MediaPlayer.create(this, R.raw.noimage);  // loads no image detected soundbyte
        detecting = MediaPlayer.create(this, R.raw.detecting); // loads detecting sound byte
        welcome.start(); // plays welcome sound
        Toast.makeText(getApplicationContext(), "app started ", Toast.LENGTH_SHORT).show();
        mTextureView = (TextureView) findViewById(R.id.textureView); // textureview to load cameraview.
        mStillImageButton = (Button) findViewById(R.id.identify); // identify button.
        total = (Button) findViewById(R.id.sum); // sum up button.
        classifier = new Classifier(Utils.assetFilePath(this, "nairanetmobile.pt")); // Loads deeplearning model.

        // onClickListener for identify button.
        mStillImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasSummed){ // if total has been clicked reset total and hasSum
                    total_amount = 0;
                    hasSummed = false;
                }
                detecting.start(); // plays detecting sound byte
                lockFocus(); // locks camera focus
            }
        });

        // onClickListener to take total count
        total.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (total_amount != 0){
                    hasSummed = true;
                    final NumToWord obj = new NumToWord();
                    totalaudio.start(); // plays total soundbyte
                    convertText(obj.convert(total_amount)); // plays soundbyte for summed up total
                    Toast.makeText(getApplicationContext(), "The total amount is "+ obj.convert(total_amount) + " naira",Toast.LENGTH_LONG).show();

                }else{
                    MediaPlayer notyet = MediaPlayer.create(getApplicationContext(), R.raw.notyetsum);
                    notyet.start(); // play soundbyte for no sum yet.

                }
            }
        });

    }

    @Override
    protected void onResume(){
        super.onResume();
        startBackgroundThread();
        if (mTextureView.isAvailable()){
            setUpCamera( mTextureView.getWidth(), mTextureView.getHeight());
            connectCamera();
        }else{
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION_RESULT){
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),
                        "Application will not run without camera services", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause(){
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if (hasFocus){
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }

    }

    // setUpCamera takes the width and height of the screen and loads the camera instance.
    private void setUpCamera(int width, int height){

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE); // The CameraManager is responsible for interacting with the camera hardware.

        try { // Error handling in case their is no camera or restrictive access to the camera.
            for (String cameraId : cameraManager.getCameraIdList()){ // Loops through all cameras and gets their id
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT){// If front facing camera, Ignore.
                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP); // Gets configuration of current camera.
                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation(); // Returns current orientation of the device.
                int totalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
                boolean swapRotation = totalRotation == 90 || totalRotation == 270; // True if device is in  Landscape mode
                int rotatedWidth  =  width;
                int rotatedHeight = height;
                if (swapRotation){ // Swaps width and height if device in landscape mode.
                    rotatedHeight = width;
                    rotatedWidth = height;
                }

                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight); // sets optimal preview size
                mImageSize = chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG), rotatedWidth, rotatedHeight); // sets optimal capture size.
                mImageReader = ImageReader.newInstance(mImageSize.getWidth(), mImageSize.getHeight(), ImageFormat.JPEG, 1); // sets Captured Image Reader
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler); // sets Image Capture Listener and Background Handler
                mCameraId = cameraId;  // cameraid of the camera we are using.
                return;
            }
        } catch (CameraAccessException e){
            e.printStackTrace();
        }
    }

    // connectCamera connects the camera sensor view to the application texture.
    private void connectCamera(){
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){ // checks if the device android version requires explicit permission.
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
                }else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                        Toast.makeText(this,
                                "App requires access to camera to work", Toast.LENGTH_SHORT).show();
                    }
                    requestPermissions(new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_RESULT);
                }
            }else
                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);

        }catch (CameraAccessException e){
            e.printStackTrace();
        }
    }

    // startPreview() starts the camera preview and also allows the application to take pictures.
    private void startPreview(){
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture(); // gets the surfaceTexture of the Texture area
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());  // assigns the area to display the camera preview
        Surface  previewSurface =  new Surface(surfaceTexture); // gets preview surface

        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface); // uses the previewSurface to show the camera view

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()),

                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mPreviewCaptureSession = session;
                            try {
                                mPreviewCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Toast.makeText(getApplicationContext(),
                                    "Unable to set up camera preview", Toast.LENGTH_SHORT).show();
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    // startStillCaptureRequest(), gets called when a picture is taken
    private void startStillCaptureRequest(){

        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            mCaptureRequestBuilder.addTarget(mImageReader.getSurface());
            CameraCaptureSession.CaptureCallback stillCaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);

                }
            };
            if (!hasCap){  // if not hasCap, takes picture and makes hasCap True.
                mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), stillCaptureCallback, null); // Takes picture
                hasCap = true;
            }
//
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // closes the camera
    private void  closeCamera(){
        if (mCameraDevice != null){
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    private void startBackgroundThread() {
        mBackgroundHandlerThread = new HandlerThread("moneycount");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
    }

    private void  stopBackgroundThread(){
        mBackgroundHandlerThread.quitSafely();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgroundHandler = null;
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    // sensorToDeviceRotation takes the cameraCharacteristics of the current camera hardware and also the device
    // orientation to estimate the total rotation.
    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation){
        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION); // Current sensor rotation
        deviceOrientation = ORIENTATIONS.get(deviceOrientation); // converts device rotation to degrees.
        return (sensorOrientation + deviceOrientation +360) % 360; // Calculate and returns total rotation.
    }

    // chooseOptimalSize takes possible suitable display dimensions and the width and height of the screen, to choose the most optimal resolution.
    private static Size chooseOptimalSize(Size[] choices, int width, int height){
        List<Size> bigEnough = new ArrayList<Size>(); // Stores sizes bigger than the default width and height.
        for (Size option : choices){ // loops through possible options and compare with width and height.
            if (option.getHeight() == option.getWidth() * height / width && option.getWidth() >= width
                    && option.getHeight() >= height){

                bigEnough.add(option);
            }
        }

        if (bigEnough.size() > 0){ // Returns the minimum choice by area.
            return Collections.min(bigEnough, new CompareSizeByArea());
        }else{
            return  choices[0];
        }
    }

    // lockFocus(), locks camera focus on an object.
    private void lockFocus(){

        mCaptureState = STATE_WAIT_LOCK; // sets captureState to lock on Image
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START); // Sets the camera to autofocus
        try {
            mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), mPreviewCaptureCallback, mBackgroundHandler); // takes picture
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

        // getStringIdentifier() takes the context and the audio file name, in order to load as raw.
        public int getStringIdentifier(Context context, String name) {
            return context.getResources().getIdentifier(name, "raw", context.getPackageName());
        }

        // convertToText() Takes in the word equivalent of a number, loads into the MediaPlayer and plays the soundbyte.
        public void convertText(String numberWord){
            String[] denoms = numberWord.split(" "); // splits words
            int i = 0;
            for (String denom : denoms) { // Loops thorugh denoms
                i += 1;
                final MediaPlayer numb = MediaPlayer.create(getApplicationContext(),  getStringIdentifier(getApplicationContext(), denom)); // loads denom soundbyte
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        numb.start();
                    }}, 1300 * i); // plays denom soundbyte and delays.
            }

            // loads and plays naira soundbyte
            final MediaPlayer numb = MediaPlayer.create(getApplicationContext(),  getStringIdentifier(getApplicationContext(), "naira"));
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    numb.start();
                }}, 1300 * (i+1));
        }

}
