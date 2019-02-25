package com.apps.luka.findmyweather.Utils;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.apps.luka.findmyweather.ChoiceActivity;
import com.apps.luka.findmyweather.R;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.List;

public class WeatherAPIHelper {
    private static final String API_URL_WEATHER = "http://api.openweathermap.org/data/2.5/forecast";

    private static final String APPID = "appid";
    private static String APPID_VALUE;

    private static final String UNITS = "units";
    private static final String UNITS_VALUE = "metric";

    private static final String MODE = "mode";
    private static final String MODE_VALUE = "json";

    private static final String TYPE = "type";
    private static final String TYPE_VALUE = "accurate";

    private static final String LATITUDE = "lat";
    private static final String LONGITUDE = "lon";

    private ChoiceActivity choiceActivity;
    private List<LatLng> dispersedPoints;
    private LatLng chosenLocation;
    private double radius;
    private int callsFinished;

    public WeatherAPIHelper(ChoiceActivity choiceActivity, List<LatLng> dispersedPoints, LatLng chosenLocation, double radius){
        this.choiceActivity = choiceActivity;
        this.APPID_VALUE = choiceActivity.getString(R.string.weather_api_key);

        this.dispersedPoints = dispersedPoints;
        this.chosenLocation = chosenLocation;
        this.radius = radius;
        this.callsFinished = 0;
    }

    public void getWeather(){
        choiceActivity.resetResults();
        for(int i=0; i < dispersedPoints.size(); i++){
            String url = makeUrl(dispersedPoints.get(i));

            ResponseListener responseListener = new ResponseListener();
            ErrorListener errorListener = new ErrorListener();

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, responseListener, errorListener);
            RequestSingleton.getInstance(choiceActivity).addToRequestQueue(jsonObjectRequest);
        }
    }

    private static String addParameter(String url, String paramName, String paramValue){
        if(url != null && paramName != null && paramValue != null){
            if(url.contains("?")){
                url += "&";
            }
            else{
                url += "?";
            }
            url += (paramName + "=" + paramValue);
        }
        return url;
    }

    private String makeUrl(LatLng location){
        String url = API_URL_WEATHER;
        url = addParameter(url, APPID, APPID_VALUE);
        url = addParameter(url, UNITS, UNITS_VALUE);
        url = addParameter(url, MODE, MODE_VALUE);
        url = addParameter(url, TYPE, TYPE_VALUE);
        url = addParameter(url, LONGITUDE, String.valueOf(location.longitude));
        url = addParameter(url, LATITUDE, String.valueOf(location.latitude));
        return url;
    }




    //VOLLEY LISTENERS AND REQUEST SINGLETON

    private class ResponseListener implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            callsFinished++;
            choiceActivity.updateResults(response.toString());
            if(callsFinished == dispersedPoints.size()){
                choiceActivity.goToResultsActivity(chosenLocation, radius);

            }
        }
    }

    private class ErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            callsFinished++;
            if(callsFinished == dispersedPoints.size()){
                choiceActivity.goToResultsActivity(chosenLocation, radius);
            }
        }
    }

    private static class RequestSingleton {
        private static RequestSingleton mInstance;
        private RequestQueue mRequestQueue;
        private static Context mCtx;

        private RequestSingleton(Context context) {
            mCtx = context;
            mRequestQueue = getRequestQueue();
        }

        public static synchronized RequestSingleton getInstance(Context context) {
            if (mInstance == null) {
                mInstance = new RequestSingleton(context);
            }
            return mInstance;
        }

        public RequestQueue getRequestQueue() {
            if (mRequestQueue == null) {
                mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
            }
            return mRequestQueue;
        }

        public <T> void addToRequestQueue(Request<T> req) {
            getRequestQueue().add(req);
        }
    }
}