package com.apps.luka.findmyweather.Data;

import com.apps.luka.findmyweather.Data.LocationObjects.City;
import com.apps.luka.findmyweather.Data.WeatherDataObjects.WeatherData;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.List;

public class WeatherResponse implements Serializable {
    private String cod;
    private double message;
    private int cnt;
    private List<WeatherData> list;
    private City city;

    public String getCod() {
        return cod;
    }

    public void setCod(String cod) {
        this.cod = cod;
    }

    public double getMessage() {
        return message;
    }

    public void setMessage(double message) {
        this.message = message;
    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public List<WeatherData> getList() {
        return list;
    }

    public void setList(List<WeatherData> list) {
        this.list = list;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    @JsonIgnore
    public WeatherData getWeatherDataByDateAndTime(String dt_txt){
        for(WeatherData weatherData : list){
            if(weatherData.getDt_txt().equals(dt_txt)){
                return weatherData;
            }
        }
        return null;
    }
}
