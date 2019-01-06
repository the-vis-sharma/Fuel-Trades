package com.skylark.tripadviser;

import android.content.Intent;
import android.icu.text.LocaleDisplayNames;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.channels.FileLock;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DriveFareDetails extends AppCompatActivity {

    private String startCity;
    private String destinationCity;
    private String viaCity;
    private String fuelType;
    private String driveType;
    private float avg;
    private RelativeLayout driveFareLayout;
    private RelativeLayout processbarLayout;
    private TextView routeTitle;
    private TextView totalDistance;
    private TextView fuelRequired;
    private TextView fuelCost;
    private TextView noOfTolls;
    private TextView TollCost;
    private TextView totalDriveCost;

    public static JSONArray steps;
    public static JSONArray tollValueJSON;
    public static String vTypeInDb;
    String destination;
    String source;
    HashMap<Integer, Long> step_distance = new HashMap();
    HashMap<Integer, Double> step_end_lat = new HashMap();
    HashMap<Integer, Double> step_end_lng = new HashMap();
    HashMap<Integer, Double> step_start_lat = new HashMap();
    HashMap<Integer, Double> step_start_lng = new HashMap();
    Integer tollValueTotal = null;
    HashMap<Integer, Boolean> toll_Presence = new HashMap();

    public String sendHttpGetRequest(String params) {
        Log.d("Pos", "sendHttp");
        IOException e;
        String mainURL = params;
        StringBuffer buffer = new StringBuffer();
        try {
            URL myURL = new URL(mainURL);
            URL url;
            try {
                URLConnection myURLConnection = myURL.openConnection();
                myURLConnection.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
                while (true) {
                    try {
                        String response = reader.readLine();
                        if (response == null) {
                            break;
                        }
                        buffer.append(response);
                    } catch (IOException e2) {
                        e = e2;
                        url = myURL;
                    }
                }
                reader.close();
                url = myURL;
            } catch (IOException e3) {
                e = e3;
                url = myURL;
                e.printStackTrace();
                return buffer.toString();
            }
        } catch (IOException e4) {
            e = e4;
            e.printStackTrace();
            return buffer.toString();
        }
        return buffer.toString();
    }

    public Double calculateDistance(Double latA, Double lngA, Double latB, Double lngB) {
        return Double.valueOf(Math.acos((Math.sin(latA.doubleValue()) * Math.sin(latB.doubleValue())) + ((Math.cos(latA.doubleValue()) * Math.cos(latB.doubleValue())) * Math.cos(lngA.doubleValue() - lngB.doubleValue()))) * 6371.0d);
    }

    public class tollCalculate extends AsyncTask<Void, Void, Void> {

        String origin;
        String des;
        String driveType;
        String distance;
        String fuelPrice;
        String totalfuelRequired;
        String totalFuelCost;
        List<String> tollNames;
        List<Double> tollLat;
        List<Double> tollLng;

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d("Started", "Yes");
            int i;
            tollValueTotal = Integer.valueOf(0);
            step_start_lat.clear();
            step_start_lng.clear();
            step_end_lat.clear();
            step_end_lng.clear();
            step_distance.clear();
            toll_Presence.clear();
            Intent intent = getIntent();
            origin = intent.getStringExtra("startCity");
            des = intent.getStringExtra("destinationCity");
            driveType = intent.getStringExtra("driveType");
            ArrayList arrayList = new ArrayList(Arrays.asList(intent.getStringExtra("viaCity").split(",")));
            String vTypeSpinner = intent.getStringExtra("vehicleType");
            StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
            try {
                stringBuilder.append("origin=" + URLEncoder.encode(origin, "UTF-8"));
                stringBuilder.append("&destination=" + URLEncoder.encode(des, "UTF-8"));
                stringBuilder.append("&waypoints=");
                for (i = 0; i < arrayList.size(); i++) {
                    if (i > 0) {
                        stringBuilder.append("|");
                    }
                    stringBuilder.append("via:" + URLEncoder.encode(arrayList.get(i).toString(), "UTF-8"));
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Source / Destination could not be interpreted. Please try again", Toast.LENGTH_SHORT).show();
            }
            stringBuilder.append("&mode=driving");
            stringBuilder.append("&alternatives=false");
            stringBuilder.append("&units=metric");
            stringBuilder.append("&region=.in");
            stringBuilder.append("&key=AIzaSyCgmoIRf3i_bF1HyNpRny6DSor0HM02yxA");
            String mainURL = stringBuilder.toString();
            String response = null;
            Log.d("Pos", "tollCal");
            response = sendHttpGetRequest(mainURL);
            Log.d("Pos", "tollCal");

            Integer stepCount = Integer.valueOf(0);
            String sourceInterpreted = null;
            String destinationInterpreted = null;
            distance = null;
            String APIstatus = null;
            try {
                JSONObject jSONObject = new JSONObject(response);
                sourceInterpreted = jSONObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).get("start_address").toString();
                destinationInterpreted = jSONObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).get("end_address").toString();
                distance = jSONObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").get("text").toString();
                steps = jSONObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
                APIstatus = jSONObject.get("status").toString();
                for (i = 0; i < steps.length(); i++) {
                    JSONObject JSONpart = steps.getJSONObject(i);
                    JSONObject startLoc = JSONpart.getJSONObject("start_location");
                    JSONObject endLoc = JSONpart.getJSONObject("end_location");
                    step_start_lat.put(Integer.valueOf(i), Double.valueOf(Math.toRadians(Double.parseDouble(startLoc.getString("lat")))));
                    step_start_lng.put(Integer.valueOf(i), Double.valueOf(Math.toRadians(Double.parseDouble(startLoc.getString("lng")))));
                    step_end_lat.put(Integer.valueOf(i), Double.valueOf(Math.toRadians(Double.parseDouble(endLoc.getString("lat")))));
                    step_end_lng.put(Integer.valueOf(i), Double.valueOf(Math.toRadians(Double.parseDouble(endLoc.getString("lng")))));
                    step_distance.put(Integer.valueOf(i), Long.valueOf(Long.parseLong(JSONpart.getJSONObject("distance").getString("value"))));
                }
            } catch (JSONException e4) {
                e4.printStackTrace();
            }
            String mLabKey = "f9Q9YyWisoYWUmiYhfeS3KoZ9e7PQAl6";
            String mLabDbName = "toll_calculator_1";
            String mLabTollValueCollName = "toll_value";
            String mLabResponse = null;
            String mLabBaseURL = "https://api.mlab.com/api/1/databases/";
            String mLabCallURL = mLabBaseURL + mLabDbName + "/collections/" + "toll_lat_lng" + "?apiKey=" + mLabKey;
            mLabResponse = sendHttpGetRequest(mLabCallURL);

            try {
                JSONArray jSONArray = new JSONArray(mLabResponse);
                for (Integer tollArrayPos = Integer.valueOf(0); tollArrayPos.intValue() < jSONArray.length(); tollArrayPos = Integer.valueOf(tollArrayPos.intValue() + 1)) {
                    for (Integer stepID : step_start_lng.keySet()) {
                        if (((Long) step_distance.get(stepID)).longValue() > 1000) {
                            Double tollLng;
                            Double tollLat = jSONArray.getJSONObject(tollArrayPos.intValue()).isNull("lat_radians") ? null : Double.valueOf(jSONArray.getJSONObject(tollArrayPos.intValue()).getDouble("lat_radians"));
                            if (jSONArray.getJSONObject(tollArrayPos.intValue()).isNull("lng_radians")) {
                                tollLng = null;
                            } else {
                                tollLng = Double.valueOf(jSONArray.getJSONObject(tollArrayPos.intValue()).getDouble("lng_radians"));
                            }
                            if ((calculateDistance(tollLat, tollLng, (Double) step_end_lat.get(stepID), (Double) step_end_lng.get(stepID)).doubleValue() + calculateDistance((Double) step_start_lat.get(stepID), (Double) step_start_lng.get(stepID), tollLat, tollLng).doubleValue()) - 1.0d < ((double) (((Long) step_distance.get(stepID)).longValue() / 1000))) {
                                toll_Presence.put((Integer) jSONArray.getJSONObject(tollArrayPos.intValue()).get("toll_id"), Boolean.valueOf(true));
                                break;
                            }
                        }
                    }
                }
            } catch (JSONException e42) {
                e42.printStackTrace();
            }

            vTypeInDb = intent.getStringExtra("vehicleType");

            mLabCallURL = mLabBaseURL + mLabDbName + "/collections/" + mLabTollValueCollName + "?apiKey=" + mLabKey;
            try {
                tollValueJSON = new JSONArray(sendHttpGetRequest(mLabCallURL));
                for (Integer stepID2 : toll_Presence.keySet()) {
                    for (Integer tollValueIndex = Integer.valueOf(0); tollValueIndex.intValue() < tollValueJSON.length(); tollValueIndex = Integer.valueOf(tollValueIndex.intValue() + 1)) {
                        if (Integer.valueOf(tollValueJSON.getJSONObject(tollValueIndex.intValue()).getInt("toll_id")).equals(stepID2)) {
                            tollValueTotal = Integer.valueOf(tollValueTotal.intValue() + tollValueJSON.getJSONObject(tollValueIndex.intValue()).getInt(vTypeInDb));
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String viaText = "   ";
            for (i = 0; i < arrayList.size(); i++) {
                viaText = viaText + System.getProperty("line.separator") + "-->" + arrayList.get(i).toString();
            }

            String url = "https://fuelpriceindia.herokuapp.com/price?city="+ startCity +"&fuel_type="+fuelType;
            String result = sendHttpGetRequest(url);
            try {
                JSONObject obj = new JSONObject(result);
                if(fuelType.equals("Petrol")) {
                    fuelPrice = String.valueOf(obj.getDouble("petrol"));
                }
                else {
                    fuelPrice = String.valueOf(obj.getDouble("diesel"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d("source", sourceInterpreted + viaText + System.getProperty("line.separator") + "-->" + destinationInterpreted);
            Log.d("toll Value", "Rs " + String.valueOf(tollValueTotal) + "/-");
            Log.d("no of Tolls", String.valueOf(toll_Presence.size()));
//        Log.d("total Distance", distance);
            Log.d("Finished", "Yes");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            NumberFormat format = new DecimalFormat("#0.00");

            if(distance == null) {
                distance = new String("0 KM");
            }
            Float dis = Float.parseFloat(distance.substring(0, distance.length()-3).replace(",", ""));

            totalfuelRequired = String.valueOf(dis/avg);
            totalFuelCost = String.valueOf(Float.parseFloat(totalfuelRequired) * Float.parseFloat(fuelPrice));

            if(driveType.equals("One Way")) {
                routeTitle.setText(origin + " - " + des);
                noOfTolls.setText(String.valueOf(toll_Presence.size()));
                totalDistance.setText(distance);
                fuelRequired.setText(format.format(Float.parseFloat(totalfuelRequired))+ " L");
                fuelCost.setText("₹ "+format.format(Float.parseFloat(totalFuelCost)));
                TollCost.setText("₹ " + String.valueOf(tollValueTotal));
                totalDriveCost.setText("₹ "+format.format(Float.parseFloat(totalFuelCost)+tollValueTotal));
            }
            else {
                routeTitle.setText(origin + " - " + des + " - " + origin);
                noOfTolls.setText(String.valueOf(toll_Presence.size()*2));
                TollCost.setText("₹ " + String.valueOf(tollValueTotal*2));
                totalDistance.setText(String.valueOf(dis*2));
                fuelRequired.setText(format.format(Float.parseFloat(totalfuelRequired)*2)+" L");
                fuelCost.setText("₹ "+format.format(Float.parseFloat(totalFuelCost)*2));
                totalDriveCost.setText("₹ "+format.format((Float.parseFloat(totalFuelCost)+tollValueTotal)*2));
            }

            processbarLayout.setVisibility(View.GONE);
            driveFareLayout.setVisibility(View.VISIBLE);
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive_fare_details);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Drive Fare Details");

        driveFareLayout = findViewById(R.id.driveFareLayout);
        processbarLayout = findViewById(R.id.processBarLayout3);
        routeTitle = findViewById(R.id.routeTitle1);
        totalDistance = findViewById(R.id.tvTotalDistance);
        fuelRequired = findViewById(R.id.tvFuelRequired);
        fuelCost = findViewById(R.id.tvFuelCost);
        noOfTolls = findViewById(R.id.tvNoOfTolls);
        TollCost = findViewById(R.id.tvTollCost);
        totalDriveCost = findViewById(R.id.tvTotalDriveCost);

        Intent intent = getIntent();
        startCity = intent.getStringExtra("startCity");
        destinationCity = intent.getStringExtra("destinationCity");
        viaCity = intent.getStringExtra("viaCity");
        fuelType = intent.getStringExtra("fuelType");
        driveType = intent.getStringExtra("driveType");
        avg = intent.getFloatExtra("avg", 0.0f);
        new tollCalculate().execute();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
