package com.skylark.tripadviser;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectCity extends AppCompatActivity implements TextWatcher {

    private ListView cityListView;
    private ProgressBar progressBar;
    private ArrayAdapter<String> adapter;
    private List<String> city;
    private AutoCompleteTextView searchCityTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);
        try {
            new getCitiesList().execute(new URL("https://fuelpriceindia.herokuapp.com/cities"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        city = new ArrayList<String>();
        searchCityTextView = findViewById(R.id.searchCityTextView);
        cityListView = findViewById(R.id.cityListView);
        progressBar = findViewById(R.id.processBar);

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, city);
        searchCityTextView.setAdapter(adapter);
        cityListView.setAdapter(adapter);

        cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                TextView textView = view.findViewById(android.R.id.text1);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", textView.getText().toString());
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

        searchCityTextView.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        searchCityTextView.dismissDropDown();
    }

    public class getCitiesList extends AsyncTask <URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            try {
                HttpURLConnection con = (HttpURLConnection) urls[0].openConnection();

                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String data;
                StringBuffer response = new StringBuffer();

                while ((data = reader.readLine()) != null) {
                    response.append(data);
                }

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
            try {
                JSONObject obj = new JSONObject(s);
                JSONArray cities = obj.getJSONArray("cities");
                for(int i=0; i < cities.length(); i++) {
                    city.add(cities.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            progressBar.setVisibility(View.GONE);
            cityListView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

}
