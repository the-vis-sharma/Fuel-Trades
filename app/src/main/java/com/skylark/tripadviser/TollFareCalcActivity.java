package com.skylark.tripadviser;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

public class TollFareCalcActivity extends AppCompatActivity {

    private EditText etStartCity;
    private EditText etDestinationCity;
    private EditText etViaCity;
    private RadioGroup rbgDriveType;
    private RadioButton radioButton;
    private Spinner spVehicleType;
    private Button btnGetFare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toll_fare_calc);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        etStartCity = findViewById(R.id.etStartCity);
        etDestinationCity = findViewById(R.id.etDestinationCity);
        etViaCity = findViewById(R.id.etViaCity);
        rbgDriveType = findViewById(R.id.rbgDriveType);
        spVehicleType = findViewById(R.id.spVehicleType);
        btnGetFare = findViewById(R.id.btnGetFare);

        final Intent i = new Intent(TollFareCalcActivity.this, SelectCity.class);

        etStartCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(i, 221);
            }
        });

        etDestinationCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(i, 222);
            }
        });

        etViaCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(i, 223);
            }
        });

        btnGetFare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isConnected()) {
                    Toast.makeText(getApplicationContext(), "Error: No internet connection.", Toast.LENGTH_SHORT).show();
                }
                else if(etStartCity.getText().toString().isEmpty()) {
                    etStartCity.setError("Start City is required.");
                }
                else if(etDestinationCity.getText().toString().isEmpty()) {
                    etDestinationCity.setError("Destination City is required.");
                }
                else {
                    Intent calFare = new Intent(TollFareCalcActivity.this, TollPriceDetails.class );
                    calFare.putExtra("startCity", etStartCity.getText().toString());
                    calFare.putExtra("destinationCity", etDestinationCity.getText().toString());
                    calFare.putExtra("viaCity", etViaCity.getText().toString());

                    radioButton = findViewById(rbgDriveType.getCheckedRadioButtonId());

                    calFare.putExtra("driveType", radioButton.getText().toString());
                    calFare.putExtra("vehicleType", spVehicleType.getSelectedItem().toString());
                    startActivity(calFare);
                }

            }
        });


    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == 221) {

            if(data != null){
                etStartCity.setText(data.getStringExtra("result"));
            }
        }
        else if(resultCode == RESULT_OK && requestCode == 222) {
            if(data != null) {
                etDestinationCity.setText(data.getStringExtra("result"));
            }
        }
        else if( resultCode == RESULT_OK && requestCode == 223) {
            if(data != null) {
                etViaCity.setText(data.getStringExtra("result"));
            }
        }
    }
}
