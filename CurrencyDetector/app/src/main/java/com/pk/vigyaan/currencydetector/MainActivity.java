package com.pk.vigyaan.currencydetector;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.pk.vigyaan.currencydetector.Models.ImageModel;
import com.pk.vigyaan.currencydetector.Models.ImageResponse;
import com.pk.vigyaan.currencydetector.restapi.APIServices;
import com.pk.vigyaan.currencydetector.restapi.AppClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private Camera camera;
    private CameraPreview cameraPreview;
    private TextToSpeech textToSpeech;
    private SpeechRecognizer  speechRecognizer;
    private int requestCode_CAMERA = 100;
    private Button capture, preview;
    private final int requestCode_AUDIO = 200;
    private final int requestCode_SPEAKER = 300;
    private boolean isFlashOn;
    private Camera.Parameters params;
    private ImageModel model;

    private Camera.PictureCallback picture = (data, camera) -> {
        File photo = getCacheDir();//new File(Environment.getDownloadCacheDirectory(), "photo.jpg");

        if (photo == null){
            Log.d("file null ", "Error creating media file, check storage permissions");
            return;
        }
        if (photo.exists()) {
            photo.delete();
        }

        try {
            FileOutputStream fos = new FileOutputStream(photo);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.e("FNF ", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.e("IO ", "Error accessing file: " + e.getMessage());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkCameraHardware(this)) {
            runCameraPreview();
        }
        capture = findViewById(R.id.capture);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechRecognizing();
            }
        });
        preview = findViewById(R.id.preview);
        preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog = Utils.showDialog(MainActivity.this, true, null, null);
                View v =LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_photo_captured, null);
                ImageView img = v.findViewById(R.id.image);
                img.setImageBitmap(BitmapFactory.decodeFile(getCacheDir().getAbsolutePath()));
                dialog.setView(v);
                dialog.show();
            }
        });

        startSpeechRecognizing();
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    int ttsLang = textToSpeech.setLanguage(Locale.ENGLISH);

                    if(ttsLang == TextToSpeech.LANG_MISSING_DATA || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Utils.showLongToast(MainActivity.this, "Language not Supported");
                    }
                }
            }
        });
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, requestCode_CAMERA);
            }
            // this device has a camera

            //checking for flash
            if(!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                AlertDialog alert = Utils.showDialog(context,false,"Flashlight not found","Sorry this app requirs flashlight");
                alert.setButton(DialogInterface.BUTTON_NEGATIVE, "Exit",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // closing the application
                        finish();
                    }
                });
                alert.show();
            }
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == requestCode_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                AlertDialog dialog = Utils.showDialog(this, false, "Permission Denied", "Permission Needed to access camera");
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkCameraHardware(MainActivity.this);
                        dialog.dismiss();
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        onDestroy();
                    }
                });
                dialog.show();
            } else
                runCameraPreview();
        } else if (requestCode == requestCode_AUDIO) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                AlertDialog dialog = Utils.showDialog(this, false, "Permission Denied", "Permission Needed to Record sound");
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        onDestroy();
                    }
                });
                dialog.show();
            }
        }
    }

    private Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
            params = camera.getParameters();
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    void runCameraPreview() {
        camera = getCameraInstance();
        if (camera == null)
            Log.e("Camera instance ", "camera instance is null");
        cameraPreview = new CameraPreview(this, camera);

        //set camera to continually auto-focus
//        params = camera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(params);

        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(cameraPreview);
        if(!isFlashOn){
            turnOnFlash();
        }
    }

    void takePic(){
        camera.takePicture(null, null, picture);
        speak("Picture is succesfully captured and is being proccessed");
    }

    private void turnOnFlash() {
        if (!isFlashOn) {
            if (camera == null || params == null) {
                return;
            }

//            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
            isFlashOn = true;
        }
    }

    private void turnOffFlash() {
        if (isFlashOn) {
            if (camera == null || params == null) {
                return;
            }

//            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
            isFlashOn = false;
        }
    }

    void startSpeechRecognizing(){
         speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                Log.e("Speech listening","buffer recived"+ Arrays.toString(buffer));
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int error) {
                Log.e("Speech listening","Error occured error code="+error);
                startSpeechRecognizing();
            }

            @Override
            public void onResults(Bundle results) {
                List<String> matches = results
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                //displaying the first match
                if (matches != null){
                    Log.e("Speech recorded: ",matches.get(0));
                    if(matches.get(0).equalsIgnoreCase("capture") || matches.get(0).equalsIgnoreCase("takePicture")) {
                        takePic();
                        speechRecognizer.stopListening();
                        startSpeechRecognizing();
                    }
                    else
                        speak(matches.get(0));
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                Log.e("Speech listening","partial results" + partialResults.toString());
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                Log.e("Speech listening","event occured type=" + eventType + " and params="+params.toString());
            }
        });

        speechRecognizer.startListening(speechRecognizerIntent);
    }

    void speak(String msg){
        textToSpeech.setSpeechRate(1f);
        textToSpeech.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (camera != null)
            camera.release();
        if(speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.cancel();
        }
        if(isFlashOn)
            turnOffFlash();
    }

    void APICall() {
        Call<ImageResponse> call = AppClient.getInstance().createService(APIServices.class).sendImage(model);
        call.enqueue(new Callback<ImageResponse>() {
            @Override
            public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                if(response.isSuccessful()) {
                    ImageResponse imageResponse = response.body();
                    if(imageResponse!=null){
                        speak(imageResponse.getResponse());
                    }
                    else{
                        try {
                            Log.e("error body: ",response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ImageResponse> call, Throwable t) {
                speak("Something went wrong, Please try again!");
                Log.e("error of failure",t.toString());
            }
        });
    }
}