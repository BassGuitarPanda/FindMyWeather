package com.apps.luka.findmyweather;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.luka.findmyweather.Utils.PreferencesHelper;

public class SettingsActivity extends AppCompatActivity {
    private PreferencesHelper preferencesHelper;

    private int weatherType;
    private double temperature;
    private int wind;
    private int windDirection;

    private Toolbar settings_toolbar;

    private Button settings_back_b;
    private Spinner weather_type_sp;
    private TextView atmospheric_desc_tv;
    private EditText temperature_et;
    private Spinner wind_sp;
    private Spinner wind_direction_sp;
    private Button save_settings_b;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settings_toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(settings_toolbar);

        settings_back_b = (Button) findViewById(R.id.settings_back_b);
        weather_type_sp = (Spinner) findViewById(R.id.weather_type_sp);
        atmospheric_desc_tv = (TextView) findViewById(R.id.atmospheric_desc_tv);
        temperature_et = (EditText) findViewById(R.id.temperature_et);
        wind_sp = (Spinner) findViewById(R.id.wind_sp);
        wind_direction_sp = (Spinner) findViewById(R.id.wind_direction_sp);
        save_settings_b = (Button) findViewById(R.id.save_settings_b);

        settings_back_b.setOnClickListener(new BackClickListener());
        weather_type_sp.setOnItemSelectedListener(new WeatherTypeSelectListener());
        temperature_et.addTextChangedListener(new TemperatureChangeListener());
        wind_sp.setOnItemSelectedListener(new WindSelectListener());
        wind_direction_sp.setOnItemSelectedListener(new WindDirectionSelectListener());
        save_settings_b.setOnClickListener(new SaveClickListener());

        loadSettings();
        showSettings();
    }

    private void loadSettings(){
        preferencesHelper = new PreferencesHelper(getApplicationContext());
        weatherType = preferencesHelper.getWeatherType();
        temperature = preferencesHelper.getTemperature();
        wind = preferencesHelper.getWind();
        windDirection = preferencesHelper.getWindDirection();
    }

    private void showSettings(){
        weather_type_sp.setSelection(weatherType);
        if(weatherType == 1){
            atmospheric_desc_tv.setVisibility(View.VISIBLE);
        }
        else{
            atmospheric_desc_tv.setVisibility(View.INVISIBLE);
        }
        temperature_et.setText(String.valueOf(temperature));
        wind_sp.setSelection(wind);
        wind_direction_sp.setSelection(windDirection);
    }



    //LISTENERS

    private class BackClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            onBackPressed();
        }
    }

    private class WeatherTypeSelectListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            weatherType = weather_type_sp.getSelectedItemPosition();
            if(weatherType == 1){
                atmospheric_desc_tv.setVisibility(View.VISIBLE);
            }
            else{
                atmospheric_desc_tv.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {}
    }

    private class TemperatureChangeListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            try{
                temperature = Double.parseDouble(charSequence.toString());
            }
            catch(Exception e){
                Toast.makeText(
                        SettingsActivity.this,
                        getString(R.string.temperature_number),
                        Toast.LENGTH_SHORT).show();
                temperature = 20;
                return;
            }
            if(temperature < -273.15){
                Toast.makeText(
                        SettingsActivity.this,
                        getString(R.string.temperature_absolute_zero),
                        Toast.LENGTH_SHORT).show();
                temperature = -273.15f;
                temperature_et.setText(Double.toString(temperature));
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {}
    }

    private class WindSelectListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            wind = wind_sp.getSelectedItemPosition();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {}
    }

    private class WindDirectionSelectListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            windDirection = wind_direction_sp.getSelectedItemPosition();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {}
    }

    private class SaveClickListener implements View.OnClickListener  {
        @Override
        public void onClick(View view) {
            preferencesHelper.setWeatherType(weatherType);
            preferencesHelper.setTemperature(temperature);
            preferencesHelper.setWind(wind);
            preferencesHelper.setWindDirection(windDirection);

            Toast.makeText(
                    SettingsActivity.this,
                    getString(R.string.settings_saved),
                    Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(SettingsActivity.this, ChoiceActivity.class);
            intent.putExtra("chosenLocation", getIntent().getParcelableExtra("chosenLocation"));
            intent.putExtra("radius", getIntent().getDoubleExtra("radius", 100));
            startActivity(intent);
        }
    }



    //ON BACK PRESSED

    @Override
    public void onBackPressed() {
        if(getIntent().getStringExtra("origin").equals("res")){
            super.onBackPressed();
        }
        else{
            Intent intent = new Intent(SettingsActivity.this, ChoiceActivity.class);
            intent.putExtra("chosenLocation", getIntent().getParcelableExtra("chosenLocation"));
            intent.putExtra("radius", getIntent().getDoubleExtra("radius", 100));
            startActivity(intent);
        }
    }
}