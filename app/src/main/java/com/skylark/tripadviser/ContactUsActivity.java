package com.skylark.tripadviser;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.Locale;

public class ContactUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void openDialer(View view) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:+919309307416"));
        startActivity(intent);
    }

    public void openMail(View view) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"trades.fuel@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Fuel Trades Android App - Contact Us");
        startActivity(Intent.createChooser(intent, "Send Email Via"));
    }

    public void openMap(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:26.9124,75.7873?q=26.9124,75.7873(Fuel Trades)"));
        startActivity(intent);
    }
}
