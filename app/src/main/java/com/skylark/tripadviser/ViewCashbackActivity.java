package com.skylark.tripadviser;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ViewCashbackActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private JSONArray list;
    private static CustomAdapter adapter;
    private static RecyclerView recyclerView;
    private SharedPreferences pref;
    private String currUsername;
    private TextView totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_cashback);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        progressDialog = new ProgressDialog(ViewCashbackActivity.this, R.style.Theme_AppCompat_Light_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);

        recyclerView = findViewById(R.id.viewCashbackDetails);
        adapter = new CustomAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        totalAmount = findViewById(R.id.totalAmount);

        pref = getSharedPreferences("com.skylark.fueltrades", MODE_PRIVATE);
        currUsername = pref.getString("currentUsername", "visnu");

        if(!isConnected()) {
            Toast.makeText(getApplicationContext(), "Error: No internet connection.", Toast.LENGTH_SHORT).show();
        } else {
            new getCashbackDetails().execute(currUsername);
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public class getCashbackDetails extends AsyncTask<String, Void, Boolean> {

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
            String result = HTTPRequest.sendHTTPRequest("https://fueltrades.prateekmathur.in/php/app/getCashbackDetails.php", "username=" + strings[0]);

            try {
                if(result != null) {
                    object = new JSONObject(result);
                    if (object.getString("status").equals("OK")) {
                        list = object.getJSONArray("cashback");
                        msg = object.getString("message");
                        return true;
                    }
                    else {
                        msg = object.getString("message");
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
                Intent intent = new Intent(ViewCashbackActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
            else {
                try {
                    totalAmount.setText("₹ " + object.getString("totalCashback"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
            try {
                JSONObject object = list.getJSONObject(position);
                holder.tvAccount.setText(object.getString("amount"));
                holder.tvTimeStamp.setText(object.getString("timeStamp"));
                holder.tvAmount.setText("₹ " + object.getString("totalAmount"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        @Override
        public int getItemCount() {
            if(list != null) {
                return list.length();
            }
            return 0;
        }

        class CustomViewHolder extends RecyclerView.ViewHolder {
            private ImageView img;
            private TextView tvAccount;
            private TextView tvTimeStamp;
            private TextView tvAmount;

            public CustomViewHolder(View view) {
                super(view);
                img = view.findViewById(R.id.icon_card);
                tvAccount = view.findViewById(R.id.tvCardNumber);
                tvTimeStamp = view.findViewById(R.id.tvCardCompany);
                tvAmount = view.findViewById(R.id.tvCardBalance);
                img.setImageDrawable(getDrawable(R.drawable.ic_add_money));
            }

        }
    }

}
