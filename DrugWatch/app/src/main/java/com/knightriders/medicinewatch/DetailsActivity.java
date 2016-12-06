package com.knightriders.medicinewatch;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

public class DetailsActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        String recallNdc = getIntent().getStringExtra("recallNdc");
        String recallNumber = getIntent().getStringExtra("recallNumber");
        String recallStatus = getIntent().getStringExtra("recallStatus");
        String recallReason = getIntent().getStringExtra("recallReason");
        String recallClassification= getIntent().getStringExtra("recallClassification");

        Dialog dialog = new Dialog(DetailsActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_fail_details);
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
        TextView number = (TextView) dialog.findViewById(R.id.recallNumber);
        TextView reason = (TextView) dialog.findViewById(R.id.recallReason);
        TextView classification = (TextView) dialog.findViewById(R.id.recallClassification);
        ndc.setText(recallNdc);
        status.setText(recallStatus);
        number.setText(recallNumber);
        reason.setText(recallReason);
        classification.setText(recallClassification);

        dialog.show();
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

        return super.onOptionsItemSelected(item);
    }
}
