package com.knightriders.medicinewatch;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.knightriders.medicinewatch.helper.ConnectionDetector;
import com.knightriders.medicinewatch.helper.RequestHandler;
import com.knightriders.medicinewatch.helper.SQLiteHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaptureImageActivity extends AppCompatActivity {

    Uri image = null;
    String realPath = null;
    ImageView capturedImage;
    Bitmap bitmap;
    ProgressDialog loading;
    String ndc = null;
    boolean drugFound = false;
    String checkGlide = null;
    boolean isImageReady = false;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private static final String TAG = CaptureImageActivity.class.getSimpleName();
    String recallStatus = "", productDescription = "", recallNumber = "", recallReason = "", recallClassification = "", productNdc;
    private static final String OPEN_FDA_API_KEY = "L8Nuy9Xx68r1WwZQyrU8Jg4BH0dsruUaBWaf1BRp";
    private static final String CLOUD_VISION_API_KEY = "AIzaSyAz6gg_NBvxRzbB0fI1dIMQXaarNHT5bgM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_captured_image);

        if (getIntent().getStringExtra("image") != null) {
            image = Uri.parse(getIntent().getStringExtra("image"));
        } else {
            realPath = getIntent().getStringExtra("realPath");
            checkGlide = realPath;
        }

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        final FloatingActionButton done = (FloatingActionButton) findViewById(R.id.done);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        capturedImage = (ImageView) findViewById(R.id.capturedImage);

        if (checkGlide != null) {

            //Toast.makeText(this, realPath, Toast.LENGTH_LONG).show();

            File file = new File(realPath);

            Glide.with(this)
                    .load(file)
                    .listener(new RequestListener<File, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, File model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, File model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            isImageReady = true;
                            return false;
                        }
                    })
                    .into(capturedImage);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{Uri.parse("file://" + Environment.getExternalStorageDirectory()).toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {

                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        // TODO Auto-generated method stub

                    }
                });
            } else {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                        Uri.parse("file://"
                                + Environment.getExternalStorageDirectory())));
            }

        } else {

            try {

                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), image);

            } catch (IOException e) {
                e.printStackTrace();
            }

            capturedImage.setImageBitmap(bitmap);
            progressBar.setVisibility(View.GONE);
            isImageReady = true;

        }

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isImageReady) {
                    try {
                        ConnectionDetector cd = new ConnectionDetector(CaptureImageActivity.this);
                        Boolean isInternetPresent = cd.isConnectingToInternet();
                        if (isInternetPresent) {
                            if (checkGlide != null)
                                callCloudVision(scaleBitmapDown(
                                        ((GlideBitmapDrawable) capturedImage.getDrawable()).getBitmap(), 1500));
                            else
                                callCloudVision(scaleBitmapDown(
                                        ((BitmapDrawable) capturedImage.getDrawable()).getBitmap(), 1500));
                        } else {
                            Toast.makeText(CaptureImageActivity.this, "No Internet Connectivity!!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(CaptureImageActivity.this, "Image resource not ready!", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
        }

        if (id == R.id.action_rotate) {
            rotate(90);
        }

        return super.onOptionsItemSelected(item);
    }

    private void rotate(float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);

        Bitmap toTransform;

        if (checkGlide != null) {
            toTransform = ((GlideBitmapDrawable) capturedImage.getDrawable()).getBitmap();
            checkGlide = null;
        } else {
            toTransform = ((BitmapDrawable) capturedImage.getDrawable()).getBitmap();
        }

        capturedImage.setImageBitmap(Bitmap.createBitmap(toTransform, 0, 0, toTransform.getWidth(), toTransform.getHeight(), matrix, true));
    }

    private void callCloudVision(final Bitmap bitmap) throws IOException {
        // Switch text to loading
        //mImageDetails.setText(R.string.loading_message);

        // Do the real work in an async task, because we need to use the network anyway
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(new
                            VisionRequestInitializer(CLOUD_VISION_API_KEY));
                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Add the image
                        Image base64EncodedImage = new Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Base64 encode the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        // add the features we want
                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature labelDetection = new Feature();
                            labelDetection.setType("TEXT_DETECTION");
                            //labelDetection.setMaxResults(10);
                            add(labelDetection);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    //Log.d(TAG, "created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    return convertResponseToString(response);

                } catch (GoogleJsonResponseException e) {
                    //Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    //Log.d(TAG, "failed to make API request because of other IOException "  + e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result) {

                result = getNDCFromData(result);

                if (ndc == null) {
                    mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

                    FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                            .setDeveloperModeEnabled(BuildConfig.DEBUG)
                            .build();
                    mFirebaseRemoteConfig.setConfigSettings(configSettings);

                    mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

                    long cacheExpiration = 3600; // 1 hour in seconds.
                    // If in developer mode cacheExpiration is set to 0 so each fetch will retrieve values from
                    // the server.
                    if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
                        cacheExpiration = 0;
                    }

                    mFirebaseRemoteConfig.fetch(cacheExpiration)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Fetch Succeeded");
                                        // Once the config is successfully fetched it must be activated before newly fetched
                                        // values are returned.
                                        mFirebaseRemoteConfig.activateFetched();
                                    } else {
                                        Log.d(TAG, "Fetch failed");
                                    }
                                    Intent i = new Intent(CaptureImageActivity.this, ResultActivity.class);
                                    i.putExtra("noNDC", true);
                                    i.putExtra("ndcText", mFirebaseRemoteConfig.getString("ndc_text"));
                                    startActivity(i);
                                    finish();
                                    loading.dismiss();
                                }
                            });

                } else {
                    new GetFDAData().execute();
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(CaptureImageActivity.this, null, "Getting Recall Status...", true, true);
                loading.setCancelable(false);
            }
        }.execute();
    }

    public String getNDCFromData(String result) {

        String regex = "((\\d{5}-\\d{4}-\\d{2})|\\d{5}-\\d{3}-\\d{2})|(\\d{4}-\\d{4}-\\d{2})|(\\d{5}-\\d{4}-\\d)";

        Matcher m = Pattern.compile(regex).matcher(result);
        if (m.find()) {
            result = m.group();
            ndc = result;
        }

        return result;

    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = "I found these things:\n\n";

        List<EntityAnnotation> labels = response.getResponses().get(0).getTextAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message += String.format("%.3f: %s", label.getScore(), label.getDescription());
                message += "\n";
            }
        } else {
            message += "nothing";
        }

        return message;
    }

    private class GetFDAData extends AsyncTask<String, Void, Object> {

        String JSON_STRING;
        JSONObject jsonObject = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            loading.dismiss();

            SQLiteHandler db = new SQLiteHandler(getApplicationContext());
            if (drugFound) {
                db.addScan(String.valueOf(db.getTableSize() + 1), "ic_recall", productNdc, recallStatus, recallNumber, recallReason, productDescription);
            } else {
                db.addScan(String.valueOf(db.getTableSize() + 1), "ic_safe", productNdc, "None", "None", "None", "None");
            }

            db.close();

            Intent i = new Intent(CaptureImageActivity.this, ResultActivity.class);
            i.putExtra("drugFound", drugFound);
            i.putExtra("productNdc", productNdc);
            if (drugFound) {
                i.putExtra("recallReason", recallReason);
                i.putExtra("recallStatus", recallStatus);
                i.putExtra("recallNumber", recallNumber);
                i.putExtra("recallClassification", recallClassification);
                i.putExtra("productDescription", productDescription);
            }
            startActivity(i);
            finish();
        }

        @Override
        protected String doInBackground(String... params) {

            RequestHandler rh = new RequestHandler();
            JSON_STRING = rh.sendGetRequest("https://api.fda.gov/drug/enforcement.json?api_key=" + OPEN_FDA_API_KEY + "&search=product_description:\"" + ndc + "\"");

            try {
                jsonObject = new JSONObject(JSON_STRING);
                if (jsonObject.has("error")) {
                    productNdc = ndc;
                    drugFound = false;
                } else {
                    drugFound = true;
                    JSONArray result = jsonObject.getJSONArray("results");

                    JSONObject jo = result.getJSONObject(0);
                    recallReason = jo.getString("reason_for_recall");
                    productDescription = jo.getString("product_description");
                    recallNumber = jo.getString("recall_number");
                    recallStatus = jo.getString("status");
                    recallClassification = jo.getString("classification");
                    productNdc = ndc;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }
}