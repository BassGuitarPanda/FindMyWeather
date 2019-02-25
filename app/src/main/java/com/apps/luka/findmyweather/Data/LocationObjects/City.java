package com.apps.luka.findmyweather.Data.LocationObjects;

import com.apps.luka.findmyweather.Data.LocationObjects.DetailedLocation.Coord;

import java.io.Serializable;

public class City implements Serializable {
    private long id;
    private String name;
    private long population;
    private Coord coord;
    private String country;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPopulation() {
        return population;
    }

    public void setPopulation(long population) {
        this.population = population;
    }

    public Coord getCoord() {
        return coord;
    }

    public void setCoord(Coord coord) {
        this.coord = coord;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
