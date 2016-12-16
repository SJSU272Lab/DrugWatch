package com.knightriders.medicinewatch;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        TextView developerNrupesh = (TextView) findViewById(R.id.developerNrupesh);
        developerNrupesh.setClickable(true);
        developerNrupesh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent localIntent = new Intent("android.intent.action.SENDTO");
                localIntent.setData(Uri.parse("mailto:" + Uri.encode("nrupesh.patel2912@gmail.com") + "?subject=" + Uri.encode("DrugWatCh Support") + "&body=" + Uri.encode("Hello Nrupesh, \n\n")));
                startActivity(Intent.createChooser(localIntent, "Send mail..."));
            }
        });

        TextView developerArpita = (TextView) findViewById(R.id.developerArpita);
        developerArpita.setClickable(true);
        developerArpita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent localIntent = new Intent("android.intent.action.SENDTO");
                localIntent.setData(Uri.parse("mailto:" + Uri.encode("arpi.dixit91@gmail.com") + "?subject=" + Uri.encode("DrugWatCh Support") + "&body=" + Uri.encode("Hello Arpita, \n\n")));
                startActivity(Intent.createChooser(localIntent, "Send mail..."));
            }
        });

        TextView developerSuraj = (TextView) findViewById(R.id.developerSuraj);
        developerSuraj.setClickable(true);
        developerSuraj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent localIntent = new Intent("android.intent.action.SENDTO");
                localIntent.setData(Uri.parse("mailto:" + Uri.encode("khurana3773@gmail.com") + "?subject=" + Uri.encode("DrugWatCh Support") + "&body=" + Uri.encode("Hello Suraj, \n\n")));
                startActivity(Intent.createChooser(localIntent, "Send mail..."));
            }
        });

        TextView developerAditi = (TextView) findViewById(R.id.developerAditi);
        developerAditi.setClickable(true);
        developerAditi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent localIntent = new Intent("android.intent.action.SENDTO");
                localIntent.setData(Uri.parse("mailto:" + Uri.encode("aditi.shetty47@gmail.com") + "?subject=" + Uri.encode("DrugWatCh Support") + "&body=" + Uri.encode("Hello Aditi, \n\n")));
                startActivity(Intent.createChooser(localIntent, "Send mail..."));
            }
        });



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