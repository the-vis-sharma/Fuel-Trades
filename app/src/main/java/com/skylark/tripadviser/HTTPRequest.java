package com.skylark.tripadviser;

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

/**
 * Created by bochi on 18-12-2017.
 */

public class HTTPRequest {

    public static String sendHTTPRequest(String request, String parameters) {
        byte[] postData = parameters.getBytes( StandardCharsets.UTF_8 );
        int postDataLength = postData.length;
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

            return response.toString();
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
