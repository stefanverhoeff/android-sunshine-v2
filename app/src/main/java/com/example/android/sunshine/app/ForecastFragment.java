package com.example.android.sunshine.app;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ForecastFragment extends Fragment {
    static String LOG_TAG = ForecastFragment.class.getSimpleName();

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        setHasOptionsMenu(true);

        Context context = getActivity();

        List<String> mockWeatherData = Arrays.asList("Hey", "Ho", "hey", "ho", "off", "to", "work", "we", "go");
        ArrayAdapter<String> forecastAdaptor =
                new ArrayAdapter<>(
                        context,
                        R.layout.list_item_forecast,
                        R.id.list_item_forecast_textview,
                        mockWeatherData);

        ListView listViewForecast = rootView.findViewById(R.id.listview_forecast);
        listViewForecast.setAdapter(forecastAdaptor);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_refresh:
                fetchWeatherData();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fetchWeatherData() {
        WeatherFetchingTask weatherFetcher = new WeatherFetchingTask();
        weatherFetcher.execute();
        String weatherDataJson = null;
        try {
            weatherDataJson = weatherFetcher.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }

        Log.i(LOG_TAG + ":jsonData", weatherDataJson);
    }

    private static class WeatherFetchingTask extends AsyncTask<Void, Void, String> {

        static final String LOG_TAG = WeatherFetchingTask.class.getSimpleName();
        static final String APP_ID = "b95677c89902dc35959d2ef9c3a455d9";
        static final int TAMPERE_CITY_ID = 634963;

        @Override
        protected String doInBackground(Void... objects) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?id=" + TAMPERE_CITY_ID + "&units=metric&mode=json&cnt=7&appid=" + APP_ID);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
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

            return forecastJsonStr;
        }
    }
}
