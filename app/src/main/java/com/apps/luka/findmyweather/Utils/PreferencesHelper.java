package com.apps.luka.findmyweather.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class PreferencesHelper {
    private static final String SHARED_PREFERENCES = "SHARED_PREFERENCES";

    private static final String WEATHER_TYPE = "weatherType";
    private static final String TEMPERATURE = "temperature";
    private static final String WIND = "wind";
    private static final String WIND_DIRECTION = "windDirection";

    private SharedPreferences prefs;

    public PreferencesHelper(Context context){
        this.prefs = context.getSharedPreferences(PreferencesHelper.SHARED_PREFERENCES, MODE_PRIVATE);
    }

    public int getWeatherType(){
        return prefs.getInt(WEATHER_TYPE, 0);
    }

    public void setWeatherType(int weatherType){
        prefs.edit().putInt(WEATHER_TYPE, weatherType).apply();
    }

    public double getTemperature(){
        return prefs.getFloat(TEMPERATURE, 20);
    }

    public void setTemperature(double temperature){
        prefs.edit().putFloat(TEMPERATURE, (float)temperature).apply();
    }

    public int getWind(){
        return prefs.getInt(WIND, 0);
    }

    public void setWind(int wind){
        prefs.edit().putInt(WIND, wind).apply();
    }

    public int getWindDirection(){
        return prefs.getInt(WIND_DIRECTION, 0);
    }

    public void setWindDirection(int windDirection){
        prefs.edit().putInt(WIND_DIRECTION, windDirection).apply();
    }
}