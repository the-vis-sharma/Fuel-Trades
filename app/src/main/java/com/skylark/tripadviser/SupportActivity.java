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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SupportActivity extends AppCompatActivity {

    private EditText subject;
    private EditText msg;
    private String userName;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        progressDialog = new ProgressDialog(SupportActivity.this, R.style.Theme_AppCompat_Light_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        subject = findViewById(R.id.etSubject);
        msg = findViewById(R.id.etMsg);

        SharedPreferences pref = getSharedPreferences("com.skylark.fueltrades", MODE_PRIVATE);
        userName = pref.getString("currentUsername", "Unknown");

    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
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

    public class sendRequest extends AsyncTask <String, Void, Boolean> {

        JSONObject object;
        String err;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Sending...");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... para) {
            String sub = subject.getText().toString();
            String message = msg.getText().toString();
            String parameter = "username=" + para[0] + "&subject=" + sub + "&message=" + message;
            String url = "https://fueltrades.prateekmathur.in/php/app/sendSupportRequest.php";

            String result = HTTPRequest.sendHTTPRequest(url, parameter);

            try {
                if (result != null) {
                    object = new JSONObject(result);
                    if (object.getString("status").equals("OK")) {
                        err = object.getString("message");
                        return true;
                    }
                } else {
                    err = "Error: Service Not Available, Try again later.";
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
            Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT).show();

            new sendMail().execute();

            Intent intent = new Intent(SupportActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public class sendMail extends AsyncTask<Void, Void, Void> {

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
                message.setSubject("Support: " + subject.getText().toString());
                message.setText(msg.getText().toString() + "\nFrom - \n" + userName + "\n\nSent Via Fuel Trades Android App");

                //send the message
                Transport.send(message);

            } catch (MessagingException e) {e.printStackTrace();}
            return null;
        }
    }

    public void send(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        if(!isConnected()) {
            Toast.makeText(getApplicationContext(), "Error: No internet connection.", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(subject.getText())) {
            subject.setError("This is required.");
        }
        else if(TextUtils.isEmpty(msg.getText())) {
            msg.setError("This is required.");
        }
        else {
            new sendRequest().execute(userName);
        }
    }
}
