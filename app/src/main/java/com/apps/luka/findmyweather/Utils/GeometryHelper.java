package com.apps.luka.findmyweather.Utils;

import com.apps.luka.findmyweather.Data.WeatherResponse;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeometryHelper {
    public static final int POINT_COUNT = 30;

    public static List<LatLng> getDispersedPoints(LatLng chosenLocation, double radius) {
        double tooClose = radius * 200; //a tenth of the diameter in meters(diameter * 1000)
        Random random = new Random();
        List<LatLng> dispersedPoints = new ArrayList<LatLng>();
        for (int i = 0; i < POINT_COUNT; i++) {
            LatLng location;
            double closestDistance;
            do {
                double tempRadius = radius * 1000 * Math.sqrt(random.nextDouble());
                double tempAngle = random.nextDouble() * 360;
                location = SphericalUtil.computeOffset(chosenLocation, tempRadius, tempAngle);
                closestDistance = getClosestDistance(location, dispersedPoints);
            } while(closestDistance < tooClose);
            dispersedPoints.add(location);
        }
        return dispersedPoints;
    }

    private static double getClosestDistance(LatLng location, List<LatLng> dispersedPoints){
        double closestDistance = Double.MAX_VALUE;
        for(LatLng point : dispersedPoints){
            double currentDistance = SphericalUtil.computeDistanceBetween(location, point);
            if(currentDistance < closestDistance){
                closestDistance = currentDistance;
            }
        }
        return closestDistance;
    }

    public static double getClosestDistanceFromWeatherResponses(LatLng location, List<WeatherResponse> weatherResponseList){
        List<LatLng> resultPoints = new ArrayList<LatLng>();
        for(WeatherResponse temp : weatherResponseList){
            LatLng resultPoint = new LatLng(temp.getCity().getCoord().getLat(), temp.getCity().getCoord().getLon());
            resultPoints.add(resultPoint);
        }
        return getClosestDistance(location, resultPoints);
    }
}