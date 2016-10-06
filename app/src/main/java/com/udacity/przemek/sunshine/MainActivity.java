package com.udacity.przemek.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    public static final String TEMP_HARDCODED_ZIP = "52-129";
    public static final String DAYS_PARAM = "7";
    public static final int DAYS = Integer.parseInt(DAYS_PARAM);
    private ListView forecastDisplay;

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

        forecastDisplay = (ListView) this.findViewById(R.id.listview_forecast);
        updateDisplayList(exampleDailyForecasts);

    }

    /**
     * Updates UI with given list of formatted strings with responses.
     *
     * @param exampleDailyForecasts
     */
    private void updateDisplayList(List<String> exampleDailyForecasts) {
        ArrayAdapter<String> forecastAdapter = new ArrayAdapter<>(
                this, R.layout.list_item_forecast, R.id.list_item_forecast_textview, exampleDailyForecasts);
        forecastDisplay.setAdapter(forecastAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            Toast.makeText(this, "Kliknięto w opcję w menu", Toast.LENGTH_SHORT).show();
            callFetchWeatherTask();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Retrieves data using Async Task in the background, and updates UI.
     */
    private void callFetchWeatherTask() {
        String response = "";
        Toast.makeText(this, "STARTING WORK!", Toast.LENGTH_SHORT).show();

        try {
            response = new FetchWeatherTask().execute(TEMP_HARDCODED_ZIP).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        try {
            String[] weatherForecasts = WeatherJsonParser.getWeatherDataFromJson(response, DAYS);
            updateDisplayList(Arrays.asList(weatherForecasts));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/**
 * Used for retrieving weather information from specified City Name.
 */
class FetchWeatherTask extends AsyncTask<String, Void, String> {

    private static final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
    private static final String COUNTRY_CODE = "pl";
    private static final String UNITS = "metric";
    private static final String WEATHER_APP_ID = "e7b0e7d19a155ba13278c7ee01eb7e43";

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
            URL url = generateURL(params[0]);

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
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                forecastJsonStr = null;
            }
            forecastJsonStr = buffer.toString();
            Log.v(LOG_TAG, forecastJsonStr);
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

    /**
     * Creates URL with query for OpenWeatherAPI with pre-set params and received zipcode.
     *
     * @param zipCode
     * @return
     * @throws MalformedURLException
     */
    private URL generateURL(String zipCode) throws MalformedURLException {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme("http")
                .authority("api.openweathermap.org")
                .appendPath("data")
                .appendPath("2.5")
                .appendPath("forecast")
                .appendPath("daily")
                .appendQueryParameter("zip", zipCode + "," + COUNTRY_CODE)
                .appendQueryParameter("mode", "json")
                .appendQueryParameter("cnt", MainActivity.DAYS_PARAM)
                .appendQueryParameter("units", UNITS)
                .appendQueryParameter("APPID", WEATHER_APP_ID);

        String url = uriBuilder.build().toString();
        Log.v(LOG_TAG, url);
        return new URL(url);
    }
}
