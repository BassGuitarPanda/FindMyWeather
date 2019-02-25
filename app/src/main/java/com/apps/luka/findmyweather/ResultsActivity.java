package com.apps.luka.findmyweather;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.luka.findmyweather.CustomParts.SortDialog;
import com.apps.luka.findmyweather.CustomParts.WeatherWindow;
import com.apps.luka.findmyweather.Data.WeatherResponse;
import com.apps.luka.findmyweather.Utils.PreferencesHelper;
import com.apps.luka.findmyweather.Utils.WeatherIconsHelper;
import com.apps.luka.findmyweather.Utils.WeatherSortHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ResultsActivity extends AppCompatActivity {
    public static final int[] SORT_MESSAGES = {R.string.sorted_overall, R.string.sorted_temperatures, R.string.sorted_wind, R.string.sorted_wind_direction};

    public String WEATHER_DATE_TIME;
    private int transparentRedColor = Color.argb(64, 255, 0, 0);
    private HashMap<String, String> countries = new HashMap<String, String>();
    private PreferencesHelper preferencesHelper;
    private WeatherSortHelper weatherSortHelper;

    private LatLng chosenLocation;
    private double radius;
    private List<Marker> markers = new ArrayList<Marker>();

    private int weatherType;
    private double temperature;
    private int wind;
    private int windDirection;

    private int sortPreference = 0;

    private int height;
    private int width;

    private List<WeatherResponse> resultList;
    private List<WeatherResponse> topResults = new ArrayList<WeatherResponse>();

    private Toolbar res_toolbar;

    private Button res_back_b;
    private TextView res_desc_tv;
    private Button sort_b;
    private Button res_settings_b;
    private SupportMapFragment res_map_f;

    private TableLayout res_table;
    private TableRow header_tr;
    private TextView order_header_tv;
    private TextView weather_type_header_tv;
    private TextView location_header_tv;
    private TextView more_header_tv;

    private GoogleMap res_map_gm;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        res_toolbar = (Toolbar) findViewById(R.id.res_toolbar);
        setSupportActionBar(res_toolbar);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        for (String iso2 : Locale.getISOCountries()) {
            Locale loc = new Locale(Locale.ENGLISH.getLanguage(), iso2);
            countries.put(iso2, loc.getDisplayCountry());
        }

        res_back_b = (Button) findViewById(R.id.res_back_b);
        res_desc_tv = (TextView) findViewById(R.id.res_desc_tv);
        sort_b = (Button) findViewById(R.id.sort_b);
        res_settings_b = (Button) findViewById(R.id.res_settings_b);

        loadData();
        loadDesc();
        loadSettings();
        calculateInitialResults();

        res_map_f = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.res_map_f);

        res_table = (TableLayout) findViewById(R.id.res_table);
        header_tr = (TableRow) findViewById(R.id.header_tr);
        order_header_tv = (TextView) findViewById(R.id.order_header_tv);
        weather_type_header_tv = (TextView) findViewById(R.id.weather_type_header_tv);
        location_header_tv = (TextView) findViewById(R.id.location_header_tv);
        more_header_tv = (TextView) findViewById(R.id.more_header_tv);

        res_back_b.setOnClickListener(new BackClickListener());
        sort_b.setOnClickListener(new SortClickListener());
        res_settings_b.setOnClickListener(new SettingsClickListener());

        res_map_f.getView().getLayoutParams().height = (int)(height/2);
        res_map_f.getMapAsync(new ResMapReadyCallback());

        fillScrollV();

        Toast.makeText(
                ResultsActivity.this,
                getString(SORT_MESSAGES[sortPreference]),
                Toast.LENGTH_SHORT).show();
    }

    public HashMap<String, String> getCountries(){
        return countries;
    }

    private void loadData() {
        chosenLocation = getIntent().getParcelableExtra("chosenLocation");
        radius = getIntent().getDoubleExtra("radius", 20);
        WEATHER_DATE_TIME = getIntent().getStringExtra("WEATHER_DATE_TIME");

        resultList = (List<WeatherResponse>) getIntent().getSerializableExtra("resultList");
    }

    private void loadDesc(){
        String[] weatherDateParts = (WEATHER_DATE_TIME.split(" ")[0]).split("-");
        String weatherDate = weatherDateParts[2] + ". " + weatherDateParts[1] + ".";
        res_desc_tv.setText(res_desc_tv.getText() + " " + weatherDate);
    }

    private void loadSettings(){
        preferencesHelper = new PreferencesHelper(getApplicationContext());
        weatherType = preferencesHelper.getWeatherType();
        temperature = preferencesHelper.getTemperature();
        wind = preferencesHelper.getWind();
        windDirection = preferencesHelper.getWindDirection();
    }

    private void calculateInitialResults(){
        Map<String, Object> chosenParams = new HashMap<String, Object>();
        chosenParams.put("weatherType", weatherType);
        chosenParams.put("temperature", temperature);
        chosenParams.put("wind", wind);
        chosenParams.put("windDirection", windDirection);

        weatherSortHelper = new WeatherSortHelper(resultList, WEATHER_DATE_TIME);
        topResults.addAll(weatherSortHelper.sort(WeatherSortHelper.SORT_OVERALL, chosenParams));
    }

    private void clearTable(){
        res_table.getLayoutParams().height = (int)(height / 3);
        header_tr.getLayoutParams().height = (int)(res_table.getLayoutParams().height / (WeatherSortHelper.DISPLAYED_RESULTS_AMOUNT+1));
        List<Integer> doNotRemoveIds = new ArrayList<Integer>();
        doNotRemoveIds.add(header_tr.getId());
        doNotRemoveIds.add(order_header_tv.getId());
        doNotRemoveIds.add(weather_type_header_tv.getId());
        doNotRemoveIds.add(location_header_tv.getId());
        doNotRemoveIds.add(more_header_tv.getId());
        for(int i = res_table.getChildCount()-1; i >= 0; i--) {
            View child = res_table.getChildAt(i);
            if (!doNotRemoveIds.contains(child.getId())) {
                res_table.removeViewAt(i);
            }
        }
    }

    public void updateSortPreference(int sortPreference){
        this.sortPreference = sortPreference;
    }

    public int getSortPreference(){
        return sortPreference;
    }

    public void fillScrollV(){
        clearTable();
        WeatherIconsHelper weatherIconsHelper = new WeatherIconsHelper(this);
        for(int i=0; i < topResults.size(); i++){
            WeatherResponse result = topResults.get(i);
            TableRow resultRow = new TableRow(this);
            TableRow.LayoutParams table_row_lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, res_table.getLayoutParams().height / (WeatherSortHelper.DISPLAYED_RESULTS_AMOUNT+1));
            resultRow.setLayoutParams(table_row_lp);

            TableRow.LayoutParams text_view_lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT,1.0f);
            TableRow.LayoutParams button_lp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT,1.0f);

            TextView orderV = new TextView(this);
            orderV.setText(String.valueOf(i + 1));
            orderV.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            orderV.setGravity(Gravity.CENTER_VERTICAL);
            orderV.setLayoutParams(text_view_lp);

            int resultWeatherType = WeatherSortHelper.getWeatherTypeInt(result.getWeatherDataByDateAndTime(WEATHER_DATE_TIME));
            TextView weatherTypeV = new TextView(this);
            weatherTypeV.setText(getResources().getStringArray(R.array.weather_types)[resultWeatherType]);
            weatherTypeV.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            weatherTypeV.setGravity(Gravity.CENTER_VERTICAL);
            weatherTypeV.setCompoundDrawablesRelative(weatherIconsHelper.getWeatherTypeIcon(resultWeatherType), null, null, null);
            weatherTypeV.setLayoutParams(text_view_lp);

            TextView locationV = new TextView(this);
            String city = result.getCity().getName();
            String country = result.getCity().getCountry();
            if(city == null && country == null){
                locationV.setText(getString(R.string.undefined));
            }
            else{
                locationV.setText(((city != null) ? city : getString(R.string.undefined)) + " " +
                        ((WeatherIconsHelper.getCountryFlag(country) != null) ?
                                WeatherIconsHelper.getCountryFlag(country) : getString(R.string.undefined)));
            }
            locationV.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            locationV.setGravity(Gravity.CENTER_VERTICAL);
            locationV.setLayoutParams(text_view_lp);

            ImageView moreB = new ImageView(this);
            Drawable moreDrawable = scaleDrawable((BitmapDrawable) getResources().getDrawable(R.drawable.baseline_more_horiz_black_36, getTheme()),
                    res_table.getLayoutParams().height / (WeatherSortHelper.DISPLAYED_RESULTS_AMOUNT+1));
            moreB.setImageDrawable(moreDrawable);
            moreB.setLayoutParams(button_lp);
            moreB.setOnClickListener(new MoreClickListener(result, i));

            resultRow.addView(orderV);
            resultRow.addView(weatherTypeV);
            resultRow.addView(locationV);
            resultRow.addView(moreB);
            resultRow.setOnClickListener(new TableRowClickListener(i));
            res_table.addView(resultRow);
        }
        res_table.getLayoutParams().height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
    }

    private Drawable scaleDrawable(BitmapDrawable bitmapDrawable, int size) {
        Bitmap bitmapScaled = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), size, size, false);
        return new BitmapDrawable(getResources(), bitmapScaled);
    }

    public void redrawMap(){
        res_map_gm.clear();
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(chosenLocation);
        circleOptions.radius(radius * 1000 * 1.1);
        circleOptions.strokeColor(transparentRedColor);
        circleOptions.fillColor(transparentRedColor);
        res_map_gm.addCircle(circleOptions);
        CameraPosition.Builder cameraPositionBuilder = new CameraPosition.Builder();
        cameraPositionBuilder.target(chosenLocation);
        CameraPosition cameraPosition = cameraPositionBuilder.build();
        res_map_gm.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        LatLngBounds.Builder latLangBuilder = new LatLngBounds.Builder();
        latLangBuilder.include(SphericalUtil.computeOffset(chosenLocation, radius * 1000 * Math.sqrt(2), WeatherSortHelper.NORTH));
        latLangBuilder.include(SphericalUtil.computeOffset(chosenLocation, radius * 1000 * Math.sqrt(2), WeatherSortHelper.EAST));
        latLangBuilder.include(SphericalUtil.computeOffset(chosenLocation, radius * 1000 * Math.sqrt(2), WeatherSortHelper.SOUTH));
        latLangBuilder.include(SphericalUtil.computeOffset(chosenLocation, radius * 1000 * Math.sqrt(2), WeatherSortHelper.WEST));
        res_map_gm.animateCamera(CameraUpdateFactory.newLatLngBounds(latLangBuilder.build(), 10));

        markers = new ArrayList<Marker>();
        for(int i=0; i < topResults.size(); i++){
            WeatherResponse weatherResponse = topResults.get(i);
            MarkerOptions markerOptions = new MarkerOptions();
            LatLng markerLatLng = new LatLng(weatherResponse.getCity().getCoord().getLat(), weatherResponse.getCity().getCoord().getLon());
            markerOptions.position(markerLatLng);
            markerOptions.title("#" + (i + 1) + " - " + weatherResponse.getCity().getName() + " " +
                    ((WeatherIconsHelper.getCountryFlag(weatherResponse.getCity().getCountry())) != null ?
                            WeatherIconsHelper.getCountryFlag(weatherResponse.getCity().getCountry()) : getString(R.string.undefined)));
            markerOptions.draggable(false);
            Marker marker = res_map_gm.addMarker(markerOptions);
            marker.setTag(weatherResponse);
            markers.add(marker);
        }
    }

    private void hideAllMarkerWindows(){
        for(Marker marker : markers){
            marker.hideInfoWindow();
        }
    }

    private void showMarkerWindow(int position){
        hideAllMarkerWindows();
        markers.get(position).showInfoWindow();
    }

    private void resetTableRowHighlights(){
        TypedValue background = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.colorBackground, background, true);
        boolean backgroundIsColor;
        Object colorBackground;
        if (background.type >= TypedValue.TYPE_FIRST_COLOR_INT && background.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            //int
            colorBackground =  background.data;
            backgroundIsColor = true;
        }
        else {
            //drawable
            colorBackground = getResources().getDrawable(background.resourceId, getTheme());
            backgroundIsColor = false;
        }
        for(int i=0; i < res_table.getChildCount(); i++){
            View view = res_table.getChildAt(i);
            if(backgroundIsColor){
                view.setBackgroundColor((int)colorBackground);
            }
            else{
                view.setBackground((Drawable)colorBackground);
            }
        }
    }

    private void highlightTableRow(int position){
        resetTableRowHighlights();
        View view = res_table.getChildAt(position + 1); //to skip the header
        view.setBackgroundColor(transparentRedColor);
    }

    public int getWidth(){
        return width;
    }



    //LISTENERS

    private class BackClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            onBackPressed();
        }
    }

    private class SortClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Map<String, Object> chosenParams = new HashMap<String, Object>();
            chosenParams.put("weatherType", weatherType);
            chosenParams.put("temperature", temperature);
            chosenParams.put("wind", wind);
            chosenParams.put("windDirection", windDirection);
            SortDialog sortDialog = new SortDialog(ResultsActivity.this, weatherSortHelper, topResults, chosenParams);
            sortDialog.show();
        }
    }

    private class SettingsClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(ResultsActivity.this, SettingsActivity.class);
            intent.putExtra("chosenLocation", getIntent().getParcelableExtra("chosenLocation"));
            intent.putExtra("radius", getIntent().getDoubleExtra("radius", 100));
            intent.putExtra("origin", "res");
            startActivity(intent);
        }
    }

    private class MoreClickListener implements View.OnClickListener {
        private WeatherResponse weatherResponse;
        private int position;

        public MoreClickListener(WeatherResponse weatherResponse, int position){
            this.weatherResponse = weatherResponse;
            this.position = position;
        }

        @Override
        public void onClick(View view) {
            WeatherWindow weatherWindow = new WeatherWindow(ResultsActivity.this, weatherResponse, position + 1);
            weatherWindow.show();
            highlightTableRow(position);
            showMarkerWindow(position);
        }
    }

    private class ResMapClickListener implements GoogleMap.OnMapClickListener {
        @Override
        public void onMapClick(LatLng latLng) {
            hideAllMarkerWindows();
            resetTableRowHighlights();
        }
    }

    private class MarkerClickListener implements GoogleMap.OnMarkerClickListener {
        @Override
        public boolean onMarkerClick(Marker marker) {
            marker.showInfoWindow();
            int position = -1;
            for(int i=0; i < markers.size(); i++){
                if(markers.get(i).equals(marker)){
                    highlightTableRow(i);
                    break;
                }
            }
            return true;    //true to consume the event, so that the default behaviour doesn't occur
        }
    }

    private class TableRowClickListener implements View.OnClickListener {
        private int position;

        public TableRowClickListener(int position){
            this.position = position;
        }

        @Override
        public void onClick(View view) {
            int currentlyChosen = -1;
            for(int i=0; i < markers.size(); i++){
                if(markers.get(i).isInfoWindowShown()){
                    currentlyChosen = i;
                    break;
                }
            }
            if(currentlyChosen == position){
                resetTableRowHighlights();
                hideAllMarkerWindows();
            }
            else{
                highlightTableRow(position);
                showMarkerWindow(position);
            }
        }
    }



    //MAP CALLBACKS

    private class ResMapReadyCallback implements OnMapReadyCallback {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            res_map_gm = googleMap;
            res_map_gm.setOnMapLoadedCallback(new ResMapLoadedCallback());
        }
    }

    private class ResMapLoadedCallback implements GoogleMap.OnMapLoadedCallback {
        @Override
        public void onMapLoaded() {
            res_map_gm.setOnMapClickListener(new ResMapClickListener());
            res_map_gm.setOnMarkerClickListener(new MarkerClickListener());
            redrawMap();
        }
    }



    //ON BACK PRESSED

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(ResultsActivity.this, ChoiceActivity.class);
        intent.putExtra("chosenLocation", getIntent().getParcelableExtra("chosenLocation"));
        intent.putExtra("radius", getIntent().getDoubleExtra("radius", 100));
        startActivity(intent);
    }
}