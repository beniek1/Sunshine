package com.udacity.przemek.sunshine;

import android.text.format.Time;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

/**
 * Helper class for extracting data from JSON responses from OpenWeather API responses.
 * Example response (one row) "Mon, Jun 1 - Clear - 18/13"
 */
public class WeatherJsonParser {

    /**
     * Open Weather Map API constants.
     */
    public static final String OWM_LIST = "list";
    public static final String OWM_WEATHER = "weather";
    public static final String OWM_TEMPERATURE = "temp";
    public static final String OWM_MAX = "max";
    public static final String OWM_MIN = "min";
    public static final String OWM_DESCRIPTION = "description";

    /**
     * Class name tag for logging.
     */
    private static final String LOG_TAG = WeatherJsonParser.class.getSimpleName();


    public static String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws Exception {

        if (forecastJsonStr == null) {
            String msg = "Empty JSON string received!";
            Log.e(LOG_TAG, msg);
            throw new Exception(msg);
        }

        // These are the names of the JSON objects that need to be extracted.

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.
        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.
        Time dayTime = new Time();
        dayTime.setToNow();
        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
        // now we work exclusively in UTC
        dayTime = new Time();
        String[] resultStrs = new String[numDays];
        for (int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime;

            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay + i);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            resultStrs[i] = day + " - " + description + " - " + formattedTemperatures(high, low);
        }
        return resultStrs;
    }

    /**
     * Returns formatted String with temperatures to be displayed.
     */
    private static String formattedTemperatures(double high, double low) {
        return formatTemperature(high) +
                " / " + formatTemperature((high + low) / 2) +
                " / " + formatTemperature(low);

    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */
    private static String getReadableDateString(long time) {
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    /**
     * Rounds given temperature to 1 decimal place and converts it to String.
     */
    private static String formatTemperature(double temp) {
        return String.valueOf(Math.round(temp));
    }


}
