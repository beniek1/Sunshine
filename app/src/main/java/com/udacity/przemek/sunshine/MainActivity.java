package com.udacity.przemek.sunshine;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO: Replace example with real impl.
        String[] arr = {
                "Today - Rainy - 15/10",
                "Tomorrow - Cloudy - 12/9",
                "Wed - Rainy - 17/11",
                "Thur - Clody - 11/10",
                "Fri - Dudu - 19/99",
                "Sat - Dudu - 19/99"
        };
        List<String> exampleDailyForecasts = new ArrayList<>(Arrays.asList(arr));

        ListView forecastDisplay = (ListView) this.findViewById(R.id.listview_forecast);
        ArrayAdapter<String> forecastAdapter = new ArrayAdapter<String>(
                this, R.layout.list_item_forecast, R.id.list_item_forecast_textview, exampleDailyForecasts);
        forecastDisplay.setAdapter(forecastAdapter);
    }
}
