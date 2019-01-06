package com.skylark.tripadviser;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class RequestMoneyActivity extends AppCompatActivity {

    private Spinner spAccounts;
    private EditText etAmount;
    private EditText etReceiptNo;
    private EditText etCmnts;
    private Button btnSendRequest;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_money);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        progressDialog = new ProgressDialog(RequestMoneyActivity.this, R.style.Theme_AppCompat_Light_Dialog);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        spAccounts = findViewById(R.id.spAccounts);
        etAmount = findViewById(R.id.etAmount);
        etReceiptNo = findViewById(R.id.etReceiptNo);
        etCmnts = findViewById(R.id.etCmnts);
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
                    etAmount.setError("Please enter a valid amount.");
                }
                else if(TextUtils.isEmpty(etReceiptNo.getText())) {
                    etReceiptNo.setError("This is required.");
                }
                else if(etReceiptNo.length() > 20) {
                    etReceiptNo.setError("Please enter a valid receipt number.");
                }
                else {
                    new sendRequest().execute(username);
                }
            }
        });

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
            String account = spAccounts.getSelectedItem().toString().split(" ")[0];
            String recepitNo = etReceiptNo.getText().toString();
            String amount = etAmount.getText().toString();
            String cmnt = etCmnts.getText().toString();
            String parameter = "username=" + para[0] + "&account=" + account + "&receiptNumber=" + recepitNo + "&amount=" + amount + "&comments=" + cmnt;
            String url = "https://fueltrades.prateekmathur.in/php/app/make-request.php";

            String result = HTTPRequest.sendHTTPRequest(url, parameter);

            try {
                if(result != null) {
                    object = new JSONObject(result);
                    if (object.getString("status").equals("OK")) {
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
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

            new sendMail().execute();

            Intent intent = new Intent(RequestMoneyActivity.this, HomeActivity.class);
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
                message.setSubject("Add Money Request");
                message.setText("Someone has requested to add money.");

                //send the message
                Transport.send(message);

            } catch (MessagingException e) {e.printStackTrace();}
            return null;
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
