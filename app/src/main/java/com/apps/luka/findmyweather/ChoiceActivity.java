package com.apps.luka.findmyweather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.luka.findmyweather.Data.WeatherResponse;
import com.apps.luka.findmyweather.Utils.GeometryHelper;
import com.apps.luka.findmyweather.Utils.WeatherAPIHelper;
import com.apps.luka.findmyweather.Utils.WeatherSortHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChoiceActivity extends AppCompatActivity {
    private static final int ACCESS_LOCATION_ALLOWED = 1;
    private static String WEATHER_DATE_TIME;

    private LatLng chosenLocation;
    private int[] todayDateNums;
    private int[] minimumDateNums;
    private int[] maxDateNums;
    private int[] chosenDateNums;
    private double radius = 100;
    private int transparentRedColor = Color.argb(64, 255, 0, 0);
    private List<String> resultJSONs = new ArrayList<String>();

    private List<WeatherResponse> resultList;

    private Toolbar map_toolbar;

    private Button choice_settings_b;
    private SupportMapFragment choice_map_f;
    private EditText radius_et;
    private TextView date_dp;
    private Button go_b;
    private Button my_location_b;

    private GoogleMap choice_map_gm;

    private LocationManager locationManager;
    private MyLocationReadyListener myLocationReadyListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);

        if(getIntent().hasExtra("chosenLocation")){
            chosenLocation = getIntent().getParcelableExtra("chosenLocation");
        }
        if(getIntent().hasExtra("radius")){
            radius = getIntent().getDoubleExtra("radius", 100);
        }

        map_toolbar = (Toolbar) findViewById(R.id.map_toolbar);
        setSupportActionBar(map_toolbar);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;

        choice_settings_b = (Button) findViewById(R.id.choice_settings_b);
        choice_map_f = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.choice_map_f);
        radius_et = (EditText) findViewById(R.id.radius_et);
        date_dp = (TextView) findViewById(R.id.date_dp);
        go_b = (Button) findViewById(R.id.go_b);
        my_location_b = (Button) findViewById(R.id.my_location_b);

        choice_settings_b.setOnClickListener(new SettingsClickListener());
        radius_et.addTextChangedListener(new RadiusEtChangeListener());
        date_dp.setOnClickListener(new DateDpClickListener());
        go_b.setOnClickListener(new GetWeatherClickListener());
        my_location_b.setOnClickListener(new MyLocationClickListener());

        radius_et.setText(String.valueOf(radius));
        handleDates();

        choice_map_f.getView().getLayoutParams().height = (int)(height * 0.5);
        choice_map_f.getMapAsync(new ChoiceMapReadyCallback());
    }

    private void handleDates(){
        Calendar cal = Calendar.getInstance();
        todayDateNums = new int[]{cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)};
        boolean addedOneDay = false;
        if(cal.get(cal.HOUR_OF_DAY) >= 21){
            addedOneDay = true;
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        minimumDateNums = new int[]{cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)};
        chosenDateNums = new int[]{cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)};

        if(!addedOneDay){   //today and minimum are same
            int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
            if(hourOfDay >= 15){
                int nextHourOfDay = 3 * (((int)(hourOfDay / 3)) + 1);   //because weather data is available for every 3 hours
                WEATHER_DATE_TIME = String.format("%04d", chosenDateNums[0]) + "-" + String.format("%02d", (chosenDateNums[1]+1)) + "-"
                        + String.format("%02d", chosenDateNums[2]) + " " + String.format("%02d", nextHourOfDay) + ":00:00";
            }
            else{
                WEATHER_DATE_TIME = String.format("%04d", chosenDateNums[0]) + "-" + String.format("%02d", (chosenDateNums[1]+1)) + "-"
                        + String.format("%02d", chosenDateNums[2]) + " 15:00:00";
            }
        }
        else{
            WEATHER_DATE_TIME = String.format("%04d", chosenDateNums[0]) + "-" + String.format("%02d", (chosenDateNums[1]+1)) + "-"
                    + String.format("%02d", chosenDateNums[2]) + " 15:00:00";
        }
        cal.add(Calendar.DAY_OF_MONTH, (addedOneDay) ? 3 : 4);
        maxDateNums = new int[]{cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)};

        date_dp.setText(getChosenDateText());
    }

    private String getMyLocationAvailability() {
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER;
        }
        else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;
        }
        else if(locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)){
            return LocationManager.PASSIVE_PROVIDER;
        }
        else {
            return null;
        }
    }

    private String getChosenDateText(){
        return chosenDateNums[2] + ". " + (chosenDateNums[1] + 1) + ". " + chosenDateNums[0];
    }

    private boolean isInternetAccessAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void redrawMap(){
        choice_map_gm.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(chosenLocation);
        markerOptions.title(getString(R.string.chosen_location));
        choice_map_gm.addMarker(markerOptions);
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(chosenLocation);
        circleOptions.radius(radius * 1000 * 1.1);
        circleOptions.strokeColor(transparentRedColor);
        circleOptions.fillColor(transparentRedColor);
        choice_map_gm.addCircle(circleOptions);
        CameraPosition.Builder cameraPositionBuilder = new CameraPosition.Builder();
        cameraPositionBuilder.target(chosenLocation);
        CameraPosition cameraPosition = cameraPositionBuilder.build();
        choice_map_gm.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        LatLngBounds.Builder latLangBuilder = new LatLngBounds.Builder();
        latLangBuilder.include(SphericalUtil.computeOffset(chosenLocation, radius * 1000 * Math.sqrt(2), 0));
        latLangBuilder.include(SphericalUtil.computeOffset(chosenLocation, radius * 1000 * Math.sqrt(2), 90));
        latLangBuilder.include(SphericalUtil.computeOffset(chosenLocation, radius * 1000 * Math.sqrt(2), 180));
        latLangBuilder.include(SphericalUtil.computeOffset(chosenLocation, radius * 1000 * Math.sqrt(2), 270));
        choice_map_gm.animateCamera(CameraUpdateFactory.newLatLngBounds(latLangBuilder.build(), 10));
    }

    private void getMyLocation() {
        if (ActivityCompat.checkSelfPermission(ChoiceActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(ChoiceActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ChoiceActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_LOCATION_ALLOWED);
        }
        else{
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            String locationProvider = getMyLocationAvailability();
            if (getMyLocationAvailability() == null) {
                terminateLocationService();
                AlertDialog.Builder builder = new AlertDialog.Builder(ChoiceActivity.this);
                builder.setTitle(getString(R.string.error));
                builder.setMessage(getString(R.string.location_providers_unavailable));
                builder.setPositiveButton(getString(R.string.continue_b), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                });
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.show();
                return;
            }
            startLocationService(locationProvider);
            Toast.makeText(
                    ChoiceActivity.this,
                    getString(R.string.acquiring_location),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void updateResults(String result){
        resultJSONs.add(result);
    }

    public void resetResults(){
        resultJSONs.clear();
    }

    public void goToResultsActivity(LatLng chosenLocation, double radius){
        try{
            deserializeResultJSONs(chosenLocation, radius);
        }
        catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ChoiceActivity.this);
            builder.setTitle(getString(R.string.error))
                    .setMessage(getString(R.string.unexpected_exception_deserializing))
                    .setPositiveButton(getString(R.string.continue_b), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return;
        }
        if(resultList.size() < WeatherSortHelper.DISPLAYED_RESULTS_AMOUNT){
            AlertDialog.Builder builder = new AlertDialog.Builder(ChoiceActivity.this);
            builder.setTitle(getString(R.string.error))
                    .setMessage(getString(R.string.not_enough_weather_data))
                    .setPositiveButton(R.string.continue_b, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return;
        }
        if(resultList.size() < (GeometryHelper.POINT_COUNT * 0.67)){
            Toast.makeText(
                    ChoiceActivity.this,
                    getString(R.string.some_data_unavailable),
                    Toast.LENGTH_SHORT).show();
        }

        terminateLocationService();
        Intent intent = new Intent(ChoiceActivity.this, ResultsActivity.class);
        intent.putExtra("chosenLocation", chosenLocation);
        intent.putExtra("radius", radius);
        intent.putExtra("WEATHER_DATE_TIME", WEATHER_DATE_TIME);
        intent.putExtra("resultList", (Serializable) resultList);
        startActivity(intent);
        ChoiceActivity.this.finish();
    }

    private void deserializeResultJSONs(LatLng chosenLocation, double radius) throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        resultList = new ArrayList<WeatherResponse>();
        for(int i = 0; i < resultJSONs.size(); i++){
            String result = resultJSONs.get(i);
            WeatherResponse weatherResponse = objectMapper.readValue(result, WeatherResponse.class);
            if(checkWeatherResponse(chosenLocation, radius, weatherResponse, resultList)){
                resultList.add(weatherResponse);
            }
        }
    }

    private boolean checkWeatherResponse(LatLng chosenLocation, double radius, WeatherResponse weatherResponse, List<WeatherResponse> weatherResponseList){
        double tooClose = radius * 200; //a tenth of the diameter in kilometers(diameter * 1000)
        if(weatherResponse.getCity().getCoord().getLon() != null && weatherResponse.getCity().getCoord().getLat() != null){
            LatLng tempLatLng = new LatLng(weatherResponse.getCity().getCoord().getLat(), weatherResponse.getCity().getCoord().getLon());
            return SphericalUtil.computeDistanceBetween(chosenLocation, tempLatLng) <= (radius * 1000) &&
                    (GeometryHelper.getClosestDistanceFromWeatherResponses(tempLatLng, weatherResponseList) >= tooClose);
        }
        else{
            return false;
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationService(String locationProvider){
        if(myLocationReadyListener != null){
            locationManager.removeUpdates(myLocationReadyListener);
        }
        myLocationReadyListener = new MyLocationReadyListener();
        locationManager.requestLocationUpdates(locationProvider, 1000, 1, myLocationReadyListener);
    }

    private void terminateLocationService(){
        if(locationManager != null){
            if(myLocationReadyListener != null){
                locationManager.removeUpdates(myLocationReadyListener);
            }
            locationManager = null;
        }
    }



    //LISTENERS

    private class SettingsClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(ChoiceActivity.this, SettingsActivity.class);
            intent.putExtra("chosenLocation", chosenLocation);
            intent.putExtra("radius", radius);
            intent.putExtra("origin", "choice");
            startActivity(intent);
            ChoiceActivity.this.finish();
        }
    }

    private class ChoiceMapClickListener implements GoogleMap.OnMapClickListener {
        @Override
        public void onMapClick(LatLng latLng) {
            chosenLocation = latLng;
            terminateLocationService();
            redrawMap();
        }
    }

    private class RadiusEtChangeListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            try{
                radius = Double.parseDouble(charSequence.toString());
            }
            catch(Exception e){
                Toast.makeText(
                        ChoiceActivity.this,
                        getString(R.string.radius_number),
                        Toast.LENGTH_SHORT).show();
                radius = 20;
                return;
            }
            if(choice_map_gm != null && chosenLocation != null){
                redrawMap();
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {}
    }

    private class DateDpClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(ChoiceActivity.this, new DateSetListener(), chosenDateNums[0], chosenDateNums[1], chosenDateNums[2]);
            Calendar cal = Calendar.getInstance();
            cal.set(minimumDateNums[0], minimumDateNums[1], minimumDateNums[2]);
            datePickerDialog.getDatePicker().setMinDate(cal.getTimeInMillis());
            cal.set(maxDateNums[0], maxDateNums[1], maxDateNums[2]);
            datePickerDialog.getDatePicker().setMaxDate(cal.getTimeInMillis());
            datePickerDialog.show();
        }
    }

    private class DateSetListener implements DatePickerDialog.OnDateSetListener {
        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            chosenDateNums = new int[]{i, i1, i2};
            Calendar cal = Calendar.getInstance();
            if(chosenDateNums[0] == todayDateNums[0] && chosenDateNums[1] == todayDateNums[1] && chosenDateNums[2] == todayDateNums[2]){
                int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
                if(hourOfDay >= 15){
                    int nextHourOfDay = 3 * (((int)(hourOfDay / 3)) + 1);   //because weather data is available for every 3 hours
                    WEATHER_DATE_TIME = String.format("%04d", chosenDateNums[0]) + "-" + String.format("%02d", (chosenDateNums[1]+1)) + "-"
                            + String.format("%02d", chosenDateNums[2]) + " " + String.format("%02d", nextHourOfDay) + ":00:00";
                }
                else{
                    WEATHER_DATE_TIME = String.format("%04d", chosenDateNums[0]) + "-" + String.format("%02d", (chosenDateNums[1]+1)) + "-"
                            + String.format("%02d", chosenDateNums[2]) + " 15:00:00";
                }
            }
            else{
                WEATHER_DATE_TIME = String.format("%04d", chosenDateNums[0]) + "-" + String.format("%02d", (chosenDateNums[1]+1)) + "-"
                        + String.format("%02d", chosenDateNums[2]) + " 15:00:00";
            }
            date_dp.setText(getChosenDateText());
        }
    }

    private class GetWeatherClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if(!isInternetAccessAvailable()){
                AlertDialog.Builder builder = new AlertDialog.Builder(ChoiceActivity.this);
                builder.setTitle(getString(R.string.error))
                        .setMessage(getString(R.string.enable_internet))
                        .setPositiveButton(getString(R.string.continue_b), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return;
            }
            if(chosenLocation == null){
                Toast.makeText(
                        ChoiceActivity.this,
                        getString(R.string.choose_location),
                        Toast.LENGTH_SHORT).show();
                radius = 20;
                radius_et.setText(Double.toString(radius));
                return;
            }
            if(radius < 20){
                Toast.makeText(
                        ChoiceActivity.this,
                        getString(R.string.radius_20),
                        Toast.LENGTH_SHORT).show();
                radius = 20;
                radius_et.setText(Double.toString(radius));
                return;
            }
            else if(radius > 1000){
                Toast.makeText(
                        ChoiceActivity.this,
                        getString(R.string.radius_100),
                        Toast.LENGTH_SHORT).show();
                radius = 1000;
                radius_et.setText(Double.toString(radius));
                return;
            }
            terminateLocationService();
            Toast.makeText(
                    ChoiceActivity.this,
                    getString(R.string.fetching_weather),
                    Toast.LENGTH_LONG).show();
            List<LatLng> dispersedPoints = GeometryHelper.getDispersedPoints(chosenLocation, radius);
            WeatherAPIHelper weatherAPIHelper = new WeatherAPIHelper(ChoiceActivity.this, dispersedPoints, chosenLocation, radius);
            weatherAPIHelper.getWeather();
        }
    }

    private class MyLocationClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            getMyLocation();
        }
    }



    //MAP CALLBACKS

    private class ChoiceMapReadyCallback implements OnMapReadyCallback {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            choice_map_gm = googleMap;
            choice_map_gm.setOnMapLoadedCallback(new ChoiceMapLoadedCallBack());
        }
    }

    private class ChoiceMapLoadedCallBack implements GoogleMap.OnMapLoadedCallback{
        @Override
        public void onMapLoaded() {
            choice_map_gm.setOnMapClickListener(new ChoiceMapClickListener());
            if(chosenLocation == null){
                getMyLocation();
            }
            else{
                redrawMap();
            }
        }
    }

    private class MyLocationReadyListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if(location.getAccuracy() <= radius * 100){
                chosenLocation = new LatLng(location.getLatitude(), location.getLongitude());
                terminateLocationService();
                Toast.makeText(
                        ChoiceActivity.this,
                        getString(R.string.acquired_location),
                        Toast.LENGTH_SHORT).show();
                redrawMap();
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {}

        @Override
        public void onProviderEnabled(String s) {}

        @Override
        public void onProviderDisabled(String s) {}
    }



    //BACK ACTION

    @Override
    public void onBackPressed(){
        finishAffinity();
    }



    //PERMISSION RESPONSE

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ACCESS_LOCATION_ALLOWED: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    String locationProvider = getMyLocationAvailability();
                    if (locationProvider == null) {
                        terminateLocationService();
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChoiceActivity.this);
                        builder.setTitle(getString(R.string.error));
                        builder.setMessage(getString(R.string.location_providers_unavailable));
                        builder.setPositiveButton(getString(R.string.continue_b), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //do nothing
                            }
                        });
                        builder.setIcon(android.R.drawable.ic_dialog_alert);
                        builder.show();
                        return;
                    }
                    startLocationService(locationProvider);
                    Toast.makeText(
                            ChoiceActivity.this,
                            getString(R.string.acquiring_location),
                            Toast.LENGTH_LONG).show();
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ChoiceActivity.this);
                    builder.setTitle(getString(R.string.notice));
                    builder.setMessage(getString(R.string.unable_access_location));
                    builder.setPositiveButton(getString(R.string.continue_b), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing
                        }
                    });
                    builder.setIcon(android.R.drawable.ic_dialog_alert);
                    builder.show();
                }
                return;
            }
        }
    }
}