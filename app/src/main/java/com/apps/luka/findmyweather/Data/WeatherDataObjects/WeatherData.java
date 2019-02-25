package com.apps.luka.findmyweather.Data.WeatherDataObjects;

import com.apps.luka.findmyweather.Data.WeatherDataObjects.WeatherDetails.Clouds;
import com.apps.luka.findmyweather.Data.WeatherDataObjects.WeatherDetails.Main;
import com.apps.luka.findmyweather.Data.WeatherDataObjects.WeatherDetails.Rain;
import com.apps.luka.findmyweather.Data.WeatherDataObjects.WeatherDetails.Snow;
import com.apps.luka.findmyweather.Data.WeatherDataObjects.WeatherDetails.Sys;
import com.apps.luka.findmyweather.Data.WeatherDataObjects.WeatherDetails.Weather;
import com.apps.luka.findmyweather.Data.WeatherDataObjects.WeatherDetails.Wind;

import java.io.Serializable;
import java.util.List;

public class WeatherData implements Serializable {
    private int dt;
    private String dt_txt;

    private Main main;
    private List<Weather> weather;

    private Clouds clouds;
    private Wind wind;
    private Rain rain;
    private Snow snow;
    private Sys sys;

    public int getDt() {
        return dt;
    }

    public void setDt(int dt) {
        this.dt = dt;
    }

    public String getDt_txt() {
        return dt_txt;
    }

    public void setDt_txt(String dt_txt) {
        this.dt_txt = dt_txt;
    }

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public void setWeather(List<Weather> weather) {
        this.weather = weather;
    }

    public Clouds getClouds() {
        return clouds;
    }

    public void setClouds(Clouds clouds) {
        this.clouds = clouds;
    }

    public Wind getWind() {
        return wind;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    public Rain getRain() {
        return rain;
    }

    public void setRain(Rain rain) {
        this.rain = rain;
    }

    public Snow getSnow() {
        return snow;
    }

    public void setSnow(Snow snow) {
        this.snow = snow;
    }

    public Sys getSys() {
        return sys;
    }

    public void setSys(Sys sys) {
        this.sys = sys;
    }
}
