package com.example.samir.testlocation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private TextView textView;
    private EditText hostName;
    private EditText userName;
    private Button buttonStop;
    private LocationManager locationManager;
    private LocationListener locationListener;
    RequestQueue queue;
    String url;
    public static final String StartTag = "StartTag";
    public static final String StopTag = "StopTag";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.btnStart);
        textView = (TextView) findViewById(R.id.textView);
        buttonStop = (Button) findViewById(R.id.buttonStop);
        buttonStop.setVisibility(View.INVISIBLE);
        hostName = (EditText) findViewById(R.id.hostAddress);
        userName = (EditText) findViewById(R.id.username);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        queue=SingletonRequest.getInstance(this.getApplicationContext()).
                getRequestQueue();
        onStartcall();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, 10);
                return;
            } else {
                configureButton();
            }
        }
    }

    protected void onStartcall(){
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                JSONObject json = new JSONObject();
                //url ="http://192.168.0.13:9000/locationupdate";
                //textView.append("\n" + location.getLatitude() + " " + location.getLongitude());
                try{
                    json.put("userName", userName.getText().toString().trim());
                    json.put("latitude", String.valueOf(location.getLatitude()));
                    json.put("longitude", String.valueOf(location.getLongitude()));
                    json.put("time", String.valueOf(System.currentTimeMillis()));                }
                catch (JSONException e){
                    e.printStackTrace();
                }

                JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url,json,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject  response) {
                                // Display the first 500 characters of the response string.
                                Log.i("AppMine", "onResponse ");
                                try {
                                    textView.setText("Inserted : \n"+ response.get("firstname")
                                            + ", " +response.get("longitude") +", " +response.get("latitude") );
                                } catch (JSONException e) {
                                    //Do nothing
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        textView.setText("Please wait for server to start up");
                    }
                });
                //{
//                    @Override
//                    public Map<String, String> getParams() {
//                        Map<String, String> params = new HashMap<>();
//                        params.put("userName", userName.getText().toString().trim());
//                        params.put("latitude", String.valueOf(location.getLatitude()));
//                        params.put("longitude", String.valueOf(location.getLongitude()));
//                        params.put("time", String.valueOf(System.currentTimeMillis()));
//                        Log.i("AppMine", params.toString());
//                        return params;
//                    }
                   // }
// Add the request to the RequestQueue.
               SingletonRequest.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
                //queue.add(stringRequest);
            };

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                configureButton();
                break;
            default:
                break;
        }
    }

    private void configureButton() {
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (queue != null) {
                    queue.cancelAll(StopTag);
                }
                //AppController.getInstance().getRequestQueue().getCache().rem‌​ove(key);
                String userName_text = userName.getText().toString().trim();
                String hostName_text = hostName.getText().toString().trim();
                if(userName_text.length() == 0 || hostName_text.length() == 0 ){
                    textView.setText("Please enter username");
                    return;
                }
                hostName_text = "http://" + hostName_text;
                textView.setText("");
                url = hostName_text + "/locationupdate";
                button.setVisibility(View.INVISIBLE);
                buttonStop.setVisibility(View.VISIBLE);
                //Log.i("AppMine", "onClick: ");
                Log.i("AppMine", "onClick: "+ url + "hostname:" + hostName_text);
                //onStartcall();
                //noinspection MissingPermission
                locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
            }
        });

    buttonStop.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View view){
            //noinspection MissingPermission
            // Request a string response from the provided URL.
            if (queue != null) {
                queue.cancelAll(StartTag);
            }
            //locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
            locationManager.removeUpdates(locationListener);
            Log.i("AppMine", "Stop onClick: ");
            button.setVisibility(View.VISIBLE);
            buttonStop.setVisibility(View.INVISIBLE);
            textView.setText("\n" + "Stop Clicked");
            String hostName_text = "http://" + hostName.getText().toString().trim();
            url = hostName_text + "/handleStop";
            JSONObject json = new JSONObject();
            try{
                json.put("userName", userName.getText().toString().trim());
                json.put("time", String.valueOf(System.currentTimeMillis()));
            }
            catch (JSONException e){
                e.printStackTrace();
            }
            //"http://192.168.0.13:9000/handleStop";
            JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url,json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Display the first 500 characters of the response string.
                            Log.i("AppMine", "onStopResponse ");
                            int max_length = response.length()<10?response.length():10;
                            try {
                                textView.setText("You covered : " + response.getString("distance") + "meters");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    textView.setText("Response is" + error.getMessage());
                }
            }) {
                @Override
                public Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("userName", userName.getText().toString().trim());
                    params.put("time", String.valueOf(System.currentTimeMillis()));
                    Log.i("AppMine", params.toString());
                    return params;
                }
            };
// Add the request to the RequestQueue.
            stringRequest.setTag(StopTag);
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(stringRequest);
        }
            //noinspection MissingPermission
            //locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);

    });
}

}

