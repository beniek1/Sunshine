package com.udacity.przemek.sunshine;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    public static final String MY_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?lat=51" +
            ".05&lon=17.06&mode=json&cnt=7&units=metric&APPID=e7b0e7d19a155ba13278c7ee01eb7e43";

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

        String response = "";
        Toast.makeText(this, "STARTING WORK!", Toast.LENGTH_SHORT).show();

        try {
            response = new FetchWeatherTask().execute(MY_URL).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        Toast.makeText(this, response, Toast.LENGTH_LONG).show();


    }
}

/**
 * Used for retrieving weather information from specified URL (OpenWeatherAPI)
 */
class FetchWeatherTask extends AsyncTask<String, Void, String> {

    public static final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    @Override
    protected String doInBackground(String... params) {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are available at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            URL url = new URL(params[0]);

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                forecastJsonStr = null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                forecastJsonStr = null;
            }
            forecastJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            forecastJsonStr = null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }
        }
        return forecastJsonStr;

    }
}
