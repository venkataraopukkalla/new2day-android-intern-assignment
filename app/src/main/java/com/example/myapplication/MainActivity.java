package com.example.myapplication;

import static com.android.volley.Request.Method.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.AndroidException;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.Model.WeatherForecastDetails;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private final String API_KEY = "https://api.weatherapi.com/v1/forecast.json?key=5e454612d15c42e0af9141437232410";
    private LocationManager locationManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;


    private TextView setLocationTxt;
    private TextView setTempTxt;

    private TextInputEditText searchEdt;
    private ImageView searchLogo;
    private ImageView currentWeatherLogo;

    private RecyclerView recyclerView;

    private ArrayList<WeatherForecastDetails> list = new ArrayList<>();
    ForecastAdapter forecastAdapter;


    @SuppressLint("ServiceCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setLocationTxt = findViewById(R.id.setloction_txt);
        setTempTxt = findViewById(R.id.temp_txt);
        searchEdt = findViewById(R.id.search_edittxt);
        searchLogo = findViewById(R.id.search_logo);
        currentWeatherLogo = findViewById(R.id.currentSatusWeather_img);
        recyclerView = findViewById(R.id.forecastRecycleview);

        //initialize location manger
        locationManager = (LocationManager) getSystemService(Context.LOCALE_SERVICE);


        // Check if location manager is not null and location services are enabled
        if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Location services are enabled, check and request permissions
            if (checkLocationPermission()) {
                // Request location updates
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            } else {
                // Request permissions if not granted
                requestLocationPermission();
            }
        } else {
            // Show a dialog or log an error if locationManager is null or GPS provider is not enabled
            if (locationManager == null) {
                Log.e("YourWeatherActivity", "Location manager is null");
            } else {
                Log.e("YourWeatherActivity", "GPS provider is not enabled");
                // Show a dialog or prompt the user to enable location services
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }



        searchLogo.setOnClickListener(e -> {
            if (!(searchEdt.getText() + "").equals(""))
                getData(searchEdt.getText() + "".trim());
            else
                Toast.makeText(this, "Please enter location", Toast.LENGTH_SHORT).show();

        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        forecastAdapter = new ForecastAdapter(this, list);

        recyclerView.setAdapter(forecastAdapter);


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // Check if the permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, request location updates
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                }
            } else {
                // Permission denied, show a dialog explaining why the permission is needed
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION) ||
                        shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    // Explanation for the user
                    showPermissionExplanationDialog();
                } else {
                    // User denied permission and checked "Never ask again"
                    showPermissionSettingsDialog();
                }
            }
        }
    }
    private void showPermissionSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Location Permission Required");
        builder.setMessage("This app requires location permission to provide accurate weather information. Please grant the location permission in the settings.");

        builder.setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Open the app settings
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Handle the user's decision to not grant the permission
                Log.e("YourWeatherActivity", "Location permission denied after explanation");
            }
        });

        builder.show();
    }

    private void showPermissionExplanationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Location Permission Required");
        builder.setMessage("This app requires location permission to provide accurate weather information. Please grant the location permission in the settings.");

        builder.setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Open the app settings
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Handle the user's decision to not grant the permission
                Log.e("YourWeatherActivity", "Location permission denied after explanation");
            }
        });

        builder.show();
    }


    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }


    private void getData(String location) {
        list.clear();
        String url = API_KEY + "&q=" + location + "&days=2&aqi=no&alerts=no";

        //Instantiate the RequestQueue.
        RequestQueue requestQueue = Volley.newRequestQueue(this);


        //Json Oject request

        JsonObjectRequest request = new JsonObjectRequest(GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject responseJSONObject = response.getJSONObject("location");
                    setLocationTxt.setText("⟟" + responseJSONObject.getString("name"));
                    setTempTxt.setText(response.getJSONObject("current").getDouble("temp_c") + "°C");

                    // set weather current status logo
                    String iconUrl = response.getJSONObject("current").getJSONObject("condition").getString("icon");

                    String fullIconUrl = "https:" + iconUrl;
                    //Log.i("VIKAS",fullIconUrl);

                    //load image
                    Picasso.get().load(fullIconUrl).into(currentWeatherLogo);


                    // now load forecast details  .all details add into arraylist;
                    JSONObject forecast = response.getJSONObject("forecast");
                    JSONArray forecastJSONArray = forecast.getJSONArray("forecastday");

                    // maxWindSpeed
                    String maxWindSpeed = forecastJSONArray.getJSONObject(0).getJSONObject("day")
                            .getString("maxwind_kph");

                    JSONArray hour = forecastJSONArray.getJSONObject(0).getJSONArray("hour");

                    for (int i = 0; i < hour.length(); i++) {

                        String tempC = hour.getJSONObject(i).getString("temp_c");

                        // to check pm or am
                        String isDay = hour.getJSONObject(i).getString("is_day");

                        //time
                        String time = hour.getJSONObject(i).getString("time");

                        String timeFormat = simpleTimeformate(time);


                        String windSpeed = hour.getJSONObject(i).getString("wind_kph");

                        //percentage
                        String rainPercenatge = hour.getJSONObject(i).getString("chance_of_rain");

                        //   Log.i("SMILE", rainPercenatge+"");

                        //icon
                        String icon = hour.getJSONObject(i).getJSONObject("condition").getString("icon");
                        String fulliconAdress = "https:" + icon;

                        // now all details add into array....
                        list.add(new WeatherForecastDetails(timeFormat, tempC, windSpeed, rainPercenatge, fulliconAdress));


                    }


                    // run background
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            forecastAdapter.notifyDataSetChanged();
                        }
                    });


                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                // handing error
                //  Toast.makeText(MainActivity.this,"PLease Enter Proper Location",Toast.LENGTH_LONG).show();

            }
        });

        requestQueue.add(request);


    }

    private String simpleTimeformate(String time) {
        String format;
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat outputFormate = new SimpleDateFormat("yyyy-MM-dd hh:mma");

        try {
            Date date = input.parse(time);
            format = outputFormate.format(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return format;


    }

    private void permissonCheck() {

        // Check for permission
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Request location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } else {
            // Handle permissions (request them if not granted)
            Toast.makeText(getApplicationContext(), "Please allow permission .To find current location", Toast.LENGTH_SHORT).show();
            // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, YOUR_REQUEST_CODE);
        }

    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Handle location updates
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        // Use reverse geocoding to get the city name
        String cityName = getCityName(latitude, longitude);

        Log.d("LOCATION", cityName);


    }


    private String getCityName(double latitude, double longitude) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        String cityName = "";

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && addresses.size() > 0) {
                cityName = addresses.get(0).getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cityName;
    }
}