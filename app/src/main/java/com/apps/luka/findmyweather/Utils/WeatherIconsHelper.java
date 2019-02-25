package com.apps.luka.findmyweather.Utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.apps.luka.findmyweather.R;

public class WeatherIconsHelper {
    private Context context;

    public WeatherIconsHelper(Context context){
        this.context = context;
    }

    public Drawable getWeatherTypeIcon(int weatherType){
        switch(weatherType){
            case(0):
                return context.getResources().getDrawable(R.drawable.weather_clear_48, context.getTheme());
            case(1):
                return context.getResources().getDrawable(R.drawable.weather_atmospheric_48, context.getTheme());
            case(2):
                return context.getResources().getDrawable(R.drawable.weather_clouds_48, context.getTheme());
            case(3):
                return context.getResources().getDrawable(R.drawable.weather_drizzle_48, context.getTheme());
            case(4):
                return context.getResources().getDrawable(R.drawable.weather_rain_48, context.getTheme());
            case(5):
                return context.getResources().getDrawable(R.drawable.weather_thunderstorm_48, context.getTheme());
            case(6):
                return context.getResources().getDrawable(R.drawable.weather_snow_48, context.getTheme());
        }
        return null;
    }

    public Drawable getWindIcon(int wind){
        switch(wind){
            case(0):
                return context.getResources().getDrawable(R.drawable.baseline_signal_cellular_1_bar_black_18, context.getTheme());
            case(1):
                return context.getResources().getDrawable(R.drawable.baseline_signal_cellular_3_bar_black_18, context.getTheme());
            case(2):
                return context.getResources().getDrawable(R.drawable.baseline_signal_cellular_4_bar_black_18, context.getTheme());
        }
        return null;
    }

    public static String getCountryFlag(String countryCode){
        if(countryCode == null || countryCode.length() != 2){
            return null;
        }
        int firstLetter = countryCode.charAt(0) + 0x1F1A5;
        int secondLetter = countryCode.charAt(1) + 0x1F1A5;
        return String.valueOf(Character.toChars(firstLetter)) + String.valueOf(Character.toChars(secondLetter));
    }
}