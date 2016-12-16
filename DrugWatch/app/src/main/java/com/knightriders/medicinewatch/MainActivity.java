package com.knightriders.medicinewatch;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.knightriders.medicinewatch.helper.PrefManager;
import com.knightriders.medicinewatch.helper.RequestHandler;
import com.knightriders.medicinewatch.helper.SQLiteHandler;

import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;
import net.i2p.android.ext.floatingactionbutton.FloatingActionsMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String ndc = null;
    String actualNumber;
    boolean drugFound = false;
    String recallStatus = "", productDescription = "", recallNumber = "", recallReason = "", recallClassification = "", productNdc;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;
    FloatingActionsMenu menu;
    private String realPath = null;
    private Uri mHighQualityImageUri = null;
    private FirebaseAnalytics mFirebaseAnalytics;
    PrefManager prefManager;
    ProgressDialog loading;
    String formatOne = "00000-0000-00", formatTwo = "0000-0000-00", formatThree = "00000-000-00", formatFour = "00000-0000-0";
    private static final String OPEN_FDA_API_KEY = "L8Nuy9Xx68r1WwZQyrU8Jg4BH0dsruUaBWaf1BRp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefManager = new PrefManager(this);
        setContentView(R.layout.activity_main);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        final FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_layout);
        frameLayout.getBackground().setAlpha(0);
        menu = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
        menu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                frameLayout.getBackground().setAlpha(240);
                frameLayout.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        menu.collapse();
                        return true;
                    }
                });
            }

            @Override
            public void onMenuCollapsed() {
                frameLayout.getBackground().setAlpha(0);
                frameLayout.setOnTouchListener(null);
            }
        });

        FloatingActionButton fabHistory = (FloatingActionButton) findViewById(R.id.scanHistory);
        fabHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(i);
                menu.collapse();
            }
        });

        FloatingActionButton fabUsage = (FloatingActionButton) findViewById(R.id.usageDemo);
        fabUsage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.collapse();
                Intent i = new Intent(MainActivity.this, WelcomeActivity.class);
                i.putExtra("mainScreen", true);
                startActivity(i);
                finish();
            }
        });

        FloatingActionButton fabShare = (FloatingActionButton) findViewById(R.id.shareApp);
        fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.collapse();
                Intent localIntent = new Intent("android.intent.action.SEND");
                localIntent.setType("text/plain");
                localIntent.putExtra("android.intent.extra.SUBJECT", "Download Drug Watch");
                localIntent.putExtra("android.intent.extra.TEXT", "https://play.google.com/store/apps/details?id=com.knightriders.drugwatch");
                startActivity(Intent.createChooser(localIntent, "Share \"Drug Watch\" via"));
            }
        });

        FloatingActionButton fabAbout = (FloatingActionButton) findViewById(R.id.aboutUs);
        fabAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.collapse();
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
            }
        });

        TextView selectCamera = (TextView) findViewById(R.id.selectCamera);
        selectCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "2");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Camera");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Button");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                startCamera();
                menu.collapse();
            }
        });

        TextView selectGallery = (TextView) findViewById(R.id.selectGallery);
        selectGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "1");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Gallery");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Button");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                startGalleryChooser();
                menu.collapse();
            }
        });

        TextView selectBarcode = (TextView) findViewById(R.id.selectBarcode);
        selectBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "3");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Barcode");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Button");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                //Toast.makeText(MainActivity.this, "Coming Soon!", Toast.LENGTH_LONG).show();
                new IntentIntegrator(MainActivity.this).setCaptureActivity(BarcodeActivity.class).initiateScan();
                menu.collapse();
            }
        });

        TextView welcomeText = (TextView) findViewById(R.id.welcomeText);
        welcomeText.setText(prefManager.getAppTheme());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (menu.isExpanded()) {
            menu.collapse();
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void startGalleryChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                GALLERY_IMAGE_REQUEST);
    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            mHighQualityImageUri = generateTimeStampPhotoFileUri();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mHighQualityImageUri);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            //uploadImage(Uri.fromFile(getCameraFile()));
            Intent i = new Intent(this, CaptureImageActivity.class);
            i.putExtra("realPath", realPath);
            startActivity(i);
        } else {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    Log.d("MainActivity", "Cancelled scan");
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    Log.d("MainActivity", "Scanned");
                    Toast.makeText(this, "Sorry for inconvenience! Barcode Scanning Coming Soon", Toast.LENGTH_LONG).show();
                    /*actualNumber = result.getContents();
                    StringBuilder sb = new StringBuilder(actualNumber);
                    sb.deleteCharAt(0);
                    sb.deleteCharAt(sb.length()-1);
                    actualNumber = sb.toString();
                    if (actualNumber.length() == 11) {
                        formatOne = actualNumber;
                        formatOne = actualNumber.substring(0, 4) + "-" + actualNumber.substring(5, 8) + "-" + actualNumber.substring(9, 10);
                    } else {
                        formatTwo = actualNumber;
                        formatTwo = actualNumber.substring(0, 3) + "-" + actualNumber.substring(4, 7) + "-" + actualNumber.substring(8, 9);
                        formatThree = actualNumber;
                        formatThree = actualNumber.substring(0, 4) + "-" + actualNumber.substring(5, 7) + "-" + actualNumber.substring(8, 9);
                        formatFour = actualNumber;
                        formatFour = actualNumber.substring(0, 4) + "-" + actualNumber.substring(5, 8) + "-" + actualNumber.substring(9);
                    }
                    new GetFDAData().execute();*/
                }
            } else {
                // This is important, otherwise the result will not be passed to the fragment
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtils.permissionGranted(
                requestCode,
                CAMERA_PERMISSIONS_REQUEST,
                grantResults)) {
            startCamera();
        }
    }

    public void uploadImage(Uri uri) {
        if (uri != null) {

            Intent i = new Intent(this, CaptureImageActivity.class);
            i.putExtra("image", uri.toString());
            startActivity(i);

        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    private class GetFDAData extends AsyncTask<String, Void, Object> {

        String JSON_STRING;
        JSONObject jsonObject = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = ProgressDialog.show(MainActivity.this, null, "Getting Recall Status...", true, true);
            loading.setCancelable(false);

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

            Intent i = new Intent(MainActivity.this, ResultActivity.class);
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
            JSON_STRING = rh.sendGetRequest("https://api.fda.gov/drug/enforcement.json?api_key=" + OPEN_FDA_API_KEY + "&search=(product_description:" + formatOne + "+" + formatTwo + "+" + formatThree + "+" + formatFour + ")");

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
                    productNdc = actualNumber;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private Uri generateTimeStampPhotoFileUri() {

        android.net.Uri photoFileUri = null;
        File outputDir = getPhotoDirectory();
        if (outputDir != null) {
            Time t = new Time();
            t.setToNow();
            File photoFile = new File(outputDir, System.currentTimeMillis()
                    + ".png");
            photoFileUri = android.net.Uri.fromFile(photoFile);
            realPath = photoFile.toString();
        }
        return photoFileUri;
    }

    private File getPhotoDirectory() {
        File outputDir = null;
        String externalStorageStagte = Environment.getExternalStorageState();
        if (externalStorageStagte.equals(Environment.MEDIA_MOUNTED)) {
            File photoDir = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            outputDir = new File(photoDir, "MedicineWatch");
            if (!outputDir.exists())
                if (!outputDir.mkdirs()) {
                    Toast.makeText(
                            this,
                            "Error Creating Directory "
                                    + outputDir.getAbsolutePath(),
                            Toast.LENGTH_SHORT).show();
                    outputDir = null;
                }
        }
        return outputDir;
    }
}