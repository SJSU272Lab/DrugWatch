package com.knightriders.medicinewatch;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class ResultActivity extends AppCompatActivity {

    boolean isDrugFound, noNDCFound;
    private FirebaseAnalytics mFirebaseAnalytics;
    String productNdc, ndcTextDynamic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-9584430180991563/3112410134");
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        isDrugFound = getIntent().getBooleanExtra("drugFound", false);
        noNDCFound = getIntent().getBooleanExtra("noNDC", false);
        productNdc = getIntent().getStringExtra("productNdc");
        ndcTextDynamic = getIntent().getStringExtra("ndcText");

        if (noNDCFound) {
            Dialog dialog = new Dialog(ResultActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.popup_sorry);
            AdView mAdView = (AdView) dialog.findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
            dialog.setCancelable(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            ImageView close = (ImageView) dialog.findViewById(R.id.closeButton);
            TextView ndcText = (TextView) dialog.findViewById(R.id.ndcText);
            ndcText.setText(ndcTextDynamic);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "4");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Close");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Button");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                    finish();
                }
            });

            CardView tryAgain = (CardView) dialog.findViewById(R.id.tryAgain);
            tryAgain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "5");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Try Again");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Button");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                    finish();
                }
            });
            dialog.show();
        } else if (isDrugFound) {
            final String recallStatus = getIntent().getStringExtra("recallStatus");
            final String recallNumber = getIntent().getStringExtra("recallNumber");
            final String recallReason = getIntent().getStringExtra("recallReason");
            final String productDescription = getIntent().getStringExtra("productDescription");
            final String productClassification = getIntent().getStringExtra("recallClassification");
            Dialog dialog = new Dialog(ResultActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.popup_fail);
            dialog.setCancelable(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            ImageView close = (ImageView) dialog.findViewById(R.id.closeButton);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "4");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Close");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Button");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                    finish();
                }
            });
            TextView ndc = (TextView) dialog.findViewById(R.id.ndc);
            TextView status = (TextView) dialog.findViewById(R.id.recallStatus);
            ndc.setText(productNdc);
            status.setText(recallStatus);

            CardView moreDetails = (CardView) dialog.findViewById(R.id.moreDetails);
            moreDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "6");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "More Details");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Button");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                    Intent i = new Intent(ResultActivity.this, DetailsActivity.class);
                    i.putExtra("recallStatus", recallStatus);
                    i.putExtra("recallReason", recallReason);
                    i.putExtra("recallNumber", recallNumber);
                    i.putExtra("recallClassification", productClassification);
                    i.putExtra("recallNdc", productNdc);
                    startActivity(i);
                    finish();
                }
            });
            dialog.show();
        } else {
            Dialog dialog = new Dialog(ResultActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.popup_success);
            dialog.setCancelable(false);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            ImageView close = (ImageView) dialog.findViewById(R.id.closeButton);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "4");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Close");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Button");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                    finish();
                }
            });
            TextView ndc = (TextView) dialog.findViewById(R.id.ndc);
            ndc.setText(productNdc);

            CardView moreDetails = (CardView) dialog.findViewById(R.id.finishButton);
            moreDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "7");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Finish");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Button");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                    finish();
                }
            });
            dialog.show();
        }
    }
}