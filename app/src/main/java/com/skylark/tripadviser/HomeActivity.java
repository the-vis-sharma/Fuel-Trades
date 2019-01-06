package com.skylark.tripadviser;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView userName;
    private TextView currentBalance;
    private TextView cardBalance;
    private TextView totalCashback;
    private TextView currentCashback;
    private Spinner spCardList;

    String currUsername;
    private SharedPreferences pref;
    private ProgressDialog progressDialog;
    private AsyncTask refreshData;
    private Boolean exit = false;
    private String cardDetail;
    private ArrayList<String> cards;
    private ArrayAdapter<String> adapter;
    private JSONArray cardsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);

        currentBalance = findViewById(R.id.tvCurrentBalance);
        cardBalance = findViewById(R.id.tvCardBalance);
        totalCashback = findViewById(R.id.tvTotalCashback);
        userName = header.findViewById(R.id.tvUserName);
        currentCashback = findViewById(R.id.tvViewCurrentCashback);
        spCardList = findViewById(R.id.spCardList);
        cards = new ArrayList<>();

        pref = getSharedPreferences("com.skylark.fueltrades", MODE_PRIVATE);
        currUsername = pref.getString("currentUsername", "visnu");

        if(!isConnected()) {
            Toast.makeText(getApplicationContext(), "Error: No internet connection.", Toast.LENGTH_SHORT).show();
        }
        else {
            refreshData = new refreshData().execute(currUsername);
        }
    }

    public void viewDetails(View view) {
        Intent intent = new Intent(HomeActivity.this, ViewPendingRefillRequests.class);
        startActivity(intent);
    }

    public void addMoney(View view) {
        Intent intent = new Intent(HomeActivity.this, RequestMoneyActivity.class);
        startActivity(intent);
    }

    public void refillCard(View view) {
        Intent intent = new Intent(HomeActivity.this, RefillCardActivity.class);
        startActivity(intent);
    }

    public void viewCardDetails(View view) {
        Intent intent = new Intent(HomeActivity.this, ViewCardDetails.class);
        startActivity(intent);
    }

    public void viewPendingRequests(View view) {
        Intent intent = new Intent(HomeActivity.this, ViewPendingMoneyRequest.class);
        startActivity(intent);
    }

    public void viewCashbackDetails(View view) {
        Intent intent = new Intent(HomeActivity.this, ViewCashbackActivity.class);
        startActivity(intent);
    }

    public class refreshData extends AsyncTask<String, Void, Void> {

        String userDetail;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(HomeActivity.this, R.style.Theme_AppCompat_Light_Dialog);
            progressDialog.setMessage("Updating...");
            progressDialog.setIndeterminate(true);
            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            cards.clear();
        }

        @Override
        protected Void doInBackground(String... username) {
            userDetail = HTTPRequest.sendHTTPRequest("http://fueltrades.prateekmathur.in/php/app/loadHome.php", "username=" + username[0]);
            cardDetail = HTTPRequest.sendHTTPRequest("http://fueltrades.prateekmathur.in/php/app/RequestCardDetails.php", "username=" + username[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            super.onPostExecute(voids);

            try {
                if(userDetail != null) {
                    JSONObject currUser = new JSONObject(userDetail);
                    JSONObject card = new JSONObject(cardDetail);
                    Toast.makeText(getApplicationContext(), currUser.getString("message"), Toast.LENGTH_SHORT).show();

                    if(currUser.getString("status").equals("OK") && card.getString("status").equals("OK")) {
                        currentBalance.setText("₹ " + currUser.getString("currentBalance"));
                        totalCashback.setText("₹ " + currUser.getString("totalCashback"));
                        userName.setText(currUser.getString("User Name"));
                        currentCashback.setText(currUser.getString("cashbackPercent") + " %");

                        cardsList = card.getJSONArray("cards");
                        for (int i = 0; i < cardsList.length(); i++) {
                            JSONObject c = cardsList.getJSONObject(i);
                            cards.add(c.getString("cardNumber") + " - " + c.getString("company"));
                        }
                        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, cards);
                        spCardList.setAdapter(adapter);

                        spCardList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                try {
                                    cardBalance.setText("₹ " + cardsList.getJSONObject(adapterView.getSelectedItemPosition()).getString("balance"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {
                                if(cards.isEmpty()) {
                                    cardBalance.setText("₹ 0.00");
                                }
                                else {
                                    try {
                                        cardBalance.setText("₹ " + cardsList.getJSONObject(0).getString("balance"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });

                        adapter.notifyDataSetChanged();
                    }
                    else {
                        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Error: Service Not Available, Try again later.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            progressDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            if(!isConnected()) {
                Toast.makeText(getApplicationContext(), "Error: No internet connection.", Toast.LENGTH_SHORT).show();
            }
            else {
                refreshData = new refreshData().execute(currUsername);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if( id == R.id.nav_accounts) {
            Intent i = new Intent(HomeActivity.this, AccountsActivity.class);
            startActivity(i);
        }
        if (id == R.id.nav_drive_fare) {
            Intent i = new Intent(HomeActivity.this, DriveFareActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_toll_fare) {
            Intent i = new Intent(HomeActivity.this, TollFareCalcActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_curr_fuel_price) {
            Intent i = new Intent(HomeActivity.this, FuelPriceActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_contact_support) {
            Intent i = new Intent(HomeActivity.this, SupportActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_contact_us) {
            Intent i = new Intent(HomeActivity.this, ContactUsActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_sign_out) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("isLogin", "false");
            editor.apply();
            Intent i = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if(exit) {
            finish();
        }
        else {
            Toast.makeText(this, "Press Back again to Exit.", Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3000);
        }
    }

}
