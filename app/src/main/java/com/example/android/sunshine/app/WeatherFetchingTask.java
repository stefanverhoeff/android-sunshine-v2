package com.example.android.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

class WeatherFetchingTask extends AsyncTask<WeatherSettings, Void, String[]> {

    private static final String LOG_TAG = WeatherFetchingTask.class.getSimpleName();
    private static final String APP_ID = "b95677c89902dc35959d2ef9c3a455d9";
    private static final String WEATHER_DAILY_URL_BASE = "http://api.openweathermap.org/data/2.5/forecast/daily";
    private static final int DAYS = 7;

    private ArrayAdapter<String> weatherDataAdaptor;

    WeatherFetchingTask(ArrayAdapter<String> weatherDataAdaptor) {
        this.weatherDataAdaptor = weatherDataAdaptor;
    }

    @Override
    protected void onPostExecute(String[] weatherForecast) {
        super.onPostExecute(weatherForecast);

        Log.i(LOG_TAG + ":forecasts", Arrays.toString(weatherForecast));
        weatherDataAdaptor.clear();
        if (weatherForecast != null)
            weatherDataAdaptor.addAll(Arrays.asList(weatherForecast));
    }

    @Override
    protected String[] doInBackground(WeatherSettings... settings) {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        if (settings.length < 1) {
            throw new RuntimeException("Settings parameter missing");
        }

        String location = settings[0].getLocation();
        String temperatureUnit = settings[0].getTemperatureUnit();

        Log.v(LOG_TAG, "Fetching weather for " + location + " shown as " + temperatureUnit);

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            Uri weatherUri = Uri.parse(WEATHER_DAILY_URL_BASE).buildUpon()
                    .appendQueryParameter("q", location)
                    .appendQueryParameter("units", "metric")
                    .appendQueryParameter("mode", "json")
                    .appendQueryParameter("cnt", ""+DAYS)
                    .appendQueryParameter("appid", APP_ID)
                    .build();
            URL weatherUrl = new URL(weatherUri.toString());

            Log.v(LOG_TAG, "URL: " + weatherUrl);

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) weatherUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return null;
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
                return null;
            }
            forecastJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        WeatherDataParser weatherDataParser = new WeatherDataParser(settings[0]);
        String[] weatherForecast = {};
        try {
            weatherForecast = weatherDataParser.getWeatherDataFromJson(forecastJsonStr, DAYS);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Failed to parse weather JSON", e);
        }

        return weatherForecast;
    }
}