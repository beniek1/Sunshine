package com.udacity.przemek.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class DetailsActivity extends AppCompatActivity {

    /**
     * Constant for intent with weather information.
     */
    public static final String WEATHER_INFO = "weatherInfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_detail);
        Intent callingIntent = getIntent();

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(callingIntent.getStringExtra(WEATHER_INFO));

    }


}
