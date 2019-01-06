package com.skylark.tripadviser;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class RefillCardActivity extends AppCompatActivity {

    private Spinner spCards;
    private EditText etAmount;
    private EditText etCmnt;
    private Button btnSendRequest;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refill_card);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(RefillCardActivity.this, R.style.Theme_AppCompat_Light_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");

        spCards = findViewById(R.id.spCards);
        etAmount = findViewById(R.id.etAmount);
        etCmnt = findViewById(R.id.etCmnts);
        btnSendRequest = findViewById(R.id.btnSendRequest);

        SharedPreferences pref = getSharedPreferences("com.skylark.fueltrades", MODE_PRIVATE);
        final String username = pref.getString("currentUsername", "visnu");

        btnSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                if(!isConnected()) {
                    Toast.makeText(getApplicationContext(), "Error: No internet connection.", Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(etAmount.getText())) {
                    etAmount.setError("This is required.");
                }
                else if(etAmount.getText().length()>6) {
                    etAmount.setError("Amount should be less than ₹ 9,99,999/-");
                }
                else if(Integer.parseInt(etAmount.getText().toString())==0) {
                    etAmount.setError("Please enter valid amount.");
                }
                else {
                    new sendRequest().execute(username);
                }
            }
        });

        if(!isConnected()) {
            Toast.makeText(getApplicationContext(), "Error: No internet connection.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RefillCardActivity.this, HomeActivity.class);
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

    public class sendRequest extends AsyncTask <String, Void, Boolean> {

        JSONObject object;
        String msg;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Sending...");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... para) {
            String card = spCards.getSelectedItem().toString().split(" ")[0];
            String amount = etAmount.getText().toString();
            String cmnt = etCmnt.getText().toString();
            String parameter = "username=" + para[0] + "&cardNumber=" + card + "&amount=" + amount + "&comments=" + cmnt;
            String url = "https://fueltrades.prateekmathur.in/php/app/make-payment.php";

            String result = HTTPRequest.sendHTTPRequest(url, parameter);

            try {
                if (result != null) {
                    object = new JSONObject(result);
                    if (object.getString("status").equals("OK")) {
                        msg = object.getString("message");
                        return true;
                    }
                } else {
                    msg = "Error: Service Not Available, Try again later.";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }

            @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                new sendMail().execute();

                Intent intent = new Intent(RefillCardActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
        }
    }

    public class sendMail extends AsyncTask <Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            String host="smtp.gmail.com";
            final String user="trades.fuel@gmail.com";//change accordingly
            final String password="PrateekMathur";//change accordingly

            String to="bochiwal.visnu@gmail.com";//change accordingly

            //Get the session object
            Properties props = new Properties();
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.user", user); // User name
            props.put("mail.smtp.password", password); // password
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");

            Session session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(user,password);
                        }
                    });

            //Compose the message
            try {
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(user));
                message.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
                message.setSubject("Refill Card Request");
                message.setText("Someone has requested to add money to his card.");

                //send the message
                Transport.send(message);

            } catch (MessagingException e) {e.printStackTrace();}
            return null;
        }
    }


    public class LoadCards extends AsyncTask <String, Void, Boolean> {

        ArrayList<String> cards = new ArrayList<String>();
        JSONObject object;
        String msg;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                            cards.add(card.getString("cardNumber") + " - " + card.getString("company"));
                        }
                        msg = object.getString("message");
                        return true;
                    }
                }
                else {
                    msg = "Error: Service Not Available, Try again later.";
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
                Intent intent = new Intent(RefillCardActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
            else {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, cards);
                spCards.setAdapter(adapter);
                adapter.notifyDataSetChanged();
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
