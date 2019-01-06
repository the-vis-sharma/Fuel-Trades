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
import android.widget.Toast;

public class FuelPriceActivity extends AppCompatActivity {

    private EditText city;
    private RadioGroup rbgFuelType;
    private RadioButton radioButton;
    private Button btnGetPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_price);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        city = findViewById(R.id.city);
        rbgFuelType = findViewById(R.id.rbgFuelType);
        btnGetPrice = findViewById(R.id.btnGetPrice);

        city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(FuelPriceActivity.this, SelectCity.class);
                startActivityForResult(i, 22);
            }
        });

        btnGetPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cityName = city.getText().toString();
                if(!isConnected()) {
                    Toast.makeText(getApplicationContext(), "Error: No internet connection.", Toast.LENGTH_SHORT).show();
                }
                else if(cityName.isEmpty()) {
                    city.setError("Select City First.");
                }
                else {
                    radioButton = findViewById(rbgFuelType.getCheckedRadioButtonId());
                    String fuelType = radioButton.getText().toString();
                    Intent fuelDetails = new Intent(FuelPriceActivity.this, FuelPriceDetails.class);
                    fuelDetails.putExtra("city", cityName);
                    fuelDetails.putExtra("type", fuelType);
                    startActivity(fuelDetails);
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
        if(resultCode == RESULT_OK && requestCode == 22) {
            if (data != null) {
                city.setText(data.getStringExtra("result"));
            }
        }
    }
}
