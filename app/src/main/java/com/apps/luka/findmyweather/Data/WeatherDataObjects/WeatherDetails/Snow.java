package com.apps.luka.findmyweather.Data.WeatherDataObjects.WeatherDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Snow implements Serializable {
    private double _3h;

    @JsonProperty("3h")
    public double get_3h() {
        return _3h;
    }

    public void set_3h(double _3h) {
        this._3h = _3h;
    }
}
