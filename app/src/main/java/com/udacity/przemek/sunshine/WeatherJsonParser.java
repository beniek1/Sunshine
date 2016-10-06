package com.udacity.przemek.sunshine;

import android.text.format.Time;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;

/**
 * Helper class for extracting data from JSON responses from OpenWeather API responses.
 * Example response (one row) "Mon, Jun 1 - Clear - 18/13"
 */
public class WeatherJsonParser {

    /**
     * Constants for parsing Open Weather API JSON responses.
     */
    public static final String LIST = "list";
    public static final String TEMP = "temp";
    public static final String MIN = "min";
    public static final String MAX = "max";
    public static final String WEATHER = "weather";
    public static final String DESC = "description";

    /**
     * Class name tag for logging.
     */
    private static final String Log_TAG = WeatherJsonParser.class.getSimpleName();


    public static String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws Exception {

        if (forecastJsonStr == null) {
            String msg = "Empty JSON string received!";
            Log.e(Log_TAG, msg);
            throw new Exception(msg);
        }

        //Initialize result array.
        String[] result = new String[numDays];

        for (int i = 0; i < numDays; i++) {
            String row = generateResponseRowFromJson(forecastJsonStr, i);
            result[i] = row;
            Log.v(Log_TAG, "Forecast entry: " + row);
        }

        return result;


    }

    /**
     * Creates formated row of weather data for display
     *
     * @param forecastJsonStr
     * @param dayNum          day numer
     * @return Row of weather data for single day.
     * @throws JSONException
     */
    private static String generateResponseRowFromJson(String forecastJsonStr, int dayNum)
            throws JSONException {
        return MessageFormat.format("{0} -  {1} {2} / {3}",
                getReadableDateStr(forecastJsonStr, dayNum),
                getWeatherDesc(forecastJsonStr, dayNum),
                getMaximumTemp(forecastJsonStr, dayNum),
                getMinimumTemp(forecastJsonStr, dayNum));
    }

    private static String getReadableDateStr(String forecastJsonStr, int dayNum)
            throws JSONException {
        JSONObject day = getSingleDay(forecastJsonStr, dayNum);
        long time = day.getLong("dt");
        return convertToHumanlyReadableTime(time, dayNum);
    }

    private static String convertToHumanlyReadableTime(long time, int dayNum) {

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.
        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        // Cheating to convert this to UTC time, which is what we want anyhow
        long dateTime = dayTime.setJulianDay(julianStartDay + dayNum);
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(dateTime);
    }


    /**
     * Gets minimum temperature for given day.
     *
     * @param json
     * @param dayIndex starting with 0.
     * @return Temperature
     * @throws JSONException
     */
    private static long getMaximumTemp(String json, int dayIndex)
            throws JSONException {
        JSONObject tempInfo = getTempJsonObj(json, dayIndex);
        return Math.round(tempInfo.getDouble(MAX));
    }


    /**
     * Gets Maximum temperature for given day.
     *
     * @param json
     * @param dayIndex starting with 0.
     * @return Temperature
     * @throws JSONException
     */
    private static long getMinimumTemp(String json, int dayIndex)
            throws JSONException {
        JSONObject tempInfo = getTempJsonObj(json, dayIndex);
        return Math.round(tempInfo.getDouble(MIN));
    }


    private static JSONObject getTempJsonObj(String json, int dayIndex)
            throws JSONException {
        JSONObject selectedDay = getSingleDay(json, dayIndex);
        return selectedDay.getJSONObject(TEMP);
    }

    private static JSONObject getSingleDay(String json, int dayIndex) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        JSONArray days = jsonObject.getJSONArray(LIST);
        return days.getJSONObject(dayIndex);
    }


    /**
     * Parses string with descripcion of weather on day with given index.
     *
     * @param json
     * @param dayIndex
     * @return
     * @throws JSONException
     */
    private static String getWeatherDesc(String json, int dayIndex)
            throws JSONException {
        JSONObject selectedDay = getSingleDay(json, dayIndex);
        JSONArray weather = selectedDay.getJSONArray(WEATHER);
        return weather.getJSONObject(0).getString(DESC);
    }


}
