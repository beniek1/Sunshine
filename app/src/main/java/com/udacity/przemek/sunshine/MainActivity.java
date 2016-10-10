package com.udacity.przemek.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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

public class MainActivity extends AppCompatActivity {

    /**
     * Default zip code in preferences.
     */
    public static final String DEFAULT_ZIP_CODE = "52-129";
    public static final String DAYS_PARAM = "7";
    public static final int DAYS = Integer.parseInt(DAYS_PARAM);

    /**
     * Log tag for logging.
     */
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    public ArrayAdapter<String> forecastAdapter;

    @Override
    protected void onStart() {
        super.onStart();
        callFetchWeatherTask();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        forecastAdapter =
                new ArrayAdapter<String>(
                        this, // The current context (this activity)
                        R.layout.list_item_forecast, // The name of the layout ID.
                        R.id.list_item_forecast_textview);

        ListView listView = (ListView) findViewById(R.id.listview_forecast);
        listView.setAdapter(forecastAdapter);
        listView.setOnItemClickListener(new ListViewOnItemClickListener());

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_refresh:
                callFetchWeatherTask();
                return true;
            case R.id.settings:
                startSettingActivity();
                return true;
            case R.id.show_location:
                showLocationOnMap();
                return true;
            default:
                return false;
        }

    }

    /**
     * Shows user's preferred location on Map using intent.
     */
    private void showLocationOnMap() {
        String zipCode = getZipCodeFromPreferences();
        Uri preferredLocUri = Uri.parse("geo:0,0").buildUpon()
                .appendQueryParameter("q", zipCode)
                .build();
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, preferredLocUri);

        //Verify if app capable of handling the intent is present
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            //Inform user
            Log.d(LOG_TAG, String.format("Location: %s .No map application installed on the device",
                    zipCode));
            Toast.makeText(this, "No map application installed on your device", Toast.LENGTH_LONG).show();
        }


    }

    /**
     * Starts setting activity.
     */
    private void startSettingActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Retrieves data using Async Task in the background, and updates UI.
     */
    private void callFetchWeatherTask() {
        new FetchWeatherTask().execute(getZipCodeFromPreferences());

    }

    /**
     * Retrieves zip code saved in preferences.
     */
    private String getZipCodeFromPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string
                .zip_preference_key), DEFAULT_ZIP_CODE);
    }

    /**
     * Returns true if user set units to Metric in preferences. Returns false for Imperial units.
     */
    private boolean useMetricUnits() {
        String pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(getString
                (R.string.unit_key), getString(R.string.metric));
        if (pref.equals("1")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Handles click on single day row.
     */
    private class ListViewOnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            String weather = forecastAdapter.getItem(position);

            Intent launchDetailActivity = new Intent(MainActivity.this, DayDetailActivity.class);
            launchDetailActivity.putExtra(DayDetailActivity.WEATHER_INFO, weather);
            startActivity(launchDetailActivity);

        }
    }


    /**
     * Used for retrieving weather information from specified City Name.
     */
    class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        private final String COUNTRY_CODE = "pl";
        private final String UNITS = "metric";
        private final String WEATHER_APP_ID = "e7b0e7d19a155ba13278c7ee01eb7e43";

        @Override
        protected String[] doInBackground(String... params) {
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
                        Log.e(MainActivity.class.getSimpleName(), "Error closing stream", e);
                    }
                }
            }

            try {
                boolean useMetricUnits = useMetricUnits();
                return WeatherJsonParser.getWeatherDataFromJson(forecastJsonStr, DAYS, useMetricUnits);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;

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
            return new URL(url);
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                forecastAdapter.clear();
                forecastAdapter.addAll(result);
            }
        }
    }


}


