package com.apps.luka.findmyweather.CustomParts;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.apps.luka.findmyweather.Data.WeatherResponse;
import com.apps.luka.findmyweather.R;
import com.apps.luka.findmyweather.ResultsActivity;
import com.apps.luka.findmyweather.Utils.WeatherSortHelper;

import java.util.List;
import java.util.Map;

public class SortDialog extends Dialog {
    private ResultsActivity resultsActivity;

    private RadioGroup sort_rg;
    private RadioButton overall_rb;
    private RadioButton temperature_rb;
    private RadioButton wind_rb;
    private RadioButton wind_direction_rb;

    private Map<String, Object> chosenParams;
    private double temperature;
    private int wind;
    private int windDirection;

    private WeatherSortHelper weatherSortHelper;
    private List<WeatherResponse> topResults;

    public SortDialog(Activity a, WeatherSortHelper weatherSortHelper, List<WeatherResponse> topResults, Map<String, Object> chosenParams) {
        super(a);
        this.resultsActivity = (ResultsActivity)a;
        this.weatherSortHelper = weatherSortHelper;
        this.topResults = topResults;
        this.chosenParams = chosenParams;

        this.temperature = (double)chosenParams.get("temperature");
        this.wind = (int)chosenParams.get("wind");
        this.windDirection = (int)chosenParams.get("windDirection");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_sort);

        sort_rg = (RadioGroup) findViewById(R.id.sort_rg);

        overall_rb = (RadioButton) findViewById(R.id.overall_rb);
        temperature_rb = (RadioButton) findViewById(R.id.temperature_rb);
        wind_rb = (RadioButton) findViewById(R.id.wind_rb);
        wind_direction_rb = (RadioButton) findViewById(R.id.wind_direction_rb);

        overall_rb.setOnClickListener(new SortClick());
        temperature_rb.setOnClickListener(new SortClick());
        wind_rb.setOnClickListener(new SortClick());
        wind_direction_rb.setOnClickListener(new SortClick());

        if(windDirection == 0){
            wind_direction_rb.setVisibility(View.INVISIBLE);
            if(resultsActivity.getSortPreference() == 3){
                resultsActivity.updateSortPreference(0);
            }
        }
        switch(resultsActivity.getSortPreference()){
            case(0):
                sort_rg.check(overall_rb.getId());
                break;
            case(1):
                sort_rg.check(temperature_rb.getId());
                break;
            case(2):
                sort_rg.check(wind_rb.getId());
                break;
            case(3):
                sort_rg.check(wind_direction_rb.getId());
                break;
        }

        this.setOnCancelListener(new OnCloseListener());
    }

    private class SortClick implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            if(view.getId() == overall_rb.getId()){
                topResults.clear();
                topResults.addAll(weatherSortHelper.sort(WeatherSortHelper.SORT_OVERALL, chosenParams));
                resultsActivity.updateSortPreference(0);
            }
            else if(view.getId() == temperature_rb.getId()){
                topResults.clear();
                topResults.addAll(weatherSortHelper.sort(WeatherSortHelper.SORT_TEMPERATURE, temperature));
                resultsActivity.updateSortPreference(1);
            }
            else if(view.getId() == wind_rb.getId()){
                topResults.clear();
                topResults.addAll(weatherSortHelper.sort(WeatherSortHelper.SORT_WIND, wind));
                resultsActivity.updateSortPreference(2);
            }
            else if(view.getId() == wind_direction_rb.getId()){
                topResults.clear();
                topResults.addAll(weatherSortHelper.sort(WeatherSortHelper.SORT_WIND_DIRECTION, windDirection));
                resultsActivity.updateSortPreference(3);
            }
            resultsActivity.fillScrollV();
            Toast.makeText(
                    resultsActivity,
                    resultsActivity.getString(resultsActivity.SORT_MESSAGES[resultsActivity.getSortPreference()]),
                    Toast.LENGTH_SHORT).show();
            resultsActivity.redrawMap();
            SortDialog.this.dismiss();
        }
    }

    private class OnCloseListener implements Dialog.OnCancelListener {
        @Override
        public void onCancel(DialogInterface dialogInterface) {
            SortDialog.this.dismiss();
        }
    }
}