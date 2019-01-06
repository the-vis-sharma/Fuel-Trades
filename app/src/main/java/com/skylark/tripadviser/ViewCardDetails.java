package com.skylark.tripadviser;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ViewCardDetails extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CustomAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ProgressDialog progressDialog;
    private ArrayList<String> cardNumbers;
    private ArrayList<String> cardCompany;
    private ArrayList<String> balance;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_card_details);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(ViewCardDetails.this, R.style.Theme_AppCompat_Light_Dialog);

        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        cardCompany = new ArrayList<>();
        cardNumbers = new ArrayList<>();
        balance = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new CustomAdapter();
        recyclerView.setAdapter(adapter);

        SharedPreferences pref = getSharedPreferences("com.skylark.fueltrades", MODE_PRIVATE);
        username = pref.getString("currentUsername", "visnu");

        if(!isConnected()) {
            Toast.makeText(getApplicationContext(), "Error: No internet connection.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ViewCardDetails.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            new LoadCards().execute(username);
        }

    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public class LoadCards extends AsyncTask<String, Void, Boolean> {

        JSONObject object;
        String msg;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            String result = HTTPRequest.sendHTTPRequest("https://fueltrades.prateekmathur.in/php/app/RequestCardDetails.php", "username=" + strings[0]);

            try {
                if(result != null) {
                    object = new JSONObject(result);
                    if (object.getString("status").equals("OK")) {
                        JSONArray cardsList = object.getJSONArray("cards");
                        for (int i = 0; i < cardsList.length(); i++) {
                            JSONObject card = cardsList.getJSONObject(i);
                            cardNumbers.add(card.getString("cardNumber"));
                            cardCompany.add(card.getString("company"));
                            balance.add("₹ " + card.getString("balance"));
                        }
                        msg = object.getString("message");
                        return true;
                    }
                }
                else {
                    msg = "Error: Server Not Available, Try again later.";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean status) {
            super.onPostExecute(status);
            progressDialog.dismiss();

            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            if(!status) {
                Intent intent = new Intent(ViewCardDetails.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
            else {
                adapter.notifyDataSetChanged();
            }
        }
    }

    class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {

        @Override
        public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.custom_card_details_layout, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CustomViewHolder holder, int position) {
            holder.tvCardNumber.setText(cardNumbers.get(position));
            holder.tvCardCompany.setText(cardCompany.get(position));
            holder.tvCardBalance.setText(balance.get(position));
        }


        @Override
        public int getItemCount() {
            return cardNumbers.size();
        }

        class CustomViewHolder extends RecyclerView.ViewHolder {
            private TextView tvCardNumber;
            private TextView tvCardCompany;
            private TextView tvCardBalance;

            public CustomViewHolder(View view) {
                super(view);
                tvCardNumber = view.findViewById(R.id.tvCardNumber);
                tvCardCompany = view.findViewById(R.id.tvCardCompany);
                tvCardBalance = view.findViewById(R.id.tvCardBalance);
            }

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
