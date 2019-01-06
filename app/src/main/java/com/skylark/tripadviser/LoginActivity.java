package com.skylark.tripadviser;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;

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
import java.util.Date;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private UserLoginTask mAuthTask = null;
    private EditText mEmailView;
    private EditText mPasswordView;
    private ProgressDialog mProgressDialog;
    private Boolean exit = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Sign in");

        mProgressDialog = new ProgressDialog(LoginActivity.this, R.style.Theme_AppCompat_Light_Dialog);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage("Authenticating...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);

        // Set up the login form.
        mEmailView = findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == 6 || id == EditorInfo.IME_NULL) {
                    if(isConnected()) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                        attemptLogin();
                        return true;
                    } else {
                        Toast.makeText(getApplicationContext(), "Error: No Internet Connection.", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }
        });

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isConnected()) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    attemptLogin();
                } else {
                    Toast.makeText(getApplicationContext(), "Error: No Internet Connection.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if(show) {
            mProgressDialog.show();
        }
        else {
            mProgressDialog.dismiss();
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;
        private JSONObject object;
        private String msg;

        UserLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            String urlParameters  = "username=" + mUsername + "&password=" + mPassword;
            byte[] postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
            int postDataLength = postData.length;
            String request = "http://fueltrades.prateekmathur.in/php/app/login.php";
            try {
                URL url = new URL(request);
                HttpURLConnection conn= (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setInstanceFollowRedirects(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("charset", "utf-8");
                conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
                conn.setUseCaches(false);

                DataOutputStream writer = new DataOutputStream(conn.getOutputStream());
                writer.write(postData);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String data;
                StringBuilder response = new StringBuilder();

                while((data = reader.readLine()) != null) {
                    response.append(data);
                }

                String result = response.toString();

                object = new JSONObject(result);

                msg = object.getString("message");

                if(object.getBoolean("success")) {
                    return true;
                }
            } catch (FileNotFoundException e) {
                msg = "Error: Service Not Available. Try again later.";
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // TODO: register the new account here.
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                SharedPreferences pref = getSharedPreferences("com.skylark.fueltrades", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("currentUsername", mUsername);
                editor.putString("isLogin", "true");
                editor.putString("lastLoginTime", new Date().toString());
                editor.commit();
                finish();
                Intent i = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(i);
            }

            Toast.makeText(getApplicationContext(), msg , Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    @Override
    public void onBackPressed() {
        if(exit) {
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



