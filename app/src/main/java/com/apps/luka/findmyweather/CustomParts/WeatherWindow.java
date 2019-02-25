package com.apps.luka.findmyweather.CustomParts;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.apps.luka.findmyweather.Data.WeatherResponse;
import com.apps.luka.findmyweather.R;
import com.apps.luka.findmyweather.ResultsActivity;
import com.apps.luka.findmyweather.Utils.WeatherIconsHelper;
import com.apps.luka.findmyweather.Utils.WeatherSortHelper;

public class WeatherWindow extends Dialog {
    private ResultsActivity resultsActivity;
    private WeatherResponse weatherResponse;
    private int position;

    private TextView weather_data_tv;
    private TextView city_data_tv;
    private TextView country_data_tv;
    private ImageView type_data_iv;
    private TextView type_data_tv;
    private TextView temp_data_tv;
    private TextView wind_sp_data_tv;
    private TextView wind_dir_data_tv;

    public WeatherWindow(Activity a, WeatherResponse weatherResponse, int position) {
        super(a);
        this.resultsActivity = (ResultsActivity) a;
        this.weatherResponse = weatherResponse;
        this.position = position;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.window_weather);

        (findViewById(R.id.window_weather)).getLayoutParams().width = (int)(resultsActivity.getWidth() * 0.8);

        weather_data_tv = (TextView) findViewById(R.id.weather_data_tv);
        city_data_tv = (TextView) findViewById(R.id.city_data_tv);
        country_data_tv = (TextView) findViewById(R.id.country_data_tv);
        type_data_iv = (ImageView) findViewById(R.id.type_data_iv);
        type_data_tv = (TextView) findViewById(R.id.type_data_tv);
        temp_data_tv = (TextView) findViewById(R.id.temp_data_tv);
        wind_sp_data_tv = (TextView) findViewById(R.id.wind_sp_data_tv);
        wind_dir_data_tv = (TextView) findViewById(R.id.wind_dir_data_tv);

        WeatherIconsHelper weatherIconsHelper = new WeatherIconsHelper(resultsActivity);

        weather_data_tv.setText(weather_data_tv.getText().toString() + " " + resultsActivity.getString(R.string.order_fill) + position);

        String city = weatherResponse.getCity().getName();
        city_data_tv.setText((city != null) ? city : resultsActivity.getString(R.string.undefined));

        String country = resultsActivity.getCountries().get(weatherResponse.getCity().getCountry());
        country_data_tv.setText((country != null) ? (WeatherIconsHelper.getCountryFlag(weatherResponse.getCity().getCountry())  + " " + country) : resultsActivity.getString(R.string.undefined));

        int resultWeatherType = WeatherSortHelper.getWeatherTypeInt(weatherResponse.getWeatherDataByDateAndTime(resultsActivity.WEATHER_DATE_TIME));
        Drawable typeDataDrawable = weatherIconsHelper.getWeatherTypeIcon(resultWeatherType);
        type_data_iv.setImageDrawable(typeDataDrawable);
        type_data_tv.setText(resultsActivity.getResources().getStringArray(R.array.weather_types)[resultWeatherType]);

        String temperatureTxt = String.format("%.1f", weatherResponse.getWeatherDataByDateAndTime(resultsActivity.WEATHER_DATE_TIME).getMain().getTemp());
        temp_data_tv.setText(temperatureTxt + resultsActivity.getString(R.string.celsius_symbol));

        String windSpeed = String.valueOf(Math.round(weatherResponse.getWeatherDataByDateAndTime(resultsActivity.WEATHER_DATE_TIME).getWind().getSpeed()));
        wind_sp_data_tv.setText(" " + windSpeed + " " + resultsActivity.getString(R.string.meters_per_second));
        Drawable windSpDrawable = weatherIconsHelper.getWindIcon(WeatherSortHelper.getWindInt(weatherResponse.getWeatherDataByDateAndTime(resultsActivity.WEATHER_DATE_TIME)));
        wind_sp_data_tv.setCompoundDrawablesWithIntrinsicBounds(windSpDrawable, null, null, null);

        String windDirectionTxt = resultsActivity.getResources().getStringArray(R.array.wind_directions_alt)
                [WeatherSortHelper.getWindDirectionInt(weatherResponse.getWeatherDataByDateAndTime(resultsActivity.WEATHER_DATE_TIME))];
        String windDirectionDeg = String.valueOf(Math.round(weatherResponse.getWeatherDataByDateAndTime(resultsActivity.WEATHER_DATE_TIME).getWind().getDeg()));
        wind_dir_data_tv.setText(windDirectionDeg + "° (" + windDirectionTxt + ")");

        this.setOnCancelListener(new OnCloseListener());
    }

    private class OnCloseListener implements Dialog.OnCancelListener {
        @Override
        public void onCancel(DialogInterface dialogInterface) {
            WeatherWindow.this.dismiss();
        }
    }
}