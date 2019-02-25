package com.apps.luka.findmyweather.Utils;

import com.apps.luka.findmyweather.Data.WeatherDataObjects.WeatherData;
import com.apps.luka.findmyweather.Data.WeatherResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WeatherSortHelper {
    public static final int DISPLAYED_RESULTS_AMOUNT = 6;

    public static final int SORT_OVERALL = 0;
    public static final int SORT_TEMPERATURE = 1;
    public static final int SORT_WIND = 2;
    public static final int SORT_WIND_DIRECTION = 3;

    private static final double MIDDLE_MODERATE_WIND = 12;

    public static final double NORTH = 0;
    public static final double NORTH_EAST = 45;
    public static final double EAST = 90;
    public static final double SOUTH_EAST = 135;
    public static final double SOUTH = 180;
    public static final double SOUTH_WEST = 225;
    public static final double WEST = 270;
    public static final double NORTH_WEST = 315;
    public static final double NORTH_2 = 360;

    private static final double[] COMPASS = {NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST};

    private List<WeatherResponse> deserializedResultList;
    private String WEATHER_DATE_TIME;

    public WeatherSortHelper(List<WeatherResponse> deserializedResultList, String WEATHER_DATE_TIME){
        this.deserializedResultList = deserializedResultList;
        this.WEATHER_DATE_TIME = WEATHER_DATE_TIME;
    }
    public List<WeatherResponse> sort(int sortType, Object sortPreference){
        List<WeatherResponse> tempList = new ArrayList<WeatherResponse>();
        tempList.addAll(deserializedResultList);
        List<WeatherResponse> sortedList = new ArrayList<WeatherResponse>();
        while(sortedList.size() < DISPLAYED_RESULTS_AMOUNT){
            switch (sortType){
                case SORT_OVERALL:
                    sortedList.add(popBestOverall(tempList, (Map<String, Object>)sortPreference));
                    break;
                case SORT_TEMPERATURE:
                    sortedList.add(popBestTemperature(tempList, (double)sortPreference));
                    break;
                case SORT_WIND:
                    sortedList.add(popBestWind(tempList, (int)sortPreference));
                    break;
                case SORT_WIND_DIRECTION:
                    sortedList.add(popBestWindDirection(tempList, (int)sortPreference));
                    break;
            }
        }
        return sortedList;
    }

    private WeatherResponse popBestOverall(List<WeatherResponse> tempList, Map<String, Object> chosenParams){
        int chosenWeatherType = (int)chosenParams.get("weatherType");
        double chosenTemperature = (double)chosenParams.get("temperature");
        int chosenWind = (int)chosenParams.get("wind");
        int chosenWindDirection = (int)chosenParams.get("windDirection");

        double weatherTypeMaxDifference = Double.MIN_VALUE;
        double temperatureMaxDifference = Double.MIN_VALUE;
        double windMaxDifference = Double.MIN_VALUE;
        double windDirectionMaxDifference = Double.MIN_VALUE;
        for(WeatherResponse temp : tempList){
            double weatherTypeDifference = Math.abs(getWeatherTypeInt(temp.getWeatherDataByDateAndTime(WEATHER_DATE_TIME)) - chosenWeatherType);
            double temperatureDifference = Math.abs(temp.getWeatherDataByDateAndTime(WEATHER_DATE_TIME).getMain().getTemp() - chosenTemperature);
            double windDifference = Math.abs(temp.getWeatherDataByDateAndTime(WEATHER_DATE_TIME).getWind().getSpeed() - chosenWind);
            double windDirectionDifference1 = -1;
            double windDirectionDifference2 = -1;
            double windDirectionDifference = -1;
            if(chosenWindDirection != 0){
                windDirectionDifference1 = Math.abs(temp.getWeatherDataByDateAndTime(WEATHER_DATE_TIME).getWind().getDeg() - COMPASS[chosenWindDirection-1]);
                windDirectionDifference2 = Math.abs((NORTH_2 - temp.getWeatherDataByDateAndTime(WEATHER_DATE_TIME).getWind().getDeg()) + COMPASS[chosenWindDirection-1]);
                if(windDirectionDifference1 < windDirectionDifference2){
                    windDirectionDifference = windDirectionDifference1;
                }
                else{
                    windDirectionDifference = windDirectionDifference2;
                }
            }
            if(weatherTypeDifference > weatherTypeMaxDifference){
                weatherTypeMaxDifference = weatherTypeDifference;
            }
            if(temperatureDifference > temperatureMaxDifference){
                temperatureMaxDifference = temperatureDifference;
            }
            if(windDifference > windMaxDifference){
                windMaxDifference = windDifference;
            }
            if(chosenWindDirection != 0){
                if(windDirectionDifference > windDirectionMaxDifference){
                    windDirectionMaxDifference = windDirectionDifference;
                }
            }
        }
        int bestIndex = -1;
        double minAvgDifference = Double.MAX_VALUE;
        for(int i=0; i < tempList.size(); i++){
            WeatherResponse temp = tempList.get(i);
            double weatherTypeDifference;
            if(weatherTypeMaxDifference == 0){
                weatherTypeDifference = 0;
            }
            else{
                weatherTypeDifference = Math.abs(getWeatherTypeInt(temp.getWeatherDataByDateAndTime(WEATHER_DATE_TIME)) - chosenWeatherType) / weatherTypeMaxDifference;
            }
            double temperatureDifference;
            if(temperatureMaxDifference == 0){
                temperatureDifference = 0;
            }
            else{
                temperatureDifference = Math.abs(temp.getWeatherDataByDateAndTime(WEATHER_DATE_TIME).getMain().getTemp() - chosenTemperature) / temperatureMaxDifference;
            }
            double windDifference;
            if(windMaxDifference == 0){
                windDifference = 0;
            }
            else{
                windDifference = Math.abs(temp.getWeatherDataByDateAndTime(WEATHER_DATE_TIME).getWind().getSpeed() - chosenWind) / windMaxDifference;
            }
            double windDirectionDifference1 = -1;
            double windDirectionDifference2 = -1;
            double windDirectionDifference = -1;
            if(chosenWindDirection != 0){
                if(windDirectionMaxDifference == 0){
                    windDirectionDifference = 0;
                }
                else{
                    windDirectionDifference1 = Math.abs(temp.getWeatherDataByDateAndTime(WEATHER_DATE_TIME).getWind().getDeg() - COMPASS[chosenWindDirection-1]);
                    windDirectionDifference2 = Math.abs((NORTH_2 - temp.getWeatherDataByDateAndTime(WEATHER_DATE_TIME).getWind().getDeg()) + COMPASS[chosenWindDirection-1]);
                    if(windDirectionDifference1 < windDirectionDifference2){
                        windDirectionDifference = windDirectionDifference1 / windDirectionMaxDifference;
                    }
                    else{
                        windDirectionDifference = windDirectionDifference2 / windDirectionMaxDifference;
                    }
                }
            }

            double avgDifference;
            if(chosenWindDirection != 0){
                avgDifference = (weatherTypeDifference + temperatureDifference + windDifference + windDirectionDifference) / 4;
            }
            else{
                avgDifference = (weatherTypeDifference + temperatureDifference + windDifference) / 3;
            }

            if(avgDifference < minAvgDifference){
                minAvgDifference = avgDifference;
                bestIndex = i;
            }
        }
        WeatherResponse toReturn = tempList.get(bestIndex);
        tempList.remove(bestIndex);
        return toReturn;
    }

    public static int getWeatherTypeInt(WeatherData weatherData){
        int weatherId = weatherData.getWeather().get(0).getId();
        if(200 <= weatherId && weatherId < 300){
            return 5;   //thunderstorm
        }
        else if(300 <= weatherId && weatherId < 400){
            return 3;   //drizzle
        }
        else if(500 <= weatherId && weatherId < 600){
            return 4;   //rain
        }
        else if(600 <= weatherId && weatherId < 700){
            return 6;   //snow
        }
        else if(700 <= weatherId && weatherId < 800){
            return 1;   //atmospheric
        }
        else if(weatherId == 800){
            return 0;   //clear
        }
        else if(801 <= weatherId && weatherId < 900){
            return 2;   //clouds
        }
        else{
            return -1;
        }
    }

    public static int getWindInt(WeatherData weatherData){
        double windSpeed = weatherData.getWind().getSpeed();
        if(windSpeed < 8){
            return 0;
        }
        else if(windSpeed < 16){
            return 1;
        }
        else{
            return 2;
        }
    }

    public static int getWindDirectionInt(WeatherData weatherData){
        double windDirection = weatherData.getWind().getDeg();
        double bestDifference = Double.MAX_VALUE;
        int bestIndex = -1;
        for(int i=0; i < COMPASS.length; i++){
            double direction = COMPASS[i];
            double difference1 = Math.abs(windDirection - direction);
            double difference2 = Math.abs((NORTH_2 - windDirection) + direction);
            double difference;
            if(difference1 <= difference2){
                difference = difference1;
            }
            else{
                difference = difference2;
            }
            if(difference < bestDifference){
                bestDifference = difference;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private WeatherResponse popBestTemperature(List<WeatherResponse> tempList, double chosenTemperature){
        int bestIndex = -1;
        double bestDifference = Double.MAX_VALUE;
        for(int i=0; i < tempList.size(); i++){
            WeatherResponse temp = tempList.get(i);
            double difference = Math.abs(temp.getWeatherDataByDateAndTime(WEATHER_DATE_TIME).getMain().getTemp() - chosenTemperature);
            if(difference < bestDifference){
                bestDifference = difference;
                bestIndex = i;
            }
        }
        WeatherResponse toReturn = tempList.get(bestIndex);
        tempList.remove(bestIndex);
        return toReturn;
    }

    //light wind(0) < 8, 8 <= moderate(1) < 16, strong(2) >= 16; all in m/s
    private WeatherResponse popBestWind(List<WeatherResponse> tempList, int chosenWind){
        double bestWindspeed;
        int bestIndex = -1;
        if(chosenWind == 0){
            bestWindspeed = Double.MAX_VALUE;
            for(int i=0; i < tempList.size(); i++){
                WeatherResponse temp = tempList.get(i);
                if(temp.getWeatherDataByDateAndTime(WEATHER_DATE_TIME).getWind().getSpeed() < bestWindspeed){
                    bestWindspeed = temp.getWeatherDataByDateAndTime(WEATHER_DATE_TIME).getWind().getSpeed();
                    bestIndex = i;
                }
            }
        }
        else if(chosenWind == 1){
            double bestDifference = Double.MAX_VALUE;
            for(int i=0; i < tempList.size(); i++){
                WeatherResponse temp = tempList.get(i);
                double difference = Math.abs(temp.getWeatherDataByDateAndTime(WEATHER_DATE_TIME).getWind().getSpeed() - MIDDLE_MODERATE_WIND);
                if(difference < bestDifference){
                    bestDifference = difference;
                    bestIndex = i;
                }
            }
        }
        else if(chosenWind == 2){
            bestWindspeed = Double.MIN_VALUE;
            for(int i=0; i < tempList.size(); i++){
                WeatherResponse temp = tempList.get(i);
                if(temp.getWeatherDataByDateAndTime(WEATHER_DATE_TIME).getWind().getSpeed() > bestWindspeed){
                    bestWindspeed = temp.getWeatherDataByDateAndTime(WEATHER_DATE_TIME).getWind().getSpeed();
                    bestIndex = i;
                }
            }
        }
        WeatherResponse toReturn = tempList.get(bestIndex);
        tempList.remove(bestIndex);
        return toReturn;
    }

    private WeatherResponse popBestWindDirection(List<WeatherResponse> tempList, int chosenWindDirection){
        if(chosenWindDirection == 0){
            //wind direction 0 = "Any"
            return null;
        }
        int bestIndex = -1;
        double bestDifference = Double.MAX_VALUE;
        for(int i=0; i < tempList.size(); i++){
            WeatherResponse temp = tempList.get(i);
            double difference1 = Math.abs(temp.getWeatherDataByDateAndTime(WEATHER_DATE_TIME).getWind().getDeg() - COMPASS[chosenWindDirection-1]);
            double difference2 = Math.abs((NORTH_2 - temp.getWeatherDataByDateAndTime(WEATHER_DATE_TIME).getWind().getDeg()) + COMPASS[chosenWindDirection-1]);
            if(difference1 < bestDifference){
                bestDifference = difference1;
                bestIndex = i;
            }
            else if(difference2 < bestDifference){
                bestDifference = difference2;
                bestIndex = i;
            }
        }
        WeatherResponse toReturn = tempList.get(bestIndex);
        tempList.remove(bestIndex);
        return toReturn;
    }
}