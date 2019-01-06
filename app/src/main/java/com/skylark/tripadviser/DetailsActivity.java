package com.skylark.tripadviser;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class DetailsActivity extends AppCompatActivity {

    private TextView title;
    private TextView requestId;
    private TextView requestIdValue;
    private TextView timeStamp;
    private TextView timeStampValue;
    private TextView amount;
    private TextView amountValue;
    private TextView account;
    private TextView accountValue;
    private TextView receiptNumber;
    private TextView receiptNumberValue;
    private TextView cmnts;
    private TextView cmntsValue;
    private TextView status;
    private TextView statusValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        title = findViewById(R.id.title);
        requestId = findViewById(R.id.tvRequestId);
        requestIdValue = findViewById(R.id.tvRequestIdValue);
        timeStamp = findViewById(R.id.tvTimeStamp);
        timeStampValue = findViewById(R.id.tvTimeStampValue);
        amount = findViewById(R.id.tvAmount);
        amountValue = findViewById(R.id.tvAmountValue);
        account = findViewById(R.id.tvAccount);
        accountValue = findViewById(R.id.tvAccountValue);
        receiptNumber = findViewById(R.id.tvReceiptNumber);
        receiptNumberValue = findViewById(R.id.tvReceiptNumberValue);
        cmnts = findViewById(R.id.tvCmnts);
        cmntsValue = findViewById(R.id.tvCmntsValue);
        status = findViewById(R.id.tvStatus);
        statusValue = findViewById(R.id.tvStatusValue);

        Intent intent = getIntent();
        if(intent.getStringExtra("Activity").equals("CardRefillRequests")) {
            title.setText("Card Refill Request");
            requestId.setText("Payment ID");
            requestIdValue.setText(intent.getStringExtra("paymentId"));
            account.setText("Card");
            accountValue.setText(intent.getStringExtra("cardNumber"));
            receiptNumber.setVisibility(View.GONE);
            receiptNumberValue.setVisibility(View.GONE);
        }
        else {
            title.setText("Money Request");
            requestIdValue.setText(intent.getStringExtra("requestId"));
            accountValue.setText(intent.getStringExtra("account"));
            receiptNumberValue.setText(intent.getStringExtra("receiptNumber"));
        }

        timeStampValue.setText(intent.getStringExtra("timeStamp"));
        amountValue.setText(intent.getStringExtra("amount"));
        cmntsValue.setText(intent.getStringExtra("comments"));
        statusValue.setText(intent.getStringExtra("status"));

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
