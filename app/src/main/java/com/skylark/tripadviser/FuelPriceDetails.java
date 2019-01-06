package com.skylark.tripadviser;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FuelPriceDetails extends AppCompatActivity {

    private String city;
    private String type;
    private TextView tvCityName;
    private TextView tvPrice;
    private TextView tvFuelType;
    private TextView tvTimeDate;
    private RelativeLayout PriceCardLayout;
    private RelativeLayout progressBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_price_details);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Current Fuel Price");

        Intent i = getIntent();
        city = i.getStringExtra("city");
        type = i.getStringExtra("type");

        PriceCardLayout = findViewById(R.id.PriceCardLayout);
        progressBarLayout = findViewById(R.id.processBarLayout);
        tvCityName = findViewById(R.id.tvCity);
        tvPrice = findViewById(R.id.tvPrice);
        tvFuelType = findViewById(R.id.tvFuelType);
        tvTimeDate = findViewById(R.id.tvTimeDate);

        new getFuelPrice().execute();

    }

    public class getFuelPrice extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {

            try {
                URL url = new URL("https://fuelpriceindia.herokuapp.com/price?city="+ city +"&fuel_type="+type);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String data;
                StringBuffer response = new StringBuffer();

                while ((data = reader.readLine()) != null) {
                    response.append(data);
                }

                Log.d("Fuel Price", response.toString());

                return response.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject obj = new JSONObject(s);
                if(type.equals("Petrol")) {
                    tvPrice.setText("₹ "+obj.getDouble("petrol"));
                    tvFuelType.setText("(Petrol)");
                }
                else {
                    tvPrice.setText("₹ "+obj.getDouble("diesel"));
                    tvFuelType.setText("(Diesel)");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            tvCityName.setText(city);
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE | dd MMMM, yyyy");
            tvTimeDate.setText(sdf.format(new Date()));

            progressBarLayout.setVisibility(View.GONE);
            PriceCardLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
