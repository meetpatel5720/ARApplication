package com.mp.android.myarapp.Activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.mp.android.myarapp.Adapter.ModelListAdapter;
import com.mp.android.myarapp.Data.Model;
import com.mp.android.myarapp.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final double MIN_OPENGL_VERSION = 3.0;
    ArSceneView arSceneView;

    Button cameraButton;
    Button settingButton;
    Button modelSelectButton;
    ArrayList<Model> modelArrayList = new ArrayList<>();
    RecyclerView modelList;
    ModelListAdapter modelListAdapter;
    RelativeLayout modelListRelativeLayout;
    String modelName;

    private GestureDetector gestureDetector;
    private ModelRenderable chaletRenderable;
    private ModelRenderable mashroomRenderable;

    public static boolean checkIsSupportedDeviceOrFinish(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e("Main activity", "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e("Main activity", "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (arSceneView == null) {
            return;
        }
        if (arSceneView.getSession() == null) {
            try {
                Session session = new Session(this);
                Config config = new Config(session);
                config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
                session.configure(config);
                arSceneView.setupSession(session);
            } catch (UnavailableException e) {
                Log.d("MainActivity", String.valueOf(e));
            }
        }

        try {
            arSceneView.resume();
        } catch (CameraNotAvailableException ex) {
            Log.d("Unable to get camera", String.valueOf(ex));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }
        setContentView(R.layout.activity_main);

        arSceneView = findViewById(R.id.ux_fragment);
        cameraButton = findViewById(R.id.camera);
        settingButton = findViewById(R.id.setting);
        modelSelectButton = findViewById(R.id.select_model);
        modelList = findViewById(R.id.model_list);
        modelListRelativeLayout = findViewById(R.id.model_list_layout);
        modelList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        modelArrayList.add(new Model(R.drawable.chalet, "Chalet.sfb", false));
        modelArrayList.add(new Model(R.drawable.mashroom, "mashroom.sfb", false));

        modelListAdapter = new ModelListAdapter(this, modelArrayList);
        modelList.setAdapter(modelListAdapter);

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        modelSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (modelListRelativeLayout.getVisibility() == View.VISIBLE) {
                    modelListRelativeLayout.setVisibility(View.GONE);
                } else {
                    modelListRelativeLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        buildModel();
        gestureDetector =
                new GestureDetector(
                        this,
                        new GestureDetector.SimpleOnGestureListener() {
                            @Override
                            public boolean onSingleTapUp(MotionEvent e) {
                                onSingleTap(e);
                                return true;
                            }

                            @Override
                            public boolean onDown(MotionEvent e) {
                                return true;
                            }
                        });

        arSceneView
                .getScene()
                .setOnTouchListener(
                        (HitTestResult hitTestResult, MotionEvent event) -> {
                            return gestureDetector.onTouchEvent(event);
                        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto(arSceneView);
            }
        });
    }

    private void onSingleTap(MotionEvent tap) {
        Frame frame = arSceneView.getArFrame();
        if (frame != null) {
            placeObject(tap, frame);
        }
    }

    private void placeObject(MotionEvent tap, Frame frame) {
        if (tap != null && frame.getCamera().getTrackingState() == TrackingState.TRACKING) {
            for (HitResult hit : frame.hitTest(tap)) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    Anchor anchor = hit.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arSceneView.getScene());
                    anchorNode.setRenderable(getModel());
                }
            }
        }
    }

    public void buildModel(){
        CompletableFuture<ModelRenderable> chalet1 =
                ModelRenderable.builder().setSource(this, Uri.parse("Chalet.sfb")).build();
        CompletableFuture<ModelRenderable> mashroom =
                ModelRenderable.builder().setSource(this, Uri.parse("mashroom.sfb")).build();

        CompletableFuture.allOf(
                chalet1,
                mashroom)
                .handle((notUsed, throwable) -> {
                    if (throwable != null) {
                        return null;
                    }
                    try {
                        chaletRenderable = chalet1.get();
                        mashroomRenderable = mashroom.get();
                    } catch (InterruptedException | ExecutionException ex) {
                        Toast.makeText(this,"Unable to load model",Toast.LENGTH_LONG).show();
                        Log.d("this", "Unable to load renderable");
                    }
                    return null;
                });
    }

    public String getModelName() {
        if (modelListAdapter.getSelectedModel().getName() == null) {
            modelName = "Chalet.sfb";
        } else {
            modelName = modelListAdapter.getSelectedModel().getName();
        }
        return modelName;
    }

    public ModelRenderable getModel() {
        String name = getModelName();
        switch (name) {
            case "Chalet.sfb":
                return chaletRenderable;
            case "mashroom.sfb":
                return mashroomRenderable;
            default:
                return chaletRenderable;
        }
    }

    private String generateFilename() {
        String date =
                new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault()).format(new Date());
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + File.separator + "Sceneform/" + "AR_" + date + ".jpg";
    }

    private void saveBitmapToDisk(Bitmap bitmap, String filename) throws IOException {

        File out = new File(filename);
        if (!out.getParentFile().exists()) {
            out.getParentFile().mkdirs();
        }
        try (FileOutputStream outputStream = new FileOutputStream(filename);
             ByteArrayOutputStream outputData = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputData);
            outputData.writeTo(outputStream);
            outputStream.flush();
        } catch (IOException ex) {
            throw new IOException("Failed to save bitmap to disk", ex);
        }
    }

    private void takePhoto(ArSceneView view) {
        final String filename = generateFilename();

        final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);

        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();

        PixelCopy.request(view, bitmap, (copyResult) -> {
            if (copyResult == PixelCopy.SUCCESS) {
                try {
                    saveBitmapToDisk(bitmap, filename);
                } catch (IOException e) {
                    Toast toast = Toast.makeText(MainActivity.this, e.toString(),
                            Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                        "Photo saved", Snackbar.LENGTH_LONG);
                snackbar.setAction("Open in Photos", v -> {
                    File photoFile = new File(filename);

                    Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                            MainActivity.this.getPackageName() + ".mparapplication",
                            photoFile);
                    Intent intent = new Intent(Intent.ACTION_VIEW, photoURI);
                    intent.setDataAndType(photoURI, "image/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);

                });
                snackbar.show();
            } else {
                Toast toast = Toast.makeText(MainActivity.this,
                        "Failed to copyPixels: " + copyResult, Toast.LENGTH_LONG);
                toast.show();
            }
            handlerThread.quitSafely();
        }, new Handler(handlerThread.getLooper()));
    }
}
