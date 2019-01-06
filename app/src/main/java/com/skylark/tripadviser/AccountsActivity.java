package com.skylark.tripadviser;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AccountsActivity extends AppCompatActivity {

    private TextView[] acNo;
    private ImageView[] cpAcNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        acNo = new TextView[4];
        cpAcNo = new ImageView[4];

        acNo[0] = findViewById(R.id.acNo1);
        acNo[1] = findViewById(R.id.acNo2);
        acNo[2] = findViewById(R.id.acNo3);
        acNo[3] = findViewById(R.id.acNo4);

        cpAcNo[0] = findViewById(R.id.cpAcNo1);
        cpAcNo[1] = findViewById(R.id.cpAcNo2);
        cpAcNo[2] = findViewById(R.id.cpAcNo3);
        cpAcNo[3] = findViewById(R.id.cpAcNo4);

        final ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        cpAcNo[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipData clip = ClipData.newPlainText("A/c No:", acNo[0].getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), "A/c No. " + acNo[0].getText().toString() + " Copied.", Toast.LENGTH_SHORT).show();
            }
        });

        cpAcNo[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipData clip = ClipData.newPlainText("A/c No:", acNo[1].getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), "A/c No. " + acNo[1].getText().toString() + " Copied.", Toast.LENGTH_SHORT).show();
            }
        });

        cpAcNo[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipData clip = ClipData.newPlainText("A/c No:", acNo[2].getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), "A/c No. " + acNo[2].getText().toString() + " Copied.", Toast.LENGTH_SHORT).show();
            }
        });

        cpAcNo[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipData clip = ClipData.newPlainText("A/c No:", acNo[3].getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), "A/c No. " + acNo[3].getText().toString() + " Copied.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
