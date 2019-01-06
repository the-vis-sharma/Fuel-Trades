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

public class DriveFareActivity extends AppCompatActivity {

    private EditText etStartCity;
    private EditText etDestinationCity;
    private EditText etViaCity;
    private EditText etAvg;
    private Spinner vehicleType;
    private RadioGroup rbgDriveType;
    private RadioButton rbDriveType;
    private RadioGroup rbgFuelType;
    private RadioButton rbFuelType;
    private Button btnGetFare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive_fare);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etStartCity = findViewById(R.id.etStartCity1);
        etDestinationCity = findViewById(R.id.etDestinationCity1);
        etViaCity = findViewById(R.id.etViaCity1);
        etAvg = findViewById(R.id.etAvg);
        rbgDriveType = findViewById(R.id.rbgDriveType1);
        rbgFuelType = findViewById(R.id.rbgFuelType1);
        btnGetFare = findViewById(R.id.btnGetFare1);

        Intent intent = getIntent();
        etStartCity.setText(intent.getStringExtra("startCity"));
        etDestinationCity.setText(intent.getStringExtra("destinationCity"));

        final Intent selectCity = new Intent(DriveFareActivity.this, SelectCity.class);

        etStartCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(selectCity, 221);
            }
        });

        etDestinationCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(selectCity, 222);
            }
        });

        etViaCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(selectCity, 223);
            }
        });

        btnGetFare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                String startCity = etStartCity.getText().toString();
                String destinationCity = etDestinationCity.getText().toString();
                String viaCity = etViaCity.getText().toString();
                rbFuelType = findViewById(rbgFuelType.getCheckedRadioButtonId());
                String fuelType = rbFuelType.getText().toString();
                rbDriveType = findViewById(rbgDriveType.getCheckedRadioButtonId());
                String driveType = rbDriveType.getText().toString();
                vehicleType = findViewById(R.id.spVehicleType);
                float avg;

                if(!isConnected()) {
                    Toast.makeText(getApplicationContext(), "Error: No internet connection.", Toast.LENGTH_SHORT).show();
                }
                else if(startCity.isEmpty()) {
                    etStartCity.setError("Start City is required.");
                }
                else if (destinationCity.isEmpty()) {
                    etDestinationCity.setError("Destinaion City is required.");
                }
                else if (etAvg.getText().toString().isEmpty()) {
                    etAvg.setError("Average is required.");
                }
                else if(Double.parseDouble(etAvg.getText().toString()) > 150.0) {
                    etAvg.setError("Please enter a valid vehicle average.");
                }
                else {
                    avg = Float.parseFloat(etAvg.getText().toString());
                    Intent intent = new Intent(DriveFareActivity.this, DriveFareDetails.class);
                    intent.putExtra("startCity", startCity);
                    intent.putExtra("destinationCity", destinationCity);
                    intent.putExtra("viaCity", viaCity);
                    intent.putExtra("fuelType", fuelType);
                    intent.putExtra("avg", avg);
                    intent.putExtra("driveType", driveType);
                    intent.putExtra("vehicleType", vehicleType.getSelectedItem().toString());
                    startActivity(intent);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data!=null) {
            if(requestCode == 221) {
                etStartCity.setText(data.getStringExtra("result"));
            }
            else if (requestCode == 222) {
                etDestinationCity.setText(data.getStringExtra("result"));
            }
            else if(requestCode == 223) {
                etViaCity.setText(data.getStringExtra("result"));
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
